package org.shanoir.ng.accountrequest.controller;

import java.time.LocalDate;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.error.UsersFieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class AccountRequestApiController extends AbstractUserRequestApiController implements AccountRequestApi {

	@Override
	public ResponseEntity<Void> saveNewAccountRequest(
			@ApiParam(value = "user to create from account request", required = true) @RequestBody final User user,
			final BindingResult result) throws RestServiceException {
		
		/* Now we generate a username for the new user creation */
		if (user.getUsername() == null && user.getFirstName() != null && user.getLastName() != null) {
			generateUsername(user);
		}
		
		final FieldErrorMap<User> errors = new UsersFieldErrorMap()
				.checkBindingContraints(result)
				.checkUniqueConstraints(user, getUserService());
		if (!errors.isEmpty()) {
			throw new RestServiceException(
				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		user.setExpirationDate(null);
		user.setCreationDate(LocalDate.now()); // Set creation date on creation.

		/* Save user in db. */
		try {
			getUserService().createAccountRequest(user);
		} catch (PasswordPolicyException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while generating the new password"));
		} catch (SecurityException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while registering the user in Keycloak"));
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
