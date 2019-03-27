package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * SecurityException exception.
 * 
 * @author jlouis
 *
 */
public class EntityNotFoundException extends ShanoirException {

	private static final long serialVersionUID = -4977613595161142500L;

	public EntityNotFoundException(String message) {
		super(message);
	}
	
	public EntityNotFoundException(Class<? extends AbstractEntity> clazz, Long id) {
		super(getMessage(clazz, id));
	}
	
	private static String getMessage(Class<? extends AbstractEntity> clazz, Long id) {
		return "Cannot find " 
				+ clazz.getSimpleName()
				+ " with id "
				+ id;
	}

}
