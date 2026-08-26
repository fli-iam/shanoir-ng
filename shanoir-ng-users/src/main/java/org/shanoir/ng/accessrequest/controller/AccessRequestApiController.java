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

package org.shanoir.ng.accessrequest.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.accessrequest.model.ValidationDTO;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.email.StudyInvitationEmail;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Parameter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Api for access request, to make a demand on
 * @author jcome
 *
 */
@Controller
public class AccessRequestApiController implements AccessRequestApi {

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private AccessRequestService accessRequestService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private StudyUserRightsRepository studyUserRightsRepository;

    @Autowired
    private StudyRightsService studyRightsService;

    @Value("${shanoir.userDefaultExpirationDays:183}")
    private int accessRequestExpirationDays;

    private static final Logger LOG = LoggerFactory.getLogger(AccessRequestApiController.class);

    public ResponseEntity<AccessRequest> saveNewAccessRequest(
            AccessRequest request,
            BindingResult result) throws RestServiceException {
        // Create a new access request
        User user = userService.findById(KeycloakUtil.getTokenUserId());
        request.setUser(user);
        request.setStatus(AccessRequest.ON_DEMAND);
        request.setExpirationDate(null); // IMPORTANT : this prevents a user from setting an expiration date for his access request,
        // which would be a security issue. Also the expiration date should be set at the time of validation, not at the time of request.

        // Sanity check: user already has a pending access request
        List<AccessRequest> accessRequests = this.accessRequestService.findByUserIdAndStudyId(user.getId(), request.getStudyId());
        if (!CollectionUtils.isEmpty(accessRequests)) {
            boolean alreadyExists = false;
            for (AccessRequest req : accessRequests) {
                if (AccessRequest.ON_DEMAND == req.getStatus()) {
                    alreadyExists = true;
                }
            }
            if (alreadyExists) {
                throw new RestServiceException(new ErrorModel(HttpStatus.BAD_REQUEST.value(), "You already have a pending access request on this study."));
            }
        }

        if (StringUtils.isEmpty(request.getStudyName())) {
            String studyName = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_QUEUE, request.getStudyId());
            request.setStudyName(studyName);
        }

        AccessRequest createdRequest = accessRequestService.createAllowed(request);
        createdRequest.setUser(user);

        // Send event
        eventService.publishEvent(new ShanoirEvent(
                ShanoirEventType.ACCESS_REQUEST_EVENT,
                "",
                KeycloakUtil.getTokenUserId(),
                "New access request from " + user.getUsername(),
                1,
                createdRequest.getStudyId()));

        // Send notification to study admin
        emailService.notifyStudyManagerAccessRequest(createdRequest);

        return new ResponseEntity<AccessRequest>(createdRequest, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AccessRequest>> findAllByUserId() throws RestServiceException {
        // Get all access requests
        List<AccessRequest> accessRequests = this.accessRequestService.findByUserId(KeycloakUtil.getTokenUserId());

        if (CollectionUtils.isEmpty(accessRequests)) {
            return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<AccessRequest>>(accessRequests, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AccessRequest>> findAllByAdminId() throws RestServiceException {
        // Get all studies I administrate

        List<Long> studiesId;
        if (KeycloakUtil.isAdmin()) {
            studiesId = Utils.toList(this.studyUserRightsRepository.findAll())
                .stream()
                .filter(StudyUser::canAccessStudy)
                .map(StudyUser::getStudyId)
                .collect(Collectors.toList());
        } else {
            studiesId = (List<Long>) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, KeycloakUtil.getTokenUserId());
        }

        if (CollectionUtils.isEmpty(studiesId)) {
            return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
        }

        // Get all access requests
        List<AccessRequest> accessRequests = this.accessRequestService.findByStudyIdAndStatus(studiesId, AccessRequest.ON_DEMAND);
        accessRequests.addAll(this.accessRequestService.findByStudyIdAndStatus(studiesId, AccessRequest.ON_EXTENSION_DEMAND));
        for (AccessRequest accessRequest : accessRequests) {
            if (accessRequest.getExpirationDate() == null) {
                // If it's an account request, set the expiration date to the asked expiration date
                if (Boolean.TRUE.equals(accessRequest.getUser().isAccountRequestDemand())
                        && accessRequest.getUser().getAccountRequestInfo() != null) {
                    accessRequest.setExpirationDate(accessRequest.getUser().getAccountRequestInfo().getStudyExpirationDate());
                } else {
                    // pre-fetch expiration date so the 6 months or so starts at validation time, not at request time
                    LocalDate expiration = LocalDate.now().plusDays(accessRequestExpirationDays);
                    accessRequest.setExpirationDate(expiration);
                }
            }
        }

        if (CollectionUtils.isEmpty(accessRequests)) {
            return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<AccessRequest>>(accessRequests, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> resolveNewAccessRequest(
            @Parameter(name = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId,
            @Parameter(name = "Accept or refuse the request", required = true) @RequestBody ValidationDTO validation,
            BindingResult result) throws RestServiceException, AccountNotOnDemandException, EntityNotFoundException, JacksonException, AmqpException {
        AccessRequest resolvedRequest = accessRequestService.findById(accessRequestId).orElse(null);
        int status = resolvedRequest != null ? resolvedRequest.getStatus() : -1;
        if (resolvedRequest == null || (status != AccessRequest.ON_DEMAND && status != AccessRequest.ON_EXTENSION_DEMAND)) {
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }
        if (validation.isAccept()) {
            if (validation.getExpiration() != null) {
                resolvedRequest.setExpirationDate(validation.getExpiration());
            } else if (resolvedRequest.getExpirationDate() == null) {
                LocalDate expiration = LocalDate.now().plusDays(accessRequestExpirationDays);
                resolvedRequest.setExpirationDate(expiration);
            }
            UserAccessData userAccessData = new UserAccessData(resolvedRequest.getUser().getUsername(), resolvedRequest.getExpirationDate());
            String serializedUserAccessData = mapper.writeValueAsString(userAccessData);
            ShanoirEvent subscription;
            if (status == AccessRequest.ON_DEMAND) {
                resolvedRequest.setStatus(AccessRequest.APPROVED);
                accessRequestService.update(resolvedRequest);
                // if there is an account request, accept it.
                if (resolvedRequest.getUser().isAccountRequestDemand() != null && resolvedRequest.getUser().isAccountRequestDemand()) {
                    // set the date accordingly to the access request expiration date
                    resolvedRequest.getUser().setExpirationDate(resolvedRequest.getExpirationDate());
                    this.userService.confirmAccountRequest(resolvedRequest.getUser());
                }
                // Update study to add a new user
                subscription = new ShanoirEvent(
                        ShanoirEventType.USER_ADD_TO_STUDY_EVENT,
                        resolvedRequest.getStudyId().toString(),
                        resolvedRequest.getUser().getId(),
                        serializedUserAccessData,
                        ShanoirEvent.SUCCESS,
                        resolvedRequest.getStudyId());

            } else if (status == AccessRequest.ON_EXTENSION_DEMAND) {
                resolvedRequest.setStatus(AccessRequest.APPROVED);
                accessRequestService.update(resolvedRequest);
                // Update study to extend a user access
                subscription = new ShanoirEvent(
                        ShanoirEventType.USER_EXTEND_TO_STUDY_EVENT,
                        resolvedRequest.getStudyId().toString(),
                        resolvedRequest.getUser().getId(),
                        serializedUserAccessData,
                        ShanoirEvent.SUCCESS,
                        resolvedRequest.getStudyId());
            } else {
                throw new IllegalStateException("Access request status is not valid for this operation: " + status);
            }
            try {
                this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE, mapper.writeValueAsString(subscription));
                // successfully sent to rabbitmq, send emails
                if (status == AccessRequest.ON_EXTENSION_DEMAND) {
                    sendEmailsForExtension(resolvedRequest);
                }
            } catch (AmqpRejectAndDontRequeueException e) {
                throw new RestServiceException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Could not transmit study-user update info through RabbitMQ", e));
            }

        } else {
            resolvedRequest.setStatus(AccessRequest.REFUSED);
            accessRequestService.update(resolvedRequest);
            if (status == AccessRequest.ON_EXTENSION_DEMAND) {
                emailService.notifyUserAccessRequestExtensionRefused(resolvedRequest.getUser(), new IdName(resolvedRequest.getStudyId(), resolvedRequest.getStudyName()));
            } else if (status == AccessRequest.ON_DEMAND) {
                emailService.notifyUserRefusedFromStudy(resolvedRequest);
            }
            // Deny account request creation
            if (resolvedRequest.getUser().isAccountRequestDemand()) {
                userService.denyAccountRequest(resolvedRequest.getUser().getId());
            }
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    private void sendEmailsForExtension(AccessRequest resolvedRequest) {
        User user = resolvedRequest.getUser();
        String studyName = resolvedRequest.getStudyName();
        if (StringUtils.isEmpty(studyName)) {
            studyName = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_QUEUE, resolvedRequest.getStudyId());
        }
        IdName study = new IdName(resolvedRequest.getStudyId(), studyName);
        LocalDate extensionDate = resolvedRequest.getExpirationDate();
        emailService.notifyAdminsAccessRequestExtensionGranted(user, study, extensionDate);
        emailService.notifyUserAccessRequestExtensionGranted(user, study, extensionDate);
    }

    @Override
    public ResponseEntity<Void> requestExtension(
            @Parameter(name = "id of the study to extend", required = true) @RequestParam("studyId") Long studyId,
            @Parameter(name = "new extension date", required = true) @RequestParam("extensionDate") LocalDate extensionDate) throws RestServiceException {
        StudyUser studyUser = studyUserRightsRepository.findByUserIdAndStudyId(KeycloakUtil.getTokenUserId(), studyId);
        if (studyUser == null) {
            throw new RestServiceException(new ErrorModel(HttpStatus.FORBIDDEN.value(), "You are not allowed to request an extension for this study."));
        } else if (extensionDate == null || extensionDate.isBefore(LocalDate.now())) {
            throw new RestServiceException(new ErrorModel(HttpStatus.BAD_REQUEST.value(), "The extension date must be in the future."));
        } else if (studyUser.getExpirationDate() != null && extensionDate.isBefore(studyUser.getExpirationDate())) {
            throw new RestServiceException(new ErrorModel(HttpStatus.BAD_REQUEST.value(), "The extension date must be after the current expiration date."));
        } else {
            accessRequestService.requestExtension(studyId, KeycloakUtil.getTokenUserId(), extensionDate);
            String studyName = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_QUEUE, studyId);
            emailService.notifyAdminsAccessRequestExtensionRequest(new IdName(KeycloakUtil.getTokenUserId(), KeycloakUtil.getTokenUserName()), new IdName(studyId, studyName), extensionDate);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
    }

    public ResponseEntity<AccessRequest> getById(@Parameter(name = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId) throws RestServiceException {
        AccessRequest acceReq = this.accessRequestService.findById(accessRequestId).get();
        // pre-fetch expiration date so the 6 months or so starts at validation time, not at request time
        if (acceReq.getExpirationDate() == null) {
            LocalDate expiration = LocalDate.now().plusDays(accessRequestExpirationDays);
            acceReq.setExpirationDate(expiration);
        }
        return new ResponseEntity<AccessRequest>(acceReq, HttpStatus.OK);
    }

    public ResponseEntity<AccessRequest> inviteUserToStudy(
            Long studyId,
            String studyName,
            String issuer,
            String function,
            String emailOrLogin) throws RestServiceException, JacksonException, AmqpException {
        boolean isEmail = emailOrLogin.contains("@");
        User user;

        if (isEmail) {
            // Check if user with such email/username exists
            user = this.userService.findByEmail(emailOrLogin).orElse(null);
        } else {
            user = this.userService.findByUsernameForInvitation(emailOrLogin).orElse(null);
        }

        if (user != null) {
            // Update study to add a new user
            ShanoirEvent subscription = new ShanoirEvent(
                    ShanoirEventType.USER_ADD_TO_STUDY_EVENT,
                    String.valueOf(studyId),
                    KeycloakUtil.getTokenUserId(),
                    "Invite and add user " + user.getUsername(),
                    ShanoirEvent.SUCCESS,
                    studyId);
            eventService.publishEvent(subscription);

            // User exists => return an access request to be added
            // create a new access request to return
            AccessRequest request = new AccessRequest();
            request.setUser(user);
            request.setStudyId(studyId);
            request.setStudyName(studyName);
            request.setMotivation("From study manager");
            request.setStatus(AccessRequest.APPROVED);
            return new ResponseEntity<AccessRequest>(request, HttpStatus.OK);
        } else {
            // Otherwise, send a mail to the new user if we have a mail in entry
            if (isEmail) {
                StudyInvitationEmail mail = new StudyInvitationEmail();
                mail.setInvitedMail(emailOrLogin);
                mail.setStudyId(studyId);
                mail.setStudyName(studyName);
                mail.setInvitationIssuer(issuer);
                mail.setFunction(function);
                this.emailService.inviteToStudy(mail);
                return new ResponseEntity<AccessRequest>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<AccessRequest>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    public ResponseEntity<List<AccessRequest>> findAllByStudyId(
            Long studyId) throws RestServiceException {
        return new ResponseEntity<List<AccessRequest>>(this.accessRequestService.findByStudyIdAndStatus(Collections.singletonList(studyId), AccessRequest.ON_DEMAND), HttpStatus.OK);
    }

}
