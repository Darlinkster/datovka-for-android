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

package cz.nic.datovka.services;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kobjects.base64.Base64;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.activities.AppUtils;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MessagesContentProvider;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.SSLCertificateException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class AddAccountService extends Service {
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String TESTENV = "testenv";
	public static final String HANDLER = "handler";
	public static final int RESULT_OK = 100;
	public static final int RESULT_EXISTS = 101;
	public static final int RESULT_NO_CONNECTION = 102;
	public static final int RESULT_BAD_LOGIN = 401;
	public static final int RESULT_ERR = 103;
	public static final int RESULT_DS_ERR = 104;
	public static final int RESULT_BAD_CERT = 105;
	public static final int PROGRESS_UPDATE = 106;
	public static final int ERROR_INTERRUPTED = 107;
	public static final int DATABOX_CREATING = 1;
	public static final int INBOX_DOWNLOADING = 2;
	public static final int OUTBOX_DOWNLOADING = 3;
	private static final int NOT_READ = 0;
	private static final int READ = 1;
	private static final int STATUS_NOT_CHANGED = 0;

	private Messenger messenger;
	private DaemonThread thread;
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Connector connector;

	@Override
	public void onStart(Intent intent, int startId) {
		if(intent == null || intent.getExtras() == null){
			logger.log(Level.WARNING, "Add account service started with empty intent extras. Aborting.");
			return;
		}
		Bundle extras = intent.getExtras();
		messenger = (Messenger) extras.get(HANDLER);

		String login = extras.getString(LOGIN);
		String password = extras.getString(PASSWORD);
		int testEnvironment = extras.getBoolean(TESTENV) ? 1 : 0;

		if (checkIfAccountExists(login)) {
			// Account already exists
			Message message = Message.obtain();
			message.arg1 = RESULT_EXISTS;

			try {
				messenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		} else {
			// Account doesn't exist, create it
			// try to login
			thread = new DaemonThread(login, password, testEnvironment);
			thread.start();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(thread != null){
			if(connector != null) {
				connector.close();
			}
			thread.interrupt();
		}
		connector = null;
		thread = null;
		logger.log(Level.INFO, "Add Account service interrupted.");
	}

	

	private boolean checkIfAccountExists(String login) {
		String[] projection = { DatabaseHelper.MSGBOX_LOGIN };
		String selection = DatabaseHelper.MSGBOX_LOGIN + " = ? ";
		String[] selectionArgs = { login };

		Cursor cursor = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI, projection, selection, selectionArgs, null);

		int rowCount = cursor.getCount();
		cursor.close();
		cursor = null;

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	private class DaemonThread extends Thread {
		private String login;
		private String password;
		private int testEnvironment;
		
		public DaemonThread(String login, String password, int testEnvironment) {
			this.login = login;
			this.password = password;
			this.testEnvironment = testEnvironment;
		}
		
		@Override
		public void run() {
			super.run();
			logger.log(Level.INFO, "Add acccount service started");
			createAccount(login, password, testEnvironment);
		}
		
		private String createOwnerName(int testEnv, String firmName, String name, String middle, String last) {
			String userName = "";
			if (testEnv == Connector.TESTING) {
				userName += "* ";
			}
			if (firmName.length() > 0) {
				userName += firmName;
			}

			if (name.length() > 0) {
				if (firmName.length() > 0) {
					userName += ", ";
				}
				userName += name;
			}
			if (middle.length() > 0) {
				if (userName.length() > 0) {
					userName += " ";
				}
				userName += middle;
			}
			if (last.length() > 0) {
				if (userName.length() > 0) {
					userName += " ";
				}
				userName += last;
			}

			return userName;
		}

		private String createName(String name, String middle, String last) {
			String userName = "";

			if (name.length() > 0) {
				userName += name;
			}
			if (middle.length() > 0) {
				if (userName.length() > 0) {
					userName += " ";
				}
				userName += middle;
			}
			if (last.length() > 0) {
				if (userName.length() > 0) {
					userName += " ";
				}
				userName += last;
			}

			return userName;
		}
		
		private void createAccount(String login, String password, int testEnvironment) {
			String encPassword = Base64.encode(password.getBytes());
			connector = new Connector();
			
			if(isInterrupted()) return;
			if (!connector.checkConnection()) {
				Message message = Message.obtain();
				message.arg1 = RESULT_NO_CONNECTION;
				try {
					messenger.send(message);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
			if(isInterrupted()) return;
			try {
				if (testEnvironment == Connector.TESTING) {
					connector.connect(login, encPassword, Connector.TESTING);
				} else {
					connector.connect(login, encPassword, Connector.PRODUCTION);
				}
			} catch (SSLCertificateException e) {
				Message msg = Message.obtain();
				msg.arg1 = RESULT_BAD_CERT;
				try {
					messenger.send(msg);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}

			// Try to acquire information from webservices, if something wrong
			// happend show the error message
			Message message = Message.obtain();
			try {
				logger.log(Level.INFO, "Downloading user, owner and databox info.");
				
				Message msg1 = Message.obtain();
				msg1.arg1 = PROGRESS_UPDATE;
				msg1.arg2 = DATABOX_CREATING;
				try {
					messenger.send(msg1);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				msg1 = null;
				
				if(isInterrupted()) return;
				GregorianCalendar cal = connector.getPasswordInfo();
				UserInfo user = connector.getUserInfo();
				OwnerInfo owner = connector.getOwnerInfo();

				String passwordExpiration;
				if (cal == null) {
					passwordExpiration = Integer.toString(-1);
				} else {
					passwordExpiration = Long.toString(cal.getTimeInMillis());
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

				String ownerName = createOwnerName(testEnvironment, owner.getFirmName(), owner.getPersonNameFirstName(), owner.getPersonNameMiddleName(),
						owner.getPersonNameLastName());

				values.put(DatabaseHelper.OWNER_NAME, ownerName);
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
				// values.put(DatabaseHelper.USER_CON_ADDRESS_STATE, user.get
				values.put(DatabaseHelper.USER_CON_ADDRESS_STREET, user.getContactAdressStreet());
				values.put(DatabaseHelper.USER_CON_ADDRESS_ZIP, user.getContactAdressZipCode());
				values.put(DatabaseHelper.USER_FIRM_NAME, user.getFirmName());
				values.put(DatabaseHelper.USER_NAME, createName(user.getPersonNameFirstName(), user.getPersonNameMiddleName(), user.getPersonNameLastName()));
				values.put(DatabaseHelper.USER_IC, user.getIC());
				values.put(DatabaseHelper.USER_ISDS_ID, user.getUserId());
				values.put(DatabaseHelper.USER_LAST_BIRTH_NAME, user.getPersonNameLastNameAtBirth());
				values.put(DatabaseHelper.USER_PRIVILS, user.getUserPrivils());
				values.put(DatabaseHelper.USER_TYPE, user.getUserType());
				values.put(DatabaseHelper.MSGBOX_ISDS_ID, owner.getDataBoxID());
				values.put(DatabaseHelper.MSGBOX_TYPE, owner.getDataBoxType().name());
				values.put(DatabaseHelper.MSGBOX_LOGIN, login);
				values.put(DatabaseHelper.MSGBOX_PASSWORD, encPassword);
				values.put(DatabaseHelper.MSGBOX_TEST_ENV, testEnvironment);
				values.put(DatabaseHelper.MSGBOX_PASSWD_EXPIRATION, passwordExpiration);
				
				if(isInterrupted()) return;
				String msgBoxId = getContentResolver().insert(MsgBoxContentProvider.CONTENT_URI, values).getLastPathSegment();
				values = null;

				if(isInterrupted()) return;
				logger.log(Level.INFO, "Downloading inbox messages.");
				
				Message msg2 = Message.obtain();
				msg2.arg1 = PROGRESS_UPDATE;
				msg2.arg2 = INBOX_DOWNLOADING;
				try {
					messenger.send(msg2);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				msg2 = null;
				
				List<MessageEnvelope> recievedMessageList = connector.getRecievedMessageList();
				Iterator<MessageEnvelope> receivedMsgIterator = recievedMessageList.iterator();
				while (receivedMsgIterator.hasNext()) {
					if(isInterrupted()) return;
					MessageEnvelope msgEnvelope = receivedMsgIterator.next();
					ContentValues rcvdMessageValues = new ContentValues();

					rcvdMessageValues.put(DatabaseHelper.MESSAGE_FOLDER, AppUtils.INBOX);
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_DM_TYPE, msgEnvelope.getDmType());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_TO_HANDS, msgEnvelope.getToHands());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(msgEnvelope.getAcceptanceTime().getTime()));
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msgEnvelope.getDeliveryTime().getTime()));
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_IS_READ, NOT_READ);
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_LAW, msgEnvelope.getLegalTitle().getLaw());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_PAR, msgEnvelope.getLegalTitle().getPar());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_POINT, msgEnvelope.getLegalTitle().getPoint());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_SECT, msgEnvelope.getLegalTitle().getSect());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_YEAR, msgEnvelope.getLegalTitle().getYear());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_OTHERSIDE_ADDRESS, msgEnvelope.getSender().getAddress());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_OTHERSIDE_ISDS_ID, msgEnvelope.getSender().getdataBoxID());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_OTHERSIDE_NAME, msgEnvelope.getSender().getIdentity());
					// rcvdMessageValues.put(DatabaseHelper.SENDER_DATABOX_TYPE,
					// msgEnvelope.getSender().getDataBoxType().name());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_SENDER_IDENT, msgEnvelope.getSenderIdent().getIdent());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_SENDER_REF_NUMBER, msgEnvelope.getSenderIdent().getRefNumber());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_IDENT, msgEnvelope.getRecipientIdent().getIdent());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_REF_NUMBER, msgEnvelope.getRecipientIdent().getRefNumber());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_STATE, msgEnvelope.getStateAsInt());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_STATUS_CHANGED, STATUS_NOT_CHANGED);
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_TYPE, msgEnvelope.getType().name());
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_MSGBOX_ID, msgBoxId);
					rcvdMessageValues.put(DatabaseHelper.MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());

					getContentResolver().insert(MessagesContentProvider.CONTENT_URI, rcvdMessageValues);
					rcvdMessageValues = null;
				}

				if(isInterrupted()) return;
				logger.log(Level.INFO, "Downloading outbox messages");
				
				Message msg3 = Message.obtain();
				msg3.arg1 = PROGRESS_UPDATE;
				msg3.arg2 = OUTBOX_DOWNLOADING;
				try {
					messenger.send(msg3);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				msg3 = null;
				
				List<MessageEnvelope> sentMessageList = connector.getSentMessageList();
				Iterator<MessageEnvelope> sentMsgIterator = sentMessageList.iterator();
				while (sentMsgIterator.hasNext()) {
					if(isInterrupted()) return;
					MessageEnvelope msgEnvelope = sentMsgIterator.next();
					ContentValues sentMessageValues = new ContentValues();
					sentMessageValues.put(DatabaseHelper.MESSAGE_FOLDER, AppUtils.OUTBOX);
					sentMessageValues.put(DatabaseHelper.MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
					sentMessageValues.put(DatabaseHelper.MESSAGE_DM_TYPE, msgEnvelope.getDmType());
					sentMessageValues.put(DatabaseHelper.MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
					sentMessageValues.put(DatabaseHelper.MESSAGE_TO_HANDS, msgEnvelope.getToHands());
					sentMessageValues.put(DatabaseHelper.MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
					sentMessageValues.put(DatabaseHelper.MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());

					GregorianCalendar acceptanceDate = msgEnvelope.getAcceptanceTime();
					if (acceptanceDate != null)
						sentMessageValues.put(DatabaseHelper.MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(acceptanceDate.getTime()));

					sentMessageValues.put(DatabaseHelper.MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msgEnvelope.getDeliveryTime().getTime()));
					sentMessageValues.put(DatabaseHelper.MESSAGE_IS_READ, READ);
					sentMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_LAW, msgEnvelope.getLegalTitle().getLaw());
					sentMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_PAR, msgEnvelope.getLegalTitle().getPar());
					sentMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_POINT, msgEnvelope.getLegalTitle().getPoint());
					sentMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_SECT, msgEnvelope.getLegalTitle().getSect());
					sentMessageValues.put(DatabaseHelper.MESSAGE_LEGALTITLE_YEAR, msgEnvelope.getLegalTitle().getYear());
					sentMessageValues.put(DatabaseHelper.MESSAGE_OTHERSIDE_ADDRESS, msgEnvelope.getRecipient().getAddress());
					sentMessageValues.put(DatabaseHelper.MESSAGE_OTHERSIDE_ISDS_ID, msgEnvelope.getRecipient().getdataBoxID());
					sentMessageValues.put(DatabaseHelper.MESSAGE_OTHERSIDE_NAME, msgEnvelope.getRecipient().getIdentity());
					// sentMessageValues.put(DatabaseHelper.RECIPIENT_DATABOX_TYPE,
					// msgEnvelope.getRecipient().getDataBoxType().name());
					sentMessageValues.put(DatabaseHelper.MESSAGE_SENDER_IDENT, msgEnvelope.getSenderIdent().getIdent());
					sentMessageValues.put(DatabaseHelper.MESSAGE_SENDER_REF_NUMBER, msgEnvelope.getSenderIdent().getRefNumber());
					sentMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_IDENT, msgEnvelope.getRecipientIdent().getIdent());
					sentMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_REF_NUMBER, msgEnvelope.getRecipientIdent().getRefNumber());
					sentMessageValues.put(DatabaseHelper.MESSAGE_STATE, msgEnvelope.getStateAsInt());
					sentMessageValues.put(DatabaseHelper.MESSAGE_STATUS_CHANGED, STATUS_NOT_CHANGED);
					sentMessageValues.put(DatabaseHelper.MESSAGE_TYPE, msgEnvelope.getType().name());
					sentMessageValues.put(DatabaseHelper.MESSAGE_MSGBOX_ID, msgBoxId);
					sentMessageValues.put(DatabaseHelper.MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());

					getContentResolver().insert(MessagesContentProvider.CONTENT_URI, sentMessageValues);
					sentMessageValues = null;
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
			} catch (StreamInterruptedException e1) {
				message.arg1 = ERROR_INTERRUPTED;
				e1.printStackTrace();
			} finally {
				if(isInterrupted()) return;
				try {
					messenger.send(message);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
