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
import java.util.Optional;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Validator for reference value exists in DB
 * 
 * @author sloury
 *
 * @param <T>
 */
public class RefValueExistsValidator<T extends AbstractGenericItem> {

	private static final Logger LOG = LoggerFactory.getLogger(RefValueExistsValidator.class);

	private RefsService service;

	/**
	 * @param service
	 */
	public RefValueExistsValidator(RefsService refsService) {
		super();
		this.service = refsService;
	}

	/**
	 * 
	 * @param entity
	 * @return
	 */
	public FieldErrorMap validate(T entity) {
		FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (Field field : entity.getClass().getDeclaredFields()) {
				// check @unique
				if (field.isAnnotationPresent(RefValueExists.class)) {
					String getterName = "get" + StringUtils.capitalize(field.getName());
					try {
						Method getter = entity.getClass().getMethod(getterName);
						Reference value = (Reference) getter.invoke(entity);
						if (value != null) {
							Optional<Reference> foundValue = service.findByTypeAndValue(field.getName(),
									value.getValue());
							// If found entities and it is not the same current entity
							if (!foundValue.isPresent()) {
								List<FieldError> errors = new ArrayList<FieldError>();
								errors.add(new FieldError("invalid value",
										"The given value do not exists for this field", value.getValue()));
								errorMap.put(field.getName(), errors);
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @RefValueExists custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error(
								"Error while checking @RefValueExists custom annotation, you must implement a method named "
										+ getterName + "() for accessing " + entity.getClass().getName() + "."
										+ field.getName(),
								e);
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @RefValueExists custom annotation", e);
		}
		return errorMap;
	}

}
