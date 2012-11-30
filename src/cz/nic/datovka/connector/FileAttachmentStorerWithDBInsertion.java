/*
Datovka - An Android client for Datove schranky
    Copyright (C) 2012  CZ NIC z.s.p.o. <podpora at nic dot cz>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package cz.nic.datovka.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cz.abclinuxu.datoveschranky.common.entities.Attachment;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.interfaces.AttachmentStorer;
import cz.nic.datovka.activities.Application;

public class FileAttachmentStorerWithDBInsertion implements AttachmentStorer {

    private String outputDir = null;
    private int folder;
    private long messageId;

    public FileAttachmentStorerWithDBInsertion(String outputDir, int folder,
			long messageId) {
        
        this.outputDir = outputDir;
        this.folder = folder;
        this.messageId = messageId;
        
    }
    
    public OutputStream store(MessageEnvelope envelope, Attachment attachment) throws IOException {
        String name = name(envelope, attachment);
        File output = new File(Application.externalStoragePath + outputDir, name);
        output.createNewFile();
        
        DatabaseTools.insertAttachmentToDb(outputDir + "/" + name,
				attachment.getDescription(), attachment.getMimeType(), folder, messageId);
        
        return new FileOutputStream(output);
    }
    
    protected String name(MessageEnvelope envelope, Attachment attachment) {
        String prefix = envelope.getMessageID();
        String description = attachment.getDescription();
        return prefix + "_" + description;
    }
    
}
