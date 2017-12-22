package org.shanoir.ng.shared.error;

import java.util.ArrayList;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class UsersFieldErrorMap extends FieldErrorMap {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3176001477448120848L;

	private static final String USERNAME_FIELD = "username";
	private static final String ROLE_FIELD = "role";
	
	/**
	 * Constructor
	 */
	public UsersFieldErrorMap() {
		super();
	}

	/**
	 * Constructor
	 */
	public UsersFieldErrorMap(final UsersFieldErrorMap... maps) {
		this.merge(maps);
	}

	/**
	 * Constructor
	 *
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link UsersFieldErrorMap}
	 */
	public UsersFieldErrorMap(final BindingResult result) {
		super(result);
	}

	/**
	 * Tell Spring to remove the hibernante validation error on username blank
	 *
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link UsersFieldErrorMap}
	 */
	public static UsersFieldErrorMap fieldErrorMapIgnoreUsernameBlank(final BindingResult result) {
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
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link UsersFieldErrorMap}
	 */
	public static UsersFieldErrorMap fieldErrorMapIgnoreUsernameAndRoleBlank(final BindingResult result) {
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
