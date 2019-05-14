package org.shanoir.ng.shared.exception;

/**
 * SecurityException exception.
 * 
 * @author jlouis
 *
 */
public class MicroServiceCommunicationException extends ShanoirException {

	private static final long serialVersionUID = 430464673099395261L;

	public MicroServiceCommunicationException(String message) {
		super(message);
	}
	
	public MicroServiceCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}
}
