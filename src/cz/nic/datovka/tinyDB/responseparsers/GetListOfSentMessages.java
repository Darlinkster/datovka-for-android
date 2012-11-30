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

package cz.nic.datovka.tinyDB.responseparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;

import cz.abclinuxu.datoveschranky.common.entities.DataBox;
import cz.abclinuxu.datoveschranky.common.entities.DataBoxType;
import cz.abclinuxu.datoveschranky.common.entities.DocumentIdent;
import cz.abclinuxu.datoveschranky.common.entities.LegalTitle;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.MessageType;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class GetListOfSentMessages extends AbstractResponseParser {

    static private final String[] wanting = {"dmID", "dbIDSender", "dmSender", "dmSenderAddress",
    	"dmRecipientAddress", "dmRecipient", "dbIDRecipient", "dmAnnotation",
        "dmDeliveryTime", "dmAcceptanceTime", "dmAttachmentSize", "dmMessageStatus",
        "dmSenderType", "dmToHands", "dmRecipientRefNumber", "dmSenderRefNumber", "dmRecipientIdent",
        "dmSenderIdent", "dmLegalTitleLaw", "dmLegalTitleYear", "dmLegalTitleSect", "dmLegalTitlePar",
        "dmLegalTitlePoint", "dmPersonalDelivery", "dmAllowSubstDelivery"
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
            String senderType = map.get("dmSenderType").toString();
            DataBoxType dbtype = DataBoxType.valueOf(Integer.parseInt(senderType));
            DataBox sender = new DataBox(senderID, dbtype, senderName, senderAddress);
            
            String recipientID = map.get("dbIDRecipient").toString();
            String recipientName = map.get("dmRecipient").toString();
            String recipientAdress = map.get("dmRecipientAddress").toString();
            DataBox recipient = new DataBox(recipientID, recipientName, recipientAdress);
            
            String legalTitleLaw = map.get("dmLegalTitleLaw").toString();
            String legalTitleYear = map.get("dmLegalTitleYear").toString();
            String legalTitleSect = map.get("dmLegalTitleSect").toString();
            String legalTitlePar = map.get("dmLegalTitlePar").toString();
            String legalTitlePoint = map.get("dmLegalTitlePoint").toString();
            LegalTitle legalTitle = new LegalTitle(legalTitleLaw, legalTitleYear, legalTitleSect, legalTitlePar, legalTitlePoint);
            
            String personalDelivery = map.get("dmPersonalDelivery").toString();
            String substDelivery = map.get("dmAllowSubstDelivery").toString();
            
            String dmAnnotation = map.get("dmAnnotation").toString();
            String messageID = map.get("dmID").toString();
            String toHands = map.get("dmToHands").toString();
            MessageEnvelope env = new MessageEnvelope(MessageType.RECEIVED, sender, recipient, messageID, dmAnnotation, 
            											legalTitle, toHands, AndroidUtils.stringToBoolean(personalDelivery),
            											AndroidUtils.stringToBoolean(substDelivery));
          
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
            
            String recipientRefNum = map.get("dmRecipientRefNumber").toString();
            String recipientIdent = map.get("dmRecipientIdent").toString();
            DocumentIdent recipientDocIdent = new DocumentIdent(recipientRefNum, recipientIdent);
            env.setRecipientIdent(recipientDocIdent);
            
            String senderRefNum = map.get("dmSenderRefNumber").toString();
            String senderIdent = map.get("dmSenderIdent").toString();
            DocumentIdent senderDocIdent = new DocumentIdent(senderRefNum, senderIdent);
            env.setSenderIdent(senderDocIdent);
            
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
