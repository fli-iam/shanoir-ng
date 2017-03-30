package org.shanoir.ng.shared.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class FieldErrorMap extends HashMap<String, List<FieldError>> {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -4372394159041578756L;

	private static final String USERNAME_FIELD = "username";
	private static final String ROLE_FIELD = "role";
	
	/**
	 * Constructor
	 */
	public FieldErrorMap() {
		super();
	}

	/**
	 * Constructor
	 */
	public FieldErrorMap(final FieldErrorMap... maps) {
		this.merge(maps);
	}

	/**
	 * Constructor
	 *
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link FieldErrorMap}
	 */
	public FieldErrorMap(final BindingResult result) {
		if (result.hasErrors()) {
			for (ObjectError objectError : result.getAllErrors()) {
				final org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
				if (!this.containsKey(fieldError.getField())) {
					this.put(fieldError.getField(), new ArrayList<org.shanoir.ng.shared.error.FieldError>());
				}
				this.get(fieldError.getField()).add(new org.shanoir.ng.shared.error.FieldError(fieldError.getCode(),
						fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
			}
		}
	}

	/**
	 * Tell Spring to remove the hibernante validation error on username blank
	 *
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link FieldErrorMap}
	 */
	public static FieldErrorMap fieldErrorMapIgnoreUsernameBlank(final BindingResult result) {
		final FieldErrorMap fieldErrorMap = new FieldErrorMap();
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
	 * @return {@link FieldErrorMap}
	 */
	public static FieldErrorMap fieldErrorMapIgnoreUsernameAndRoleBlank(final BindingResult result) {
		final FieldErrorMap fieldErrorMap = new FieldErrorMap();
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

	/**
	 * Merge errors properly
	 *
	 * @param maps
	 */
	public void merge(final FieldErrorMap... maps) {
		for (final FieldErrorMap map : maps) {
			for (final Entry<String, List<FieldError>> field : map.entrySet()) {
				final String fieldName = field.getKey();
				final List<FieldError> error = field.getValue();
				if (!this.containsKey(fieldName)) {
					this.put(fieldName, error);
				} else {
					this.get(fieldName).addAll(error);
				}
			}
		}
	}
}
