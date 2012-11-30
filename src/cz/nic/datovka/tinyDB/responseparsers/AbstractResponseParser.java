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

package cz.nic.datovka.tinyDB.responseparsers;

import cz.abclinuxu.datoveschranky.common.impl.Status;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.StringHolder;

import java.io.IOException;
import java.util.HashMap;
import org.xml.sax.Attributes;

/**
 * Tato třída slouží k parsování odpovědi webové služby, je to nástavba nad
 * SAX parserem, která umožnuje jednoduchou navigaci (viz metoda match) a podstatně
 * zjednodušuje čtení obsahu elementu.
 * 
 * TODO: rozepsat dokumentaci k metodám.
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public abstract class AbstractResponseParser implements ResponseParser {

    private static final boolean debug = false;
    private static final int MAX_DEPTH = 128;
    private static final String[] wanting = {"dmStatusCode", "dmStatusMessage",
    										"dbStatusCode", "dbStatusMessage"};
    
    private String[] path = new String[MAX_DEPTH];
    private int pathIndex = 0;
    private HashMap<String, StringHolder> map = new HashMap<String, StringHolder>();
    private Status status = null;

    public AbstractResponseParser() {
        for (String key : wanting) {
            map.put(key, new StringHolder());
        }
    }

    public Status getStatus() {
        return status;
    }

    public void done() {
        String StatusCode = map.get("dmStatusCode").toString();
        String StatusMessage = map.get("dmStatusMessage").toString();
         
        if(StatusCode.length() == 0 && StatusMessage.length() == 0){
        	StatusCode = map.get("dbStatusCode").toString();
        	StatusMessage = map.get("dbStatusMessage").toString();
        }
        
        status = new Status(StatusCode, StatusMessage);
    }

    /**
     * Začátek elementu, vrátí OutputHandler, do kterého se načte obsah elementu
     */
    public OutputHolder startElement(String elName, Attributes attributes) {
        path[pathIndex] = elName.intern(); // rychlejší porovnání
        pathIndex++;
        if (debug) {
            System.err.println(dumpState());
        }
        OutputHolder handle = map.get(elName);
        if (handle == null) {
            return startElementImpl(elName, attributes);
        } else {
            return handle;
        }
    }
    
    /**
     * Konec elementu, handle obsahuje OutputHandler s obsaheme elementu.
     * @throws IOException 
     */ 
    public void endElement(String elName, OutputHolder handle) throws IOException {
        if (debug) {
            System.err.println(dumpState());
        }
        if (!map.containsKey(elName)) {
            endElementImpl(elName, handle);
        }
        path[pathIndex] = null;
        pathIndex--;
    }
    
    /**
     *  Navigace x XML dokumentu, ověří shodu args proti vrcholu zásobníků.
     *  TODO rozepsat.
     */ 
    public boolean match(String... args) {
        if (pathIndex >= args.length) {
            int start = pathIndex - args.length;
            for (int i = 0; i!= args.length; i++) {
                String a = path[start + i];
                String b = args[i];
                if (!"*".equals(b) && !a.equals(b)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    public String dumpState() {
        StringBuffer result = new StringBuffer("/");
        for (int i = 0; i!= pathIndex; i++) {
            result.append(path[i]);
            result.append("/");
        }
        return result.toString();
    }

    public abstract OutputHolder startElementImpl(String elName, Attributes attributes);

    public abstract void endElementImpl(String elName, OutputHolder handle) throws IOException;
}
