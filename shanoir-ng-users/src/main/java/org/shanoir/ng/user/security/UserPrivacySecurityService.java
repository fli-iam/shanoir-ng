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

package org.shanoir.ng.user.security;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.shanoir.ng.shared.security.VisibleOnlyBy;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserPrivacySecurityService {
		
    public boolean filterPersonnalData(List<User> users) {
    	
    	final Collection<String> connectedUserRoles = KeycloakUtil.getConnectedUserRoles();
    	
    	for (User user : users) {
    		
    		for (final Field field : User.class.getDeclaredFields()) {
    			if (field.isAnnotationPresent(VisibleOnlyBy.class)) {
    				final VisibleOnlyBy annotation = field.getAnnotation(VisibleOnlyBy.class);
    				if (!Utils.haveOneInCommon(Arrays.asList(annotation.roles()), connectedUserRoles)) {
    					final String setterName = "set" + StringUtils.capitalize(field.getName());
    					try {
    						final Method setter = User.class.getMethod(setterName, field.getType());
    						Object arg = null;
	    					setter.invoke(user, arg);
    					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
    						throw new IllegalStateException("Error while checking @VisibleOnlyBy custom annotation", e);
    					} catch (NoSuchMethodException e) {
    						throw new IllegalStateException(
    								"Error while checking @VisibleOnlyBy custom annotation, you must implement a method named "
    										+ setterName + " for accessing " + user.getClass().getName() + "."
    										+ field.getName());
    					}
    					
    				}
    			}
    		}
    		
    		
    		
    		
    		
    		
    	}
    	
    	return true;
    }
}