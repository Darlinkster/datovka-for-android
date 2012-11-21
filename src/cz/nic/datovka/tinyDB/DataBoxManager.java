package cz.nic.datovka.tinyDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.kobjects.base64.Base64;
import org.xml.sax.SAXException;

import android.content.Context;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.MessageState;
import cz.abclinuxu.datoveschranky.common.entities.MessageType;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxException;
import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.abclinuxu.datoveschranky.common.interfaces.AttachmentStorer;
import cz.nic.datovka.R.raw;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.StreamInterruptedException;
import cz.nic.datovka.tinyDB.responseparsers.AbstractResponseParser;
import cz.nic.datovka.tinyDB.responseparsers.DownloadReceivedMessage;
import cz.nic.datovka.tinyDB.responseparsers.DownloadSignedReceivedMessage;
import cz.nic.datovka.tinyDB.responseparsers.DownloadSignedSentMessage;
import cz.nic.datovka.tinyDB.responseparsers.GetDeliveryInfo;
import cz.nic.datovka.tinyDB.responseparsers.GetListOfReceivedMessages;
import cz.nic.datovka.tinyDB.responseparsers.GetListOfSentMessages;
import cz.nic.datovka.tinyDB.responseparsers.GetOwnerInfoFromLogin;
import cz.nic.datovka.tinyDB.responseparsers.GetPasswordInfo;
import cz.nic.datovka.tinyDB.responseparsers.GetUserInfoFromLogin;
import cz.nic.datovka.tinyDB.responseparsers.VerifyMessage;

/**
 * Tato třída umožnuje přihlášení k datové schránce a základní operace s ní,
 * tzn. stažení přijatých zpráv a stažení přijaté zprávy včetně příloh.
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 * 
 */
public class DataBoxManager {

	private static final List<Integer> OKCodes = Arrays.asList(200, 304);
	protected final Config config;
	protected String authCookie = null;
	protected SSLSocketFactory socketFactory = null;
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	protected String authorization;

	private HttpsURLConnection con;
	private InputStream is;

	private DataBoxManager(Config configuration) {
		this.config = configuration;
	}

	/**
	 * Realizuje přihlášení do datové schránky pod daným uživatelským jménem a
	 * heslem a při úspěšném přihlášení vrátí příslušnou instanci ISDSManageru
	 * poskytující služby k této schránce.
	 * 
	 * @param userName
	 *            jméno uživatele
	 * @param password
	 *            heslo uživatele
	 * @throws DataBoxException
	 *             při přihlašování do DS došlo k chybě. Důvodem může být špatné
	 *             heslo či uživatelské jméno, zacyklení při přesměrování či
	 *             absence autorizační cookie.
	 * 
	 */
	public static DataBoxManager login(Config config, String userName, String password, Context context) throws Exception {
		DataBoxManager manager = new DataBoxManager(config);
		manager.loginImpl(userName, password, context);
		return manager;
	}

	// metody z DataBoxMessages
	public List<MessageEnvelope> getListOfReceivedMessages(Date from, Date to, EnumSet<MessageState> state, int offset, int limit) throws HttpException,
			DSException {

		String resource = "/res/raw/get_list_of_received_messages.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);

		post = post.replace("${DATE_FROM}", AndroidUtils.toXmlDate(from));
		post = post.replace("${DATE_TO}", AndroidUtils.toXmlDate(to));
		post = post.replace("${OFFSET}", String.valueOf(offset));
		post = post.replace("${LIMIT}", String.valueOf(limit));
		GetListOfReceivedMessages result = new GetListOfReceivedMessages();
		try {
			this.postAndParseResponse(post, "dx", result);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}
		return result.getMessages();
	}

	public List<MessageEnvelope> getListOfSentMessages(Date from, Date to, EnumSet<MessageState> state, int offset, int limit) throws HttpException,
			DSException {

		String resource = "/res/raw/get_list_of_sent_messages.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);

		post = post.replace("${DATE_FROM}", AndroidUtils.toXmlDate(from));
		post = post.replace("${DATE_TO}", AndroidUtils.toXmlDate(to));
		post = post.replace("${OFFSET}", String.valueOf(offset));
		post = post.replace("${LIMIT}", String.valueOf(limit));
		GetListOfSentMessages result = new GetListOfSentMessages();
		try {
			this.postAndParseResponse(post, "dx", result);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}
		return result.getMessages();
	}

	public Hash verifyMessage(MessageEnvelope envelope) throws HttpException, DSException {
		String resource = "/res/raw/verify_message.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", envelope.getMessageID());
		VerifyMessage parser = new VerifyMessage();
		try {
			this.postAndParseResponse(post, "dx", parser);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}
		return parser.getResult();
	}

	// metody z DataBoxDownload

	public void parseSignedMessage(AttachmentStorer storer, int messageIsdsId, InputStream input) {
		MessageEnvelope envelope = new MessageEnvelope();
		envelope.setMessageID(Integer.toString(messageIsdsId));
		DownloadReceivedMessage rp = new DownloadReceivedMessage(envelope, storer);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(input, new SimpleSAXParser(rp));

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void parseSignedSentMessage(int messageIsdsId, OutputStream os) {

	}

	public void downloadSignedReceivedMessage(int messageIsdsId, OutputStream os) throws HttpException, StreamInterruptedException, DSException {
		String resource = "/res/raw/download_signed_received_message.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", Integer.toString(messageIsdsId));
		DownloadSignedReceivedMessage parser = new DownloadSignedReceivedMessage(os);
		this.postAndParseResponse(post, "dz", parser);
	}

	public void downloadSignedSentMessage(int messageIsdsId, OutputStream os) throws HttpException, StreamInterruptedException, DSException {
		String resource = "/res/raw/download_signed_sent_message.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", Integer.toString(messageIsdsId));
		DownloadSignedSentMessage parser = new DownloadSignedSentMessage(os);
		this.postAndParseResponse(post, "dz", parser);
	}

	// metody z DataAccessService

	public OwnerInfo GetOwnerInfoFromLogin() throws HttpException, DSException {
		String resource = "/res/raw/get_owner_info_from_login.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		GetOwnerInfoFromLogin parser = new GetOwnerInfoFromLogin();
		try {
			this.postAndParseResponse(post, "DsManage", parser);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}

		return parser.getOwnerInfo();
	}

	public UserInfo GetUserInfoFromLogin() throws HttpException, DSException {
		String resource = "/res/raw/get_user_info_from_login.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		GetUserInfoFromLogin parser = new GetUserInfoFromLogin();
		try {
			this.postAndParseResponse(post, "DsManage", parser);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}

		return parser.getUserInfo();
	}

	public GregorianCalendar GetPasswordInfo() throws HttpException, DSException {
		String resource = "/res/raw/get_password_info.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		GetPasswordInfo parser = new GetPasswordInfo();
		try {
			this.postAndParseResponse(post, "DsManage", parser);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}

		GregorianCalendar cal = new GregorianCalendar();
		Date expirationDate = parser.getPasswordInfo().getPasswordExpiration();

		if (expirationDate != null) {
			cal.setTime(expirationDate);
		} else {
			cal = null;
		}
		return cal;
	}

	public MessageEnvelope GetDeliveryInfo(String messageIsdsId) throws HttpException, DSException {
		String resource = "/res/raw/get_delivery_info.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", messageIsdsId);
		GetDeliveryInfo parser = new GetDeliveryInfo();
		try {
			this.postAndParseResponse(post, "dx", parser);
		} catch (StreamInterruptedException e) {
			e.printStackTrace();
		}

		return parser.getDeliveryInfo();
	}

	/**
	 * Stáhne přijatou zprávu včetně SOAP obálky a příloh jako XML soubor.
	 * Vhodné pouze pro debugovací účely, ne pro záholování.
	 * 
	 * @param envelope
	 *            obálka zprávy, která se má stáhnout
	 * @param os
	 *            kam přijde uložit
	 * @throws DataBoxException
	 * 
	 */
	public void storeMessageAsXML(MessageEnvelope envelope, OutputStream os) throws HttpException {
		if (envelope.getType() != MessageType.RECEIVED) {
			throw new UnsupportedOperationException("Stahnout lze pouze prijatou zpravu");
		}
		String resource = "/res/raw/download_received_message.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", envelope.getMessageID());
		this.storeRequest(post, "dz", os);
	}

	private void loginImpl(String username, String password, Context context) throws Exception {
		String userPassword = username + ":" + password;

		authorization = "Basic " + new String(Base64.encode(userPassword.getBytes()));

		KeyStore keyStore = KeyStore.getInstance("BKS");
		SSLContext sslcontext = SSLContext.getInstance("TLS");

		InputStream keyStoreStream = context.getResources().openRawResource(raw.key_store);
		keyStore.load(keyStoreStream, "kiasdhkjsdh@$@R%.S1257".toCharArray());

		sslcontext.init(null, new TrustManager[] { new MyAndroidTrustManager(keyStore) }, null);
		this.socketFactory = sslcontext.getSocketFactory();
	}

	private void postAndParseResponse(String post, String prefix, AbstractResponseParser rp) throws HttpException, StreamInterruptedException, DSException {
		try {
			// udelame post
			URL url = new URL(config.getServiceURL() + prefix);
			con = (HttpsURLConnection) url.openConnection();
			this.configure(con);
			con.getOutputStream().write(post.getBytes("UTF-8"));
			checkHttpResponseCode(con);

			// zparsujeme výsledek SAX parserem
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			SAXParser parser = factory.newSAXParser();
			is = con.getInputStream();
	
			/*String a;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while((a = br.readLine()) != null){
				
				System.out.println(a);
			}
			*/
			parser.parse(is, new SimpleSAXParser(rp));

			// ověříme vrácený stav pri volani webove služby
			if (!rp.getStatus().ok()) {
				// String message =
				// String.format("Pozadavek selhal chybou %s:%s",
				// rp.getStatus().getStatusCode(),
				// rp.getStatus().getStatusMesssage());
				// logger.log(Level.SEVERE, message);
				throw new DSException(rp.getStatus().getStatusMesssage(), Integer.parseInt(rp.getStatus().getStatusCode()));
			} 
		} catch (SAXException sax) {
			throw new DataBoxException("Chyba pri parsovani odpovedi.", sax);
		} catch (ParserConfigurationException pce) {
			throw new DataBoxException("Chyba pri konfiguraci SAX parseru.", pce);
		} catch (IOException ioe) {
			String message = "IOException, error while reading answer. The input stream may be closed by user decision.";
			throw new StreamInterruptedException(message);
		} finally {
			con.disconnect();
		}
	}

	private void storeRequest(String request, String prefix, OutputStream os) throws HttpException {
		HttpsURLConnection con = null;
		try {
			URL url = new URL(config.getServiceURL() + prefix);
			con = (HttpsURLConnection) url.openConnection();
			this.configure(con);
			con.getOutputStream().write(request.getBytes("UTF-8"));
			this.checkHttpResponseCode(con);
			InputStream is = con.getInputStream();
			Utils.copy(is, os);
		} catch (IOException ioe) {
			throw new DataBoxException("Nemohu ulozit zpravu", ioe);
		} finally {
			con.disconnect();
		}
	}

	private void checkHttpResponseCode(HttpsURLConnection con) throws IOException, HttpException {
		if (!OKCodes.contains(con.getResponseCode())) {
			String message = "Pozadavek selhal se stavovym kodem" + " " + con.getResponseCode() + ", " + con.getResponseMessage() + ".";
			logger.log(Level.SEVERE, message);
			throw new HttpException(con.getResponseMessage(), con.getResponseCode());
		}
	}

	private void configure(HttpsURLConnection connect) throws ProtocolException {

		connect.setSSLSocketFactory(socketFactory);
		connect.setRequestProperty("Authorization", authorization);
		// connect.setRequestProperty("Cookie", authCookie);
		connect.setRequestMethod("POST");
		connect.setDoOutput(true);
		connect.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		connect.setRequestProperty("Soapaction", "");
	}

	public void close() {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (con != null) {
			con.disconnect();
		}
	}
}
