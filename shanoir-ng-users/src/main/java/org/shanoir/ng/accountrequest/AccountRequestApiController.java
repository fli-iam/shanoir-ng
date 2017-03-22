package org.shanoir.ng.accountrequest;

import java.util.Date;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.shanoir.ng.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class AccountRequestApiController extends AbstractUserRequestApiController implements AccountRequestApi {

	public ResponseEntity<Void> saveNewAccountRequest(
			@ApiParam(value = "user to create from account request", required = true) @RequestBody User user,
			BindingResult result) throws RestServiceException {
		/* Now we generate a username for the new user creation */
		if (user.getUsername() == null) {
			if (user.getFirstName() != null && user.getLastName() != null) {
				generateUsername(user);
			}
		}

		/* Validation */
		// Check hibernate validation
		/*
		 * Tell Spring to remove the hibernante validation error on username or
		 * role blank for user account request
		 */
		final FieldErrorMap hibernateErrors = FieldErrorMap.fieldErrorMapIgnoreUsernameAndRoleBlank(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		user.setId(null);
		user.setRole(null);
		user.setExpirationDate(null);
		// Set creation date on creation.
		user.setCreationDate(new Date());

		/* Save user in db. */
		try {
			getUserService().save(user);
		} catch (ShanoirUsersException e) {
			if (ErrorModelCode.PASSWORD_NOT_CORRECT == e.getErrorCode()) {
				throw new RestServiceException(new ErrorModel(422, "Password does not match policy", null));
			}
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}
		
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

}
