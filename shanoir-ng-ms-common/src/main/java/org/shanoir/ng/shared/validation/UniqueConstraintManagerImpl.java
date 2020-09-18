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
import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class UniqueConstraintManagerImpl <T extends AbstractEntity> implements UniqueConstraintManager<T> {
	
	@Autowired
	protected FindByRepository<T> repository;



	/**
	 * Validates what can't be done by Spring/Hibernate validation, in particular unique constraints
	 * Calls database !!!
	 * Check equals implementation for your entity !!!
	 *
	 * @param entity
	 * @return
	 */
	@Override
	public FieldErrorMap validate(T entity) {
		FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (Field field : entity.getClass().getDeclaredFields()) {
				// check @unique
				if (field.isAnnotationPresent(Unique.class)) {
					String getterName = "get"+StringUtils.capitalize(field.getName());
					try {
						Method getter = entity.getClass().getMethod(getterName);
						Object value = getter.invoke(entity);
						if (value instanceof String) value = ((String) value).trim();
						List<T> foundedList = repository.findBy(field.getName(), value, entity.getClass());
						// If found entities and it is not the same current entity
						if (!foundedList.isEmpty() && !(foundedList.size() == 1 && foundedList.get(0).getId().equals(entity.getId()))) {
							List<FieldError> errors = new ArrayList<FieldError>();
							errors.add(new FieldError("unique", "The given value is already taken for this field, choose another", value));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						throw new IllegalStateException("Error while checking @Unique custom annotation", e);
					} catch (NoSuchMethodException e) {
						throw new IllegalStateException("Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
								+ getterName + "() for accessing " + entity.getClass().getName() + "." + field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			throw new IllegalStateException("Error while checking @Unique custom annotation", e);
		}
		return errorMap;
	}

}
