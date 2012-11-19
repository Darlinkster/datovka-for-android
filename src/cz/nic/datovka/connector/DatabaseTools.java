package cz.nic.datovka.connector;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.RecvAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;

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
	
	public static synchronized void deleteAccount(Long msgBoxId){
		Uri userUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);
		Application.ctx.getContentResolver().delete(userUri, null, null);
		Application.ctx.getContentResolver().notifyChange(SentMessagesContentProvider.CONTENT_URI, null);
		Application.ctx.getContentResolver().notifyChange(ReceivedMessagesContentProvider.CONTENT_URI, null);
		Application.ctx.getContentResolver().notifyChange(RecvAttachmentsContentProvider.CONTENT_URI, null);
		Application.ctx.getContentResolver().notifyChange(SentAttachmentsContentProvider.CONTENT_URI, null);
		
	}
}
