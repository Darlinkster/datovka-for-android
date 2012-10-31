package cz.nic.datovka.tinyDB.exceptions;

public class HttpException extends Exception{

	private static final long serialVersionUID = 1L;
	private int errorCode;
	
	public HttpException(String message){
		super(message);
		this.errorCode = -1;
	}
	
	public HttpException(String message, int errorCode){
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
