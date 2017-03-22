package org.shanoir.ng.shared.controller;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.shanoir.ng.user.User;
import org.shanoir.ng.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for users request API controllers.
 * 
 * @author msimon
 *
 */
public abstract class AbstractUserRequestApiController {

	@Autowired
	private UserService userService;

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

	/*
	 * Get access rights errors
	 *
	 * @param user
	 * 
	 * @return an error map
	 */
	protected FieldErrorMap getUpdateRightsErrors(final User user) {
		final User previousStateUser = userService.findById(user.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<User>().validate(previousStateUser, user);
		return accessErrors;
	}

	/*
	 * Get access rights errors
	 *
	 * @param user
	 * 
	 * @return an error map
	 */
	protected FieldErrorMap getCreationRightsErrors(final User user) {
		return new EditableOnlyByValidator<User>().validate(user);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param user
	 * 
	 * @return an error map
	 */
	protected FieldErrorMap getUniqueConstraintErrors(final User user) {
		final UniqueValidator<User> uniqueValidator = new UniqueValidator<User>(userService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(user);
		return uniqueErrors;
	}

}
