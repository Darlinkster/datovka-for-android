/*
Copyright (c) 2010, Vaclav Rosecky <xrosecky at gmail dot com>
All rights reserved.
Modification: 09/2012 CZ NIC z.s.p.o. <podpora at nic dot cz>

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL COPYRIGHT HOLDER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.nic.datovka.tinyDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

import android.content.res.Resources.NotFoundException;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.MessageState;
import cz.abclinuxu.datoveschranky.common.entities.MessageType;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.abclinuxu.datoveschranky.common.entities.PasswordExpirationInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.abclinuxu.datoveschranky.common.impl.Config;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxEnvironment;
import cz.abclinuxu.datoveschranky.common.impl.DataBoxException;
import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.abclinuxu.datoveschranky.common.interfaces.AttachmentStorer;
import cz.nic.datovka.tinyDB.exceptions.DSException;
import cz.nic.datovka.tinyDB.exceptions.HttpException;
import cz.nic.datovka.tinyDB.exceptions.SSLCertificateException;
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
	
	private static final int PRODUCTION = 0;
	//private static final int TESTING = 1;

	private DataBoxManager(int environment) {
		if(environment == PRODUCTION){
			this.config = new Config(DataBoxEnvironment.PRODUCTION);
		} else {
			this.config = new Config(DataBoxEnvironment.TEST);
		}
		
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
	 * @throws SSLCertificateException 
	 * @throws DataBoxException
	 *             při přihlašování do DS došlo k chybě. Důvodem může být špatné
	 *             heslo či uživatelské jméno, zacyklení při přesměrování či
	 *             absence autorizační cookie.
	 * 
	 */
	public static DataBoxManager login(int environment, String userName, String password) throws SSLCertificateException {
		DataBoxManager manager = new DataBoxManager(environment);
		manager.loginImpl(userName, password);
		return manager;
	}

	// metody z DataBoxMessages
	public List<MessageEnvelope> getListOfReceivedMessages(Date from, Date to, EnumSet<MessageState> state, int offset, int limit) throws HttpException,
			DSException, StreamInterruptedException {

		String resource = "/res/raw/get_list_of_received_messages.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);

		post = post.replace("${DATE_FROM}", AndroidUtils.toXmlDate(from));
		post = post.replace("${DATE_TO}", AndroidUtils.toXmlDate(to));
		post = post.replace("${OFFSET}", String.valueOf(offset));
		post = post.replace("${LIMIT}", String.valueOf(limit));
		GetListOfReceivedMessages result = new GetListOfReceivedMessages();
		this.postAndParseResponse(post, "dx", result);
		
		return result.getMessages();
	}

	public List<MessageEnvelope> getListOfSentMessages(Date from, Date to, EnumSet<MessageState> state, int offset, int limit) throws HttpException,
			DSException, StreamInterruptedException {

		String resource = "/res/raw/get_list_of_sent_messages.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);

		post = post.replace("${DATE_FROM}", AndroidUtils.toXmlDate(from));
		post = post.replace("${DATE_TO}", AndroidUtils.toXmlDate(to));
		post = post.replace("${OFFSET}", String.valueOf(offset));
		post = post.replace("${LIMIT}", String.valueOf(limit));
		GetListOfSentMessages result = new GetListOfSentMessages();
		this.postAndParseResponse(post, "dx", result);

		return result.getMessages();
	}

	public Hash verifyMessage(MessageEnvelope envelope) throws HttpException, DSException, StreamInterruptedException {
		String resource = "/res/raw/verify_message.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", envelope.getMessageID());
		VerifyMessage parser = new VerifyMessage();
		this.postAndParseResponse(post, "dx", parser);

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

	public OwnerInfo GetOwnerInfoFromLogin() throws HttpException, DSException, StreamInterruptedException {
		String resource = "/res/raw/get_owner_info_from_login.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		GetOwnerInfoFromLogin parser = new GetOwnerInfoFromLogin();
		this.postAndParseResponse(post, "DsManage", parser);

		return parser.getOwnerInfo();
	}

	public UserInfo GetUserInfoFromLogin() throws HttpException, DSException, StreamInterruptedException {
		String resource = "/res/raw/get_user_info_from_login.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		GetUserInfoFromLogin parser = new GetUserInfoFromLogin();
		this.postAndParseResponse(post, "DsManage", parser);

		return parser.getUserInfo();
	}

	public GregorianCalendar GetPasswordInfo() throws HttpException, DSException, StreamInterruptedException {
		String resource = "/res/raw/get_password_info.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		GetPasswordInfo parser = new GetPasswordInfo();
		this.postAndParseResponse(post, "DsManage", parser);
		

		GregorianCalendar cal = new GregorianCalendar();
		PasswordExpirationInfo pei = parser.getPasswordInfo();
		if(pei == null)
			return null;
		
		Date expirationDate = pei.getPasswordExpiration();

		if (expirationDate != null) {
			cal.setTime(expirationDate);
		} else {
			cal = null;
		}
		return cal;
	}

	public MessageEnvelope GetDeliveryInfo(String messageIsdsId) throws HttpException, DSException, StreamInterruptedException {
		String resource = "/res/raw/get_delivery_info.xml";
		String post = Utils.readResourceAsString(this.getClass(), resource);
		post = post.replace("${ID}", messageIsdsId);
		GetDeliveryInfo parser = new GetDeliveryInfo();
		this.postAndParseResponse(post, "dx", parser);

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

	private void loginImpl(String username, String password) throws SSLCertificateException  {
		String userPassword = username + ":" + password;

		authorization = "Basic " + new String(Base64.encode(userPassword.getBytes()));

		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			
			sslcontext.init(null, new TrustManager[] { new MyAndroidTrustManager() }, null);
			this.socketFactory = sslcontext.getSocketFactory();
			
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new SSLCertificateException(e.getMessage());
		} catch (KeyManagementException e) {
			e.printStackTrace();
			throw new SSLCertificateException(e.getMessage());
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new SSLCertificateException(e.getMessage());
		}

		
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
			//System.out.println(sax.getCause().getMessage());
			throw new DSException("Cannot parse xml.");
		} catch (ParserConfigurationException pce) {
			throw new DSException("Bad configuration of the SAX parser.");
		} catch (IOException ioe) {
			//logger.log(Level.INFO, post);
			String message = "IOException, error while reading answer. The input stream may be closed by user decision.";
			throw new StreamInterruptedException(message);
		} finally {
			//con.disconnect();
			this.close();
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
		if (con != null) {
			int responseCode = con.getResponseCode();
			String responseMessage = con.getResponseMessage();
			if (!OKCodes.contains(responseCode)) {
				String message = "Pozadavek selhal se stavovym kodem" + " " + responseCode + ", " + responseMessage + ".";
				logger.log(Level.SEVERE, message);
				throw new HttpException(responseMessage, responseCode);
			}
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
		if (con != null) {
			con.disconnect();
		}
		
		try {
			if (is != null)
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
