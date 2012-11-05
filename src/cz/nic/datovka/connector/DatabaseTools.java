package cz.nic.datovka.connector;

import android.content.ContentValues;
import android.content.Context;
import cz.nic.datovka.contentProviders.RecvAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentAttachmentsContentProvider;

public class DatabaseTools {

	private static final int INBOX = 0;

	public static synchronized void insertAttachmentToDb(String path, String filename, String mime, int folder,
			long messageId, Context ctx) {
		ContentValues value = new ContentValues();

		if (folder == INBOX) {
			value.put(DatabaseHelper.RECV_ATTACHMENTS_MSG_ID, messageId);
			value.put(DatabaseHelper.RECV_ATTACHMENTS_PATH, path);
			value.put(DatabaseHelper.RECV_ATTACHMENTS_FILENAME, filename);
			value.put(DatabaseHelper.RECV_ATTACHMENTS_MIME, mime);
			ctx.getContentResolver().insert(RecvAttachmentsContentProvider.CONTENT_URI, value);
		} else {
			value.put(DatabaseHelper.SENT_ATTACHMENTS_MSG_ID, messageId);
			value.put(DatabaseHelper.SENT_ATTACHMENTS_PATH, path);
			value.put(DatabaseHelper.SENT_ATTACHMENTS_FILENAME, filename);
			value.put(DatabaseHelper.SENT_ATTACHMENTS_MIME, mime);
			ctx.getContentResolver().insert(SentAttachmentsContentProvider.CONTENT_URI, value);
		}
	}
}
