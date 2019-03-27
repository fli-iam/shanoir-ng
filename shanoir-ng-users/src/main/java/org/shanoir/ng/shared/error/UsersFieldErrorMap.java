package org.shanoir.ng.shared.error;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.utils.PasswordUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class UsersFieldErrorMap extends FieldErrorMap {

	private static final long serialVersionUID = -3176001477448120848L;

	private static final String USERNAME_FIELD = "username";
	private static final String ROLE_FIELD = "role";

	
	public UsersFieldErrorMap checkBindingIgnoreBlankUsername(BindingResult result) {
		return this.merge(UsersFieldErrorMap.fieldErrorMapIgnoreUsernameBlank(result));
	}
	
	public UsersFieldErrorMap checkBindingIgnoreBlankUsernameAndRole(BindingResult result) {
		return this.merge(UsersFieldErrorMap.fieldErrorMapIgnoreUsernameAndRoleBlank(result));
	}
	
	public UsersFieldErrorMap checkPasswordPolicy(String password) {
		if (!PasswordUtils.checkPasswordPolicy(password)) {
			List<FieldError> errors = new ArrayList<>();
			errors.add(new FieldError("format", "Password does not match policy", null));
			this.put("password", errors);
		}
		return this;
	}
	
	public UsersFieldErrorMap merge(UsersFieldErrorMap map) {
		return (UsersFieldErrorMap) super.merge(map);
	}

	/**
	 * Tell Spring to remove the hibernante validation error on username blank
	 *
	 * @param {@link BindingResult}
	 * @return {@link UsersFieldErrorMap}
	 */
	private static UsersFieldErrorMap fieldErrorMapIgnoreUsernameBlank(final BindingResult result) {
		final UsersFieldErrorMap fieldErrorMap = new UsersFieldErrorMap();
		if (result.hasErrors()) {
			for (ObjectError objectError : result.getAllErrors()) {
				final org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
				if (!USERNAME_FIELD.equals(fieldError.getField()) || fieldError.getRejectedValue() != null) {
					if (!fieldErrorMap.containsKey(fieldError.getField())) {
						fieldErrorMap.put(fieldError.getField(),
								new ArrayList<org.shanoir.ng.shared.error.FieldError>());
					}
					fieldErrorMap.get(fieldError.getField()).add(new org.shanoir.ng.shared.error.FieldError(
							fieldError.getCode(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
				}
			}
		}
		return fieldErrorMap;
	}

	/**
	 * Tell Spring to remove the hibernante validation error on username or role
	 * blank for user account request
	 *
	 * @param {@link BindingResult}
	 * @return {@link UsersFieldErrorMap}
	 */
	private static UsersFieldErrorMap fieldErrorMapIgnoreUsernameAndRoleBlank(final BindingResult result) {
		final UsersFieldErrorMap fieldErrorMap = new UsersFieldErrorMap();
		if (result.hasErrors()) {
			for (ObjectError objectError : result.getAllErrors()) {
				final org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
				if ((!ROLE_FIELD.equals(fieldError.getField()) && !USERNAME_FIELD.equals(fieldError.getField()))
						|| fieldError.getRejectedValue() != null) {
					if (!fieldErrorMap.containsKey(fieldError.getField())) {
						fieldErrorMap.put(fieldError.getField(),
								new ArrayList<org.shanoir.ng.shared.error.FieldError>());
					}
					fieldErrorMap.get(fieldError.getField()).add(new org.shanoir.ng.shared.error.FieldError(
							fieldError.getCode(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
				}
			}
		}
		return fieldErrorMap;
	}

}
