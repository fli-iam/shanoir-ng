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