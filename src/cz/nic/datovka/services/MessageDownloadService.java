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
import cz.nic.datovka.R;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.connector.DatabaseTools;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.exceptions.StorageNotAwailableException;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class MessageDownloadService extends Service {
	public static final int UPDATE_PROGRESS = 8344;
	public static final int ERROR = 8355;
	public static final int ERROR_NO_CONNECTION = 8366;
	public static final int ERROR_STORAGE_NOT_AVAILABLE = 8377;
	public static final String MSG_ID = "msgid";
	public static final String FOLDER = "folder";
	public static final String RECEIVER = "receiver";

	private static final int INBOX = 0;

	private String directory;
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
			Uri singleUri;
			String[] projection;
			String IsdsIdColName;
			String msgBoxIdColName;
			String fileSizeColName;
			if (folder == INBOX) {
				singleUri = ContentUris.withAppendedId(ReceivedMessagesContentProvider.CONTENT_URI, messageId);
				projection = new String[] { DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID,
						DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID, DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE };
				IsdsIdColName = DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID;
				msgBoxIdColName = DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID;
				fileSizeColName = DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE;
			} else {
				singleUri = ContentUris.withAppendedId(SentMessagesContentProvider.CONTENT_URI, messageId);
				projection = new String[] { DatabaseHelper.SENT_MESSAGE_ISDS_ID, DatabaseHelper.SENT_MESSAGE_MSGBOX_ID,
						DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE };
				IsdsIdColName = DatabaseHelper.SENT_MESSAGE_ISDS_ID;
				msgBoxIdColName = DatabaseHelper.SENT_MESSAGE_MSGBOX_ID;
				fileSizeColName = DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE;
			}
			if(interrupted()) return;
			Cursor msgCursor = getContentResolver().query(singleUri, projection, null, null, null);
			msgCursor.moveToFirst();

			int isdsIdColIndex = msgCursor.getColumnIndex(IsdsIdColName);
			int msgBoxIdColIndex = msgCursor.getColumnIndex(msgBoxIdColName);
			int fileSizeColIndex = msgCursor.getColumnIndex(fileSizeColName);

			int messageIsdsId = msgCursor.getInt(isdsIdColIndex);
			long msgBoxId = msgCursor.getInt(msgBoxIdColIndex);
			long fileSize = msgCursor.getInt(fileSizeColIndex) * 1024; // kB to bytes
			fileSize *= 1.33f; // base64 makes the content bigger by 33%
			//fileSize += 20 * 1024; // 20 kB is the size of the envelope
			msgCursor.close();

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
			String programFolder = Environment.getExternalStorageDirectory().getPath() + "/Datovka";
			directory = programFolder + "/" + Integer.toString(messageIsdsId) + "_" + Long.toString(messageId) + "/";
			File destFolder = new File(directory);
			if (!destFolder.exists()) {
				if (!destFolder.mkdirs()) {
					try {
						// TODO
						throw new Exception("neco se po..");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// Save the signed message
			if(interrupted()) return;
			File outFileTmp = null;
			Bundle resultData = new Bundle();
			try {
				String outFileName = messageIsdsId + ".bin";
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
				DatabaseTools.insertAttachmentToDb(directory + outFileName,
						getResources().getString(R.string.signed_message_name), "application/pkcs7+xml", folder,
						messageId);
				
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
				connector.parseSignedMessage(destFolder, folder, messageId, csis, messageIsdsId);
				
				// Send 100% to gauge, just for sure
				resultData.putInt("progress", 100);
				if(receiver != null)
					receiver.send(UPDATE_PROGRESS, resultData);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (HttpException e) {
				String errorMessage = e.getMessage();
				resultData.putString("error", errorMessage);
				if( receiver!= null)
					receiver.send(ERROR, resultData);
			} catch (StreamInterruptedException e) {
				// Probably user interrupted download the file, delete it.
				if (outFileTmp != null && outFileTmp.exists()) {
					outFileTmp.delete();
				}
				logger.log(Level.WARNING, e.getMessage());
			} catch (DSException e) {
				resultData.putString("error", e.getErrorCode() + ": " + e.getMessage());
				if( receiver!= null)
					receiver.send(ERROR, resultData);

			} catch (NullPointerException e){
				logger.log(Level.WARNING, "Null pointer Exception: User probably killed download thread.");
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
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
