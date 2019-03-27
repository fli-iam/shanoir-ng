package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.utils.KeycloakUtil;

/**
 * AccessDeniedException exception.
 * 
 * @author jlouis
 */
public class AccessDeniedException extends SecurityException {

	private static final long serialVersionUID = 825251865870288948L;

	public AccessDeniedException(String message) {
		super(message);
	}
	
	public AccessDeniedException(Class<? extends AbstractEntity> clazz, Long id) {
		super(getMessage(clazz, id));
	}
	
	public AccessDeniedException(AbstractEntity entity) {
		this(AccessDeniedException.getMessage(entity.getClass(), entity.getId()));
	}
	
	private static String getMessage(Class<? extends AbstractEntity> clazz, Long id) {
		return "Current user " 
				+ KeycloakUtil.getTokenUserId()
				+ " cannot access to " 
				+ clazz.getSimpleName() 
				+ " with id = " 
				+ id;
	}

}
