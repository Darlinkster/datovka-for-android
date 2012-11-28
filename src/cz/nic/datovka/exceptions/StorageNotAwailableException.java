package cz.nic.datovka.exceptions;

public class StorageNotAwailableException extends Exception{

	private static final long serialVersionUID = -3137809411762706000L;

	public StorageNotAwailableException(String message){
		super(message);
	}
}
