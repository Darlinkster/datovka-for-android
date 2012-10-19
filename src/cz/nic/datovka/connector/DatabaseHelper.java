package cz.nic.datovka.connector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	protected static final String DATABASE_NAME = "datovka";
	protected static final int DATABASE_VERSION = 1;

	protected static final String USER_TB_NAME = "user";
	public static final String USER_ID = "_id";
	public static final String USER_ISDS_ID = "isds_id";
	public static final String USER_FIRST_NAME = "first_name";
	public static final String USER_MIDDLE_NAME = "middle_name";
	public static final String USER_LAST_NAME = "last_name";
	public static final String USER_LAST_BIRTH_NAME = "last_birth_name";
	public static final String USER_ADDRESS_CITY = "address_city";
	public static final String USER_ADDRESS_STREET = "address_street";
	public static final String USER_ADDRESS_STREET_NUMBER = "address_street_number";
	public static final String USER_ADDRESS_MUNIC_NUMBER = "address_munic_number";
	public static final String USER_ADDRESS_ZIP = "address_zip_code";
	public static final String USER_ADDRESS_STATE = "address_state";
	public static final String USER_BIRTH_DATE = "birth_date";
	public static final String USER_TYPE = "type";
	public static final String USER_PRIVILS = "privils";
	public static final String USER_IC = "ic";
	public static final String USER_FIRM_NAME = "firm_name";
	public static final String USER_CON_ADDRESS_STREET = "con_address_street";
	public static final String USER_CON_ADDRESS_CITY = "con_address_city";
	public static final String USER_CON_ADDRESS_ZIP = "con_address_zip";
	public static final String USER_CON_ADDRESS_STATE = "con_address_strate";
	public static final String[] user_columns = { USER_ID, USER_ISDS_ID,
			USER_FIRST_NAME, USER_MIDDLE_NAME, USER_LAST_NAME,
			USER_LAST_BIRTH_NAME, USER_ADDRESS_CITY, USER_ADDRESS_STREET,
			USER_ADDRESS_STREET_NUMBER, USER_ADDRESS_MUNIC_NUMBER,
			USER_ADDRESS_ZIP, USER_ADDRESS_STATE, USER_BIRTH_DATE, USER_TYPE,
			USER_PRIVILS, USER_IC, USER_FIRM_NAME, USER_CON_ADDRESS_STREET,
			USER_CON_ADDRESS_CITY, USER_CON_ADDRESS_ZIP, USER_CON_ADDRESS_STATE };

	protected static final String OWNER_TB_NAME = "owner";
	public static final String OWNER_ID = "_id";
	public static final String OWNER_ISDS_ID = "isds_id";
	public static final String OWNER_FIRST_NAME = "first_name";
	public static final String OWNER_MIDDLE_NAME = "middle_name";
	public static final String OWNER_LAST_NAME = "last_name";
	public static final String OWNER_LAST_BIRTH_NAME = "last_birth_name";
	public static final String OWNER_ADDRESS_CITY = "address_city";
	public static final String OWNER_ADDRESS_STREET = "address_street";
	public static final String OWNER_ADDRESS_STREET_NUMBER = "address_street_number";
	public static final String OWNER_ADDRESS_MUNIC_NUMBER = "address_munic_number";
	public static final String OWNER_ADDRESS_ZIP = "address_zip_code";
	public static final String OWNER_ADDRESS_STATE = "address_state";
	public static final String OWNER_BIRTH_DATE = "birth_date";
	public static final String OWNER_IC = "ic";
	public static final String OWNER_FIRM_NAME = "firm_name";
	public static final String OWNER_BIRTH_COUNTY = "birth_county";
	public static final String OWNER_BIRTH_CITY = "birth_city";
	public static final String OWNER_BIRTH_STATE = "birth_state";
	public static final String OWNER_NATIONALITY = "nationality";
	public static final String OWNER_EMAIL = "email";
	public static final String OWNER_TELEPHONE = "telephone";
	public static final String OWNER_IDENTIFIER = "identifier";
	public static final String OWNER_REGISTRY_CODE = "registry_code";
	public static final String[] owner_columns = { OWNER_ID, OWNER_ISDS_ID,
			OWNER_FIRST_NAME, OWNER_MIDDLE_NAME, OWNER_LAST_NAME,
			OWNER_LAST_BIRTH_NAME, OWNER_ADDRESS_CITY, OWNER_ADDRESS_STREET,
			OWNER_ADDRESS_STREET_NUMBER, OWNER_ADDRESS_MUNIC_NUMBER,
			OWNER_ADDRESS_ZIP, OWNER_ADDRESS_STATE, OWNER_BIRTH_DATE, OWNER_IC,
			OWNER_FIRM_NAME, OWNER_BIRTH_COUNTY, OWNER_BIRTH_CITY,
			OWNER_BIRTH_STATE, OWNER_NATIONALITY, OWNER_EMAIL, OWNER_TELEPHONE,
			OWNER_IDENTIFIER, OWNER_REGISTRY_CODE };

	public static final String MSGBOX_TB_NAME = "msgbox";
	public static final String MSGBOX_ID = "_id";
	public static final String MSGBOX_LOGIN = "login";
	public static final String MSGBOX_PASSWORD = "password";
	public static final String MSGBOX_TEST_ENV = "test_env";
	public static final String MSGBOX_OWNER_ID = "owner_id";
	public static final String MSGBOX_USER_ID = "user_id";
	public static final String[] msgbox_columns = { MSGBOX_ID, MSGBOX_LOGIN,
			MSGBOX_PASSWORD, MSGBOX_TEST_ENV, MSGBOX_OWNER_ID, MSGBOX_USER_ID };

	protected static final String RECEIVED_MESSAGE_TB_NAME = "received_message";
	public static final String RECEIVED_MESSAGE_ID = "_id";
	public static final String RECEIVED_MESSAGE_ISDS_ID = "isds_id";
	public static final String RECEIVED_MESSAGE_ANNOTATION = "annotation";
	public static final String RECEIVED_MESSAGE_ACCEPTANCE_DATE = "date_acceptance";
	public static final String RECEIVED_MESSAGE_RECEIVED_DATE = "date_received";
	public static final String RECEIVED_MESSAGE_SENDER_ID = "sender_id";
	public static final String RECEIVED_MESSAGE_MSGBOX_ID = "msgbox_id";
	public static final String RECEIVED_MESSAGE_TYPE = "type";
	public static final String[] received_message_columns = {
			RECEIVED_MESSAGE_ID, RECEIVED_MESSAGE_ISDS_ID,
			RECEIVED_MESSAGE_ANNOTATION, RECEIVED_MESSAGE_ACCEPTANCE_DATE,
			RECEIVED_MESSAGE_RECEIVED_DATE, RECEIVED_MESSAGE_SENDER_ID,
			RECEIVED_MESSAGE_MSGBOX_ID, RECEIVED_MESSAGE_TYPE };

	protected static final String SENT_MESSAGE_TB_NAME = "sent_message";
	public static final String SENT_MESSAGE_ID = "_id";
	public static final String SENT_MESSAGE_ISDS_ID = "isds_id";
	public static final String SENT_MESSAGE_ANNOTATION = "annotation";
	public static final String SENT_MESSAGE_ACCEPTANCE_DATE = "date_acceptance";
	public static final String SENT_MESSAGE_RECEIVED_DATE = "date_received";
	public static final String SENT_MESSAGE_RECIPIENT_ID = "recipient_id";
	public static final String SENT_MESSAGE_MSGBOX_ID = "msgbox_id";
	public static final String SENT_MESSAGE_TYPE = "type";
	public static final String[] sent_message_columns = { SENT_MESSAGE_ID,
			SENT_MESSAGE_ISDS_ID, SENT_MESSAGE_ANNOTATION,
			SENT_MESSAGE_ACCEPTANCE_DATE, SENT_MESSAGE_RECEIVED_DATE,
			SENT_MESSAGE_RECIPIENT_ID, SENT_MESSAGE_MSGBOX_ID,
			SENT_MESSAGE_TYPE };

	protected static final String CONTACTS_TB_NAME = "contacts";
	public static final String CONTACTS_ID = "_id";
	public static final String CONTACTS_ISDS_ID = "isds_id";
	public static final String CONTACTS_NAME = "name";
	public static final String CONTACTS_ADDRESS = "address";
	public static final String[] sender_columns = { CONTACTS_ID,
			CONTACTS_ISDS_ID, CONTACTS_NAME, CONTACTS_ADDRESS };

	// protected static final String ORDER_BY = ACCOUNT_ID + " DESC";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + MSGBOX_TB_NAME + " (" 
				+ MSGBOX_ID	+ " INTEGER PRIMARY KEY," 
				+ MSGBOX_LOGIN + " TEXT NOT NULL,"
				+ MSGBOX_PASSWORD + " TEXT NOT NULL, "
				+ MSGBOX_TEST_ENV + " INTEGER NOT NULL, "
				+ MSGBOX_OWNER_ID + " TEXT NOT NULL," 
				+ MSGBOX_USER_ID + " TEXT NOT NULL, "
				+ " FOREIGN KEY (" + MSGBOX_USER_ID + ") REFERENCES " + USER_TB_NAME + " (" + USER_ID + ")," 
				+ " FOREIGN KEY (" + MSGBOX_OWNER_ID + ") REFERENCES " + OWNER_TB_NAME + " (" + OWNER_ID + "));" );

		db.execSQL("CREATE TABLE " + CONTACTS_TB_NAME + " (" 
				+ CONTACTS_ID + " INTEGER PRIMARY KEY," 
				+ CONTACTS_ISDS_ID + " INTEGER NOT NULL," 
				+ CONTACTS_NAME + " TEXT NOT NULL,"
				+ CONTACTS_ADDRESS + " TEXT" + ");");

		db.execSQL("CREATE TABLE " + RECEIVED_MESSAGE_TB_NAME + " ("
				+ RECEIVED_MESSAGE_ID + " INTEGER PRIMARY KEY,"
				+ RECEIVED_MESSAGE_ISDS_ID + " INTEGER NOT NULL,"
				+ RECEIVED_MESSAGE_ANNOTATION + " TEXT NOT NULL,"
				+ RECEIVED_MESSAGE_ACCEPTANCE_DATE + " TEXT NOT NULL,"
				+ RECEIVED_MESSAGE_RECEIVED_DATE + " TEXT NOT NULL,"
				+ RECEIVED_MESSAGE_SENDER_ID + " INTEGER NOT NULL,"
				+ RECEIVED_MESSAGE_MSGBOX_ID + " INTEGER NOT NULL,"
				+ RECEIVED_MESSAGE_TYPE + " TEXT NOT NULL," 
				+ " FOREIGN KEY (" + RECEIVED_MESSAGE_SENDER_ID + ") REFERENCES " + CONTACTS_TB_NAME + " (" + CONTACTS_ID + ")," 
				+ " FOREIGN KEY (" + RECEIVED_MESSAGE_MSGBOX_ID + ") REFERENCES " + MSGBOX_TB_NAME + " (" + MSGBOX_ID + "));");
		
		db.execSQL("CREATE TABLE " + SENT_MESSAGE_TB_NAME + " ("
				+ SENT_MESSAGE_ID + " INTEGER PRIMARY KEY,"
				+ SENT_MESSAGE_ISDS_ID + " INTEGER NOT NULL,"
				+ SENT_MESSAGE_ANNOTATION + " TEXT NOT NULL,"
				+ SENT_MESSAGE_ACCEPTANCE_DATE + " TEXT NOT NULL,"
				+ SENT_MESSAGE_RECEIVED_DATE + " TEXT NOT NULL,"
				+ SENT_MESSAGE_RECIPIENT_ID + " INTEGER NOT NULL,"
				+ SENT_MESSAGE_MSGBOX_ID + " INTEGER NOT NULL,"
				+ SENT_MESSAGE_TYPE + " TEXT NOT NULL," 
				+ " FOREIGN KEY (" + SENT_MESSAGE_RECIPIENT_ID + ") REFERENCES " + CONTACTS_TB_NAME	+ " (" + CONTACTS_ID + "), "
				+ " FOREIGN KEY (" + SENT_MESSAGE_MSGBOX_ID + ") REFERENCES " + MSGBOX_TB_NAME + " (" + MSGBOX_ID + "));");
		
		db.execSQL("CREATE TABLE " + USER_TB_NAME + " ("
				+ USER_ID + " INTEGER PRIMARY KEY,"
				+ USER_ISDS_ID  + " INTEGER NOT NULL,"
				+ USER_FIRST_NAME + " TEXT NOT NULL,"
				+ USER_MIDDLE_NAME + " TEXT NOT NULL,"
				+ USER_LAST_NAME + " TEXT NOT NULL,"
				+ USER_LAST_BIRTH_NAME + " TEXT NOT NULL,"
				+ USER_ADDRESS_CITY + " TEXT NOT NULL,"
				+ USER_ADDRESS_STREET + " TEXT NOT NULL,"
				+ USER_ADDRESS_STREET_NUMBER + " TEXT NOT NULL,"
				+ USER_ADDRESS_MUNIC_NUMBER + " TEXT NOT NULL,"
				+ USER_ADDRESS_ZIP + " TEXT NOT NULL,"
				+ USER_ADDRESS_STATE + " TEXT,"
				+ USER_BIRTH_DATE + " TEXT NOT NULL,"
				+ USER_TYPE + " TEXT NOT NULL,"
				+ USER_PRIVILS + " TEXT NOT NULL,"
				+ USER_IC + " TEXT,"
				+ USER_FIRM_NAME + " TEXT,"
				+ USER_CON_ADDRESS_STREET + " TEXT NOT NULL,"
				+ USER_CON_ADDRESS_CITY + " TEXT NOT NULL,"
				+ USER_CON_ADDRESS_ZIP + " INTEGER NOT NULL,"
				+ USER_CON_ADDRESS_STATE + " TEXT NOT NULL);");
		
		db.execSQL("CREATE TABLE " + OWNER_TB_NAME + " ("
				+ OWNER_ID + " INTEGER PRIMARY KEY,"
				+ OWNER_ISDS_ID  + " INTEGER NOT NULL,"
				+ OWNER_FIRST_NAME + " TEXT ,"
				+ OWNER_MIDDLE_NAME + " TEXT,"
				+ OWNER_LAST_NAME + " TEXT ,"
				+ OWNER_LAST_BIRTH_NAME + " TEXT ,"
				+ OWNER_ADDRESS_CITY + " TEXT NOT NULL,"
				+ OWNER_ADDRESS_STREET + " TEXT NOT NULL,"
				+ OWNER_ADDRESS_STREET_NUMBER + " TEXT NOT NULL,"
				+ OWNER_ADDRESS_MUNIC_NUMBER + " TEXT NOT NULL,"
				+ OWNER_ADDRESS_ZIP + " TEXT NOT NULL,"
				+ OWNER_ADDRESS_STATE + " TEXT ,"
				+ OWNER_BIRTH_DATE + " TEXT ,"
				+ OWNER_IC + " INTEGER,"
				+ OWNER_FIRM_NAME + " TEXT ,"
				+ OWNER_BIRTH_COUNTY + " TEXT,"
				+ OWNER_BIRTH_CITY + " TEXT ,"
				+ OWNER_BIRTH_STATE + " TEXT ,"
				+ OWNER_NATIONALITY + " TEXT ,"
				+ OWNER_EMAIL + " TEXT,"
				+ OWNER_TELEPHONE + " TEXT,"
				+ OWNER_IDENTIFIER + " INTEGER,"
				+ OWNER_REGISTRY_CODE + " INTEGER);");
	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MSGBOX_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + RECEIVED_MESSAGE_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SENT_MESSAGE_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + OWNER_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + USER_TB_NAME);

		onCreate(db);
	}
}