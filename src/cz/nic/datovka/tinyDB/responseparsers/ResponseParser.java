package cz.nic.datovka.tinyDB.responseparsers;


import java.io.IOException;

import org.xml.sax.Attributes;

import cz.nic.datovka.tinyDB.holders.OutputHolder;

/**
 *
 * Rozhraní, přijde refaktorovat
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 * 
 */
public interface ResponseParser {

    public OutputHolder startElement(String elName, Attributes attributes);

    public void endElement(String elName, OutputHolder handle) throws IOException;

    public void done();
}
