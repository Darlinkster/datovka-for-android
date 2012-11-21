package cz.nic.datovka.services;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;

public class MessageBoxRefreshService extends Service {
	public static final int ERROR = -1;
	private DaemonThread thread;
	private static final int NOT_READ = 0;
	private static final int READ = 1;
	public static final String HANDLER = "handler";
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Message message;
	private Messenger messenger;
	
	public void onStart(Intent intent, int startId) {
		if(intent == null || intent.getExtras() == null){
			logger.log(Level.WARNING, "Service started with empty intent extras. Aborting.");
			return;
		}
		if(thread != null && thread.isAlive()){
			logger.log(Level.WARNING, "Service started but previous thread is still running. Aborting.");
			return;
		}
		
		Bundle extras = intent.getExtras();
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
			Cursor msgBoxCursor = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI,
					new String[] { DatabaseHelper.MSGBOX_ID }, null, null, null);
			msgBoxCursor.moveToFirst();
			int msgBoxIdColIndex = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
			int msgBoxCount = msgBoxCursor.getCount();
			
			// new message counter
			int newMessageCounter = 0;
			// Iterate over all Message Boxes IDs
			for(int i = 0; i < msgBoxCount; i++){
				// get msgbox id
				long msgBoxId = msgBoxCursor.getLong(msgBoxIdColIndex);
				msgBoxCursor.moveToNext();
				
				// get last inbox message
				Cursor inboxMsg = getContentResolver().query(ReceivedMessagesContentProvider.CONTENT_URI,
						new String[] { DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE, DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID },
						DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID + "=?", new String[] { Long.toString(msgBoxId) },
						DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE);
				int rcvdMsgBoxIdColIndex = inboxMsg.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE);
								
				// get last outbox message
				Cursor outboxMsg = getContentResolver().query(SentMessagesContentProvider.CONTENT_URI,
						new String[] { DatabaseHelper.SENT_MESSAGE_SENT_DATE, DatabaseHelper.SENT_MESSAGE_MSGBOX_ID },
						DatabaseHelper.SENT_MESSAGE_MSGBOX_ID + "=?", new String[] { Long.toString(msgBoxId) },
						DatabaseHelper.SENT_MESSAGE_SENT_DATE);
				int sentMsgBoxIdColIndex = outboxMsg.getColumnIndex(DatabaseHelper.SENT_MESSAGE_SENT_DATE);
				
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
											
				// Connect
				Connector connector = Connector.connectToWs(msgBoxId);
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
						
						//System.out.println(msgEnvelope.getAnnotation());
						
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_DM_TYPE, msgEnvelope.getDmType());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_TO_HANDS, msgEnvelope.getToHands());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
						
						GregorianCalendar recvAcceptanceDate = msgEnvelope.getAcceptanceTime();
						if(recvAcceptanceDate != null)
							rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(recvAcceptanceDate.getTime()));
						
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
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_STATE, msgEnvelope.getStateAsInt());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_TYPE, msgEnvelope.getType().name());
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID, msgBoxId);
						rcvdMessageValues.put(DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());
						
						getContentResolver().insert(ReceivedMessagesContentProvider.CONTENT_URI, rcvdMessageValues);
						newMessageCounter++;
					}
					
					while(newOutboxMsgIterator.hasNext()){
						ContentValues sentMessageValues = new ContentValues();
						MessageEnvelope msgEnvelope = newOutboxMsgIterator.next();
						
						//System.out.println(msgEnvelope.getAnnotation());
						
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ANNOTATION, msgEnvelope.getAnnotation());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_DM_TYPE, msgEnvelope.getDmType());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ISDS_ID, msgEnvelope.getMessageID());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_TO_HANDS, msgEnvelope.getToHands());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ALLOW_SUBST_DELIVERY, msgEnvelope.getAllowSubstDelivery());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_PERSONAL_DELIVERY, msgEnvelope.getPersonalDelivery());
						
						GregorianCalendar acceptanceDate = msgEnvelope.getAcceptanceTime();
						if(acceptanceDate != null)
							sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ACCEPTANCE_DATE, AndroidUtils.toXmlDate(acceptanceDate.getTime()));
						
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_SENT_DATE, AndroidUtils.toXmlDate(msgEnvelope.getDeliveryTime().getTime()));
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_IS_READ, READ);
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
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_STATE, msgEnvelope.getStateAsInt());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_TYPE, msgEnvelope.getType().name());
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_MSGBOX_ID, msgBoxId);
						sentMessageValues.put(DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE, msgEnvelope.getAttachmentSize());
						
						getContentResolver().insert(SentMessagesContentProvider.CONTENT_URI, sentMessageValues);
						//newMessageCounter++;
					}
					
					message.arg1 = newMessageCounter;
				} catch (HttpException e) {
					e.printStackTrace();
				} catch (DSException e) {
					message.arg1 = ERROR;
					message.obj = new String(e.getErrorCode() + ": " + e.getMessage());
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
