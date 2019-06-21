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

package org.shanoir.ng.user.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.ExtensionRequestInfo;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserApiController extends AbstractUserRequestApiController implements UserApi {
	
	@Override
	public ResponseEntity<Void> confirmAccountRequest(@PathVariable("userId") final Long userId,
			@RequestBody final User user, final BindingResult result) throws RestServiceException {
		
		try {
			validate(user, result);

			getUserService().confirmAccountRequest(user);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (AccountNotOnDemandException e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage()));
		}
	}

	
	@Override
	public ResponseEntity<Void> deleteUser(@PathVariable("userId") final Long userId) throws ForbiddenException {
		try {
			getUserService().deleteById(userId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	
	@Override
	public ResponseEntity<Void> denyAccountRequest(@PathVariable("userId") final Long userId) throws RestServiceException {
		try { 
			getUserService().denyAccountRequest(userId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT); 
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (AccountNotOnDemandException e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage()));
		}
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
			getUserService().requestExtension(KeycloakUtil.getTokenUserId(), requestInfo);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	
	@Override
	public ResponseEntity<User> saveNewUser(@RequestBody @Valid final User user, final BindingResult result) throws RestServiceException {
		/* Generate a username. */
		if (user.getUsername() == null && user.getFirstName() != null && user.getLastName() != null) {
			generateUsername(user);
		}
		
		user.setCreationDate(LocalDate.now()); // Set creation date on creation, which is now
		validateIgnoreBlankUsername(user, result);

		/* Save user in db. */
		try {
			User createdUser = getUserService().create(user);
			return new ResponseEntity<>(createdUser, HttpStatus.OK);
		} catch (PasswordPolicyException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while generating the new password"));
		} catch (SecurityException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while registering the user in Keycloak"));
		}

	}

	@Override
	public ResponseEntity<List<IdName>> searchUsers(@RequestBody final IdList userIds) {
		final List<IdName> users = getUserService().findByIds(userIds.getIdList());
		if (users.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateUser(@PathVariable("userId") final Long userId,
			@RequestBody @Valid final User user, final BindingResult result) throws RestServiceException {

		try {
			validate(user, result);
			
			/* Update user in db. */
			getUserService().update(user);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (final EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
