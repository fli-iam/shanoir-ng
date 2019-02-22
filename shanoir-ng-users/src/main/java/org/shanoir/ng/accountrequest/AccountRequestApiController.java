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

package org.shanoir.ng.accountrequest;

import java.time.LocalDate;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.error.UsersFieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.shanoir.ng.shared.exception.UsersErrorModelCode;
import org.shanoir.ng.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class AccountRequestApiController extends AbstractUserRequestApiController implements AccountRequestApi {

	@Override
	public ResponseEntity<User> saveNewAccountRequest(
			@ApiParam(value = "user to create from account request", required = true) @RequestBody final User user,
			final BindingResult result) throws RestServiceException {
		/* Now we generate a username for the new user creation */
		if (user.getUsername() == null && user.getFirstName() != null && user.getLastName() != null) {
			generateUsername(user);
		}

		/* Validation */
		// Check hibernate validation
		/*
		 * Tell Spring to remove the hibernante validation error on username or
		 * role blank for user account request
		 */
		final FieldErrorMap hibernateErrors = UsersFieldErrorMap.fieldErrorMapIgnoreUsernameAndRoleBlank(result);
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
		user.setCreationDate(LocalDate.now());

		/* Save user in db. */
		User savedUser = null;
		try {
			savedUser = getUserService().save(user);
		} catch (final ShanoirUsersException e) {
			if (UsersErrorModelCode.PASSWORD_NOT_CORRECT == e.getErrorCode()) {
				throw new RestServiceException(new ErrorModel(422, "Password does not match policy", null));
			}
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}

		return new ResponseEntity<User>(savedUser, HttpStatus.OK);
	}

}
