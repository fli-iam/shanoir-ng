package org.shanoir.ng.controller.rest;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.exception.RestServiceException;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorDetails;
import org.shanoir.ng.exception.error.ErrorModel;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.error.FieldErrorMap;
import org.shanoir.ng.model.validation.EditableOnlyByValidator;
import org.shanoir.ng.model.validation.UniqueValidator;
import org.shanoir.ng.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class UserApiController implements UserApi {

	private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

	@Autowired
	private UserService userService;
	
	@Override
	public ResponseEntity<Void> confirmAccountRequest(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") final Long userId,
			@ApiParam(value = "user to update", required = true) @RequestBody final User user,
			final BindingResult result) throws RestServiceException {
		// IMPORTANT : avoid any confusion that could lead to security breach
		user.setId(userId);

		/* Validation */
		// A basic user can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(user);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			userService.confirmAccountRequest(userId, user);
		} catch (ShanoirUsersException e) {
			if (ErrorModelCode.USER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") final Long userId) {
		if (userService.findById(userId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			userService.deleteById(userId);
		} catch (ShanoirUsersException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> denyAccountRequest(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") final Long userId)
			throws RestServiceException {
		try {
			userService.denyAccountRequest(userId);
		} catch (ShanoirUsersException e) {
			if (ErrorModelCode.USER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<User> findUserById(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") final Long userId) {
		final User user = userService.findById(userId);
		if (user == null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<User>> findUsers(KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal) {
		AccessToken token = principal.getKeycloakSecurityContext().getToken();
	       
        LOG.info(token.getId() + " - " + token.getPreferredUsername());
		final List<User> users = userService.findAll();
		if (users.isEmpty()) {
			return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<User> saveNewUser(
			@ApiParam(value = "the user to create", required = true) @RequestBody @Valid final User user,
			final BindingResult result) throws RestServiceException {
		
		/* Now we generate a username for the new user creation */
		if (user.getUsername() == null) {
			if (user.getFirstName() != null && user.getLastName() != null) {
				generateUsername(user);
			}
		}
		
		/* Validation */
		// A basic user can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(user);
		// Check hibernate validation
		/* Tell Spring to remove the hibernante validation error on username blank now */
		final FieldErrorMap hibernateErrors = FieldErrorMap.fieldErrorMapIgnoreUsernameBlank(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		user.setId(null);
		// Set creation date on creation.
		user.setCreationDate(new Date());

		/* Save user in db. */
		try {
			final User createdUser = userService.save(user);
			return new ResponseEntity<User>(createdUser, HttpStatus.OK);
		} catch (ShanoirUsersException e) {
			if (ErrorModelCode.PASSWORD_NOT_CORRECT == e.getErrorCode()) {
				throw new RestServiceException(new ErrorModel(422, "Password does not match policy", null));
			}
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}
	}

	private void generateUsername(User user) {
		String username = "";
		String usernameAsked = "";
		final String firstnames = user.getFirstName().trim();

		for (final String firstname : firstnames.split("\\s+")) {
			for (String f : firstname.split("-")) {
				username = username + f.substring(0, 1);
			}
		}

		final String lastnames = user.getLastName().trim();

		for (final String lastname : lastnames.split("\\s+")) {
			for (String l : lastname.split("-")) {
				username = username + l;
			}
		}
		
		username = username.toLowerCase();
		usernameAsked = username;
		
		int i = 1;
		while (userService.findByUsername(username).isPresent()) {
			username += i;
			i++;
		}
		if (username != usernameAsked) {
			user.setUsername(usernameAsked + (i - 1));
		} else {
			user.setUsername(usernameAsked);
		}
		
	}

	@Override
	public ResponseEntity<Void> updateUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") final Long userId,
			@ApiParam(value = "the user to update", required = true) @RequestBody @Valid final User user,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		user.setId(userId);

		// A basic user can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(user);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update user in db. */
		try {
			userService.update(user);
		} catch (ShanoirUsersException e) {
			LOG.error("Error while trying to update user " + userId + " : ", e);
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors
	 *
	 * @param user
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUpdateRightsErrors(final User user) {
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
	private FieldErrorMap getCreationRightsErrors(final User user) {
		return new EditableOnlyByValidator<User>().validate(user);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param user
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final User user) {
		final UniqueValidator<User> uniqueValidator = new UniqueValidator<User>(userService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(user);
		return uniqueErrors;
	}

}
