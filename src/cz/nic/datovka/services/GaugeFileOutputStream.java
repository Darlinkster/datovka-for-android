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
