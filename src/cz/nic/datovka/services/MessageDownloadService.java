package cz.nic.datovka.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.connector.DatabaseTools;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class MessageDownloadService extends Service {
	public static final int UPDATE_PROGRESS = 8344;
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
		messageId = intent.getLongExtra(MSG_ID, 0);
		folder = intent.getIntExtra(FOLDER, 0);
		receiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER);

		thread = new DaemonThread();
		thread.start();
	}

	@Override
	public void onDestroy() {

		if (thread != null) {
			if(connector != null){
				connector.close();
				//System.out.println("odpojeno");
			}
			
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		super.onDestroy();
	}

	private class DaemonThread extends Thread {
		public void run() {
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
			Cursor msgCursor = getContentResolver().query(singleUri, projection, null, null, null);
			msgCursor.moveToFirst();

			int isdsIdColIndex = msgCursor.getColumnIndex(IsdsIdColName);
			int msgBoxIdColIndex = msgCursor.getColumnIndex(msgBoxIdColName);
			int fileSizeColIndex = msgCursor.getColumnIndex(fileSizeColName);

			int messageIsdsId = msgCursor.getInt(isdsIdColIndex);
			long msgBoxId = msgCursor.getInt(msgBoxIdColIndex);
			long fileSize = msgCursor.getInt(fileSizeColIndex) * 1024; // kB to
																		// bytes
			fileSize *= 1.36f; // base64 makes the content bigger by 33%
			fileSize += 20 * 1024; // 20 kB is the size of the envelope
			msgCursor.close();

			// Connect to WS
			connector = Connector.connectToWs(msgBoxId, getApplicationContext());

			// If the download folder not exists create it
			checkExternalStorage();
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
			File outFileTmp = null;
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
				
				// It seems that the file is downloaded correctly, so remove the .tmp suffix and insert it to db
				File outFile = new File(destFolder, outFileName); 
				outFileTmp.renameTo(outFile);
				DatabaseTools.insertAttachmentToDb(directory + outFileName,
						getResources().getString(R.string.signed_message_name), "application/pkcs7+xml", folder,
						messageId, getApplicationContext());
				
				// Parse the signed message and extract attachments
				InputStream input = new FileInputStream(outFile);
				CMSSignedData signeddata = new CMSSignedData(input);
				CMSProcessable data = signeddata.getSignedContent();
				ASN1InputStream asn1is = new ASN1InputStream((byte[]) data.getContent());
				connector.parseSignedMessage(destFolder, folder, messageId, getApplicationContext(), asn1is, messageIsdsId);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (HttpException e) {
				// TODO
				String errorMessage = e.getMessage();
				Toast.makeText(getApplicationContext(), "chyba " + errorMessage, Toast.LENGTH_LONG).show();
			} catch (StreamInterruptedException e) {
				// Probably user interrupted download the file, delete it.
				if(outFileTmp != null && outFileTmp.exists()){
					outFileTmp.delete();
				}
				logger.log(Level.WARNING, e.getMessage());
			} catch (CMSException e) {
				e.printStackTrace();
			}

			// Send 100% to gauge, just for sure
			Bundle resultData = new Bundle();
			resultData.putInt("progress", 100);
			receiver.send(UPDATE_PROGRESS, resultData);
		}
	}

	private void checkExternalStorage() {

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
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (!mExternalStorageAvailable && !mExternalStorageWriteable) {

			try {
				throw new Exception("External storage available: " + mExternalStorageAvailable + " writable: "
						+ mExternalStorageWriteable);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
