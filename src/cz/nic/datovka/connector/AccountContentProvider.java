package cz.nic.datovka.connector;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class AccountContentProvider extends ContentProvider {

	private static final int ACCOUNTS = 10;
	private static final int ACCOUNT_ID = 20;

	private static final String AUTHORITY = "cz.nic.datovka.connector.accountcontentprovider";

	private static final String BASE_PATH = "accounts";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/accounts";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/todo";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ACCOUNTS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ACCOUNT_ID);
	}

	private DatabaseHelper database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;

		switch (uriType) {
		case ACCOUNTS:
			rowsDeleted = sqlDB.delete(DatabaseHelper.ACCOUNT_TB_NAME,
					selection, selectionArgs);
			break;

		case ACCOUNT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(DatabaseHelper.ACCOUNT_TB_NAME,
						DatabaseHelper.ACCOUNT_ID + " = " + id, null);
			} else {
				rowsDeleted = sqlDB.delete(DatabaseHelper.ACCOUNT_TB_NAME,
						DatabaseHelper.ACCOUNT_ID + " = " + id + " AND "
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
		case ACCOUNTS:
			id = sqlDB.insert(DatabaseHelper.ACCOUNT_TB_NAME, null, values);
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
		queryBuilder.setTables(DatabaseHelper.ACCOUNT_TB_NAME);

		int uriType = sURIMatcher.match(uri);

		switch (uriType) {
		case ACCOUNTS:
			break;

		case ACCOUNT_ID:
			queryBuilder.appendWhere(DatabaseHelper.ACCOUNT_ID + " = "
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
		case ACCOUNTS:
			rowsUpdated = sqlDB.update(DatabaseHelper.ACCOUNT_TB_NAME, values,
					selection, selectionArgs);
			break;

		case ACCOUNT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(DatabaseHelper.ACCOUNT_TB_NAME,
						values, DatabaseHelper.ACCOUNT_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(DatabaseHelper.ACCOUNT_TB_NAME,
						values, DatabaseHelper.ACCOUNT_ID + "=" + id + " and "
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
		String[] available = DatabaseHelper.account_columns;
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));

			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

}
