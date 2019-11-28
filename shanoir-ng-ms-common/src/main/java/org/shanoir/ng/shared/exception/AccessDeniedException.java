/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
