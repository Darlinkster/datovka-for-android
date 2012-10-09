package cz.nic.datovka.connector;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxEnvironment;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxDownloadService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxMessagesService;
import cz.abclinuxu.datoveschranky.common.interfaces.DataBoxServices;
import cz.nic.datovka.tinyDB.DataBoxManager;


public class Connector {
	public static int PRODUCTION = 1;
	public static int TESTING = 2;
	
	private static DataBoxMessagesService messagesService;
	private DataBoxDownloadService downloadService;
	private static DataBoxServices service;
	
	public static void connect(String login, String password, int environment, Context context) throws Exception {
		Config config;
		if(environment == PRODUCTION)
			config = new Config(DataBoxEnvironment.PRODUCTION);
		else
			config = new Config(DataBoxEnvironment.TEST);
		
		service = DataBoxManager.login(config, login, password, context);
		messagesService = service.getDataBoxMessagesService();
	}

	public static List<MessageEnvelope> getMessageList() {
		if (messagesService == null){
			throw new IllegalStateException("Object not initialized");
		}
		
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();
		
		from.roll(Calendar.DAY_OF_YEAR, -28);
		now.roll(Calendar.DAY_OF_YEAR, 1);
		
		List<MessageEnvelope> recievedMessageList = messagesService
				.getListOfReceivedMessages(from.getTime(), now.getTime(), null, 0, 15);
		
		return recievedMessageList;
	}
}
