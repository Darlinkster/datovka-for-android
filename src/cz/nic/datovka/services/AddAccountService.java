package cz.nic.datovka.services;

import java.util.Arrays;
import java.util.GregorianCalendar;
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
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;

public class AddAccountService extends IntentService {
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String TESTENV = "testenv";
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
		int testEnvironment = extras.getBoolean(TESTENV) ? 1 : 0;

		Cursor cursor = getContentResolver().query(
				MsgBoxContentProvider.CONTENT_URI,
				DatabaseHelper.msgbox_columns,
				DatabaseHelper.MSGBOX_LOGIN + " = ? and "
						+ DatabaseHelper.MSGBOX_PASSWORD + " = ?",
				new String[] { login, password }, null);

		if (cursor.getCount() > 0) {
			// Account already exists
			message.arg1 = RESULT_EXISTS;
		} else {
			// Account doesn't exist, so let's create it
			// try to login
			try {
				Connector.connect(login, password, Connector.TESTING,
						getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
			}

			UserInfo uinfo = Connector.getUserInfo();
			List<MessageEnvelope> recievedMessageList = Connector
					.getRecievedMessageList();
			List<MessageEnvelope> sentMessageList = Connector
					.getSentMessageList();
			GregorianCalendar cal = Connector.getPasswordInfo();

			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.MSGBOX_LOGIN, login);
			values.put(DatabaseHelper.MSGBOX_PASSWORD, password);
			values.put(DatabaseHelper.MSGBOX_TEST_ENV, testEnvironment);
			values.put(DatabaseHelper.MSGBOX_OWNER_ID, "0");
			values.put(DatabaseHelper.MSGBOX_USER_ID, "0");
			getContentResolver().insert(MsgBoxContentProvider.CONTENT_URI,
					values);

			message.arg1 = RESULT_OK;

		}
		try {
			messenger.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
}
