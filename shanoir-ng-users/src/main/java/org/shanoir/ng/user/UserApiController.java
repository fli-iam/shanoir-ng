package org.shanoir.ng.user;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.shanoir.ng.utils.KeycloakUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserApiController extends AbstractUserRequestApiController implements UserApi {

	private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

	@Override
	public ResponseEntity<Void> confirmAccountRequest(@PathVariable("userId") final Long userId,
			@RequestBody final User user, final BindingResult result) throws RestServiceException {
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
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			getUserService().confirmAccountRequest(userId, user);
		} catch (final ShanoirUsersException e) {
			if (ErrorModelCode.USER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteUser(@PathVariable("userId") final Long userId) {
		if (getUserService().findById(userId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			getUserService().deleteById(userId);
		} catch (final ShanoirUsersException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> denyAccountRequest(@PathVariable("userId") final Long userId)
			throws RestServiceException {
		try {
			getUserService().denyAccountRequest(userId);
		} catch (final ShanoirUsersException e) {
			if (ErrorModelCode.USER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<User> findUserById(@PathVariable("userId") final Long userId) {
		final User user = getUserService().findById(userId);
		if (user == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<User>> findUsers() {
		final List<User> users = getUserService().findAll();
		if (users.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> requestExtension(@RequestBody final ExtensionRequestInfo requestInfo) {
		try {
			getUserService().requestExtension(KeycloakUtils.getTokenUserId(), requestInfo);
		} catch (final ShanoirUsersException e) {
			if (ErrorModelCode.USER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<User> saveNewUser(@RequestBody @Valid final User user, final BindingResult result)
			throws RestServiceException {

		/* Now we generate a username for the new user creation */
		if (user.getUsername() == null && user.getFirstName() != null && user.getLastName() != null) {
			generateUsername(user);
		}

		/* Validation */
		// A basic user can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(user);
		// Check hibernate validation
		/*
		 * Tell Spring to remove the hibernate validation error on username
		 * blank now
		 */
		final FieldErrorMap hibernateErrors = FieldErrorMap.fieldErrorMapIgnoreUsernameBlank(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		user.setId(null);
		// Set creation date on creation.
		user.setCreationDate(new Date());

		/* Save user in db. */
		try {
			final User createdUser = getUserService().save(user);
			return new ResponseEntity<>(createdUser, HttpStatus.OK);
		} catch (final ShanoirUsersException e) {
			if (ErrorModelCode.PASSWORD_NOT_CORRECT == e.getErrorCode()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
						"Password does not match policy", null));
			}
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Void> updateUser(@PathVariable("userId") final Long userId,
			@RequestBody @Valid final User user, final BindingResult result) throws RestServiceException {

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
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update user in db. */
		try {
			getUserService().update(user);
		} catch (final ShanoirUsersException e) {
			LOG.error("Error while trying to update user " + userId + " : ", e);
			if (ErrorModelCode.USER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
