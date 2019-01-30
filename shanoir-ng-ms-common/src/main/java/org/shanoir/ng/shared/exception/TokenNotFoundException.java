package org.shanoir.ng.shared.exception;

/**
 * TokenNotFoundException exception.
 * 
 * @author jlouis
 *
 */
public class TokenNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 825251865870288948L;

	public TokenNotFoundException(String message) {
		super(message);
	}

}
