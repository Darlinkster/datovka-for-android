package cz.nic.datovka.connector;

import android.content.ContentValues;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.contentProviders.RecvAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentAttachmentsContentProvider;

public class DatabaseTools {

	private static final int INBOX = 0;

	public static synchronized void insertAttachmentToDb(String path, String name, String mime, int folder,
			long messageId) {
		ContentValues value = new ContentValues();

		if (folder == INBOX) {
			value.put(DatabaseHelper.RECV_ATTACHMENTS_MSG_ID, messageId);
			value.put(DatabaseHelper.RECV_ATTACHMENTS_PATH, path);
			value.put(DatabaseHelper.RECV_ATTACHMENTS_FILENAME, name);
			value.put(DatabaseHelper.RECV_ATTACHMENTS_MIME, mime);
			Application.ctx.getContentResolver().insert(RecvAttachmentsContentProvider.CONTENT_URI, value);
		} else {
			value.put(DatabaseHelper.SENT_ATTACHMENTS_MSG_ID, messageId);
			value.put(DatabaseHelper.SENT_ATTACHMENTS_PATH, path);
			value.put(DatabaseHelper.SENT_ATTACHMENTS_FILENAME, name);
			value.put(DatabaseHelper.SENT_ATTACHMENTS_MIME, mime);
			Application.ctx.getContentResolver().insert(SentAttachmentsContentProvider.CONTENT_URI, value);
		}
	}
}
