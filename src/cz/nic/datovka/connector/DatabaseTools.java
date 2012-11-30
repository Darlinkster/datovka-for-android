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
