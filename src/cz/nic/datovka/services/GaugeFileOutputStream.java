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
	private long total;
	private int receiverIdent;
	int laststatus;
	public GaugeFileOutputStream(File file, ResultReceiver receiver, int receiverIdent, long expectedFileSize) throws FileNotFoundException {
		super(file);
		this.receiver = receiver;
		this.expectedFileSize = expectedFileSize;
		this.receiverIdent = receiverIdent;
		laststatus = -1;
		total = 0;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException{
		super.write(b, off, len);
		
		total += len;
		Bundle resultData = new Bundle();
		int status;
		
		try{
		 status = (int) (total * 100 / expectedFileSize);
		} catch(ArithmeticException e){
			status = 100;
		}
		resultData.putInt("progress", status);
		receiver.send(receiverIdent, resultData); 
		/*
		if(laststatus < status){
			System.out.println("expect: " + expectedFileSize + " total: " + total + " status: " + status);
			laststatus = status;
		}*/
	}
	
	@Override
	public void write(byte[] b) throws IOException{
		super.write(b);
		
		total += b.length;
		Bundle resultData = new Bundle();
		int status = (int) (total * 100 / expectedFileSize);
		resultData.putInt("progress", status);
		receiver.send(receiverIdent, resultData); 
	}
	
}
