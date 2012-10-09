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
	private String login;
	private String password;
	private DataBoxMessagesService messagesService;
	private DataBoxDownloadService downloadService;
	private DataBoxServices service;
	private Context context;

	public Connector(String login, String password, Context context) {
		this.login = login;
		this.password = password;
		this.context = context;
	}

	public void connectToProduction() throws Exception {
		Config config = new Config(DataBoxEnvironment.PRODUCTION);
		service = DataBoxManager.login(config, login, password, context);
		messagesService = service.getDataBoxMessagesService();
	}

	public void connectToTesting() throws Exception {
		Config config = new Config(DataBoxEnvironment.TEST);
		service = DataBoxManager.login(config, login, password, context);
		messagesService = service.getDataBoxMessagesService();
	}

	public List<MessageEnvelope> getMessageList() {
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar from = new GregorianCalendar();
		
		from.roll(Calendar.DAY_OF_YEAR, -28);
		now.roll(Calendar.DAY_OF_YEAR, 1);
		
		List<MessageEnvelope> recievedMessageList = messagesService
				.getListOfReceivedMessages(from.getTime(), now.getTime(), null, 0, 15);
		
		return recievedMessageList;
	}
}
