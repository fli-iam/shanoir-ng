package org.shanoir.ng.shared.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Validator for unicity.
 * 
 * @author msimon
 *
 * @param <T>
 */
public class UniqueValidator <T extends AbstractGenericItem> {

	private static final Logger LOG = LoggerFactory.getLogger(UniqueValidator.class);

	private UniqueCheckableService<T> service;

	/**
	 * @param service
	 */
	public UniqueValidator(UniqueCheckableService<T> service) {
		super();
		this.service = service;
	}

	/**
	 * Validates what can't be done by Spring/Hibernate validation, in particular unique constraints
	 * Calls database !!!
	 * Check equals implementation for your entity !!!
	 *
	 * @param entity
	 * @return
	 */
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
						List<T> foundedList = service.findBy(field.getName(), value);
						// If found entities and it is not the same current entity
						if (!foundedList.isEmpty() && !(foundedList.size() == 1 && foundedList.get(0).getId().equals(entity.getId()))) {
							List<FieldError> errors = new ArrayList<FieldError>();
							errors.add(new FieldError("unique", "The given value is already taken for this field, choose another", value));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @Unique custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error("Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
								+ getterName + "() for accessing " + entity.getClass().getName() + "." + field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @Unique custom annotation", e);
		}
		return errorMap;
	}

}
