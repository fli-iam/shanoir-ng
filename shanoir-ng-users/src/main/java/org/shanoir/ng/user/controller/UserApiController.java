package org.shanoir.ng.user.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.dto.IdListDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.error.UsersFieldErrorMap;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.user.model.ExtensionRequestInfo;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.KeycloakUtil;
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
		
		user.setId(userId); // IMPORTANT : this avoid any confusion that could lead to a security breach
		
		// Validation
		final FieldErrorMap<User> errors = new FieldErrorMap<User>()
				.checkFieldAccess(user, getUserService()) 
				.checkBindingContraints(result)
				.checkUniqueConstraints(user, getUserService());
		if (!errors.isEmpty()) {
			throw new RestServiceException(
				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}
		
		try {
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
		
		/* Validation. */
		final FieldErrorMap<User> errors = new UsersFieldErrorMap()
				.checkBindingIgnoreBlankUsername(result)
				.checkFieldAccess(user)
				.checkUniqueConstraints(user, getUserService());
		if (!errors.isEmpty()) {
			throw new RestServiceException(
				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}	
		
		user.setId(null); // Guarantees it is a creation, not an update
		user.setCreationDate(LocalDate.now()); // Set creation date on creation, which is now

		/* Save user in db. */
		User createdUser;
		try {
			createdUser = getUserService().save(user);
		} catch (PasswordPolicyException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while generating the new password"));
		}
		return new ResponseEntity<>(createdUser, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<List<IdNameDTO>> searchUsers(@RequestBody final IdListDTO userIds) {
		final List<IdNameDTO> users = getUserService().findByIds(userIds.getIdList());
		if (users.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateUser(@PathVariable("userId") final Long userId,
			@RequestBody @Valid final User user, final BindingResult result) throws RestServiceException {
		
		user.setId(userId); // IMPORTANT : avoid any confusion that could lead to security breach

		/* Validation. */
		final FieldErrorMap<User> errors = new FieldErrorMap<User>()
				.checkFieldAccess(user, getUserService())
				.checkBindingContraints(result)
				.checkUniqueConstraints(user, getUserService());
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
		
		/* Update user in db. */
		try {
			getUserService().update(user);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (final EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
