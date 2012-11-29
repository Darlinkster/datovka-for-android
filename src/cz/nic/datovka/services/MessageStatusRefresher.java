package cz.nic.datovka.services;

import java.util.GregorianCalendar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;

public class MessageStatusRefresher extends Thread {
	public static final String MSG_ID = "msgid";
	public static final String FOLDER = "folder";
	public static final String RECEIVER = "receiver";
	public static final int ERROR_BAD_LOGIN = 100;
	public static final int ERROR = 200;
	private static final int INBOX = 0;
	private static final int STATUS_CHANGED = 1;
	private long msgId;
	private int folder;
	private Messenger messenger;

	public MessageStatusRefresher(Intent param) {
		super();
		msgId = param.getLongExtra(MSG_ID, 0);
		folder = param.getIntExtra(FOLDER, 0);
		messenger = (Messenger) param.getParcelableExtra(RECEIVER);
	}

	@Override
	public void run() {
		Uri msgUri;
		String msgIsdsIdTb;
		String msgboxIdTb;
		String msgStatusTb;
		if (folder == INBOX) {
			msgUri = ContentUris.withAppendedId(ReceivedMessagesContentProvider.CONTENT_URI, msgId);
			msgIsdsIdTb = DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID;
			msgboxIdTb = DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID;
			msgStatusTb = DatabaseHelper.RECEIVED_MESSAGE_STATE;
		} else {
			msgUri = ContentUris.withAppendedId(SentMessagesContentProvider.CONTENT_URI, msgId);
			msgIsdsIdTb = DatabaseHelper.SENT_MESSAGE_ISDS_ID;
			msgboxIdTb = DatabaseHelper.SENT_MESSAGE_MSGBOX_ID;
			msgStatusTb = DatabaseHelper.SENT_MESSAGE_STATE;
		}
		String[] msgProjection = {msgIsdsIdTb , msgboxIdTb, msgStatusTb};
		Cursor msgCursor = Application.ctx.getContentResolver().query(msgUri, msgProjection, null, null, null);
		msgCursor.moveToFirst();
				
		String msgIsdsId = msgCursor.getString(msgCursor.getColumnIndex(msgIsdsIdTb));
		long msgboxId = msgCursor.getLong(msgCursor.getColumnIndex(msgboxIdTb));
		int msgStatus = msgCursor.getInt(msgCursor.getColumnIndex(msgStatusTb));
		msgCursor.close();

		// Get MsgBox ISDS ID
		String msgBoxIsdsId = "-1";
		Uri msgBoxUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgboxId);
		Cursor msgBoxCursor = Application.ctx.getContentResolver().query(msgBoxUri, new String[] { DatabaseHelper.MSGBOX_ISDS_ID }, null, null, null);
		if (msgBoxCursor.moveToFirst()) {
			msgBoxIsdsId = msgBoxCursor.getString(msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID));
		}
		msgBoxCursor.close();
		msgBoxCursor = null;
		
		Connector connector = Connector.connectToWs(msgboxId);
		try {
			MessageEnvelope msg = connector.GetDeliveryInfo(msgIsdsId);
			if(msgStatus != msg.getStateAsInt()){
				ContentValues val = new ContentValues();
				if (folder == INBOX) {
					GregorianCalendar recvAcceptanceDate = msg.getAcceptanceTime();
					if(recvAcceptanceDate != null)
						val.put(DatabaseHelper.RECEIVED_MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(recvAcceptanceDate.getTime()));
					val.put(DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE, AndroidUtils.toXmlDate(msg.getDeliveryTime().getTime()));
					val.put(DatabaseHelper.RECEIVED_MESSAGE_STATE, msg.getStateAsInt());
					val.put(DatabaseHelper.RECEIVED_MESSAGE_STATUS_CHANGED, STATUS_CHANGED);
				} else {
					GregorianCalendar sentAcceptanceDate = msg.getAcceptanceTime();
					if(sentAcceptanceDate != null)
						val.put(DatabaseHelper.SENT_MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(sentAcceptanceDate.getTime()));
					val.put(DatabaseHelper.SENT_MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msg.getDeliveryTime().getTime()));
					val.put(DatabaseHelper.SENT_MESSAGE_STATE, msg.getStateAsInt());
					val.put(DatabaseHelper.SENT_MESSAGE_STATUS_CHANGED, STATUS_CHANGED);
				}
				Application.ctx.getContentResolver().update(msgUri, val, null, null);
			}
			
		} catch (HttpException e) {
			e.printStackTrace();
			Message message = Message.obtain();
			if(e.getErrorCode() == 401){
				String msg = Application.ctx.getString(R.string.cannot_login, msgBoxIsdsId);
				message.arg1 = ERROR_BAD_LOGIN;
				message.obj = msg;
			} else {
				message.arg1 = ERROR;
				message.obj = e.getMessage();
			}
			try {
				messenger.send(message);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		} catch (DSException e) {
			e.printStackTrace();
			Message message2 = Message.obtain();
			message2.arg1 = ERROR;
			message2.obj = e.getMessage();
			try {
				messenger.send(message2);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
		
	}
}
