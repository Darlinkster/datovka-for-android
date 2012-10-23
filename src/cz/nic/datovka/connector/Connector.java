package cz.nic.datovka.connector;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.PasswordExpirationInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxEnvironment;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxAccessService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxDownloadService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxMessagesService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxServices;
import cz.nic.datovka.tinyDB.DataBoxManager;

public class Connector {
	public static int PRODUCTION = 1;
	public static int TESTING = 2;

	private static DataBoxMessagesService messagesService;
	private static DataBoxDownloadService downloadService;
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

	public static UserInfo getUserInfo(){
		if (accessService == null) {
			throw new IllegalStateException("Object not initialized");
		}
		
		return accessService.GetUserInfoFromLogin();
	}
	
	public static OwnerInfo getOwnerInfo(){
		if (accessService == null) {
			throw new IllegalStateException("Object not initialized");
		}
		
		return accessService.GetOwnerInfoFromLogin();
	}
	
	public static GregorianCalendar getPasswordInfo(){
		if (accessService == null) {
			throw new IllegalStateException("Object not initialized");
		}
		
		return accessService.GetPasswordInfo();
	}
	
	public static List<MessageEnvelope> getRecievedMessageList() {
		List<MessageEnvelope> recievedMessageList;
		
		if (messagesService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		recievedMessageList = messagesService.getListOfReceivedMessages(
				from.getTime(), now.getTime(), null, 0, 15);

		return recievedMessageList;
	}
	
	public static List<MessageEnvelope> getSentMessageList() {
		List<MessageEnvelope> sentMessageList;
		
		if (messagesService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		sentMessageList = messagesService.getListOfSentMessages(
				from.getTime(), now.getTime(), null, 0, 15);

		return sentMessageList;
	}

	
	public static void getAttachments(int id) {
	//	MessageEnvelope message = getMessageById(id);
		//List<Attachment> attachments = downloadService.
		
		/*
		FileAttachmentStorer storer = new FileAttachmentStorer(whereToPutFiles);
		for (MessageEnvelope envelope : messages) {
			// uložíme celou podepsanou zprávu
			FileOutputStream fos = new FileOutputStream(new File(whereToPutFiles, envelope.getMessageID() + ".bin"));
			try {
				downloadService.downloadSignedMessage(envelope, fos);
			} finally {
				fos.close();
			}
			// stáhneme přílohy ke zprávě
			List<Attachment> attachments = downloadService.downloadMessage(envelope, storer).getAttachments();
			Hash hash = messagesService.verifyMessage(envelope);
			print(envelope, attachments, hash);
		}
		*/
	}
}
