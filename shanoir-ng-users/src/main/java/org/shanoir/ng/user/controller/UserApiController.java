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

import org.shanoir.ng.accessrequest.controller.AccessRequestService;
import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;

@Controller
public class UserApiController extends AbstractUserRequestApiController implements UserApi {

    @Value("${vip.enabled}")
    private boolean vipEnabled;

    @Autowired
    private AccessRequestService accessRequestService;

    @Override
    public ResponseEntity<Void> deleteUser(final Long userId) throws ForbiddenException {
        try {
            getUserService().deleteById(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<Void> confirmAccountRequest(final Long userId,
            User user, final BindingResult result) throws RestServiceException {
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
    public ResponseEntity<Void> denyAccountRequest(final Long userId) throws RestServiceException {
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
    public ResponseEntity<User> findUserById(final Long userId) {
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
    public ResponseEntity<List<User>> findAccountRequests() {
        final List<User> users = getUserService().findAccountRequests();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> saveNewUser(final User user, final BindingResult result) throws RestServiceException {
        /* Generate a username. */
        if (user.getUsername() == null && user.getFirstName() != null && user.getLastName() != null) {
            generateUsername(user);
        }

        user.setCreationDate(LocalDate.now()); // Set creation date on creation, which is now
        validateIgnoreBlankUsername(user, result);

        if (user.getAccountRequestInfo() != null
                && (user.getAccountRequestInfo().getStudyId() != null)) {

            if (user.getAccountRequestInfo().getStudyId() != null) {
                // Directly create an access request for the given study
                AccessRequest request = new AccessRequest();
                request.setUser(user);
                request.setStudyId(user.getAccountRequestInfo().getStudyId());
                request.setStudyName(user.getAccountRequestInfo().getStudyName());
                request.setStatus(AccessRequest.ON_DEMAND);
                request.setMotivation("A study admin invited this user to your study, please confirm its access to the study: " + user.getUsername());
                // So that when the user account request is accepted, it directly has access to the data
                accessRequestService.createAllowed(request);
            }
        }

        /* Save user in db. */
        try {
            User createdUser = getUserService().create(user);
            if (vipEnabled) {
                getVipUserService().createVIPAccountRequest(createdUser);
            }
            return new ResponseEntity<>(createdUser, HttpStatus.OK);
        } catch (PasswordPolicyException e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while generating the new password"));
        } catch (SecurityException e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while registering the user in Keycloak"));
        } catch (MicroServiceCommunicationException e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error while communicating with VIP"));
        }
    }

    @Override
    public ResponseEntity<List<IdName>> searchUsers(final IdList userIds) {
        final List<IdName> users = getUserService().findByIds(userIds.getIdList());
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateUser(final Long userId, final User user,
            final BindingResult result) throws RestServiceException {
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
