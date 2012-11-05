package cz.nic.datovka.connector;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import cz.abclinuxu.datoveschranky.common.entities.Attachment;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxEnvironment;
import cz.nic.datovka.tinyDB.DataBoxManager;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;

public class Connector {
	public static final int PRODUCTION = 0;
	public static final int TESTING = 1;

	private static final int MAX_MSG_COUNT = 1000;
	private DataBoxManager service;
	
	public void connect(String login, String password, int environment,
			Context context) throws Exception {
		Config config;
		if (environment == PRODUCTION)
			config = new Config(DataBoxEnvironment.PRODUCTION);
		else
			config = new Config(DataBoxEnvironment.TEST);

		service = DataBoxManager.login(config, login, password, context);
	}

	public boolean isOnline() {
		if (service == null)
			return false;
		return true;
	}
	
	public void downloadSignedReceivedMessage(int messageIsdsId, OutputStream fos) throws HttpException, StreamInterruptedException {
		service.downloadSignedReceivedMessage(messageIsdsId, fos);
	}
	
	public void downloadSignedSentMessage(int messageIsdsId, OutputStream fos) throws HttpException, StreamInterruptedException {
		service.downloadSignedSentMessage(messageIsdsId, fos);
	}
	
	public UserInfo getUserInfo()throws HttpException {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetUserInfoFromLogin()  ;
	}

	public OwnerInfo getOwnerInfo()throws HttpException  {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetOwnerInfoFromLogin() ;
	}

	public GregorianCalendar getPasswordInfo() throws HttpException {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetPasswordInfo() ;
	}

	public List<MessageEnvelope> getRecievedMessageList() throws HttpException {
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

	public List<MessageEnvelope> getSentMessageList() throws HttpException  {
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

	public Hash verifyMessage(MessageEnvelope envelope) throws HttpException  {
		return service.verifyMessage(envelope);
	}
	
	public List<Attachment>  parseSignedReceivedMessage(File outputDir, int folder, Long messageId, Context ctx, InputStream input, int messageIsdsId){
		FileAttachmentStorerWithDBInsertion faswd = new FileAttachmentStorerWithDBInsertion(outputDir, folder, messageId, ctx);
		
		return service.parseSignedReceivedMessage(faswd, messageIsdsId, input);
	}

	public void close(){
		service.close();
	}
}
