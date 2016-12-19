package org.shanoir.ng.model.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.shanoir.ng.model.User;
import org.shanoir.ng.model.error.FieldError;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

public class EditableOnlyByValidator <T> {

	private static final Logger LOG = LoggerFactory.getLogger(EditableOnlyByValidator.class);


	/**
	 * Validates
	 *
	 * @param user
	 * @return the forgotten fields names
	 */
	public List<FieldError> validate(User connectedUser, T originalEntity, T editedEntity) {
		if (connectedUser == null) {
			throw new IllegalArgumentException("connectedUser cannot be null");
		}
		List<FieldError> errorList = new ArrayList<FieldError>();
		try {
			for (Field field : originalEntity.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(EditableOnlyBy.class)) {
					EditableOnlyBy annotation = field.getAnnotation(EditableOnlyBy.class);
					String getterName = "get"+StringUtils.capitalize(field.getName());
					try {
						Method originalGetter = originalEntity.getClass().getMethod(getterName);
						Method editedGetter = editedEntity.getClass().getMethod(getterName);
						boolean fieldHasBeenModified = !Utils.equalsIgnoreNull(originalGetter.invoke(originalEntity), editedGetter.invoke(editedEntity));
						if (fieldHasBeenModified && !haveOneRoleInCommon(annotation.roles(), connectedUser.getAuthorities())) {
							List<String> errorCodes = new ArrayList<String>();
							errorCodes.add("unauthorized");
							errorList.add(new FieldError(field.getName(), errorCodes));
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @EditableOnlyBy custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error("Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
								+ getterName + "() for accessing " + originalEntity.getClass().getName() + "." + field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @EditableOnlyBy custom annotation", e);
		}
		return errorList;
	}


	private boolean haveOneRoleInCommon(String[] roles, Collection<? extends GrantedAuthority> authorities) {
		for (String role : roles) {
			for (GrantedAuthority authority : authorities) {
				if (role != null && role.equals(authority.getAuthority())) {
					return true;
				}
			}
		}
		return false;
	}

}
