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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Validator for edition by role.
 * 
 * @author msimon
 *
 * @param <T>
 */
@Service
public abstract class FieldEditionSecurityManagerImpl <T extends AbstractEntity> implements FieldEditionSecurityManager<T> {
	
	@Autowired
	public CrudRepository<T, Long> repository;

	@Override
	public FieldErrorMap validate(final T entity) {
		if (entity.getId() == null) {
			return validateCreate(entity);
		} else {
			T originalEntity = repository.findById(entity.getId()).orElse(null);
			return validateUpdate(entity, originalEntity);
		}		
	}
	
	
	private FieldErrorMap validateUpdate(final T editedEntity, final T originalEntity) {

		final Collection<String> connectedUserRoles = KeycloakUtil.getConnectedUserRoles();
		final FieldErrorMap errorMap = new FieldErrorMap();
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
					if (fieldHasBeenModified && !Utils.haveOneInCommon(Arrays.asList(annotation.roles()), connectedUserRoles)) {
						final List<FieldError> errors = new ArrayList<FieldError>();
						errors.add(new FieldError("unauthorized", "You do not have the right to edit this field",
								givenValue));
						errorMap.put(field.getName(), errors);
					}
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new IllegalStateException("Error while checking @EditableOnlyBy custom annotation", e);
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
									+ getterName + "() for accessing " + originalEntity.getClass().getName() + "."
									+ field.getName());
				}
			}
		}
		return errorMap;
	}

	/**
	 * Validates a creation
	 *
	 * @param user
	 * @return the forgotten fields names
	 */
	private FieldErrorMap validateCreate(final T editedEntity) {
		final Collection<String> connectedUserRoles = KeycloakUtil.getConnectedUserRoles();
		final FieldErrorMap errorMap = new FieldErrorMap();
		for (final Field field : editedEntity.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(EditableOnlyBy.class)) {
				final EditableOnlyBy annotation = field.getAnnotation(EditableOnlyBy.class);
				final String getterName = "get" + StringUtils.capitalize(field.getName());
				try {
					final Method editedGetter = editedEntity.getClass().getMethod(getterName);
					final Object givenValue = editedGetter.invoke(editedEntity);
					if (givenValue != null && !Utils.haveOneInCommon(Arrays.asList(annotation.roles()), connectedUserRoles)) {
						final List<FieldError> errors = new ArrayList<FieldError>();
						errors.add(new FieldError("unauthorized", "You do not have the right to edit this field",
								givenValue));
						errorMap.put(field.getName(), errors);
					}
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new IllegalStateException("Error while checking @EditableOnlyBy custom annotation", e);
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
									+ getterName + "() for accessing " + editedEntity.getClass().getName() + "."
									+ field.getName());
				}
			}
		}
		return errorMap;
	}
}
