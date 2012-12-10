/*
Datovka - An Android client for Datove schranky
    Copyright (C) 2012  CZ NIC z.s.p.o. <podpora at nic dot cz>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cz.nic.datovka.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.StatFs;
import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MessagesContentProvider;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.exceptions.StorageNotAwailableException;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class MessageDownloadService extends Service {
	public static final int UPDATE_PROGRESS = 8344;
	public static final int ERROR = 8355;
	public static final int ERROR_NO_CONNECTION = 8366;
	public static final int ERROR_STORAGE_NOT_AVAILABLE = 8377;
	public static final int ERROR_STORAGE_LOW_SPACE = 8388;
	public static final int RESULT_BAD_LOGIN = 401;
	public static final int SERVICE_FINISHED = 999;
	public static final String MSG_ID = "msgid";
	public static final String FOLDER = "folder";
	public static final String RECEIVER = "receiver";

	private static final int INBOX = 0;

	private long messageId;
	private int folder;
	private ResultReceiver receiver;
	private DaemonThread thread;
	private GaugeFileOutputStream fos;
	private Connector connector;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void onStart(Intent intent, int startId) {
		if(intent == null || intent.getExtras() == null){
			logger.log(Level.WARNING, "Message download service started with empty intent extras. Aborting.");
			return;
		}
		
		messageId = intent.getLongExtra(MSG_ID, 0);
		folder = intent.getIntExtra(FOLDER, 0);
		receiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER);

		thread = new DaemonThread();
		thread.start();
	}

	@Override
	public void onDestroy() {

		if (thread != null) {
			if (connector != null) {
				connector.close();
				// System.out.println("odpojeno");
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			thread.interrupt();
		}
		connector = null;
		receiver = null;
		fos = null;
		thread = null;
		
		super.onDestroy();
		logger.log(Level.INFO, "Downloading service interrupted.");
	}

	private class DaemonThread extends Thread {
		public void run() {
			logger.log(Level.INFO, "Downloading service started");
			// Get ISDS ID and MSGBOX ID of the message
			Uri singleUri = ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, messageId);
			String[] projection = new String[] { DatabaseHelper.MESSAGE_ISDS_ID,
					DatabaseHelper.MESSAGE_MSGBOX_ID, DatabaseHelper.MESSAGE_ATTACHMENT_SIZE };
			if(interrupted()) return;
			Cursor msgCursor = getContentResolver().query(singleUri, projection, null, null, null);
			msgCursor.moveToFirst();

			int messageIsdsId = msgCursor.getInt(msgCursor.getColumnIndex(DatabaseHelper.MESSAGE_ISDS_ID));
			long msgBoxId = msgCursor.getInt(msgCursor.getColumnIndex(DatabaseHelper.MESSAGE_MSGBOX_ID));
			long fileSize = msgCursor.getInt(msgCursor.getColumnIndex(DatabaseHelper.MESSAGE_ATTACHMENT_SIZE));
			msgCursor.close();
			msgCursor = null;
			
			// Get MsgBox ISDS ID
			String msgBoxIsdsId = "-1";
			Uri msgBoxUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);
			Cursor msgBoxCursor = getContentResolver().query(msgBoxUri, new String[]{DatabaseHelper.MSGBOX_ISDS_ID}, null, null, null);
			if(msgBoxCursor.moveToFirst()){
				msgBoxIsdsId = msgBoxCursor.getString(msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID));
			}
			msgBoxCursor.close();
			msgBoxCursor = null;
			
			
			// Check if there is enough space to save the message and for
			// unpacking it.
			fileSize *= 1.33f; // base64 makes the content bigger by 33%
			if ((2 * fileSize) > getAvailableSpaceInKB()) {
				//System.out.println(fileSize + " " + getAvailableSpaceInKB());
				if (receiver != null)
					receiver.send(ERROR_STORAGE_LOW_SPACE, null);
				return;
			}

			fileSize *= 1024f; // kB to bytes
			// fileSize += 20 * 1024; // 20 kB is the size of the envelope
			

			if(interrupted()) return;
			// Connect to WS
			connector = Connector.connectToWs(msgBoxId);
			if(!connector.checkConnection()){
				if( receiver!= null)
					receiver.send(ERROR_NO_CONNECTION, null);
				return;
			}

			// If the download folder not exists create it
			try {
				checkExternalStorage();
			} catch (StorageNotAwailableException e1) {
				if( receiver!= null)
					receiver.send(ERROR_STORAGE_NOT_AVAILABLE, null);
				return;
			}
			String outputDirectory = "/Datovka/" + Integer.toString(messageIsdsId) + "_" + Long.toString(messageId) + "/";
			File destFolder = new File(Application.externalStoragePath + outputDirectory);
			if (!destFolder.exists()) {
				if (!destFolder.mkdirs()) {
					try {
						throw new Exception("Cannot create output folder.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// Save the signed message
			if(interrupted()) return;
			File outFileTmp = null;
			try {
				String outFileName = messageIsdsId + ".zfo";
				outFileTmp = new File(destFolder, outFileName + ".tmp");
				fos = new GaugeFileOutputStream(outFileTmp, receiver, UPDATE_PROGRESS, fileSize);

				if (folder == INBOX) {
					connector.downloadSignedReceivedMessage(messageIsdsId, fos);
				} else {
					connector.downloadSignedSentMessage(messageIsdsId, fos);
				}
				fos.flush();
				fos.close();

				// It seems that the file is downloaded correctly, so remove the
				// .tmp suffix and insert it to db
				if(interrupted()) return;
				File outFile = new File(destFolder, outFileName);
				outFileTmp.renameTo(outFile);
				/*DatabaseTools.insertAttachmentToDb(directory + outFileName,
						getResources().getString(R.string.signed_message_name), "application/vnd.software602.filler.form-xml-zip", folder,
						messageId);*/
				
				// Parse the signed message and extract attachments
				InputStream input = new FileInputStream(outFile);
				CMSStripperInputStream csis = new CMSStripperInputStream(input);
			/*	
				FileOutputStream fos = new FileOutputStream(new File(destFolder, "bbb"));
				int x;
				while((x = csis.read()) != -1){
					fos.write(x);
				}
				fos.close();
				csis.close();
				*/
				if(interrupted()){
					csis.close();
					csis = null;
					input = null;
					return;
				}
				connector.parseSignedMessage(outputDirectory, folder, messageId, csis, messageIsdsId);
				
				// Send SERVICE_FINISHED to dismiss download progress bar
				if(receiver != null)
					receiver.send(SERVICE_FINISHED, null);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (HttpException e) {
				Bundle resultData = new Bundle();
				if(e.getErrorCode() == 401){
					resultData.putString("error", new String(getString(R.string.cannot_login, msgBoxIsdsId)));
				} else {
					String errorMessage = e.getMessage();
					resultData.putString("error", errorMessage);
				}
				if( receiver!= null){
					receiver.send(ERROR, resultData);
				}
			} catch (StreamInterruptedException e) {
				// Probably user interrupted download the file, delete it.
				if (outFileTmp != null && outFileTmp.exists()) {
					outFileTmp.delete();
				}
				logger.log(Level.WARNING, e.getMessage());
			} catch (DSException e) {
				Bundle resultData = new Bundle();
				resultData.putString("error", e.getErrorCode() + ": " + e.getMessage());
				if( receiver!= null)
					receiver.send(ERROR, resultData);

			} catch (NullPointerException e){
				Bundle resultData = new Bundle();
				logger.log(Level.WARNING, "Null pointer Exception: User probably killed download thread.");
				resultData.putString("error", getString(R.string.message_download_crashed));
				if( receiver!= null)
					receiver.send(ERROR, resultData);
			}
		}

		private void checkExternalStorage() throws StorageNotAwailableException {

			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// We can read and write the media
				mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				// We can only read the media
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				// Something else is wrong. It may be one of many other states,
				// but
				// all we need
				// to know is we can neither read nor write
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}

			if (!mExternalStorageAvailable && !mExternalStorageWriteable) {
				throw new StorageNotAwailableException("External storage available: " + mExternalStorageAvailable + " writable: " + mExternalStorageWriteable);
			}
		}
		
		public long getAvailableSpaceInKB(){
		    final long SIZE_KB = 1024L;
		    long availableSpace = -1L;
		    StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		    availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		    return availableSpace/SIZE_KB;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
