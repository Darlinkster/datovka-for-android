package cz.nic.datovka.tinyDB.responseparsers;

import java.util.HashMap;

import org.xml.sax.Attributes;

import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

/**
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class GetDeliveryInfo extends AbstractResponseParser {

	static private final String[] wanting = { "dmMessageStatus", "dmAcceptanceTime", "dmDeliveryTime", "dmID" };
	private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
	private MessageEnvelope message;

	public GetDeliveryInfo() {
		fillMap();
	}

	@Override
	public OutputHolder startElementImpl(String elName, Attributes attributes) {
		OutputHolder handle = null;
		if (super.match("GetDeliveryInfoResponse", "dmDelivery", "*")) { // tohle

			handle = map.get(elName);
		}
		return handle;
	}

	@Override
	public void endElementImpl(String elName, OutputHolder handle) {
		if (super.match("dmDelivery")) { // máme jeden seznam
			String deliveryTime = map.get("dmDeliveryTime").toString();
			String acceptanceTime = map.get("dmAcceptanceTime").toString();
			String messageStatus = map.get("dmMessageStatus").toString();
			String messageId = map.get("dmID").toString();

			message = new MessageEnvelope();
			message.setMessageID(messageId);
			message.setState(Integer.parseInt(messageStatus));

			if (deliveryTime != null && !deliveryTime.equals("")) {
				message.setDeliveryTime(AndroidUtils.toGregorianCalendar(deliveryTime));
			}
			if (acceptanceTime != null && !acceptanceTime.equals("")) {
				message.setAcceptanceTime(AndroidUtils.toGregorianCalendar(acceptanceTime));
			}

		}
	}

	public MessageEnvelope getDeliveryInfo() {
		return message;
	}

	private void fillMap() {
		map.clear();
		for (String key : wanting) {
			map.put(key, new StringHolder());
		}
	}
}
