package cz.nic.datovka.tinyDB.responseparsers;

import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;

import cz.abclinuxu.datoveschranky.common.entities.DataBox;
import cz.abclinuxu.datoveschranky.common.entities.MessageType;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
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
public class GetUserInfoFromLogin extends AbstractResponseParser {

	static private final String[] wanting = { "pnFirstName", "pnMiddleName",
			"pnLastName", "pnLastNameAtBirth", "adCity", "adStreet",
			"adNumberInStreet", "adNumberInMunicipality", "adZipCode",
			"adState", "biDate", "UserID", "userType", "userPrivils", "ic",
			"firmName", "caStreet", "caCity", "caZipCode", "caState" };
    private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
    private UserInfo userInfo;

    public GetUserInfoFromLogin() {
        fillMap();
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        OutputHolder handle = null;
        if (super.match("tDbUserInfo", "*")) { // tohle nás zajímá

            handle = map.get(elName);
        }
        return handle;
    }

    @Override
	public void endElementImpl(String elName, OutputHolder handle) {

		if (super.match("tDbUserInfo")) { // máme jeden seznam
			String userId = map.get("UserID").toString();
			String userType = map.get("userType").toString();
			String userPrivils = map.get("userPrivils").toString();

			userInfo = new UserInfo(userId, userType, userPrivils);
		}
		this.fillMap(); // a jedeme dál

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
