package cz.nic.datovka.tinyDB.holders;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.abclinuxu.datoveschranky.common.impl.Utils;

/**
 * Zapíše obsah elementu do OutputStreamu.
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class OutputStreamHolder implements OutputHolder<OutputStream>, Closeable {

	private boolean closed = false;
	private final OutputStream os;
	private final BufferedWriter bw;
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	public OutputStreamHolder(OutputStream os) {
		this.os = os;
		bw = new BufferedWriter(new OutputStreamWriter(os));
	}

	public void write(char[] array, int start, int length) {
		if (closed == true) return; 
		
		try {
			bw.write(array, start, length);
			//bw.flush();
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "Cannot write to buffer. Maybe user cancel downloading of that file.");
			closed = true;
		}
	}

	public OutputStream getResult() {
		return os;
	}

	public void close() {
			Utils.close(bw, os);
	}

}
