package cz.nic.datovka.tinyDB.responseparsers;

import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;

import cz.abclinuxu.datoveschranky.common.entities.DataBox;
import cz.abclinuxu.datoveschranky.common.entities.MessageType;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xml.sax.Attributes;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class GetListOfSentMessages extends AbstractResponseParser {

    static private final String[] wanting = {"dmID", "dbIDSender", "dmSender", "dmSenderAddress",
    	"dmRecipientAddress", "dmRecipient", "dbIDRecipient", "dmAnnotation",
        "dmDeliveryTime", "dmAcceptanceTime", "dmAttachmentSize", "dmMessageStatus"
    };
    private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
    private List<MessageEnvelope> messages = new ArrayList<MessageEnvelope>();

    public GetListOfSentMessages() {
        fillMap();
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        OutputHolder handle = null;
        if (super.match("dmRecords", "dmRecord", "*")) { // tohle nás zajímá

            handle = map.get(elName);
        }
        return handle;
    }

    @Override
    public void endElementImpl(String elName, OutputHolder handle) {
        if (super.match("dmRecords", "dmRecord")) { // máme jeden seznam
            String senderID = map.get("dbIDSender").toString();
            String senderName = map.get("dmSender").toString();
            String senderAddress = map.get("dmSenderAddress").toString();
            DataBox sender = new DataBox(senderID, senderName, senderAddress);
            String recipientID = map.get("dbIDRecipient").toString();
            String recipientName = map.get("dmRecipient").toString();
            String recipientAdress = map.get("dmRecipientAddress").toString();
            DataBox recipient = new DataBox(recipientID, recipientName, recipientAdress);
            String dmAnnotation = map.get("dmAnnotation").toString();
            String messageID = map.get("dmID").toString();
            MessageEnvelope env = new MessageEnvelope(MessageType.RECEIVED, sender, recipient, messageID, dmAnnotation);
            String accepted = map.get("dmAcceptanceTime").toString();
            if (accepted != null && !accepted.equals("")) {
                env.setAcceptanceTime(AndroidUtils.toGregorianCalendar(accepted));
            }
            String delivered = map.get("dmDeliveryTime").toString();
            if (delivered != null && !delivered.equals("")) {
                env.setDeliveryTime(AndroidUtils.toGregorianCalendar(delivered));
            }
            String attachmentSize = map.get("dmAttachmentSize").toString();
            if (attachmentSize != null && !attachmentSize.equals("")) {
            	env.setAttachmentSize(Integer.parseInt(attachmentSize));
            }
            String status = map.get("dmMessageStatus").toString();
            if (status != null && !status.equals("")) {
                env.setState(Integer.parseInt(status));
            }
            messages.add(env);
            this.fillMap(); // a jedeme dál

        }
    }

    public List<MessageEnvelope> getMessages() {
        return messages;
    }

    private void fillMap() {
        map.clear();
        for (String key : wanting) {
            map.put(key, new StringHolder());
        }
    }
}
