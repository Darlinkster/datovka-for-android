package cz.nic.datovka.services;

import java.util.GregorianCalendar;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.connector.Connector;

public class AddAccountService extends IntentService {
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String HANDLER = "handler";
	public static final int RESULT_OK = 100;
	public static final int RESULT_EXISTS = 101;

	public AddAccountService() {
		super("AddAccountService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		Messenger messenger = (Messenger) extras.get(HANDLER);
		Message message = Message.obtain();
		
		String login = extras.getString(LOGIN);
		String password = extras.getString(PASSWORD);
		
		/*
		Cursor cursor = getContentResolver().query(
				AccountContentProvider.CONTENT_URI,
				DatabaseHelper.account_columns,
				DatabaseHelper.ACCOUNT_LOGIN + " = \"" + login + "\" and "
						+ DatabaseHelper.ACCOUNT_PASSWORD + " = \"" + password + "\"",
				new String[] { login, password }, null);
		
		if(cursor.getCount() > 0) {
			// Account already exists
			message.arg1 = RESULT_EXISTS;
		}
		*/
		// try to login
		try {
			Connector.connect(login, password, Connector.TESTING, getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		UserInfo uinfo = Connector.getUserInfo();
		List<MessageEnvelope> recievedMessageList = Connector.getRecievedMessageList();
		List<MessageEnvelope> sentMessageList = Connector.getSentMessageList();
		GregorianCalendar cal = Connector.getPasswordInfo();
		
		
		/*
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.ACCOUNT_LOGIN, login);
		values.put(DatabaseHelper.ACCOUNT_PASSWORD, password);
		getContentResolver().insert(AccountContentProvider.CONTENT_URI, values);
		*/
		message.arg1 = RESULT_OK;
		
		
		try {
			messenger.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
