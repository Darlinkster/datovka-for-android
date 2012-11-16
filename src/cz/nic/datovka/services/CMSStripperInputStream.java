package cz.nic.datovka.services;

import java.io.IOException;
import java.io.InputStream;

public class CMSStripperInputStream extends InputStream {
	private InputStream input;
	private boolean start = true;
	private byte[] buffer;
	private int count;

	public CMSStripperInputStream(InputStream input) {
		this.input = input;
		this.count = 0;
	}

	@Override
	public int read() throws IOException {
		if (start) {
			readHeader();
			if(loadData() == -1)
				return -1;
			start = false;
		}

		if (count < buffer.length) {
			return buffer[count++];
		} else {
			if(loadData() == -1)
				return -1;
			count = 0; // Restart counter.
			return buffer[count++];
		}

	}
	
	@Override
	public int read(byte[] b) throws IOException{
		if (start) {
			readHeader();
			if(loadData() == -1)
				return -1;
			start = false;
		}
		
		int paramLength = b.length;
		int bufferLength = buffer.length;
		if(count < bufferLength){
			int length = bufferLength - count;
			if(paramLength <= length){
				System.arraycopy(buffer, count, b, 0, paramLength);
				count += paramLength;
				return paramLength;
			} else {
				System.arraycopy(buffer, count, b, 0, length);
				count += length;
				return length;
			}
		} else {
			if(loadData() == -1)
				return -1;
			bufferLength = buffer.length;
			count = 0;
			
			if(paramLength <= bufferLength){
				System.arraycopy(buffer, count, b, 0, paramLength);
				count += paramLength;
				return paramLength;
			} else {
				System.arraycopy(buffer, count, b, 0, bufferLength);
				count += bufferLength;
				return bufferLength;
			}
		}
	}

	private int loadData() throws IOException {
		int contentLength = readOctetStringHeader();

		if (contentLength == -1) {
			return -1;
		} else if (buffer == null || (!(buffer.length == contentLength))) {
			buffer = new byte[contentLength];
			input.read(buffer);
			return contentLength;
		} else {
			input.read(buffer);
			return contentLength;
		}
	}

	private int readOctetStringHeader() throws IOException {
		String hexByte;
		//int headerLength = 2; // default header size (for arrays smaller then
								// 128B)
		boolean hasStartByte = false;
		boolean endTag = false;

		while (!hasStartByte) {
			hexByte = Integer.toHexString(input.read());
			if (hexByte.equalsIgnoreCase("4")) { // Start tag of TLV.
				hasStartByte = true;
			} else if (hexByte.equalsIgnoreCase("0")) { // 0x0, 0x0 means end of
														// the content.
				if (endTag) { // Is this 0x0 the second one in a row? Then
								// return -1.
					return -1;
				}
				endTag = true;
			} else if (endTag) { // Previous byte was 0x0, but this one is
									// different, so delete the flag.
				endTag = false;
			}
		}

		int firstLengthByte = input.read();
		int firstBit = firstLengthByte & 0x80; // Mask lower 7 bits.

		if (firstBit == 0) {
			return firstLengthByte; 
		} else {
			int lengthBytesCount = firstLengthByte & 0x7f; // Mask the first
															// bit.
			//headerLength += lengthBytesCount;
			int contentLength = 0;

			for (int i = 0; i < lengthBytesCount; i++) {
				int nextByte = input.read(); // Read next byte.
				contentLength = contentLength << 0x8; // Bit shift the
														// contentLength to left
														// by 8.
				contentLength += nextByte;
			}
			return contentLength;
		}

	}

	private void readHeader() throws IOException {
		boolean contentStart = false;
		boolean hasFirstByte = false;
		String hexByte;

		while (!contentStart) {
			hexByte = Integer.toHexString(input.read());
			if (!hasFirstByte) {
				if (hexByte.equalsIgnoreCase("24")) {
					hasFirstByte = true; // We catche the first byte (of two) in
											// the next iteration look for the
											// second one.
				}
			} else {
				if (hexByte.equalsIgnoreCase("80")) {
					contentStart = true; // We catch the second byte. Jump off
											// the while loop.
				} else if (hexByte.equalsIgnoreCase("24")) {
					// The second byte is not the right one but the first one
					// again, so do nothing and look for the second byte
					// again;
				} else {
					hasFirstByte = false; // The second byte is not the right
											// one, look for the first again.
				}
			}
		}
	}
}
