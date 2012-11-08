package cz.nic.datovka.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cz.abclinuxu.datoveschranky.common.entities.Attachment;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.abclinuxu.datoveschranky.common.entities.content.FileContent;
import cz.abclinuxu.datoveschranky.common.interfaces.AttachmentStorer;

public class FileAttachmentStorerWithDBInsertion implements AttachmentStorer {

    private File outputDir = null;
    private int folder;
    private long messageId;

    public FileAttachmentStorerWithDBInsertion(File outputDir, int folder,
			long messageId) {
        if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s neni adresarem.", outputDir.getAbsolutePath()));
        }
        this.outputDir = outputDir;
        this.folder = folder;
        this.messageId = messageId;
        
    }
    
    public OutputStream store(MessageEnvelope envelope, Attachment attachment) throws IOException {
        String name = name(envelope, attachment);
        File output = new File(outputDir, name);
        attachment.setContents(new FileContent(output));
        
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
