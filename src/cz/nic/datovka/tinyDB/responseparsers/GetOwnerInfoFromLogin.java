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
			
			// TODO OwnerInfo nema email a telNumber, patch?
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
