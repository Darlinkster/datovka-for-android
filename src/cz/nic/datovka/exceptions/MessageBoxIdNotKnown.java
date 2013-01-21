package cz.nic.datovka.exceptions;

public class MessageBoxIdNotKnown extends Exception{
	private static final long serialVersionUID = 5850947027023785316L;

	public MessageBoxIdNotKnown (String message) {
		super(message);
	}
}
