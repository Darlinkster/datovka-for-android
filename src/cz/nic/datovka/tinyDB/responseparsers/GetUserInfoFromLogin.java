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
			userInfo.setContactAdressCity(map.get("caState").toString());
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
