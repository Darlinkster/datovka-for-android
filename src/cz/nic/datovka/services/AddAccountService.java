package cz.nic.datovka.services;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;

public class AddAccountService extends IntentService {
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String TESTENV = "testenv";
	public static final String HANDLER = "handler";
	public static final int RESULT_OK = 100;
	public static final int RESULT_EXISTS = 101;
	public static final int RESULT_BAD_LOGIN = 401;
	public static final int RESULT_ERR = 99;
	public static final int RESULT_DS_ERR = 999;
	private static final int NOT_READ = 0;
	
	private Message message;
	private Messenger messenger;
	
	public AddAccountService() {
		super("AddAccountService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		messenger = (Messenger) extras.get(HANDLER);
		message = Message.obtain();

		String login = extras.getString(LOGIN);
		String password = extras.getString(PASSWORD);
		int testEnvironment = extras.getBoolean(TESTENV) ? 1 : 0;
		
		if (checkIfAccountExists(login, password)) {
			// Account already exists
			message.arg1 = RESULT_EXISTS;
			
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
		} else {
			// Account doesn't exist, create it
			// try to login
			createAccount(login, password, testEnvironment);

		}
		
	}

	private String createName(String name, String middle, String last){
		String userName = "";
		if(name.length() > 0){
			userName += name;
		}
		if(middle.length() > 0){
			if(userName.length() > 0){
				userName += " ";
			}
			userName += middle;
		}
		if(last.length() > 0){
			if(userName.length() > 0){
				userName += " ";
			}
			userName += last;
		}
		
		return userName;
	}
	
	private boolean checkIfAccountExists(String login, String password) {
		String[] projection = { DatabaseHelper.MSGBOX_LOGIN, DatabaseHelper.MSGBOX_PASSWORD };
		String selection = DatabaseHelper.MSGBOX_LOGIN + " = ? and " + DatabaseHelper.MSGBOX_PASSWORD + " = ?";
		String[] selectionArgs = { login, password };
		
		Cursor cursor = getContentResolver().query(
				MsgBoxContentProvider.CONTENT_URI, projection, selection,
				selectionArgs, null);
		
		int rowCount = cursor.getCount();
		cursor.close();

		if (rowCount > 0)
			return true;
		else
			return false;
	}
	Connector connector = new Connector();
	private void createAccount(String login, String password, int testEnvironment){
		try {
			if(testEnvironment == 1){
				connector.connect(login, password, Connector.TESTING);
			}
			else{
				connector.connect(login, password, Connector.PRODUCTION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		//Try to acquire information from webservices, if something wrong happend show the error message
		List<MessageEnvelope> recievedMessageList = null;
		List<MessageEnvelope> sentMessageList = null;
		GregorianCalendar cal = null;
		UserInfo user = null;
		OwnerInfo owner = null;
		try {
			recievedMessageList = connector.getRecievedMessageList();
			sentMessageList = connector.getSentMessageList();
			cal = connector.getPasswordInfo();
			user = connector.getUserInfo();
			owner = connector.getOwnerInfo();
		
			String passwordExpiration;			
			if(cal == null){
				passwordExpiration = Integer.toString(-1);
			}
			else{
				passwordExpiration = cal.toString();
			}
			
			
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.OWNER_ADDRESS_CITY, owner.getAddressCity());
			values.put(DatabaseHelper.OWNER_ADDRESS_MUNIC_NUMBER, owner.getAddressNumberInMunicipality());
			values.put(DatabaseHelper.OWNER_ADDRESS_STATE, owner.getAddressState());
			values.put(DatabaseHelper.OWNER_ADDRESS_STREET, owner.getAddressStreet());
			values.put(DatabaseHelper.OWNER_ADDRESS_STREET_NUMBER, owner.getAddressNumberInStreet());
			values.put(DatabaseHelper.OWNER_ADDRESS_ZIP, owner.getAddressZipCode());
			values.put(DatabaseHelper.OWNER_BIRTH_CITY, owner.getBirthCity());
			values.put(DatabaseHelper.OWNER_BIRTH_COUNTY, owner.getBirthCounty());
			values.put(DatabaseHelper.OWNER_BIRTH_DATE, owner.getBirthDate());
			values.put(DatabaseHelper.OWNER_BIRTH_STATE, owner.getBirthState());
			values.put(DatabaseHelper.OWNER_EMAIL, owner.getEmail());
			values.put(DatabaseHelper.OWNER_FIRM_NAME, owner.getFirmName());
			values.put(
					DatabaseHelper.OWNER_NAME,
					createName(owner.getPersonNameFirstName(),
							owner.getPersonNameMiddleName(),
							owner.getPersonNameLastName()));
			values.put(DatabaseHelper.OWNER_IC, owner.getIC());
			values.put(DatabaseHelper.OWNER_IDENTIFIER, owner.getIdentifier());
			values.put(DatabaseHelper.OWNER_LAST_BIRTH_NAME, owner.getPersonNameLastNameAtBirth());
			values.put(DatabaseHelper.OWNER_NATIONALITY, owner.getNationality());
			values.put(DatabaseHelper.OWNER_REGISTRY_CODE, owner.getRegistryCode());
			values.put(DatabaseHelper.OWNER_TELEPHONE, owner.getTelNumber());

			values.put(DatabaseHelper.USER_ADDRESS_CITY, user.getAddressCity());
			values.put(DatabaseHelper.USER_ADDRESS_MUNIC_NUMBER, user.getAddressNumberInMunicipality());
			values.put(DatabaseHelper.USER_ADDRESS_STATE, user.getAddressState());
			values.put(DatabaseHelper.USER_ADDRESS_STREET, user.getAddressStreet());
			values.put(DatabaseHelper.USER_ADDRESS_STREET_NUMBER, user.getAddressNumberInStreet());
			values.put(DatabaseHelper.USER_ADDRESS_ZIP, user.getAddressZipCode());
			values.put(DatabaseHelper.USER_BIRTH_DATE, user.getBirthDate());
			values.put(DatabaseHelper.USER_CON_ADDRESS_CITY, user.getContactAdressCity());
			//values.put(DatabaseHelper.USER_CON_ADDRESS_STATE, user.get
			values.put(DatabaseHelper.USER_CON_ADDRESS_STREET, user.getContactAdressStreet());
			values.put(DatabaseHelper.USER_CON_ADDRESS_ZIP, user.getContactAdressZipCode());
			values.put(DatabaseHelper.USER_FIRM_NAME, user.getFirmName());
			values.put(
					DatabaseHelper.USER_NAME,
					createName(user.getPersonNameFirstName(),
							user.getPersonNameMiddleName(),
							user.getPersonNameLastName()));
			values.put(DatabaseHelper.USER_IC, user.getIC());
			values.put(DatabaseHelper.USER_ISDS_ID, user.getUserId());
			values.put(DatabaseHelper.USER_LAST_BIRTH_NAME, user.getPersonNameLastNameAtBirth());
			values.put(DatabaseHelper.USER_PRIVILS, user.getUserPrivils());
			values.put(DatabaseHelper.USER_TYPE, user.getUserType());
			
			values.put(DatabaseHelper.MSGBOX_ISDS_ID, owner.getDataBoxID());
			values.put(DatabaseHelper.MSGBOX_TYPE, owner.getDataBoxType().name());
			values.put(DatabaseHelper.MSGBOX_LOGIN, login);
			values.put(DatabaseHelper.MSGBOX_PASSWORD, password);
			values.put(DatabaseHelper.MSGBOX_TEST_ENV, testEnvironment);
			values.put(DatabaseHelper.MSGBOX_PASSWD_EXPIRATION, passwordExpiration);
			
			
			String msgBoxId = getContentResolver().insert(
					MsgBoxContentProvider.CONTENT_URI, values)
					.getLastPathSegment();
			
			Iterator<MessageEnvelope> receivedMsgIterator = recievedMessageList.iterator();
			while(receivedMsgIterator.hasNext()){
				ContentValues rcvdMessageValues = new ContentValues();
				MessageEnvelope msgEnvelope = receivedMsgIterator.next();
				
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_DM_TYPE, msgEnvelope.getDmType());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_TO_HANDS, msgEnvelope.getToHands());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(msgEnvelope.getAcceptanceTime().getTime()));
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE, AndroidUtils.toXmlDate(msgEnvelope.getDeliveryTime().getTime()));
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_IS_READ, NOT_READ);
				//rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_LAW, msgEnvelope.getLegalTitle().getLaw());
				//rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_PAR, msgEnvelope.getLegalTitle().getPar());
				//rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_POINT, msgEnvelope.getLegalTitle().getPoint());
				//rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_SECT, msgEnvelope.getLegalTitle().getSect());
				//rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_YEAR, msgEnvelope.getLegalTitle().getYear());
				rcvdMessageValues.put(DatabaseHelper.SENDER_ADDRESS, msgEnvelope.getSender().getAddress());
				rcvdMessageValues.put(DatabaseHelper.SENDER_ISDS_ID, msgEnvelope.getSender().getdataBoxID());
				rcvdMessageValues.put(DatabaseHelper.SENDER_NAME, msgEnvelope.getSender().getIdentity());
				//rcvdMessageValues.put(DatabaseHelper.SENDER_DATABOX_TYPE, msgEnvelope.getSender().getDataBoxType().name());
				//rcvdMessageValues.put(DatabaseHelper.SENDER_IDENT, msgEnvelope.getSenderIdent().getIdent());
				//rcvdMessageValues.put(DatabaseHelper.SENDER_REF_NUMBER, msgEnvelope.getSenderIdent().getRefNumber());
				//rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_STATE, msgEnvelope.getState().name());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_TYPE, msgEnvelope.getType().name());
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID, msgBoxId);
				rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());
				
				getContentResolver().insert(ReceivedMessagesContentProvider.CONTENT_URI, rcvdMessageValues);
			}
			
			Iterator<MessageEnvelope> sentMsgIterator = sentMessageList.iterator();
			while(sentMsgIterator.hasNext()){
				ContentValues sentMessageValues = new ContentValues();
				MessageEnvelope msgEnvelope = sentMsgIterator.next();
				
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_DM_TYPE, msgEnvelope.getDmType());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_TO_HANDS, msgEnvelope.getToHands());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(msgEnvelope.getAcceptanceTime().getTime()));
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msgEnvelope.getDeliveryTime().getTime()));
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_IS_READ, NOT_READ);
				//sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_LAW, msgEnvelope.getLegalTitle().getLaw());
				//sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_PAR, msgEnvelope.getLegalTitle().getPar());
				//sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_POINT, msgEnvelope.getLegalTitle().getPoint());
				//sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_SECT, msgEnvelope.getLegalTitle().getSect());
				//sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_YEAR, msgEnvelope.getLegalTitle().getYear());
				sentMessageValues.put(DatabaseHelper.RECIPIENT_ADDRESS, msgEnvelope.getRecipient().getAddress());
				sentMessageValues.put(DatabaseHelper.RECIPIENT_ISDS_ID, msgEnvelope.getRecipient().getdataBoxID());
				sentMessageValues.put(DatabaseHelper.RECIPIENT_NAME, msgEnvelope.getRecipient().getIdentity());
				//sentMessageValues.put(DatabaseHelper.RECIPIENT_DATABOX_TYPE, msgEnvelope.getRecipient().getDataBoxType().name());
				//sentMessageValues.put(DatabaseHelper.RECIPIENT_IDENT, msgEnvelope.getRecipientIdent().getIdent());
				//sentMessageValues.put(DatabaseHelper.RECIPIENT_REF_NUMBER, msgEnvelope.getRecipientIdent().getRefNumber());
				//sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_STATE, msgEnvelope.getState().name());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_TYPE, msgEnvelope.getType().name());
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_MSGBOX_ID, msgBoxId);
				sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());
				
				getContentResolver().insert(SentMessagesContentProvider.CONTENT_URI, sentMessageValues);
			}
			
			message.arg1 = RESULT_OK;

		} catch (HttpException e) {
			if (e.getErrorCode() == RESULT_BAD_LOGIN) {
				message.arg1 = RESULT_BAD_LOGIN;
			} else
				message.arg1 = RESULT_ERR;
		} catch (DSException e) {
				message.arg1 = RESULT_DS_ERR;
				message.obj = (Object) e.getMessage();
		} finally {
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
