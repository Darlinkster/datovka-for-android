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

import java.util.HashMap;

import org.xml.sax.Attributes;

import cz.abclinuxu.datoveschranky.common.entities.DataBoxState;
import cz.abclinuxu.datoveschranky.common.entities.DataBoxType;
import cz.abclinuxu.datoveschranky.common.entities.OwnerInfo;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class GetOwnerInfoFromLogin extends AbstractResponseParser {

	static private final String[] wanting = { "dbID", "dbType", "pnFirstName",
			"pnMiddleName", "pnLastName", "pnLastNameAtBirth", "adCity",
			"adStreet", "adNumberInStreet", "adNumberInMunicipality",
			"adZipCode", "adState", "biDate", "ic", "firmName", "biCounty",
			"biCity", "biState", "nationality", "email", "telNumber", "identifier", "registryCode" };
    private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
    private OwnerInfo ownerInfo;

    public GetOwnerInfoFromLogin() {
        fillMap();
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        OutputHolder handle = null;
        if (super.match("GetOwnerInfoFromLoginResponse", "dbOwnerInfo", "*")) { // tohle nás zajímá

            handle = map.get(elName);
        }
        return handle;
    }

    @Override
	public void endElementImpl(String elName, OutputHolder handle) {

		if (super.match("dbOwnerInfo")) { // máme jeden seznam
			String dataBoxID = map.get("dbID").toString();
			DataBoxType dataBoxType = DataBoxType.valueOfByName(map.get("dbType").toString().toUpperCase());
			DataBoxState dbState = null;
			boolean dbEffectiveOVM = false;
			boolean dbOpenAddressing = false;
			
			ownerInfo = new OwnerInfo(dataBoxID, dataBoxType, dbState, dbEffectiveOVM, dbOpenAddressing);
			
			ownerInfo.setPersonNameFirstName(map.get("pnFirstName").toString());
			ownerInfo.setPersonNameMiddleName(map.get("pnMiddleName").toString());
			ownerInfo.setPersonNameLastName(map.get("pnLastName").toString());
			ownerInfo.setPersonNameLastNameAtBirth(map.get("pnLastNameAtBirth").toString());
			
			ownerInfo.setAddressStreet(map.get("adStreet").toString());
			ownerInfo.setAddressCity(map.get("adCity").toString());
			ownerInfo.setAddressNumberInStreet(map.get("adNumberInStreet").toString());
			ownerInfo.setAddressNumberInMunicipality(map.get("adNumberInMunicipality").toString());
			ownerInfo.setAddressZipCode(map.get("adZipCode").toString());
			ownerInfo.setAddressState(map.get("adState").toString());
			
			ownerInfo.setBirthDate(map.get("biDate").toString());
			ownerInfo.setBirthCity(map.get("biCity").toString());
			ownerInfo.setBirthCounty(map.get("biCounty").toString());
			ownerInfo.setBirthState(map.get("biState").toString());
			
			ownerInfo.setIC(map.get("ic").toString());
			ownerInfo.setFirmName(map.get("firmName").toString());
			
			ownerInfo.setNationality(map.get("nationality").toString());
			ownerInfo.setIdentifier(map.get("identifier").toString());
			ownerInfo.setRegistryCode(map.get("registryCode").toString());
			ownerInfo.setTelNumber(map.get("telNumber").toString());
			ownerInfo.setEmail(map.get("email").toString());
			
			// TODO dbState? dbEffectiveOVM? dbOpenAddressing?
		}
	}

    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }

    private void fillMap() {
        map.clear();
        for (String key : wanting) {
            map.put(key, new StringHolder());
        }
    }
}
