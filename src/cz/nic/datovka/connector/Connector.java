package cz.nic.datovka.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import cz.abclinuxu.datoveschranky.common.entities.Attachment;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxEnvironment;
import cz.abclinuxu.datoveschranky.common.impl.FileAttachmentStorer;
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
	private static List<MessageEnvelope> recievedMessageList;

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
		return accessService.GetUserInfoFromLogin();
	}
	
	public static List<MessageEnvelope> getMessageList() {
		if (messagesService == null) {
			throw new IllegalStateException("Object not initialized");
		}

		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();

		from.setTimeInMillis(0);
		//from.roll(Calendar.DAY_OF_YEAR, -28);
		now.roll(Calendar.DAY_OF_YEAR, 1);

		recievedMessageList = messagesService.getListOfReceivedMessages(
				from.getTime(), now.getTime(), null, 0, 15);

		return recievedMessageList;
	}

	public static MessageEnvelope getMessageById(int id) {
		Iterator<MessageEnvelope> iterator = recievedMessageList.iterator();
		MessageEnvelope result = null;

		while (iterator.hasNext()) {
			result = iterator.next();
			if (Integer.parseInt(result.getMessageID()) == id) {
				return result;
			}
		}

		throw new IllegalStateException("Cannot find message id=" + id);
	}

	public static void getAttachments(int id) {
		MessageEnvelope message = getMessageById(id);
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
