package org.shanoir.ng.model.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.shanoir.ng.model.User;
import org.shanoir.ng.model.error.FieldError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class UniqueValidator <T> {

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
	 * @param user
	 * @return
	 */
	public List<FieldError> Validate(T entity) {
		List<FieldError> errorList = new ArrayList<FieldError>();
		try {
			for (Field field : User.class.getDeclaredFields()) {
				// check @unique
				if (field.isAnnotationPresent(Unique.class)) {
					String getterName = "get"+StringUtils.capitalize(field.getName());
					try {
						Method getter = entity.getClass().getMethod(getterName);
						Object value = getter.invoke(entity);
						List<T> foundedList = service.findBy(field.getName(), value);
						// If found users and it is not the same current user
						if (!foundedList.isEmpty() && !(foundedList.size() == 1 && foundedList.get(0).equals(entity))) {
							FieldError formError = new FieldError(field.getName(), Arrays.asList("unique"));
							errorList.add(formError);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @Unique custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error("Error while checking @Unique custom annotation, you must implement a method named "
								+ getterName + "() for accessing User." + field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @Unique custom annotation", e);
		}
		return errorList;
	}

}
