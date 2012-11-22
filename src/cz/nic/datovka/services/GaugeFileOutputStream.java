package cz.nic.datovka.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.ResultReceiver;

public class GaugeFileOutputStream extends FileOutputStream {
	private ResultReceiver receiver;
	private long expectedFileSize;
	private volatile long total;
	private int receiverIdent;
	private volatile int laststatus;
	private volatile int status;
	private Bundle resultData;
	public GaugeFileOutputStream(File file, ResultReceiver receiver, int receiverIdent, long expectedFileSize) throws FileNotFoundException {
		super(file);
		this.receiver = receiver;
		this.expectedFileSize = expectedFileSize;
		this.receiverIdent = receiverIdent;
		laststatus = -1;
		total = 0;
		resultData = new Bundle();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException{
		super.write(b, off, len);
		
		total += len;
		
		try{
		 status = (int) (total * 100 / expectedFileSize);
		} catch(ArithmeticException e){
			status = 100;
		}
		
		if(laststatus < status){
			laststatus = status;
			resultData.putInt("progress", status);
			if(receiver != null)
				receiver.send(receiverIdent, resultData);
			else
				return;
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException{
		super.write(b);
		
		total += b.length;
		status = (int) (total * 100 / expectedFileSize);
		
		
		if(laststatus < status){
			laststatus = status;
			resultData.putInt("progress", status);
			if(receiver != null)
				receiver.send(receiverIdent, resultData);
			else
				return;
		}
	}
	
}
