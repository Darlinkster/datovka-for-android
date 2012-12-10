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
import cz.nic.datovka.contentProviders.AttachmentsContentProvider;
import cz.nic.datovka.contentProviders.MessagesContentProvider;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;

public class DatabaseTools {

	public static synchronized void insertAttachmentToDb(String path, String name, String mime, int folder,
			long messageId) {
		ContentValues value = new ContentValues();

			value.put(DatabaseHelper.ATTACHMENTS_MSG_ID, messageId);
			value.put(DatabaseHelper.ATTACHMENTS_PATH, path);
			value.put(DatabaseHelper.ATTACHMENTS_FILENAME, name);
			value.put(DatabaseHelper.ATTACHMENTS_MIME, mime);
			Application.ctx.getContentResolver().insert(AttachmentsContentProvider.CONTENT_URI, value);
	}
	
	public static synchronized void deleteAccount(Long msgBoxId){
		Uri userUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);
		Application.ctx.getContentResolver().delete(userUri, null, null);
		Application.ctx.getContentResolver().notifyChange(MessagesContentProvider.CONTENT_URI, null);
		Application.ctx.getContentResolver().notifyChange(MessagesContentProvider.CONTENT_URI, null);
		Application.ctx.getContentResolver().notifyChange(AttachmentsContentProvider.CONTENT_URI, null);
		Application.ctx.getContentResolver().notifyChange(AttachmentsContentProvider.CONTENT_URI, null);
		
	}
}
