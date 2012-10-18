package cz.nic.datovka.tinyDB.responseparsers;

import java.util.Date;
import java.util.HashMap;

import org.xml.sax.Attributes;

import cz.abclinuxu.datoveschranky.common.entities.PasswordExpirationInfo;
import cz.abclinuxu.datoveschranky.common.entities.UserInfo;
import cz.nic.datovka.tinyDB.AndroidUtils;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class GetPasswordInfo extends AbstractResponseParser {

	static private final String[] wanting = { "pswExpDate" };
    private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
    private PasswordExpirationInfo passwordInfo;

    public GetPasswordInfo() {
        fillMap();
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        OutputHolder handle = null;
        if (super.match("Body", "GetPasswordInfoResponse", "*")) { // tohle nás zajímá

            handle = map.get(elName);
        }
        return handle;
    }

    @Override
	public void endElementImpl(String elName, OutputHolder handle) {

		if (super.match("GetPasswordInfoResponse")) { // máme jeden seznam
			
			Date date = AndroidUtils.toGregorianCalendar(map.get("pswExpDate").toString()).getTime();
			passwordInfo = new PasswordExpirationInfo(date);
		}
	}

    public PasswordExpirationInfo getPasswordInfo() {
        return passwordInfo;
    }

    private void fillMap() {
        map.clear();
        for (String key : wanting) {
            map.put(key, new StringHolder());
        }
    }
}
