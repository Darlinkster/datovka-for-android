package cz.nic.datovka.tinyDB;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.responseparsers.ResponseParser;

import java.io.IOException;
import java.util.Stack;

/**
 * 
 * Nadstavba nad SAX parserem, tohle přijde refaktorovat s třídou ResponsePraser.
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class SimpleSAXParser extends DefaultHandler {

    private static class State {

        public String element = null;
        public OutputHolder handler = null;

        public State(String el, OutputHolder handler) {
            this.element = el;
            this.handler = handler;
        }
    }
    private Stack<State> path = new Stack<State>();
    private ResponseParser delegate;

    public SimpleSAXParser(ResponseParser parser) {
        this.delegate = parser;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        String elementName = qName.substring(qName.lastIndexOf(':') + 1).intern();
        OutputHolder handler = delegate.startElement(elementName, attributes);
        path.push(new State(elementName, handler));
    }

    @Override
    public void characters(char[] array, int start, int length) throws SAXException {
            OutputHolder handler = this.state().handler;
            if (handler != null) {
                try {
					handler.write(array, start, length);
				} catch (IOException e) {
					throw new SAXException(e);
				}
            }
    }

    @Override
    public void endElement(String arg0, String arg1, String arg2) throws SAXException {
        State state = this.state();
        try {
			delegate.endElement(state.element, state.handler);
			path.pop();
		} catch (IOException e) {
			throw new SAXException(e);
		}
       
    }

    private State state() {
        return path.peek();
    }

    @Override
    public void endDocument() throws SAXException {
        delegate.done();
    }
}
