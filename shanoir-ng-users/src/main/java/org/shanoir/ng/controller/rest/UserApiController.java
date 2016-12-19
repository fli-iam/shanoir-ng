package org.shanoir.ng.controller.rest;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.configuration.swagger.SwaggerDocumentationConfig;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.error.ErrorDetails;
import org.shanoir.ng.model.error.ErrorModel;
import org.shanoir.ng.model.error.FieldError;
import org.shanoir.ng.model.exception.RestServiceException;
import org.shanoir.ng.model.validation.EditableOnlyByValidator;
import org.shanoir.ng.model.validation.UniqueValidator;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

        if (result.hasErrors()) {
            throw Utils.buildValidationException(result);
        }

        user.setId(null); // Guarantees it is a creation, not an update
        user.setCreationDate(new Date()); // Set creation date on creation, seems logical
        try {
            userService.save(user);
        } catch (DataIntegrityViolationException e) {
            UniqueValidator<User> uniqueValidator = new UniqueValidator<User>(userService);
            throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(uniqueValidator.validate(user))));
        }
        final User createdUser = userService.save(user);
        return new ResponseEntity<User>(createdUser, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateUser(
            @RequestHeader(value=SwaggerDocumentationConfig.XSRF_TOKEN_NAME) String authToken,
            @ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
            @ApiParam(value = "the user to update", required = true) @RequestBody @Valid User user,
            BindingResult result) throws RestServiceException {

        /* A basic user can only update certain fields, check that. */
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken ) {
            throw new IllegalStateException("updateUser() must be restricted to connected user but it seems an anonymous user is accessing it");
        }
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User previousStateUser = userService.findById(userId);
        List<FieldError> errors = new EditableOnlyByValidator<User>().validate(connectedUser, previousStateUser, user);
        if (!errors.isEmpty()) {
            throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
        }

        /* Check hibernate validation. */
        if (result.hasErrors()) {
            throw Utils.buildValidationException(result);
        }
        user.setId(userId);

        /* Try to save user in db.
         * If it fails, it may be because of unique constraints. So if it fails we check that, but not before
         * (in order to save one transaction with the database). */
        try {
            userService.save(user);
        } catch (DataIntegrityViolationException e) {
            UniqueValidator<User> uniqueValidator = new UniqueValidator<User>(userService);
            throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(uniqueValidator.validate(user))));
        }

        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

}
