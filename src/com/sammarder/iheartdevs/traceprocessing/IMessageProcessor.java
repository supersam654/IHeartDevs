package com.sammarder.iheartdevs.traceprocessing;

/**
 * Designates that an object is capable of processing a multiline message while only receiving one line at a time (in
 * quick succession).
 */
public interface IMessageProcessor {
	/**
	 * Processes a given message. If this returns true, the caller should treat the message as if it were fully
	 * processed and does not need further handling. Note that process() cannot partially interpret a message.
	 * 
	 * @param message
	 *            The message to process.
	 * @return true if the message was consumed and does not require further handling.
	 */
	public boolean process(final String message);
}
