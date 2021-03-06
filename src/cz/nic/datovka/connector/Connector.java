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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.kobjects.base64.Base64;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.activities.AppUtils;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.exceptions.MessageBoxIdNotKnown;
import cz.nic.datovka.tinyDB.DataBoxManager;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.SSLCertificateException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class Connector {
	public static final int PRODUCTION = 0;
	public static final int TESTING = 1;

	private static final int MAX_MSG_COUNT = 1000;
	private DataBoxManager service;
	
	public void connect(String login, String password, int environment) throws SSLCertificateException{
		service = DataBoxManager.login(environment, login, new String(Base64.decode(password)));
	}

	public boolean isOnline() {
		if (service == null)
			return false;
		return true;
	}
	
	public void downloadSignedReceivedMessage(int messageIsdsId, OutputStream fos) throws HttpException, StreamInterruptedException, DSException {
		service.downloadSignedReceivedMessage(messageIsdsId, fos);
	}
	
	public void downloadSignedSentMessage(int messageIsdsId, OutputStream fos) throws HttpException, StreamInterruptedException, DSException {
		service.downloadSignedSentMessage(messageIsdsId, fos);
	}
	
	public UserInfo getUserInfo()throws HttpException, DSException, StreamInterruptedException {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetUserInfoFromLogin()  ;
	}

	public OwnerInfo getOwnerInfo()throws HttpException, DSException, StreamInterruptedException  {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetOwnerInfoFromLogin() ;
	}

	public GregorianCalendar getPasswordInfo() throws HttpException, DSException, StreamInterruptedException {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetPasswordInfo() ;
	}

	public List<MessageEnvelope> getRecievedMessageList() throws HttpException, DSException, StreamInterruptedException {
		List<MessageEnvelope> recievedMessageList;
		int offset = 0;
		
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		recievedMessageList = service.getListOfReceivedMessages(
				from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);

		if(recievedMessageList.size() == MAX_MSG_COUNT){
			while(true){
				List<MessageEnvelope> recievedMessageListNext;
				offset += MAX_MSG_COUNT;
				recievedMessageListNext = service.getListOfReceivedMessages(
						from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);
				
				if(recievedMessageListNext.size() > 0){
					recievedMessageList.addAll(recievedMessageListNext);
				}
				else{
					break;
				}
			}
		}
		
		return recievedMessageList;
	}

	public List<MessageEnvelope> getSentMessageList() throws HttpException, DSException, StreamInterruptedException  {
		List<MessageEnvelope> sentMessageList;
		int offset = 0;

		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		sentMessageList = service.getListOfSentMessages(from.getTime(),
				now.getTime(), null, offset, MAX_MSG_COUNT);

		if(sentMessageList.size() == MAX_MSG_COUNT){
			while(true){
				List<MessageEnvelope> sentMessageListNext;
				offset += MAX_MSG_COUNT;
				sentMessageListNext = service.getListOfSentMessages(
						from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);
				
				if(sentMessageListNext.size() > 0){
					sentMessageList.addAll(sentMessageListNext);
				}
				else{
					break;
				}
			}
		}
			
		return sentMessageList;
	}

	public List<MessageEnvelope> getRecievedMessageListFromDate(long fromParam) throws HttpException, DSException, StreamInterruptedException {
		List<MessageEnvelope> recievedMessageList;
		int offset = 0;
		
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(fromParam);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		recievedMessageList = service.getListOfReceivedMessages(
				from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);

		if(recievedMessageList.size() == MAX_MSG_COUNT){
			while(true){
				List<MessageEnvelope> recievedMessageListNext;
				offset += MAX_MSG_COUNT;
				recievedMessageListNext = service.getListOfReceivedMessages(
						from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);
				
				if(recievedMessageListNext.size() > 0){
					recievedMessageList.addAll(recievedMessageListNext);
				}
				else{
					break;
				}
			}
		}
		
		return recievedMessageList;
	}

	public List<MessageEnvelope> getSentMessageListFromDate(long fromParam) throws HttpException, DSException, StreamInterruptedException  {
		List<MessageEnvelope> sentMessageList;
		int offset = 0;

		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(fromParam);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		sentMessageList = service.getListOfSentMessages(from.getTime(),
				now.getTime(), null, offset, MAX_MSG_COUNT);

		if(sentMessageList.size() == MAX_MSG_COUNT){
			while(true){
				List<MessageEnvelope> sentMessageListNext;
				offset += MAX_MSG_COUNT;
				sentMessageListNext = service.getListOfSentMessages(
						from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);
				
				if(sentMessageListNext.size() > 0){
					sentMessageList.addAll(sentMessageListNext);
				}
				else{
					break;
				}
			}
		}
			
		return sentMessageList;
	}
	
	public Hash verifyMessage(MessageEnvelope envelope) throws HttpException, DSException, StreamInterruptedException  {
		return service.verifyMessage(envelope);
	}
	
	public void  parseSignedMessage(String outputDir, Long messageId, InputStream input, int messageIsdsId){
		FileAttachmentStorerWithDBInsertion faswd = new FileAttachmentStorerWithDBInsertion(outputDir, messageId);
		
		service.parseSignedMessage(faswd, messageIsdsId, input);
	}

	public void close(){
		service.close();
		service = null;
	}
	
	public static Connector connectToWs(long msgBoxId) throws SSLCertificateException, MessageBoxIdNotKnown {
		Connector conn = new Connector();
		Uri msgBoxUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);
		String[] msgBoxProjection = new String[] { DatabaseHelper.MSGBOX_LOGIN, DatabaseHelper.MSGBOX_PASSWORD,
				DatabaseHelper.MSGBOX_TEST_ENV };
		Cursor msgBoxCursor = AppUtils.ctx.getContentResolver().query(msgBoxUri, msgBoxProjection, null, null, null);
		
		if(msgBoxCursor.moveToFirst() == false) {
			throw new MessageBoxIdNotKnown("Message box ID " + Long.toString(msgBoxId) + " not known");
		}

		int loginIndex = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_LOGIN);
		int passwordIndex = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_PASSWORD);
		int envIndex = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_TEST_ENV);
		String login = msgBoxCursor.getString(loginIndex);
		String password = msgBoxCursor.getString(passwordIndex);
		int environment = msgBoxCursor.getInt(envIndex);
		msgBoxCursor.close();
		
		conn.connect(login, password, environment);
		
		return conn;
	}
	
	public MessageEnvelope GetDeliveryInfo(String messageIsdsId) throws HttpException, DSException, StreamInterruptedException {
		return service.GetDeliveryInfo(messageIsdsId);
	}
	
	public boolean checkConnection(Context ctx) {
		final ConnectivityManager conMgr =  (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		// TODO if conMgr is null, what to do?
		if(conMgr == null)
			return false;
			
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
		    return true;
		} else {
		    return false;
		} 
	}
}
