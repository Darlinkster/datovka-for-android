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

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

public class MessageBoxRefreshService extends Service {
	public static final int ERROR = -1;
	public static final int ERROR_NO_CONNECTION = -99;
	public static final int ERROR_BAD_LOGIN = -401;
	public static final int ERROR_CERT = -500;
	public static final int ERROR_INTERRUPTED = -600;
	public static final int ERROR_MSGBOXID_NOTKNOWN = -700;
	private DaemonThread thread;
	private static final int NOT_READ = 0;
	private static final int READ = 1;
	private static final int NOT_CHANGED = 0;
	private static final int STATUS_CHANGED = 1;
	public static final String HANDLER = "handler";
	public static final String MSGBOX_ID = "msgboxid";
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Message message;
	private Messenger messenger;
	private String messageBoxId;
	
	public void onStart(Intent intent, int startId) {
		if(intent == null || intent.getExtras() == null){
			logger.log(Level.WARNING, "Message Box refresh service started with empty intent extras. Aborting.");
			return;
		}
		if(thread != null && thread.isAlive()){
			logger.log(Level.WARNING, "Message Box refresh service started but previous thread is still running. Aborting.");
			return;
		}
		
		Bundle extras = intent.getExtras();
		messageBoxId = extras.getString(MSGBOX_ID);
		messenger = (Messenger) extras.get(HANDLER);
		message = Message.obtain();
		
		thread = new DaemonThread();
		thread.start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class DaemonThread extends Thread {
		public void run() {
			//System.out.println("thread started");
			Cursor msgBoxCursor;
			if(messageBoxId == null) { 
				msgBoxCursor = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI,
						new String[] { DatabaseHelper.MSGBOX_ID, DatabaseHelper.MSGBOX_ISDS_ID }, null, null, null);
			} else {
				msgBoxCursor = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI,
						new String[] { DatabaseHelper.MSGBOX_ID, DatabaseHelper.MSGBOX_ISDS_ID }, DatabaseHelper.MSGBOX_ID + "=?", new String[]{ messageBoxId }, null);
			}
			int msgBoxIdColIndex = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
			int msgBoxIsdsIdColIndex = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID);
			
			// new message counter
			int newMessageCounter = 0;
			int messageStatusChangeCounter = 0;
			
			// Iterate over all Message Boxes IDs
			while(msgBoxCursor.moveToNext()){
				// get msgbox id
				long msgBoxId = msgBoxCursor.getLong(msgBoxIdColIndex);
				String msgBoxIsdsId = msgBoxCursor.getString(msgBoxIsdsIdColIndex);

				// get last inbox message
				Cursor inboxMsg = getContentResolver().query(MessagesContentProvider.CONTENT_URI,
						new String[] { DatabaseHelper.MESSAGE_SENT_DATE, DatabaseHelper.MESSAGE_MSGBOX_ID, DatabaseHelper.MESSAGE_FOLDER },
						DatabaseHelper.MESSAGE_MSGBOX_ID + "=? and " + DatabaseHelper.MESSAGE_FOLDER + "=?", 
						new String[] { Long.toString(msgBoxId), Integer.toString(AppUtils.INBOX) },	
						DatabaseHelper.MESSAGE_SENT_DATE);
				int rcvdMsgBoxIdColIndex = inboxMsg.getColumnIndex(DatabaseHelper.MESSAGE_SENT_DATE);

				// get last outbox message
				Cursor outboxMsg = getContentResolver().query(MessagesContentProvider.CONTENT_URI,
						new String[] { DatabaseHelper.MESSAGE_SENT_DATE, DatabaseHelper.MESSAGE_MSGBOX_ID, DatabaseHelper.MESSAGE_FOLDER },
						DatabaseHelper.MESSAGE_MSGBOX_ID + "=? and " + DatabaseHelper.MESSAGE_FOLDER + "=?", 
						new String[] { Long.toString(msgBoxId), Integer.toString(AppUtils.OUTBOX) }, 
						DatabaseHelper.MESSAGE_SENT_DATE);
				int sentMsgBoxIdColIndex = outboxMsg.getColumnIndex(DatabaseHelper.MESSAGE_SENT_DATE);

				// convert dates
				long lastInboxMsgTime;
				long lastOutboxMsgTime;
				if(inboxMsg.moveToLast()){
					lastInboxMsgTime = AndroidUtils.toGregorianCalendar(inboxMsg.getString(rcvdMsgBoxIdColIndex)).getTimeInMillis();
				} else {
					lastInboxMsgTime = 0;
				}
				if(outboxMsg.moveToLast()){
					lastOutboxMsgTime = AndroidUtils.toGregorianCalendar(outboxMsg.getString(sentMsgBoxIdColIndex)).getTimeInMillis();
				}else {
					lastOutboxMsgTime = 0;
				}
				
				//close cursors
				inboxMsg.close();
				outboxMsg.close();
				inboxMsg = null;
				outboxMsg = null;
											
				// Connect
				Connector connector;
				try {
					connector = Connector.connectToWs(msgBoxId);
				} catch (SSLCertificateException e2) {
					// certicate error
					Message message = Message.obtain();
					message.arg1 = ERROR_CERT;
					try {
						messenger.send(message);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return;
				} catch (MessageBoxIdNotKnown e) {
					e.printStackTrace();
					Message msg1 = Message.obtain();
					msg1.arg1 = ERROR_MSGBOXID_NOTKNOWN;
					try {
						messenger.send(msg1);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					return;
				}
				
				if(!connector.checkConnection(getApplicationContext())){
					Message msg1 = Message.obtain();
					msg1.arg1 = ERROR_NO_CONNECTION;
					try {
						messenger.send(msg1);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return;
				}
				try {
					// get new messages
					List<MessageEnvelope> newInboxMsg = connector.getRecievedMessageListFromDate(lastInboxMsgTime + 1);
					List<MessageEnvelope> newOutboxMsg = connector.getSentMessageListFromDate(lastOutboxMsgTime + 1);
					
					// insert new messages to database
					Iterator<MessageEnvelope> newInboxMsgIterator = newInboxMsg.iterator();
					Iterator<MessageEnvelope> newOutboxMsgIterator = newOutboxMsg.iterator();
					
					while(newInboxMsgIterator.hasNext()){
						MessageEnvelope msgEnvelope = newInboxMsgIterator.next();
						ContentValues rcvdMessageValues = new ContentValues();
						
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_FOLDER, AppUtils.INBOX);
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_DM_TYPE, msgEnvelope.getDmType());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_TO_HANDS, msgEnvelope.getToHands());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
						
						GregorianCalendar recvAcceptanceDate = msgEnvelope.getAcceptanceTime();
						if(recvAcceptanceDate != null)
							rcvdMessageValues.put(DatabaseHelper.MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(recvAcceptanceDate.getTime()));
						
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
						//rcvdMessageValues.put(DatabaseHelper.SENDER_DATABOX_TYPE, msgEnvelope.getSender().getDataBoxType().name());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_SENDER_IDENT, msgEnvelope.getSenderIdent().getIdent());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_SENDER_REF_NUMBER, msgEnvelope.getSenderIdent().getRefNumber());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_IDENT, msgEnvelope.getRecipientIdent().getIdent());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_REF_NUMBER, msgEnvelope.getRecipientIdent().getRefNumber());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_STATE, msgEnvelope.getStateAsInt());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_STATUS_CHANGED, NOT_CHANGED);
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_TYPE, msgEnvelope.getType().name());
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_MSGBOX_ID, msgBoxId);
						rcvdMessageValues.put(DatabaseHelper.MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());
						
						getContentResolver().insert(MessagesContentProvider.CONTENT_URI, rcvdMessageValues);
						newMessageCounter++;
					}
					
					while(newOutboxMsgIterator.hasNext()){
						ContentValues sentMessageValues = new ContentValues();
						MessageEnvelope msgEnvelope = newOutboxMsgIterator.next();
						
						//System.out.println(msgEnvelope.getAnnotation());
						
						sentMessageValues.put(DatabaseHelper.MESSAGE_FOLDER, AppUtils.OUTBOX);
						sentMessageValues.put(DatabaseHelper.MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
						sentMessageValues.put(DatabaseHelper.MESSAGE_DM_TYPE, msgEnvelope.getDmType());
						sentMessageValues.put(DatabaseHelper.MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
						sentMessageValues.put(DatabaseHelper.MESSAGE_TO_HANDS, msgEnvelope.getToHands());
						sentMessageValues.put(DatabaseHelper.MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
						sentMessageValues.put(DatabaseHelper.MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
						
						GregorianCalendar acceptanceDate = msgEnvelope.getAcceptanceTime();
						if(acceptanceDate != null)
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
						//sentMessageValues.put(DatabaseHelper.RECIPIENT_DATABOX_TYPE, msgEnvelope.getRecipient().getDataBoxType().name());
						sentMessageValues.put(DatabaseHelper.MESSAGE_SENDER_IDENT, msgEnvelope.getSenderIdent().getIdent());
						sentMessageValues.put(DatabaseHelper.MESSAGE_SENDER_REF_NUMBER, msgEnvelope.getSenderIdent().getRefNumber());
						sentMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_IDENT, msgEnvelope.getRecipientIdent().getIdent());
						sentMessageValues.put(DatabaseHelper.MESSAGE_RECIPIENT_REF_NUMBER, msgEnvelope.getRecipientIdent().getRefNumber());
						sentMessageValues.put(DatabaseHelper.MESSAGE_STATE, msgEnvelope.getStateAsInt());
						sentMessageValues.put(DatabaseHelper.MESSAGE_STATUS_CHANGED, NOT_CHANGED);
						sentMessageValues.put(DatabaseHelper.MESSAGE_TYPE, msgEnvelope.getType().name());
						sentMessageValues.put(DatabaseHelper.MESSAGE_MSGBOX_ID, msgBoxId);
						sentMessageValues.put(DatabaseHelper.MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());
						
						getContentResolver().insert(MessagesContentProvider.CONTENT_URI, sentMessageValues);
						//newMessageCounter++;
					}
					
					// Select all outbox messages with status lower than 6 and check if there is any status update
					String select = DatabaseHelper.MESSAGE_STATE + " < ? AND " + DatabaseHelper.MESSAGE_MSGBOX_ID + " = ? and " + DatabaseHelper.MESSAGE_FOLDER + "=?";
					String[] params = { "6", Long.toString(msgBoxId), Integer.toString(AppUtils.OUTBOX) };
					String[] projection = { DatabaseHelper.MESSAGE_ID,
											DatabaseHelper.MESSAGE_FOLDER, 
											DatabaseHelper.MESSAGE_STATE, 
											DatabaseHelper.MESSAGE_ISDS_ID };
					
					Cursor outboxMsgWithBadStatus = getContentResolver().query(MessagesContentProvider.CONTENT_URI, projection, select, params, null);
					int outboxMsgIdColId = outboxMsgWithBadStatus.getColumnIndex(DatabaseHelper.MESSAGE_ID);
					int outboxMsgStateColId = outboxMsgWithBadStatus.getColumnIndex(DatabaseHelper.MESSAGE_STATE);
					int outboxMsgIsdsIdColId = outboxMsgWithBadStatus.getColumnIndex(DatabaseHelper.MESSAGE_ISDS_ID);
					
					while(outboxMsgWithBadStatus.moveToNext()) {
						String msgIsdsId = outboxMsgWithBadStatus.getString(outboxMsgIsdsIdColId);
						MessageEnvelope msg = connector.GetDeliveryInfo(msgIsdsId);
						int msgStatus = outboxMsgWithBadStatus.getInt(outboxMsgStateColId);
						
						if(msgStatus != msg.getStateAsInt()){
							ContentValues val = new ContentValues();
							long msgId = outboxMsgWithBadStatus.getLong(outboxMsgIdColId);
							
							GregorianCalendar sentAcceptanceDate = msg.getAcceptanceTime();
							if(sentAcceptanceDate != null)
								val.put(DatabaseHelper.MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(sentAcceptanceDate.getTime()));
							val.put(DatabaseHelper.MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msg.getDeliveryTime().getTime()));
							val.put(DatabaseHelper.MESSAGE_STATE, msg.getStateAsInt());
							val.put(DatabaseHelper.MESSAGE_STATUS_CHANGED, STATUS_CHANGED);
							AppUtils.ctx.getContentResolver().update(ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, msgId), val, null,
									null);			
							messageStatusChangeCounter++;
						}
					}
					
					outboxMsgWithBadStatus.close();
					outboxMsgWithBadStatus = null;
		
					message.arg1 = newMessageCounter;
					message.arg2 = messageStatusChangeCounter;
				} catch (HttpException e) {
					e.printStackTrace();
					Message msg1 = Message.obtain();
					if(e.getErrorCode() == 401){
						msg1.arg1 = ERROR_BAD_LOGIN;
						msg1.obj = new String(getString(R.string.cannot_login, msgBoxIsdsId));
					} else {
						msg1.arg1 = ERROR;
						msg1.obj = new String(e.getErrorCode() + ": " + e.getMessage());
					}
					
					try {
						messenger.send(msg1);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				} catch (DSException e) {
					Message msg2 = Message.obtain();
					msg2.arg1 = ERROR;
					msg2.obj = new String(e.getErrorCode() + ": " + e.getMessage());
					
					try {
						messenger.send(msg2);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				} catch (StreamInterruptedException e) {
					Message message = Message.obtain();
					message.arg1 = ERROR_INTERRUPTED;
					try {
						messenger.send(message);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
				
			}
			msgBoxCursor.close();
			
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}


