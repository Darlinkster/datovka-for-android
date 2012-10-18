package cz.nic.datovka.tinyDB.responseparsers;

import android.util.Base64OutputStream;
import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.OutputStreamHolder;

import java.io.Closeable;
import java.io.OutputStream;
import org.xml.sax.Attributes;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class DownloadSignedReceivedMessage extends AbstractResponseParser {

    private OutputStream output;

    public DownloadSignedReceivedMessage(OutputStream os) {
        this.output = os;
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        if ("dmSignature".equals(elName)) {
            //Base64OutputStream bos = new Base64OutputStream(output, false, 0, null);
        	Base64OutputStream bos = new Base64OutputStream(output, 0);
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
