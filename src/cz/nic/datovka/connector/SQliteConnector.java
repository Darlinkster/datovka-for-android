package cz.nic.datovka.connector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQliteConnector {
	protected static final String DATABASE_NAME = "datovka";
	protected static final int DATABASE_VERSION = 1;

	protected static final String ACCOUNT_TB_NAME = "account";
	public static final String ACCOUNT_ID = "_id";
	public static final String ACCOUNT_LOGIN = "login";
	public static final String ACCOUNT_PASSWORD = "password";
	public static final String[] account_columns = { ACCOUNT_ID, ACCOUNT_LOGIN,
			ACCOUNT_PASSWORD };

	protected static final String MESSAGE_TB_NAME = "message";
	public static final String MESSAGE_ID = "_id";
	public static final String MESSAGE_ANNOTATION = "annotation";
	public static final String MESSAGE_DATE_RECIEVED = "date_recieved";
	public static final String MESSAGE_DATE_OPEN = "date_open";
	public static final String MESSAGE_SENDER_ID = "sender_id";
	public static final String MESSAGE_ACCOUNT_ID = "account_id";
	public static final String MESSAGE_TYPE = "type";
	public static final String[] message_columns = { MESSAGE_TB_NAME,
			MESSAGE_TB_NAME, MESSAGE_ANNOTATION, MESSAGE_DATE_RECIEVED,
			MESSAGE_DATE_OPEN, MESSAGE_SENDER_ID, MESSAGE_ACCOUNT_ID, MESSAGE_TYPE };

	protected static final String SENDER_TB_NAME = "sender";
	public static final String SENDER_ID = "_id";
	public static final String SENDER_NAME = "name";
	public static final String SENDER_ADDRESS = "address";
	public static final String[] sender_columns = { SENDER_ID, SENDER_ID,
			SENDER_ADDRESS };

	protected static final String ORDER_BY = ACCOUNT_ID + " DESC";

	private SQLiteOpenHelper openHelper;

	static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + ACCOUNT_TB_NAME + " (" + ACCOUNT_ID
					+ " INTEGER PRIMARY KEY," + ACCOUNT_LOGIN
					+ " TEXT NOT NULL," + ACCOUNT_PASSWORD + " TEXT NOT NULL"
					+ ");");
			
			db.execSQL("CREATE TABLE " + SENDER_TB_NAME + " (" + SENDER_ID
					+ " INTEGER PRIMARY KEY," + SENDER_NAME
					+ " TEXT NOT NULL," + SENDER_ADDRESS + " TEXT"
					+ ");");
			
			db.execSQL("CREATE TABLE " + MESSAGE_TB_NAME + " (" + MESSAGE_ID
					+ " INTEGER PRIMARY KEY," + MESSAGE_ANNOTATION + " TEXT NOT NULL,"
					+ MESSAGE_DATE_OPEN + " TEXT NOT NULL,"
					+ MESSAGE_DATE_RECIEVED + " TEXT NOT NULL,"
					+ MESSAGE_SENDER_ID + " INTEGER NOT NULL,"
					+ MESSAGE_ACCOUNT_ID + " INTEGER NOT NULL,"
					+ MESSAGE_TYPE + " TEXT NOT NULL,"
					+ " FOREIGN KEY ("+MESSAGE_SENDER_ID+") REFERENCES "+SENDER_TB_NAME+" ("+SENDER_ID+")"
					+ " FOREIGN KEY ("+MESSAGE_ACCOUNT_ID+") REFERENCES "+ACCOUNT_TB_NAME+" ("+ACCOUNT_ID+")"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TB_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TB_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SENDER_TB_NAME);
			
			onCreate(db);
		}
	}

	public SQliteConnector(Context ctx) {
		openHelper = new DatabaseHelper(ctx);
	}

	public void close() {
		openHelper.close();
	}
	
	public Cursor getAccounts(){
		SQLiteDatabase db = openHelper.getReadableDatabase();
		return db.query(ACCOUNT_TB_NAME, account_columns, null, null, null, null, null, null);
	}
	
	public long createAccount(String username, String password){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ACCOUNT_LOGIN, username);
		values.put(ACCOUNT_PASSWORD, password);
		
		long id = db.insert(ACCOUNT_TB_NAME, null, values);
		db.close();
		
		return id;
	}
	
	public long insertMessage(int accountID, int messageID,
			String messageAnnotation, String dateOpen, String dateRecv,
			int senderID, String messageType) {
		
		SQLiteDatabase db = openHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE_ID, messageID);
		values.put(MESSAGE_ACCOUNT_ID, accountID);
		values.put(MESSAGE_ANNOTATION, messageAnnotation);
		values.put(MESSAGE_DATE_OPEN, dateOpen);
		values.put(MESSAGE_DATE_RECIEVED, dateRecv);
		values.put(MESSAGE_SENDER_ID, senderID);
		values.put(MESSAGE_TYPE, messageType);
		
		long id = db.insert(MESSAGE_TB_NAME, null, values);
		db.close();
		
		return id;
	}
	
	public Cursor getAllMessages(int accountID){
		SQLiteDatabase db = openHelper.getReadableDatabase();
		
		return db.query(MESSAGE_TB_NAME, message_columns, MESSAGE_ACCOUNT_ID+" = ?", new String[]{Integer.toString(accountID)}, null, null, null);
	}
	
	public long getSenderIdByName(String name, String address) throws Exception{
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.query(SENDER_TB_NAME,new String[]{SENDER_ID}, SENDER_NAME+" = ?", new String[]{name}, null, null, null);
		
		if(cursor.getCount() == 1){
			return cursor.getLong(1);
		}
		else if (cursor.getCount() == 0){
			db = openHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(SENDER_ADDRESS, address);
			values.put(SENDER_NAME, name);
			
			long id = db.insert(SENDER_TB_NAME, null, values);
			db.close();
			return id;
		}
		else{
			throw new Exception("DB Inconsistency");
		}
	}
}
