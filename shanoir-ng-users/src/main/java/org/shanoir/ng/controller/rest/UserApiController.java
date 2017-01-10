package org.shanoir.ng.controller.rest;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.configuration.swagger.SwaggerDocumentationConfig;
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
import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.annotations.ApiParam;

@Controller
public class UserApiController implements UserApi {

    private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    private UserService userService;

	@Override
	public ResponseEntity<Void> confirmAccountRequest(Long userId, User user) {
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
            @RequestHeader(value=SwaggerDocumentationConfig.XSRF_TOKEN_NAME) String authToken,
            @ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
        if (userService.findById(userId) == null) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        userService.deleteById(userId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

	@Override
	public ResponseEntity<Void> denyAccountRequest(Long userId) {
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
    public ResponseEntity<User> findUserById(@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> findUsers() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<User>>(users, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> saveNewUser(
            @RequestHeader(value=SwaggerDocumentationConfig.XSRF_TOKEN_NAME) String authToken,
            @ApiParam(value = "the user to create", required = true) @RequestBody @Valid User user,
            BindingResult result) throws RestServiceException {

    	/* Validation */
        FieldErrorMap accessErrors = this.getCreationRightsErrors(user); //A basic user can only update certain fields, check that
        FieldErrorMap hibernateErrors = new FieldErrorMap(result); // Check hibernate validation
        FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user); // Check unique constrainte
        /* Merge errors. */
        FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
        if (!errors.isEmpty()) {
            throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
        }

        user.setId(null); // Guarantees it is a creation, not an update
        user.setCreationDate(new Date()); // Set creation date on creation.

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

    @Override
    public ResponseEntity<Void> updateUser(
            @RequestHeader(value=SwaggerDocumentationConfig.XSRF_TOKEN_NAME) String authToken,
            @ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
            @ApiParam(value = "the user to update", required = true) @RequestBody @Valid User user,
            BindingResult result) throws RestServiceException {

    	user.setId(userId); // IMPORTANT : avoid any confusion that could lead to security breach

    	/* Validation */
        FieldErrorMap accessErrors = this.getUpdateRightsErrors(user); //A basic user can only update certain fields, check that
        FieldErrorMap hibernateErrors = new FieldErrorMap(result); // Check hibernate validation
        FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(user); // Check unique constrainte
        /* Merge errors. */
        FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
        if (!errors.isEmpty()) {
            throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
        }

        /* Update user in db. */
        try {
            userService.update(user);
        } catch (ShanoirUsersException e) {
        	LOG.error("Error while trying to update user " + userId + " : ", e);
        	// TODO: 422 ???
            throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
        }

        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    /*
     * Get access rights errors
     *
     * @param user
     * @return an error map
     */
    private FieldErrorMap getUpdateRightsErrors(User user) {
        User previousStateUser = userService.findById(user.getId());
        FieldErrorMap accessErrors = new EditableOnlyByValidator<User>().validate(previousStateUser, user);
        return accessErrors;
    }

    /*
     * Get access rights errors
     *
     * @param user
     * @return an error map
     */
    private FieldErrorMap getCreationRightsErrors(User user) {
    	return new EditableOnlyByValidator<User>().validate(user);
    }

	/*
	 * Get unique constraint errors
	 *
	 * @param user
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(User user) {
		UniqueValidator<User> uniqueValidator = new UniqueValidator<User>(userService);
		FieldErrorMap uniqueErrors = uniqueValidator.validate(user);
		return uniqueErrors;
	}

}
