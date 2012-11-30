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

import cz.abclinuxu.datoveschranky.common.impl.Utils;
import cz.abclinuxu.datoveschranky.common.entities.Hash;
import cz.nic.datovka.tinyDB.base64.Base64OutputStream;
import cz.nic.datovka.tinyDB.holders.OutputHolder;
import cz.nic.datovka.tinyDB.holders.OutputStreamHolder;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import org.xml.sax.Attributes;

/**
 *
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public class VerifyMessage extends AbstractResponseParser {

    private ByteArrayOutputStream hash = new ByteArrayOutputStream();
    private String algorithm = null;

    public VerifyMessage() {
    }

    @Override
    public OutputHolder startElementImpl(String elName, Attributes attributes) {
        if (super.match("dmHash")) { // tohle nás zajímá
            algorithm = attributes.getValue("algorithm");
            //Base64OutputStream bos = new Base64OutputStream(hash, false, 0, null);
            Base64OutputStream bos = new Base64OutputStream(hash, 0, false);
            return new OutputStreamHolder(bos);
        }
        return null;
    }

    @Override
    public void endElementImpl(String elName, OutputHolder holder) {
        if (holder instanceof Closeable) {
            Utils.close((Closeable) holder);
        }
    }
    
    public Hash getResult() {
        return new Hash(algorithm, hash.toByteArray());
    }
    
}
