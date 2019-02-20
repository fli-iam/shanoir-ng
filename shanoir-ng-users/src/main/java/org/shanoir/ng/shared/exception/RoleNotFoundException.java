package org.shanoir.ng.shared.exception;

/**
 * Microservice exception.
 * 
 * @author jlouis
 *
 */
public class RoleNotFoundException extends EntityNotFoundException {

	private static final long serialVersionUID = -5649910456591832721L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message.
	 */
	public RoleNotFoundException(final String roleStr) {
		super("Role " + roleStr + " was not found");
	}

}
