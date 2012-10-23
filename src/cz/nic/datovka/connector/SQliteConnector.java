package cz.nic.datovka.connector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQliteConnector {

	private SQLiteOpenHelper openHelper;

	public SQliteConnector(Context ctx) {
		openHelper = new DatabaseHelper(ctx);
	}

	public void close() {
		openHelper.close();
	}

	
/*
	synchronized public long createAccount(String username, String password) {
		SQLiteDatabase db = openHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.ACCOUNT_LOGIN, username);
		values.put(DatabaseHelper.ACCOUNT_PASSWORD, password);

		long id = db.insert(DatabaseHelper.ACCOUNT_TB_NAME, null, values);
		db.close();

		return id;
	}

	public long insertMessage(int accountID, int messageID,
			String messageAnnotation, String dateOpen, String dateRecv,
			int senderID, String messageType) {

		SQLiteDatabase db = openHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.RECEIVED_MESSAGE_ID, messageID);
		values.put(DatabaseHelper.RECEIVED_MESSAGE_ACCOUNT_ID, accountID);
		values.put(DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION, messageAnnotation);
		values.put(DatabaseHelper.RECEIVED_MESSAGE_DATE_OPEN, dateOpen);
		values.put(DatabaseHelper.RECEIVED_MESSAGE_DATE_RECIEVED, dateRecv);
		values.put(DatabaseHelper.RECEIVED_MESSAGE_SENDER_ID, senderID);
		values.put(DatabaseHelper.RECEIVED_MESSAGE_TYPE, messageType);

		long id = db.insert(DatabaseHelper.RECEIVED_MESSAGE_TB_NAME, null, values);
		db.close();

		return id;
	}

	public Cursor getAllMessages(int accountID) {
		SQLiteDatabase db = openHelper.getReadableDatabase();

		return db.query(DatabaseHelper.RECEIVED_MESSAGE_TB_NAME,
				DatabaseHelper.received_message_columns,
				DatabaseHelper.RECEIVED_MESSAGE_ACCOUNT_ID + " = ?",
				new String[] { Integer.toString(accountID) }, null, null, null);
	}

	public long getSenderIdByName(String name, String address) throws Exception {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.query(DatabaseHelper.SENDER_TB_NAME,
				new String[] { DatabaseHelper.SENDER_ID },
				DatabaseHelper.SENDER_NAME + " = ?", new String[] { name },
				null, null, null);

		if (cursor.getCount() == 1) {
			return cursor.getLong(1);
		} else if (cursor.getCount() == 0) {
			db = openHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.SENDER_ADDRESS, address);
			values.put(DatabaseHelper.SENDER_NAME, name);

			long id = db.insert(DatabaseHelper.SENDER_TB_NAME, null, values);
			db.close();
			return id;
		} else {
			throw new Exception("DB Inconsistency");
		}
	}
	*/
}
