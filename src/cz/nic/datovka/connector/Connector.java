package cz.nic.datovka.connector;

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
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxAccessService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxDownloadService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxMessagesService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxServices;
import cz.nic.datovka.tinyDB.DataBoxManager;

public class Connector {
	public static final int PRODUCTION = 0;
	public static final int TESTING = 1;

	private static final int MAX_MSG_COUNT = 1000;
	private static DataBoxMessagesService messagesService;
	private static DataBoxAccessService accessService;
	private static DataBoxServices service;

	public static void connect(String login, String password, int environment,
			Context context) throws Exception {
		Config config;
		if (environment == PRODUCTION)
			config = new Config(DataBoxEnvironment.PRODUCTION);
		else
			config = new Config(DataBoxEnvironment.TEST);

		service = DataBoxManager.login(config, login, password, context);
		messagesService = service.getDataBoxMessagesService();
		accessService = service.getDataBoxAccessService();
	}

	public static boolean isOnline() {
		if (service == null)
			return false;
		return true;
	}

	public static DataBoxDownloadService getDownloadService() {
		return service.getDataBoxDownloadService();
	}

	public static UserInfo getUserInfo() {
		if (accessService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return accessService.GetUserInfoFromLogin();
	}

	public static OwnerInfo getOwnerInfo() {
		if (accessService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return accessService.GetOwnerInfoFromLogin();
	}

	public static GregorianCalendar getPasswordInfo() {
		if (accessService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		return accessService.GetPasswordInfo();
	}

	public static List<MessageEnvelope> getRecievedMessageList() {
		List<MessageEnvelope> recievedMessageList;
		int offset = 0;
		
		if (messagesService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		recievedMessageList = messagesService.getListOfReceivedMessages(
				from.getTime(), now.getTime(), null, offset, MAX_MSG_COUNT);

		if(recievedMessageList.size() == MAX_MSG_COUNT){
			while(true){
				List<MessageEnvelope> recievedMessageListNext;
				offset += MAX_MSG_COUNT;
				recievedMessageListNext = messagesService.getListOfReceivedMessages(
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

	public static List<MessageEnvelope> getSentMessageList() {
		List<MessageEnvelope> sentMessageList;
		int offset = 0;

		if (messagesService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		sentMessageList = messagesService.getListOfSentMessages(from.getTime(),
				now.getTime(), null, offset, MAX_MSG_COUNT);

		if(sentMessageList.size() == MAX_MSG_COUNT){
			while(true){
				List<MessageEnvelope> sentMessageListNext;
				offset += MAX_MSG_COUNT;
				sentMessageListNext = messagesService.getListOfSentMessages(
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

	public static Hash verifyMessage(MessageEnvelope envelope) {
		return messagesService.verifyMessage(envelope);
	}

}
