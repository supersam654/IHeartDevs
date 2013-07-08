package com.sammarder.iheartdevs;

/**
 * Should be thrown when any type of input must be processed but can't be. For example, receiving a a message on
 * System.err that cannot be processed should cause an InputProcessingException (or at least the stack trace to one).
 */
public class InputProcessingException extends Exception {
	// Shutup Eclipse...
	private static final long serialVersionUID = 1650158412333004298L;

	/**
	 * Only constructor for creating an IPE.
	 * 
	 * @param message
	 *            The message to associate with this exception.
	 */
	public InputProcessingException(String message) {
		super(message);
	}
}
