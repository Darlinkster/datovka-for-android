package cz.nic.datovka.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.widget.Toast;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.AttachmentsContentProvider;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.exceptions.HttpException;

public class MessageDownloadService extends IntentService {
	public static final int UPDATE_PROGRESS = 8344;
	public static final String MSG_ID = "msgid";
	public static final String FOLDER = "folder";
	public static final String RECEIVER = "receiver";

	private static String PROGRAM_FOLDER;
	private static final int INBOX = 0;

	private String directory;
	private long messageId;
	private int messageIsdsId;
	private int folder;

	public MessageDownloadService() {
		super("DownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		PROGRAM_FOLDER = Environment.getExternalStorageDirectory().getPath()
				+ "/Datovka";
		messageId = intent.getLongExtra(MSG_ID, 0);
		folder = intent.getIntExtra(FOLDER, 0);
		ResultReceiver receiver = (ResultReceiver) intent
				.getParcelableExtra(RECEIVER);

		// Get ISDS ID and MSGBOX ID of the message
		Uri singleUri;
		String[] projection;
		String IsdsIdColName;
		String msgBoxIdColName;
		if (folder == INBOX) {
			singleUri = ContentUris.withAppendedId(
					ReceivedMessagesContentProvider.CONTENT_URI, messageId);
			projection = new String[] {
					DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID,
					DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID };
			IsdsIdColName = DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID;
			msgBoxIdColName = DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID;
		} else {
			singleUri = ContentUris.withAppendedId(
					SentMessagesContentProvider.CONTENT_URI, messageId);
			projection = new String[] { DatabaseHelper.SENT_MESSAGE_ISDS_ID,
					DatabaseHelper.SENT_MESSAGE_MSGBOX_ID };
			IsdsIdColName = DatabaseHelper.SENT_MESSAGE_ISDS_ID;
			msgBoxIdColName = DatabaseHelper.SENT_MESSAGE_MSGBOX_ID;
		}
		Cursor msgCursor = getContentResolver().query(singleUri, projection,
				null, null, null);
		msgCursor.moveToFirst();

		int isdsIdColIndex = msgCursor.getColumnIndex(IsdsIdColName);
		int msgBoxIdColIndex = msgCursor.getColumnIndex(msgBoxIdColName);

		messageIsdsId = msgCursor.getInt(isdsIdColIndex);
		int msgBoxId = msgCursor.getInt(msgBoxIdColIndex);
		msgCursor.close();

		// Connect to WS
		if (!Connector.isOnline()) {
			connectToWs(msgBoxId);
		}

		// If the download folder not exists create it
		checkExternalStorage();
		directory = PROGRAM_FOLDER + "/" + Integer.toString(messageIsdsId)
				+ "_" + Long.toString(messageId) + "/";
		File destFolder = new File(directory);
		if (!destFolder.exists()) {
			if (!destFolder.mkdirs()) {
				// TODO
				try {
					throw new Exception("neco se po..");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// TODO get rid of the MessageEnvelope
		MessageEnvelope envelope = new MessageEnvelope();
		envelope.setMessageID(Integer.toString(messageIsdsId));

		// uložíme celou podepsanou zprávu
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(destFolder, messageIsdsId
					+ ".bin"));
			if (folder == INBOX) {
				Connector.downloadSignedReceivedMessage(envelope, fos);
			} else {
				Connector.downloadSignedSentMessage(envelope, fos);
			}
			fos.flush();
			fos.close();
			insertAttachmentToDb(directory + messageIsdsId + ".bin",
					getResources().getString(R.string.signed_message_name),
					"application/pkcs7+xml");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO
			String errorMessage = e.getMessage();
			Toast.makeText(getApplicationContext(), "chyba " + errorMessage,
					Toast.LENGTH_LONG).show();
			this.stopSelf();
		}

		/*
		 * Iterator<Attachment> messageIterator = attachments.iterator();
		 * while(messageIterator.hasNext()){ Attachment attachment =
		 * messageIterator.next(); insertAttachmentToDb(attachment.get,
		 * filename) }
		 */
		/*
		 * String urlToDownload =
		 * "http://download.documentfoundation.org/libreoffice/stable/3.6.2/rpm/x86_64/LibO_3.6.2_Linux_x86-64_install-rpm_en-US.tar.gz"
		 * ; try { URL url = new URL(urlToDownload); URLConnection connection =
		 * url.openConnection(); connection.connect(); // this will be useful so
		 * that you can show a typical 0-100% // progress bar int fileLength =
		 * connection.getContentLength();
		 * 
		 * // download the file
		 * 
		 * InputStream input = new BufferedInputStream(url.openStream());
		 * OutputStream output = new FileOutputStream(PROGRAM_FOLDER + "pokus");
		 * 
		 * 
		 * byte data[] = new byte[1024]; long total = 0; int count; while
		 * ((count = input.read(data)) != -1) { total += count; // publishing
		 * the progress.... Bundle resultData = new Bundle();
		 * resultData.putInt("progress", (int) (total * 100 / fileLength));
		 * receiver.send(UPDATE_PROGRESS, resultData); output.write(data, 0,
		 * count); }
		 * 
		 * output.flush(); output.close(); input.close(); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */
		Bundle resultData = new Bundle();
		resultData.putInt("progress", 100);
		receiver.send(UPDATE_PROGRESS, resultData);
	}

	private void connectToWs(int msgBoxId) {
		Uri msgBoxUri = ContentUris.withAppendedId(
				MsgBoxContentProvider.CONTENT_URI, msgBoxId);
		String[] msgBoxProjection = new String[] { DatabaseHelper.MSGBOX_LOGIN,
				DatabaseHelper.MSGBOX_PASSWORD, DatabaseHelper.MSGBOX_TEST_ENV };
		Cursor msgBoxCursor = getContentResolver().query(msgBoxUri,
				msgBoxProjection, null, null, null);
		msgBoxCursor.moveToFirst();

		int loginIndex = msgBoxCursor
				.getColumnIndex(DatabaseHelper.MSGBOX_LOGIN);
		int passwordIndex = msgBoxCursor
				.getColumnIndex(DatabaseHelper.MSGBOX_PASSWORD);
		int envIndex = msgBoxCursor
				.getColumnIndex(DatabaseHelper.MSGBOX_TEST_ENV);
		String login = msgBoxCursor.getString(loginIndex);
		String password = msgBoxCursor.getString(passwordIndex);
		int environment = msgBoxCursor.getInt(envIndex);
		msgBoxCursor.close();
		try {
			Connector.connect(login, password, environment,
					getApplicationContext());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				throw new Exception("External storage available: "
						+ mExternalStorageAvailable + " writable: "
						+ mExternalStorageWriteable);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void insertAttachmentToDb(String path, String filename, String mime) {
		ContentValues value = new ContentValues();
		value.put(DatabaseHelper.ATTACHMENTS_MSG_ID, messageId);
		value.put(DatabaseHelper.ATTACHMENTS_MSG_FOLDER_ID, folder);
		value.put(DatabaseHelper.ATTACHMENTS_PATH, path);
		value.put(DatabaseHelper.ATTACHMENTS_FILENAME, filename);
		value.put(DatabaseHelper.ATTACHMENTS_MIME, mime);
		getContentResolver().insert(AttachmentsContentProvider.CONTENT_URI,
				value);
	}
}
