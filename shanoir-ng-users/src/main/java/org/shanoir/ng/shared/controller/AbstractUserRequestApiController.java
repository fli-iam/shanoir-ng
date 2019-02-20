package org.shanoir.ng.shared.controller;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
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

}
