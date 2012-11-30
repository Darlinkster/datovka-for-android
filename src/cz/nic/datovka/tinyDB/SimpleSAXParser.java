/*
Copyright (c) 2010, Vaclav Rosecky <xrosecky at gmail dot com>
All rights reserved.
Modification: 09/2012 CZ NIC z.s.p.o. <podpora at nic dot cz>

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL COPYRIGHT HOLDER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
