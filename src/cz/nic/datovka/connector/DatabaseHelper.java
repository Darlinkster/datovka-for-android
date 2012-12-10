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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	protected static final String DATABASE_NAME = "datovka";
	protected static final int DATABASE_VERSION = 2;

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
	public static final String OWNER_BIRTH_COUNTY = "owner_birth_county";
	public static final String OWNER_BIRTH_CITY = "owner_birth_city";
	public static final String OWNER_BIRTH_STATE = "owner_birth_state";
	public static final String OWNER_NATIONALITY = "owner_nationality";
	public static final String OWNER_EMAIL = "owner_email";
	public static final String OWNER_TELEPHONE = "owner_telephone";
	public static final String OWNER_IDENTIFIER = "owner_identifier";
	public static final String OWNER_REGISTRY_CODE = "owner_registry_code";
	
	public static final String[] msgbox_columns = { MSGBOX_ID, MSGBOX_ISDS_ID, MSGBOX_LOGIN, MSGBOX_PASSWORD, MSGBOX_TEST_ENV, MSGBOX_TYPE,
			MSGBOX_PASSWD_EXPIRATION, OWNER_NAME, OWNER_LAST_BIRTH_NAME, OWNER_ADDRESS_CITY, OWNER_ADDRESS_STREET, OWNER_ADDRESS_STREET_NUMBER,
			OWNER_ADDRESS_MUNIC_NUMBER, OWNER_ADDRESS_ZIP, OWNER_ADDRESS_STATE, OWNER_BIRTH_DATE, OWNER_IC, OWNER_BIRTH_COUNTY,
			OWNER_BIRTH_CITY, OWNER_BIRTH_STATE, OWNER_NATIONALITY, OWNER_EMAIL, OWNER_TELEPHONE, OWNER_IDENTIFIER, OWNER_REGISTRY_CODE, USER_ISDS_ID,
			USER_NAME, USER_LAST_BIRTH_NAME, USER_ADDRESS_CITY, USER_ADDRESS_STREET, USER_ADDRESS_STREET_NUMBER, USER_ADDRESS_MUNIC_NUMBER, USER_ADDRESS_ZIP,
			USER_ADDRESS_STATE, USER_BIRTH_DATE, USER_TYPE, USER_PRIVILS, USER_IC, USER_FIRM_NAME, USER_CON_ADDRESS_STREET, USER_CON_ADDRESS_CITY,
			USER_CON_ADDRESS_ZIP, USER_CON_ADDRESS_STATE };
	
	public static final String MESSAGE_TB_NAME = "messages";
	public static final String MESSAGE_FOLDER = "message_folder";
	public static final String MESSAGE_ID = "_id";
	public static final String MESSAGE_ISDS_ID = "isds_id";
	public static final String MESSAGE_ANNOTATION = "annotation";
	public static final String MESSAGE_ACCEPTANCE_DATE = "date_acceptance";
	public static final String MESSAGE_MSGBOX_ID = "msgbox_id";
	public static final String MESSAGE_TYPE = "type";
	public static final String MESSAGE_DM_TYPE = "dm_type";
	public static final String MESSAGE_TO_HANDS = "to_hands";
	public static final String MESSAGE_ALLOW_SUBST_DELIVERY = "subst_delivery";
	public static final String MESSAGE_PERSONAL_DELIVERY = "personal_delivery";
	public static final String MESSAGE_SENT_DATE = "sent_date";
	public static final String MESSAGE_LEGALTITLE_LAW = "legaltitle_law";
	public static final String MESSAGE_LEGALTITLE_PAR = "legaltitle_par";
	public static final String MESSAGE_LEGALTITLE_POINT = "legaltitle_point";
	public static final String MESSAGE_LEGALTITLE_SECT = "legaltitle_sect";
	public static final String MESSAGE_LEGALTITLE_YEAR = "legaltitle_year";
	public static final String MESSAGE_IS_READ = "is_read";
	public static final String MESSAGE_ATTACHMENT_SIZE = "attachment_size";
	public static final String MESSAGE_STATE = "msg_state";
	public static final String MESSAGE_STATUS_CHANGED = "status_changed";
	public static final String MESSAGE_SENDER_IDENT = "sender_ident";
	public static final String MESSAGE_SENDER_REF_NUMBER = "sender_ref_number";
	public static final String MESSAGE_RECIPIENT_IDENT = "recipient_ident";
	public static final String MESSAGE_RECIPIENT_REF_NUMBER = "recipient_ref_num";
	public static final String MESSAGE_OTHERSIDE_DATABOX_TYPE = "recipient_databox_type";
	public static final String MESSAGE_OTHERSIDE_ISDS_ID = "recipient_isds_id";
	public static final String MESSAGE_OTHERSIDE_NAME = "recipient_name";
	public static final String MESSAGE_OTHERSIDE_ADDRESS = "recipient_address";
	public static final String[] message_columns = { MESSAGE_FOLDER, MESSAGE_ID, MESSAGE_ISDS_ID, MESSAGE_ANNOTATION, MESSAGE_ACCEPTANCE_DATE,
			MESSAGE_MSGBOX_ID, MESSAGE_OTHERSIDE_ISDS_ID, MESSAGE_OTHERSIDE_NAME, MESSAGE_OTHERSIDE_ADDRESS, MESSAGE_TYPE, MESSAGE_DM_TYPE, MESSAGE_TO_HANDS,
			MESSAGE_ALLOW_SUBST_DELIVERY, MESSAGE_PERSONAL_DELIVERY, MESSAGE_SENT_DATE, MESSAGE_OTHERSIDE_DATABOX_TYPE, MESSAGE_RECIPIENT_IDENT,
			MESSAGE_RECIPIENT_REF_NUMBER, MESSAGE_IS_READ, MESSAGE_ATTACHMENT_SIZE, MESSAGE_STATE, MESSAGE_STATUS_CHANGED, MESSAGE_LEGALTITLE_LAW,
			MESSAGE_LEGALTITLE_PAR, MESSAGE_LEGALTITLE_POINT, MESSAGE_LEGALTITLE_SECT, MESSAGE_LEGALTITLE_YEAR, MESSAGE_SENDER_IDENT, MESSAGE_SENDER_REF_NUMBER };

	// protected static final String ORDER_BY = ACCOUNT_ID + " DESC";

	public static final String ATTACHMENTS_TB_NAME = "sent_attachments";
	public static final String ATTACHMENTS_MSG_ID = "attachment_msg_id";
	public static final String ATTACHMENTS_ID = "_id";
	public static final String ATTACHMENTS_PATH = "attachment_path";
	public static final String ATTACHMENTS_FILENAME = "attachment_filename";
	public static final String ATTACHMENTS_MIME = "attachment_mime";
	public static final String[] attachments_columns = { ATTACHMENTS_MSG_ID,
			ATTACHMENTS_ID, ATTACHMENTS_PATH, ATTACHMENTS_FILENAME, ATTACHMENTS_MIME };

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
			
		db.execSQL("CREATE TABLE " + ATTACHMENTS_TB_NAME + " (" 
				+ ATTACHMENTS_ID	+ " INTEGER PRIMARY KEY,"
				+ ATTACHMENTS_MSG_ID + " INTEGER NOT NULL,"
				+ ATTACHMENTS_PATH + " TEXT NOT NULL UNIQUE, " 
				+ ATTACHMENTS_MIME + " TEXT, "
				+ ATTACHMENTS_FILENAME + " TEXT NOT NULL, "
				+ " FOREIGN KEY (" + ATTACHMENTS_MSG_ID + ") REFERENCES " + MESSAGE_TB_NAME + " (" + MESSAGE_ID + ") ON DELETE CASCADE );");
		
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
				+ OWNER_BIRTH_COUNTY + " TEXT,"
				+ OWNER_BIRTH_CITY + " TEXT ,"
				+ OWNER_BIRTH_STATE + " TEXT ,"
				+ OWNER_NATIONALITY + " TEXT ,"
				+ OWNER_EMAIL + " TEXT,"
				+ OWNER_TELEPHONE + " TEXT,"
				+ OWNER_IDENTIFIER + " INTEGER,"
				+ OWNER_REGISTRY_CODE + " INTEGER);");
		
		db.execSQL("CREATE TABLE " + MESSAGE_TB_NAME + " ("
				+ MESSAGE_ID + " INTEGER PRIMARY KEY,"
				+ MESSAGE_FOLDER + " INTEGER NOT NULL,"
				+ MESSAGE_ISDS_ID + " INTEGER NOT NULL,"
				+ MESSAGE_ANNOTATION + " TEXT NOT NULL,"
				+ MESSAGE_ACCEPTANCE_DATE + " TEXT,"
				+ MESSAGE_MSGBOX_ID + " INTEGER NOT NULL,"
				+ MESSAGE_TYPE + " TEXT NOT NULL," 
				+ MESSAGE_DM_TYPE + " TEXT, "
				+ MESSAGE_TO_HANDS + " TEXT, "
				+ MESSAGE_ALLOW_SUBST_DELIVERY + " TEXT, "
				+ MESSAGE_PERSONAL_DELIVERY + " TEXT, "
				+ MESSAGE_SENT_DATE + " TEXT, "
				+ MESSAGE_LEGALTITLE_LAW  + " TEXT, "
				+ MESSAGE_LEGALTITLE_PAR  + " TEXT, "
				+ MESSAGE_LEGALTITLE_POINT  + " TEXT, "
				+ MESSAGE_LEGALTITLE_SECT  + " TEXT, "
				+ MESSAGE_LEGALTITLE_YEAR  + " TEXT, "
				+ MESSAGE_IS_READ + " INTEGER NOT NULL,"
				+ MESSAGE_ATTACHMENT_SIZE + " INTEGER,"
				+ MESSAGE_STATE  + " INTEGER, "
				+ MESSAGE_STATUS_CHANGED + " INTEGER NOT NULL,"
				+ MESSAGE_SENDER_IDENT + " TEXT, "
				+ MESSAGE_SENDER_REF_NUMBER + " TEXT, "
				+ MESSAGE_OTHERSIDE_DATABOX_TYPE + " TEXT, "
				+ MESSAGE_RECIPIENT_IDENT + " TEXT, "
				+ MESSAGE_RECIPIENT_REF_NUMBER + " TEXT, "
				+ MESSAGE_OTHERSIDE_ISDS_ID + " INTEGER NOT NULL," 
				+ MESSAGE_OTHERSIDE_NAME + " TEXT NOT NULL,"
				+ MESSAGE_OTHERSIDE_ADDRESS + " TEXT, " 
				+ " FOREIGN KEY (" + MESSAGE_MSGBOX_ID + ") REFERENCES " + MSGBOX_TB_NAME + " (" + MSGBOX_ID + ") ON DELETE CASCADE );");
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MSGBOX_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ATTACHMENTS_TB_NAME);
		
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