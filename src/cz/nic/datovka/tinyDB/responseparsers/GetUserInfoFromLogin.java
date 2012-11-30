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

import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class GetUserInfoFromLogin extends AbstractResponseParser {

	static private final String[] wanting = { "pnFirstName", "pnMiddleName",
			"pnLastName", "pnLastNameAtBirth", "adCity", "adStreet",
			"adNumberInStreet", "adNumberInMunicipality", "adZipCode",
			"adState", "biDate", "userID", "userType", "userPrivils", "ic",
			"firmName", "caStreet", "caCity", "caZipCode", "caState" };
    private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
    private UserInfo userInfo;

    public GetUserInfoFromLogin() {
        fillMap();
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        OutputHolder handle = null;
        if (super.match("GetUserInfoFromLoginResponse", "dbUserInfo", "*")) { // tohle nás zajímá

            handle = map.get(elName);
        }
        return handle;
    }

    @Override
	public void endElementImpl(String elName, OutputHolder handle) {

		if (super.match("dbUserInfo")) { // máme jeden seznam
			String userId = map.get("userID").toString();
			String userType = map.get("userType").toString();
			String userPrivils = map.get("userPrivils").toString();

			userInfo = new UserInfo(userId, userType, userPrivils);
			
			userInfo.setPersonNameFirstName(map.get("pnFirstName").toString());
			userInfo.setPersonNameMiddleName(map.get("pnMiddleName").toString());
			userInfo.setPersonNameLastName(map.get("pnLastName").toString());
			userInfo.setPersonNameLastNameAtBirth(map.get("pnLastNameAtBirth").toString());
			
			userInfo.setAddressStreet(map.get("adStreet").toString());
			userInfo.setAddressCity(map.get("adCity").toString());
			userInfo.setAddressNumberInStreet(map.get("adNumberInStreet").toString());
			userInfo.setAddressNumberInMunicipality(map.get("adNumberInMunicipality").toString());
			userInfo.setAddressZipCode(map.get("adZipCode").toString());
			userInfo.setAddressState(map.get("adState").toString());
			userInfo.setBirthDate(map.get("biDate").toString());
			
			userInfo.setIC(map.get("ic").toString());
			userInfo.setFirmName(map.get("firmName").toString());
			userInfo.setContactAdressStreet(map.get("caStreet").toString());
			userInfo.setContactAdressZipCode(map.get("caZipCode").toString());
			userInfo.setContactAdressCity(map.get("caCity").toString());
		}
	}

    public UserInfo getUserInfo() {
        return userInfo;
    }

    private void fillMap() {
        map.clear();
        for (String key : wanting) {
            map.put(key, new StringHolder());
        }
    }
}
