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
