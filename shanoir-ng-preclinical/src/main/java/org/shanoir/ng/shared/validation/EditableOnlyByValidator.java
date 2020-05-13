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

package org.shanoir.ng.shared.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.keycloak.KeycloakPrincipal;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;

/**
 * Validator for edition by role.
 * 
 * @author msimon
 *
 * @param <T>
 */
public class EditableOnlyByValidator<T> {

	private static final String ERROR_CHECKING_EDITABLE_ANNOTATION = "Error while checking @EditableOnlyBy custom annotation";
	private static final Logger LOG = LoggerFactory.getLogger(EditableOnlyByValidator.class);

	/**
	 * Validates an update
	 *
	 * @param user
	 * @return the forgotten fields names
	 */
	public FieldErrorMap validate(final T originalEntity, final T editedEntity) {
		final Collection<String> connectedUserRoles = getConnectedUserRoles();
		final FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (final Field field : originalEntity.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(EditableOnlyBy.class)) {
					final EditableOnlyBy annotation = field.getAnnotation(EditableOnlyBy.class);
					final String getterName = "get" + StringUtils.capitalize(field.getName());
					try {
						final Method originalGetter = originalEntity.getClass().getMethod(getterName);
						final Method editedGetter = editedEntity.getClass().getMethod(getterName);
						final Object originalValue = originalGetter.invoke(originalEntity);
						final Object givenValue = editedGetter.invoke(editedEntity);
						final boolean fieldHasBeenModified = !Utils.equalsIgnoreNull(originalValue, givenValue);
						if (fieldHasBeenModified && !haveOneRoleInCommon(annotation.roles(), connectedUserRoles)) {
							final List<FieldError> errors = new ArrayList<>();
							errors.add(new FieldError("unauthorized", "You do not have the right to edit this field",
									givenValue));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error(ERROR_CHECKING_EDITABLE_ANNOTATION, e);
					} catch (NoSuchMethodException e) {
						LOG.error(
								"Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
										+ getterName + "() for accessing " + originalEntity.getClass().getName() + "."
										+ field.getName(),
								e);
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error(ERROR_CHECKING_EDITABLE_ANNOTATION, e);
		}
		return errorMap;
	}

	/**
	 * Validates a creation
	 *
	 * @param user
	 * @return the forgotten fields names
	 */
	public FieldErrorMap validate(final T editedEntity) {
		final Collection<String> connectedUserRoles = getConnectedUserRoles();
		final FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (final Field field : editedEntity.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(EditableOnlyBy.class)) {
					final EditableOnlyBy annotation = field.getAnnotation(EditableOnlyBy.class);
					final String getterName = "get" + StringUtils.capitalize(field.getName());
					try {
						final Method editedGetter = editedEntity.getClass().getMethod(getterName);
						final Object givenValue = editedGetter.invoke(editedEntity);
						if (givenValue != null && !haveOneRoleInCommon(annotation.roles(), connectedUserRoles)) {
							final List<FieldError> errors = new ArrayList<>();
							errors.add(new FieldError("unauthorized", "You do not have the right to edit this field",
									givenValue));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error(ERROR_CHECKING_EDITABLE_ANNOTATION, e);
					} catch (NoSuchMethodException e) {
						LOG.error(
								"Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
										+ getterName + "() for accessing " + editedEntity.getClass().getName() + "."
										+ field.getName(),
								e);
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error(ERROR_CHECKING_EDITABLE_ANNOTATION, e);
		}
		return errorMap;
	}

	private boolean haveOneRoleInCommon(final String[] roles, final Collection<String> authorities) {
		for (final String role : roles) {
			for (final String authority : authorities) {
				if (role != null && role.equals(authority)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get connected user roles. If anonymous user, returns an empty list.
	 * 
	 * @return roles
	 */
	@SuppressWarnings("rawtypes")
	private Collection<String> getConnectedUserRoles() {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			return new ArrayList<>();
		} else {
			final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal == null) {
				throw new IllegalArgumentException("connectedUser cannot be null");
			}
			if (principal instanceof User) {
				final List<String> userRoles = new ArrayList<>();
				for (GrantedAuthority authority : ((User) principal).getAuthorities()) {
					userRoles.add(authority.getAuthority());
				}
				return userRoles;
			}
			return ((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getRealmAccess().getRoles();
		}
	}

}
