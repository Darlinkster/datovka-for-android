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
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.R;
import cz.nic.datovka.activities.AppUtils;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MessagesContentProvider;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.exceptions.MessageBoxIdNotKnown;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.SSLCertificateException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class MessageStatusRefresher extends Service {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static final String MSG_ID = "msgid";
	public static final String RECEIVER = "receiver";
	private long msgId;
	private Messenger messenger;
	private Thread thread;
	private Connector connector;
	
	public static final int ERROR_BAD_LOGIN = 100;
	public static final int ERROR = 200;
	public static final int STATUS_UPDATED = 300;
	public static final int ERROR_CERT = 400;
	public static final int ERROR_INTERRUPTED = 500;
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent == null || intent.getExtras() == null){
			logger.log(Level.WARNING, "Message status refresh service started with empty intent extras. Aborting.");
			return;
		}

		msgId = intent.getLongExtra(MSG_ID, 0);
		messenger = (Messenger) intent.getParcelableExtra(RECEIVER);

		thread = new DaemonThread();
		thread.start();
	}
	
	@Override
	public void onDestroy() {

		if (thread != null) {
			if (connector != null) {
				connector.close();
				// System.out.println("odpojeno");
			}
			thread.interrupt();
		}
		thread = null;
		connector = null;
		messenger = null;
		
		super.onDestroy();
		logger.log(Level.INFO, "Message status refresh service interrupted.");
	}
	
	private class DaemonThread extends Thread {
		@Override
		public void run() {
			logger.log(Level.INFO, "Message status refresh service is running.");
			Uri msgUri = ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, msgId);
			String msgIsdsIdTb = DatabaseHelper.MESSAGE_ISDS_ID;
			String msgboxIdTb = DatabaseHelper.MESSAGE_MSGBOX_ID;
			String msgStatusTb = DatabaseHelper.MESSAGE_STATE;

			if(isInterrupted()) return;
			
			String[] msgProjection = { msgIsdsIdTb, msgboxIdTb, msgStatusTb };
			Cursor msgCursor = AppUtils.ctx.getContentResolver().query(msgUri, msgProjection, null, null, null);
			if (!msgCursor.moveToFirst()) {
				// There is no message with that ID, send an error message
				msgCursor.close();
				String msg = AppUtils.ctx.getString(R.string.message_with_id_not_found, msgId);
				Message message = Message.obtain();
				message.arg1 = ERROR;
				message.obj = msg;
				try {
					messenger.send(message);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				return;
			}

			String msgIsdsId = msgCursor.getString(msgCursor.getColumnIndex(msgIsdsIdTb));
			long msgboxId = msgCursor.getLong(msgCursor.getColumnIndex(msgboxIdTb));
			int msgStatus = msgCursor.getInt(msgCursor.getColumnIndex(msgStatusTb));
			msgCursor.close();
			msgCursor = null;
			
			if(isInterrupted()) return;

			// Get MsgBox ISDS ID
			String msgBoxIsdsId = "-1";
			Uri msgBoxUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgboxId);
			Cursor msgBoxCursor = AppUtils.ctx.getContentResolver().query(msgBoxUri, new String[] { DatabaseHelper.MSGBOX_ISDS_ID }, null, null, null);
			if (msgBoxCursor.moveToFirst()) {
				msgBoxIsdsId = msgBoxCursor.getString(msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID));
			}
			msgBoxCursor.close();
			msgBoxCursor = null;

			int statusChanged = 0;

			Message message = Message.obtain();
			try {
				connector = Connector.connectToWs(msgboxId);
				
				if(isInterrupted()) return;
				
				MessageEnvelope msg = connector.GetDeliveryInfo(msgIsdsId);
				if (msgStatus != msg.getStateAsInt()) {
					ContentValues val = new ContentValues();

					GregorianCalendar recvAcceptanceDate = msg.getAcceptanceTime();
					if (recvAcceptanceDate != null)
						val.put(DatabaseHelper.MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(recvAcceptanceDate.getTime()));
					val.put(DatabaseHelper.MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msg.getDeliveryTime().getTime()));
					val.put(DatabaseHelper.MESSAGE_STATE, msg.getStateAsInt());

					if(isInterrupted()) return;
					
					AppUtils.ctx.getContentResolver().update(msgUri, val, null, null);
					statusChanged = 1;
				}
				
				message.arg1 = STATUS_UPDATED;
				message.arg2 = statusChanged;
				

			} catch (HttpException e) {
				e.printStackTrace();
				message = Message.obtain();
				if (e.getErrorCode() == 401) {
					String msg = AppUtils.ctx.getString(R.string.cannot_login, msgBoxIsdsId);
					message.arg1 = ERROR_BAD_LOGIN;
					message.obj = msg;
				} else {
					message.arg1 = ERROR;
					message.obj = e.getMessage();
				}
			} catch (DSException e) {
				e.printStackTrace();
				message = Message.obtain();
				message.arg1 = ERROR;
				message.obj = e.getMessage();
			} catch (SSLCertificateException e) {
				// certicate error
				message = Message.obtain();
				message.arg1 = ERROR_CERT;
				e.printStackTrace();
			} catch (StreamInterruptedException e) {
				message = Message.obtain();
				message.arg1 = ERROR_INTERRUPTED;
				e.printStackTrace();
			} catch (NullPointerException e){
				logger.log(Level.WARNING, "Null pointer Exception: User probably killed download thread.");
				e.printStackTrace();
				message = Message.obtain();
				message.arg1 = ERROR;
				message.obj = AppUtils.ctx.getString(R.string.download_crashed);
			} catch (MessageBoxIdNotKnown e) {
				e.printStackTrace();
				message.arg1 = ERROR;
				message.obj = AppUtils.ctx.getString(R.string.msgbox_id_not_known);
			} finally {
				if(message != null) {
					if(messenger != null) {
						try {
							messenger.send(message);
						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
