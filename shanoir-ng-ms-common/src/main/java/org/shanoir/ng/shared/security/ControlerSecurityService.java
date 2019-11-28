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

package org.shanoir.ng.shared.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.stereotype.Service;

@Service
public class ControlerSecurityService {
		
	/**
	 * Check that id and entity.getId() matches.
	 * 
	 * @param id
	 * @param entity
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
    public boolean idMatches(Long id, Object entity) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	if (id == null) throw new IllegalArgumentException("id cannot be null");
    	if (entity == null) throw new IllegalArgumentException("entity cannot be null");
    	Method getId = entity.getClass().getMethod("getId");
    	Long entityId = (Long) getId.invoke(entity);
    	return id.equals(entityId);
    }
    
}