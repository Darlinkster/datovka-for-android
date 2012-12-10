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

package cz.nic.datovka.contentProviders;

import java.util.Arrays;
import java.util.HashSet;

import cz.nic.datovka.connector.DatabaseHelper;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MessagesContentProvider  extends ContentProvider {

	private static final int RCVDMSG = 14;
	private static final int RCVDMSG_ID = 24;
	
	private static final String AUTHORITY = "cz.nic.datovka.contentproviders.messagescontentprovider";

	private static final String BASE_PATH = "msg";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/msg";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/todo";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, RCVDMSG);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RCVDMSG_ID);
	}
	
	private DatabaseHelper database;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;

		switch (uriType) {
		case RCVDMSG:
			rowsDeleted = sqlDB.delete(DatabaseHelper.MESSAGE_TB_NAME,
					selection, selectionArgs);
			break;

		case RCVDMSG_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(DatabaseHelper.MESSAGE_TB_NAME,
						DatabaseHelper.MESSAGE_ID + " = " + id, null);
			} else {
				rowsDeleted = sqlDB.delete(DatabaseHelper.MESSAGE_TB_NAME,
						DatabaseHelper.MESSAGE_ID + " = " + id + " AND "
								+ selection, selectionArgs);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;

		switch (uriType) {
		case RCVDMSG:
			id = sqlDB.insert(DatabaseHelper.MESSAGE_TB_NAME, null, values);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		checkColumns(projection);
		queryBuilder.setTables(DatabaseHelper.MESSAGE_TB_NAME);

		int uriType = sURIMatcher.match(uri);

		switch (uriType) {
		case RCVDMSG:
			break;

		case RCVDMSG_ID:
			queryBuilder.appendWhere(DatabaseHelper.MESSAGE_ID + " = "
					+ uri.getLastPathSegment());
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;

		switch (uriType) {
		case RCVDMSG:
			rowsUpdated = sqlDB.update(DatabaseHelper.MESSAGE_TB_NAME, values,
					selection, selectionArgs);
			break;

		case RCVDMSG_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(DatabaseHelper.MESSAGE_TB_NAME,
						values, DatabaseHelper.MESSAGE_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(DatabaseHelper.MESSAGE_TB_NAME,
						values, DatabaseHelper.MESSAGE_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	private void checkColumns(String[] projection) {
		String[] available = DatabaseHelper.message_columns;
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
