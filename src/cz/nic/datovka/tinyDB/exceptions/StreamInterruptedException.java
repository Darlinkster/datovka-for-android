package cz.nic.datovka.tinyDB.exceptions;

public class StreamInterruptedException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public StreamInterruptedException(String message){
		super(message);
	}

}
