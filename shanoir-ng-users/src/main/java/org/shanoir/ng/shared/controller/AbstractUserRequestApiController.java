package org.shanoir.ng.shared.controller;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.error.UsersFieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.security.UserFieldEditionSecurityManager;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.service.UserUniqueConstraintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

/**
 * Abstract class for users request API controllers.
 * 
 * @author msimon
 *
 */
public abstract class AbstractUserRequestApiController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserFieldEditionSecurityManager fieldEditionSecurityManager;
	
	@Autowired
	private UserUniqueConstraintManager uniqueConstraintManager;

	/**
	 * @return the userService
	 */
	protected UserService getUserService() {
		return userService;
	}

	/*
	 * Generate username of a user from first name and last name.
	 * 
	 * @param user user.
	 */
	protected void generateUsername(final User user) {
		final StringBuilder usernameSb = new StringBuilder();

		final String firstnames = user.getFirstName().trim();
		for (final String firstname : firstnames.split("\\s+")) {
			for (String f : firstname.split("-")) {
				usernameSb.append(f.substring(0, 1));
			}
		}

		final String lastnames = user.getLastName().trim();
		for (final String lastname : lastnames.split("\\s+")) {
			for (String l : lastname.split("-")) {
				usernameSb.append(l);
			}
		}

		// Username in lower case without accent
		String usernameAsked = StringUtils.stripAccents(usernameSb.toString().toLowerCase());
		String username = usernameAsked;

		int i = 1;
		while (userService.findByUsername(username).isPresent()) {
			username = usernameAsked + i;
			i++;
		}

		user.setUsername(username);
	}
	
	protected void validate(User user, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap()
				.add(fieldEditionSecurityManager.validate(user))
				.add(new FieldErrorMap(result))
				.add(uniqueConstraintManager.validate(user));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
	
	protected void validateIgnoreBlankUsername(User user, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new UsersFieldErrorMap()
				.checkBindingIgnoreBlankUsername(result)
				.add(fieldEditionSecurityManager.validate(user))
				.add(uniqueConstraintManager.validate(user));
		if (!errors.isEmpty()) {
			throw new RestServiceException(
				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}	
	}

}
