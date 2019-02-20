package org.shanoir.ng.shared.exception;

/**
 * ForbiddenException exception.
 * 
 * @author jlouis
 */
public class ForbiddenException extends SecurityException {

	private static final long serialVersionUID = 4407134016934725760L;

	public ForbiddenException(String message) {
		super(message);
	}
}
