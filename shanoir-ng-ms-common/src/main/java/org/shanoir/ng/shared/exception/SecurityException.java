package org.shanoir.ng.shared.exception;

/**
 * SecurityException exception.
 * 
 * @author jlouis
 *
 */
public class SecurityException extends ShanoirException {

	private static final long serialVersionUID = -1545868693201382850L;

	public SecurityException(String message) {
		super(message);
	}
	
	public SecurityException(String message, Exception cause) {
		super(message, cause);
	}

}
