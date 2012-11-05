package cz.nic.datovka.tinyDB.responseparsers;

import java.io.Closeable;
import java.io.OutputStream;

import org.xml.sax.Attributes;




import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.nic.datovka.tinyDB.base64.Base64OutputStream;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.OutputStreamHolder;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class DownloadSignedSentMessage extends AbstractResponseParser {

    private OutputStream output;

    public DownloadSignedSentMessage(OutputStream os) {
        this.output = os;
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        if ("dmSignature".equals(elName)) {
        	Base64OutputStream bos = new Base64OutputStream(output, 0, false);
            OutputHolder input = new OutputStreamHolder(bos);
            return input;

        }
        return null;
    }

    @Override
    public void endElementImpl(String elName, OutputHolder handle) {
        if (handle instanceof Closeable) {
            Utils.close((Closeable) handle);
        }
    }
}
