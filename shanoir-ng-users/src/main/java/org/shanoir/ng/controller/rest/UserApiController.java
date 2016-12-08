package org.shanoir.ng.controller.rest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.configuration.swagger.SwaggerDocumentationConfig;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.error.ErrorDetails;
import org.shanoir.ng.model.error.ErrorModel;
import org.shanoir.ng.model.error.FormError;
import org.shanoir.ng.model.exception.RestServiceException;
import org.shanoir.ng.model.validation.Unique;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.annotations.ApiParam;

@Controller
public class UserApiController implements UserApi {

	private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

	@Autowired
	private UserService userService;

	public ResponseEntity<Void> deleteUser(
			@RequestHeader(value=SwaggerDocumentationConfig.AUTH_TOKEN_NAME) String authToken,
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
		if (userService.findById(userId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		userService.deleteById(userId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<User> findUserById(@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
		User user = userService.findById(userId);
		if (user == null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	public ResponseEntity<List<User>> findUsers() {
		List<User> users = userService.findAll();
		if (users.isEmpty()) {
			return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}

	public ResponseEntity<User> saveNewUser(
			@RequestHeader(value=SwaggerDocumentationConfig.AUTH_TOKEN_NAME) String authToken,
			@ApiParam(value = "the user to create", required = true) @RequestBody @Valid User user,
			BindingResult result) throws RestServiceException {

		if (result.hasErrors()) {
			throw Utils.buildValidationException(result);
		}

		user.setId(null); // Guarantees it is a creation, not an update
		user.setCreationDate(new Date()); // Set creation date on creation, seems logical
		try {
			userService.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(SecondLevelValidation(user))));
		}
		final User createdUser = userService.save(user);
		return new ResponseEntity<User>(createdUser, HttpStatus.OK);
	}

	public ResponseEntity<Void> updateUser(
			@RequestHeader(value=SwaggerDocumentationConfig.AUTH_TOKEN_NAME) String authToken,
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "the user to update", required = true) @RequestBody @Valid User user,
			BindingResult result) throws RestServiceException {

		if (result.hasErrors()) {
			throw Utils.buildValidationException(result);
		}
		user.setId(userId);
		try {
			userService.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(SecondLevelValidation(user))));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	/**
	 * Validates what can't be done by Spring/Hibernate validation, in particular unique constraints
	 * !!! Calls database !!!
	 *
	 * @param user
	 * @return
	 */
	private List<FormError> SecondLevelValidation(User user) {
		List<FormError> errorList = new ArrayList<FormError>();
		try {
			for (Field field : User.class.getDeclaredFields()) {
				// check @unique
				if (field.isAnnotationPresent(Unique.class)) {
					String getterName = "get"+StringUtils.capitalize(field.getName());
					try {
						Method getter = user.getClass().getMethod(getterName);
						Object value = getter.invoke(user);
						List<User> foundedList = userService.findBy(field.getName(), value);
						// If found users and it is not the same current user
						if (!foundedList.isEmpty() && !(foundedList.size() == 1 && foundedList.get(0).getId().equals(user.getId()))) {
							FormError formError = new FormError(field.getName(), Arrays.asList("unique"));
							errorList.add(formError);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @Unique custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error("Error while checking @Unique custom annotation, you must implement a method named "
								+ getterName + "() for accessing User." + field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @Unique custom annotation", e);
		}
		return errorList;
	}

}
