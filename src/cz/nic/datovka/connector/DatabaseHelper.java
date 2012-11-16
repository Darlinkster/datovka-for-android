package cz.nic.datovka.connector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	protected static final String DATABASE_NAME = "datovka";
	protected static final int DATABASE_VERSION = 1;

	public static final String MSGBOX_TB_NAME = "msgbox";
	public static final String MSGBOX_ID = "_id";
	public static final String MSGBOX_ISDS_ID = "msgbox_isds_id";
	public static final String MSGBOX_LOGIN = "msgbox_login";
	public static final String MSGBOX_PASSWORD = "msgbox_password";
	public static final String MSGBOX_TEST_ENV = "msgbox_test_env";
	public static final String MSGBOX_TYPE = "msgbox_type";
	public static final String MSGBOX_PASSWD_EXPIRATION = "msgbox_passwd_expiration";
	
	public static final String USER_ISDS_ID = "user_isds_id";
	public static final String USER_LAST_BIRTH_NAME = "user_last_birth_name";
	public static final String USER_NAME = "user_name";
	public static final String USER_ADDRESS_CITY = "user_address_city";
	public static final String USER_ADDRESS_STREET = "user_address_street";
	public static final String USER_ADDRESS_STREET_NUMBER = "user_address_street_number";
	public static final String USER_ADDRESS_MUNIC_NUMBER = "user_address_munic_number";
	public static final String USER_ADDRESS_ZIP = "user_address_zip_code";
	public static final String USER_ADDRESS_STATE = "user_address_state";
	public static final String USER_BIRTH_DATE = "user_birth_date";
	public static final String USER_TYPE = "user_type";
	public static final String USER_PRIVILS = "user_privils";
	public static final String USER_IC = "user_ic";
	public static final String USER_FIRM_NAME = "user_firm_name";
	public static final String USER_CON_ADDRESS_STREET = "user_con_address_street";
	public static final String USER_CON_ADDRESS_CITY = "user_con_address_city";
	public static final String USER_CON_ADDRESS_ZIP = "user_con_address_zip";
	public static final String USER_CON_ADDRESS_STATE = "user_con_address_state";
	
	public static final String OWNER_LAST_BIRTH_NAME = "owner_last_birth_name";
	public static final String OWNER_NAME = "owner_name";
	public static final String OWNER_ADDRESS_CITY = "owner_address_city";
	public static final String OWNER_ADDRESS_STREET = "owner_address_street";
	public static final String OWNER_ADDRESS_STREET_NUMBER = "owner_address_street_number";
	public static final String OWNER_ADDRESS_MUNIC_NUMBER = "owner_address_munic_number";
	public static final String OWNER_ADDRESS_ZIP = "owner_address_zip_code";
	public static final String OWNER_ADDRESS_STATE = "owner_address_state";
	public static final String OWNER_BIRTH_DATE = "owner_birth_date";
	public static final String OWNER_IC = "owner_ic";
	public static final String OWNER_FIRM_NAME = "owner_firm_name";
	public static final String OWNER_BIRTH_COUNTY = "owner_birth_county";
	public static final String OWNER_BIRTH_CITY = "owner_birth_city";
	public static final String OWNER_BIRTH_STATE = "owner_birth_state";
	public static final String OWNER_NATIONALITY = "owner_nationality";
	public static final String OWNER_EMAIL = "owner_email";
	public static final String OWNER_TELEPHONE = "owner_telephone";
	public static final String OWNER_IDENTIFIER = "owner_identifier";
	public static final String OWNER_REGISTRY_CODE = "owner_registry_code";
	
	public static final String[] msgbox_columns = { MSGBOX_ID, MSGBOX_ISDS_ID,
			MSGBOX_LOGIN, MSGBOX_PASSWORD, MSGBOX_TEST_ENV, MSGBOX_TYPE,
			MSGBOX_PASSWD_EXPIRATION, 
			OWNER_NAME, OWNER_LAST_BIRTH_NAME, OWNER_ADDRESS_CITY,
			OWNER_ADDRESS_STREET, OWNER_ADDRESS_STREET_NUMBER,
			OWNER_ADDRESS_MUNIC_NUMBER, OWNER_ADDRESS_ZIP, OWNER_ADDRESS_STATE,
			OWNER_BIRTH_DATE, OWNER_IC, OWNER_FIRM_NAME, OWNER_BIRTH_COUNTY,
			OWNER_BIRTH_CITY, OWNER_BIRTH_STATE, OWNER_NATIONALITY,
			OWNER_EMAIL, OWNER_TELEPHONE, OWNER_IDENTIFIER,
			OWNER_REGISTRY_CODE, USER_ISDS_ID, USER_NAME,
			USER_LAST_BIRTH_NAME,
			USER_ADDRESS_CITY, USER_ADDRESS_STREET, USER_ADDRESS_STREET_NUMBER,
			USER_ADDRESS_MUNIC_NUMBER, USER_ADDRESS_ZIP, USER_ADDRESS_STATE,
			USER_BIRTH_DATE, USER_TYPE, USER_PRIVILS, USER_IC, USER_FIRM_NAME,
			USER_CON_ADDRESS_STREET, USER_CON_ADDRESS_CITY,
			USER_CON_ADDRESS_ZIP, USER_CON_ADDRESS_STATE };

	public static final String RECEIVED_MESSAGE_TB_NAME = "received_message";
	public static final String RECEIVED_MESSAGE_ID = "_id";
	public static final String RECEIVED_MESSAGE_ISDS_ID = "isds_id";
	public static final String RECEIVED_MESSAGE_ANNOTATION = "annotation";
	public static final String RECEIVED_MESSAGE_ACCEPTANCE_DATE = "date_acceptance";
	public static final String RECEIVED_MESSAGE_RECEIVED_DATE = "date_received";
	public static final String RECEIVED_MESSAGE_MSGBOX_ID = "msgbox_id";
	public static final String RECEIVED_MESSAGE_TYPE = "type";
	public static final String RECEIVED_MESSAGE_DM_TYPE = "dm_type";
	public static final String RECEIVED_MESSAGE_TO_HANDS = "to_hands";
	public static final String RECEIVED_MESSAGE_ALLOW_SUBST_DELIVERY = "subst_delivery";
	public static final String RECEIVED_MESSAGE_PERSONAL_DELIVERY = "personal_delivery";
	public static final String RECEIVED_MESSAGE_STATE = "msg_state";
	public static final String RECEIVED_MESSAGE_LEGALTITLE_LAW = "legaltitle_law";
	public static final String RECEIVED_MESSAGE_LEGALTITLE_PAR = "legaltitle_par";
	public static final String RECEIVED_MESSAGE_LEGALTITLE_POINT = "legaltitle_point";
	public static final String RECEIVED_MESSAGE_LEGALTITLE_SECT = "legaltitle_sect";
	public static final String RECEIVED_MESSAGE_LEGALTITLE_YEAR = "legaltitle_year";
	public static final String RECEIVED_MESSAGE_IS_READ = "is_read";
	public static final String RECEIVED_MESSAGE_ATTACHMENT_SIZE = "attachment_size";
	public static final String SENDER_ISDS_ID = "sender_isds_id";
	public static final String SENDER_NAME = "sender_name";
	public static final String SENDER_ADDRESS = "sender_address";
	public static final String SENDER_DATABOX_TYPE = "sender_databox_type";
	public static final String SENDER_IDENT = "sender_ident";
	public static final String SENDER_REF_NUMBER = "sender_ref_number";
	public static final String[] received_message_columns = {
			RECEIVED_MESSAGE_ID, RECEIVED_MESSAGE_ISDS_ID,
			RECEIVED_MESSAGE_ANNOTATION, RECEIVED_MESSAGE_ACCEPTANCE_DATE,
			RECEIVED_MESSAGE_RECEIVED_DATE, SENDER_ISDS_ID, SENDER_NAME,
			SENDER_ADDRESS, RECEIVED_MESSAGE_MSGBOX_ID, RECEIVED_MESSAGE_TYPE,
			RECEIVED_MESSAGE_DM_TYPE, RECEIVED_MESSAGE_TO_HANDS,
			RECEIVED_MESSAGE_ALLOW_SUBST_DELIVERY,
			RECEIVED_MESSAGE_PERSONAL_DELIVERY, SENDER_DATABOX_TYPE,
			SENDER_IDENT, SENDER_REF_NUMBER, RECEIVED_MESSAGE_STATE,
			RECEIVED_MESSAGE_LEGALTITLE_LAW, RECEIVED_MESSAGE_LEGALTITLE_PAR,
			RECEIVED_MESSAGE_LEGALTITLE_POINT,
			RECEIVED_MESSAGE_LEGALTITLE_SECT, RECEIVED_MESSAGE_LEGALTITLE_YEAR,
			RECEIVED_MESSAGE_IS_READ, RECEIVED_MESSAGE_ATTACHMENT_SIZE };

	public static final String SENT_MESSAGE_TB_NAME = "sent_message";
	public static final String SENT_MESSAGE_ID = "_id";
	public static final String SENT_MESSAGE_ISDS_ID = "isds_id";
	public static final String SENT_MESSAGE_ANNOTATION = "annotation";
	public static final String SENT_MESSAGE_ACCEPTANCE_DATE = "date_acceptance";
	public static final String SENT_MESSAGE_MSGBOX_ID = "msgbox_id";
	public static final String SENT_MESSAGE_TYPE = "type";
	public static final String SENT_MESSAGE_DM_TYPE = "dm_type";
	public static final String SENT_MESSAGE_TO_HANDS = "to_hands";
	public static final String SENT_MESSAGE_ALLOW_SUBST_DELIVERY = "subst_delivery";
	public static final String SENT_MESSAGE_PERSONAL_DELIVERY = "personal_delivery";
	public static final String SENT_MESSAGE_SENT_DATE = "sent_date";
	public static final String SENT_MESSAGE_IS_READ = "is_read";
	public static final String SENT_MESSAGE_ATTACHMENT_SIZE = "attachment_size";
	public static final String SENT_MESSAGE_STATE = "msg_state";
	public static final String RECIPIENT_DATABOX_TYPE = "recipient_databox_type";
	public static final String RECIPIENT_IDENT = "recipient_ident";
	public static final String RECIPIENT_REF_NUMBER = "recipient_ref_num";
	public static final String RECIPIENT_ISDS_ID = "recipient_isds_id";
	public static final String RECIPIENT_NAME = "recipient_name";
	public static final String RECIPIENT_ADDRESS = "recipient_address";
	public static final String[] sent_message_columns = { SENT_MESSAGE_ID,
			SENT_MESSAGE_ISDS_ID, SENT_MESSAGE_ANNOTATION,
			SENT_MESSAGE_ACCEPTANCE_DATE, SENT_MESSAGE_MSGBOX_ID,
			RECIPIENT_ISDS_ID, RECIPIENT_NAME, RECIPIENT_ADDRESS,
			SENT_MESSAGE_TYPE, SENT_MESSAGE_DM_TYPE, SENT_MESSAGE_TO_HANDS,
			SENT_MESSAGE_ALLOW_SUBST_DELIVERY, SENT_MESSAGE_PERSONAL_DELIVERY,
			SENT_MESSAGE_SENT_DATE, RECIPIENT_DATABOX_TYPE, RECIPIENT_IDENT,
			RECIPIENT_REF_NUMBER, SENT_MESSAGE_IS_READ, SENT_MESSAGE_ATTACHMENT_SIZE, SENT_MESSAGE_STATE };

	// protected static final String ORDER_BY = ACCOUNT_ID + " DESC";
	
	public static final String RECV_ATTACHMENTS_TB_NAME = "recv_attachments";
	public static final String RECV_ATTACHMENTS_MSG_ID = "attachment_msg_id";
	public static final String RECV_ATTACHMENTS_ID = "_id";
	public static final String RECV_ATTACHMENTS_PATH = "attachment_path";
	public static final String RECV_ATTACHMENTS_FILENAME = "attachment_filename";
	public static final String RECV_ATTACHMENTS_MIME = "attachment_mime";
	public static final String[] recv_attachments_columns = { RECV_ATTACHMENTS_MSG_ID,
			RECV_ATTACHMENTS_ID, RECV_ATTACHMENTS_PATH, RECV_ATTACHMENTS_FILENAME, RECV_ATTACHMENTS_MIME };

	public static final String SENT_ATTACHMENTS_TB_NAME = "sent_attachments";
	public static final String SENT_ATTACHMENTS_MSG_ID = "attachment_msg_id";
	public static final String SENT_ATTACHMENTS_ID = "_id";
	public static final String SENT_ATTACHMENTS_PATH = "attachment_path";
	public static final String SENT_ATTACHMENTS_FILENAME = "attachment_filename";
	public static final String SENT_ATTACHMENTS_MIME = "attachment_mime";
	public static final String[] sent_attachments_columns = { SENT_ATTACHMENTS_MSG_ID,
			SENT_ATTACHMENTS_ID, SENT_ATTACHMENTS_PATH, SENT_ATTACHMENTS_FILENAME, SENT_ATTACHMENTS_MIME };

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + SENT_ATTACHMENTS_TB_NAME + " (" 
				+ SENT_ATTACHMENTS_ID	+ " INTEGER PRIMARY KEY,"
				+ SENT_ATTACHMENTS_MSG_ID + " INTEGER NOT NULL,"
				+ SENT_ATTACHMENTS_PATH + " TEXT NOT NULL UNIQUE, " 
				+ SENT_ATTACHMENTS_MIME + " TEXT, "
				+ SENT_ATTACHMENTS_FILENAME + " TEXT NOT NULL, "
				+ " FOREIGN KEY (" + SENT_ATTACHMENTS_MSG_ID + ") REFERENCES " + SENT_MESSAGE_TB_NAME + " (" + SENT_MESSAGE_ID + ") ON DELETE CASCADE );");
		
		db.execSQL("CREATE TABLE " + RECV_ATTACHMENTS_TB_NAME + " (" 
				+ RECV_ATTACHMENTS_ID	+ " INTEGER PRIMARY KEY,"
				+ RECV_ATTACHMENTS_MSG_ID + " INTEGER NOT NULL,"
				+ RECV_ATTACHMENTS_PATH + " TEXT NOT NULL UNIQUE, " 
				+ RECV_ATTACHMENTS_MIME + " TEXT, "
				+ RECV_ATTACHMENTS_FILENAME + " TEXT NOT NULL);"
				+ " FOREIGN KEY (" + RECV_ATTACHMENTS_MSG_ID + ") REFERENCES " + RECEIVED_MESSAGE_TB_NAME + " (" + RECEIVED_MESSAGE_ID + ") ON DELETE CASCADE );");
		
		db.execSQL("CREATE TABLE " + MSGBOX_TB_NAME + " (" 
				+ MSGBOX_ID	+ " INTEGER PRIMARY KEY,"
				+ MSGBOX_ISDS_ID + " INTEGER ,"
				+ MSGBOX_LOGIN + " TEXT NOT NULL,"
				+ MSGBOX_PASSWORD + " TEXT NOT NULL, "
				+ MSGBOX_TEST_ENV + " INTEGER NOT NULL, "
				+ MSGBOX_TYPE + " TEXT NOT NULL, "
				+ MSGBOX_PASSWD_EXPIRATION + " TEXT NOT NULL, "
				+ USER_ISDS_ID  + " INTEGER NOT NULL,"
				+ USER_NAME + " TEXT NOT NULL,"
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
				+ USER_CON_ADDRESS_STATE + " TEXT , "
				+ OWNER_NAME + " TEXT ,"
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

		db.execSQL("CREATE TABLE " + RECEIVED_MESSAGE_TB_NAME + " ("
				+ RECEIVED_MESSAGE_ID + " INTEGER PRIMARY KEY,"
				+ RECEIVED_MESSAGE_ISDS_ID + " INTEGER NOT NULL,"
				+ RECEIVED_MESSAGE_ANNOTATION + " TEXT NOT NULL,"
				+ RECEIVED_MESSAGE_ACCEPTANCE_DATE + " TEXT NOT NULL,"
				+ RECEIVED_MESSAGE_RECEIVED_DATE + " TEXT NOT NULL,"
				+ RECEIVED_MESSAGE_MSGBOX_ID + " INTEGER NOT NULL,"
				+ RECEIVED_MESSAGE_TYPE + " TEXT NOT NULL," 
				+ RECEIVED_MESSAGE_DM_TYPE  + " TEXT, "
				+ RECEIVED_MESSAGE_TO_HANDS  + " TEXT, "
				+ RECEIVED_MESSAGE_ALLOW_SUBST_DELIVERY  + " TEXT, "
				+ RECEIVED_MESSAGE_PERSONAL_DELIVERY   + " TEXT, "
				+ RECEIVED_MESSAGE_STATE  + " INTEGER, "
				+ RECEIVED_MESSAGE_LEGALTITLE_LAW  + " TEXT, "
				+ RECEIVED_MESSAGE_LEGALTITLE_PAR  + " TEXT, "
				+ RECEIVED_MESSAGE_LEGALTITLE_POINT  + " TEXT, "
				+ RECEIVED_MESSAGE_LEGALTITLE_SECT  + " TEXT, "
				+ RECEIVED_MESSAGE_LEGALTITLE_YEAR  + " TEXT, "
				+ RECEIVED_MESSAGE_IS_READ + " INTEGER NOT NULL,"
				+ RECEIVED_MESSAGE_ATTACHMENT_SIZE+ " INTEGER,"
				+ SENDER_ISDS_ID + " INTEGER NOT NULL," 
				+ SENDER_NAME + " TEXT NOT NULL,"
				+ SENDER_ADDRESS + " TEXT, " 
				+ SENDER_DATABOX_TYPE + " TEXT, "
				+ SENDER_IDENT + " TEXT, "
				+ SENDER_REF_NUMBER + " TEXT, "
				+ " FOREIGN KEY (" + RECEIVED_MESSAGE_MSGBOX_ID + ") REFERENCES " + MSGBOX_TB_NAME + " (" + MSGBOX_ID + ") ON DELETE CASCADE );");
		
		db.execSQL("CREATE TABLE " + SENT_MESSAGE_TB_NAME + " ("
				+ SENT_MESSAGE_ID + " INTEGER PRIMARY KEY,"
				+ SENT_MESSAGE_ISDS_ID + " INTEGER NOT NULL,"
				+ SENT_MESSAGE_ANNOTATION + " TEXT NOT NULL,"
				+ SENT_MESSAGE_ACCEPTANCE_DATE + " TEXT NOT NULL,"
				+ SENT_MESSAGE_MSGBOX_ID + " INTEGER NOT NULL,"
				+ SENT_MESSAGE_TYPE + " TEXT NOT NULL," 
				+ SENT_MESSAGE_DM_TYPE + " TEXT, "
				+ SENT_MESSAGE_TO_HANDS + " TEXT, "
				+ SENT_MESSAGE_ALLOW_SUBST_DELIVERY + " TEXT, "
				+ SENT_MESSAGE_PERSONAL_DELIVERY + " TEXT, "
				+ SENT_MESSAGE_SENT_DATE + " TEXT, "
				+ SENT_MESSAGE_IS_READ + " INTEGER NOT NULL,"
				+ SENT_MESSAGE_ATTACHMENT_SIZE + " INTEGER,"
				+ SENT_MESSAGE_STATE  + " INTEGER, "
				+ RECIPIENT_DATABOX_TYPE + " TEXT, "
				+ RECIPIENT_IDENT + " TEXT, "
				+ RECIPIENT_REF_NUMBER + " TEXT, "
				+ RECIPIENT_ISDS_ID + " INTEGER NOT NULL," 
				+ RECIPIENT_NAME + " TEXT NOT NULL,"
				+ RECIPIENT_ADDRESS + " TEXT, " 
				+ " FOREIGN KEY (" + SENT_MESSAGE_MSGBOX_ID + ") REFERENCES " + MSGBOX_TB_NAME + " (" + MSGBOX_ID + ") ON DELETE CASCADE );");
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MSGBOX_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + RECEIVED_MESSAGE_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SENT_MESSAGE_TB_NAME);
		
		onCreate(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
	
}