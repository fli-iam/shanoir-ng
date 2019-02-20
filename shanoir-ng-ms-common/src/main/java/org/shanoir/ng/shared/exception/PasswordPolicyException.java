package org.shanoir.ng.shared.exception;

/**
 * AccessDeniedException exception.
 * 
 * @author jlouis
 */
public class PasswordPolicyException extends SecurityException {

	private static final long serialVersionUID = 6668952716780935465L;

	public PasswordPolicyException() {
		super("Password did not match policy");
	}


}
