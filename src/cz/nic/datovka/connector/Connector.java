package cz.nic.datovka.connector;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxEnvironment;
import cz.abclinuxu.datoveschranky.common.interfaces.AttachmentStorer;
import cz.nic.datovka.tinyDB.DataBoxManager;
import cz.nic.datovka.tinyDB.exceptions.HttpException;

public class Connector {
	public static final int PRODUCTION = 0;
	public static final int TESTING = 1;

	private static final int MAX_MSG_COUNT = 1000;
	//private static DataBoxMessagesService messagesService;
	//private static DataBoxAccessService accessService;
	private static DataBoxManager service;

	public static void connect(String login, String password, int environment,
			Context context) throws Exception {
		Config config;
		if (environment == PRODUCTION)
			config = new Config(DataBoxEnvironment.PRODUCTION);
		else
			config = new Config(DataBoxEnvironment.TEST);

		service = DataBoxManager.login(config, login, password, context);
		//messagesService = service.getDataBoxMessagesService();
		//accessService = service.getDataBoxAccessService();
	}

	public static boolean isOnline() {
		if (service == null)
			return false;
		return true;
	}
	
	public static void downloadSignedReceivedMessage(MessageEnvelope envelope, OutputStream fos) throws HttpException {
		service.downloadSignedReceivedMessage(envelope, fos);
	}
	
	public static void downloadSignedSentMessage(MessageEnvelope envelope, OutputStream fos) throws HttpException {
		service.downloadSignedSentMessage(envelope, fos);
	}
	
	public static void downloadMessage(MessageEnvelope envelope, AttachmentStorer storer) throws HttpException {
		service.downloadMessage(envelope, storer);
	}

	public static UserInfo getUserInfo()throws HttpException {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetUserInfoFromLogin()  ;
	}

	public static OwnerInfo getOwnerInfo()throws HttpException  {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetOwnerInfoFromLogin() ;
	}

	public static GregorianCalendar getPasswordInfo() throws HttpException {
		if (service == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return service.GetPasswordInfo() ;
	}

	public static List<MessageEnvelope> getRecievedMessageList() throws HttpException {
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

	public static List<MessageEnvelope> getSentMessageList() throws HttpException  {
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

	public static Hash verifyMessage(MessageEnvelope envelope) throws HttpException  {
		return service.verifyMessage(envelope);
	}

}
