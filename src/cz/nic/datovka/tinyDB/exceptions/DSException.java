package cz.nic.datovka.tinyDB.exceptions;

public class DSException extends Exception{

	private static final long serialVersionUID = 1L;
	private int errorCode;
	
	public DSException(String message){
		super(message);
		this.errorCode = -1;
	}
	
	public DSException(String message, int errorCode){
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
