package cz.nic.datovka.tinyDB.holders;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Zapíše obsah elementu do OutputStreamu.
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class OutputStreamHolder implements OutputHolder<OutputStream>, Closeable {

	private final OutputStream os;
	private final BufferedWriter bw;

	public OutputStreamHolder(OutputStream os) {
		this.os = os;
		bw = new BufferedWriter(new OutputStreamWriter(os));
	}

	public void write(char[] array, int start, int length) {
		try {

			bw.write(array, start, length);
			bw.flush();
		} catch (IOException ioe) {
			// This is awful right? But how to handle message downloading
			// service interruption?
		}
	}

	public OutputStream getResult() {
		return os;
	}

	public void close() {
		try {
			// Utils.close(bw, os);
			bw.close();
			os.close();
		} catch (IOException e) {
			// This is awful right? But how to handle message downloading
			// service interruption?
		}
	}

}
