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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.StudyInvitationEmail;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final Logger LOG = LoggerFactory.getLogger(AccessRequestApiController.class);

    public ResponseEntity<AccessRequest> saveNewAccessRequest(
            @Parameter(name = "access request to create", required = true) @RequestBody AccessRequest request,
            BindingResult result) throws RestServiceException {
        // Create a new access request
        User user = userService.findById(KeycloakUtil.getTokenUserId());
        request.setUser(user);
        request.setStatus(AccessRequest.ON_DEMAND);

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
            studiesId = Utils.toList(this.studyUserRightsRepository.findAll()).stream().map(StudyUser::getStudyId
            ).collect(Collectors.toList());
        } else {
            studiesId = (List<Long>) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, KeycloakUtil.getTokenUserId());
        }

        if (CollectionUtils.isEmpty(studiesId)) {
            return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
        }

        // Get all access requests
        List<AccessRequest> accessRequests = this.accessRequestService.findByStudyIdAndStatus(studiesId, AccessRequest.ON_DEMAND);

        if (CollectionUtils.isEmpty(accessRequests)) {
            return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<List<AccessRequest>>(accessRequests, HttpStatus.OK);
    }

    public ResponseEntity<Void> resolveNewAccessRequest(
            @Parameter(name = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId,
            @Parameter(name = "Accept or refuse the request", required = true) @RequestBody boolean validation,
            BindingResult result) throws RestServiceException, AccountNotOnDemandException, EntityNotFoundException, JsonProcessingException, AmqpException {
        AccessRequest resolvedRequest = accessRequestService.findById(accessRequestId).orElse(null);
        if (resolvedRequest == null) {
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }
        if (validation) {
            resolvedRequest.setStatus(AccessRequest.APPROVED);
        } else {
            resolvedRequest.setStatus(AccessRequest.REFUSED);
        }

        accessRequestService.update(resolvedRequest);

        if (validation) {
            // if there is an account request, accept it.
            if (resolvedRequest.getUser().isAccountRequestDemand() != null && resolvedRequest.getUser().isAccountRequestDemand()) {
                this.userService.confirmAccountRequest(resolvedRequest.getUser());
            }

            // Update study to add a new user
            ShanoirEvent subscription = new ShanoirEvent(
                    ShanoirEventType.USER_ADD_TO_STUDY_EVENT,
                    resolvedRequest.getStudyId().toString(),
                    resolvedRequest.getUser().getId(),
                    resolvedRequest.getUser().getUsername(),
                    ShanoirEvent.SUCCESS,
                    resolvedRequest.getStudyId());

            this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE, mapper.writeValueAsString(subscription));
        } else {
            emailService.notifyUserRefusedFromStudy(resolvedRequest);
            // Deny account request creation
            if (resolvedRequest.getUser().isAccountRequestDemand()) {
                userService.denyAccountRequest(resolvedRequest.getUser().getId());
            }
        }

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<AccessRequest> getByid(@Parameter(name = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId) throws RestServiceException {
        AccessRequest acceReq = this.accessRequestService.findById(accessRequestId).get();
        return new ResponseEntity<AccessRequest>(acceReq, HttpStatus.OK);
    }

    public     ResponseEntity<AccessRequest> inviteUserToStudy(
            @Parameter(name = "Study the user is invited in", required = true)
                @RequestParam(value = "studyId", required = true) Long studyId,
            @Parameter(name = "Study name the user is invited in", required = true)
                @RequestParam(value = "studyName", required = true) String studyName,
            @Parameter(name = "Issuer of the invitation", required = true)
                @RequestParam(value = "issuer", required = false) String issuer,
            @Parameter(name = "The future function of the user in the study he is invited in", required = true)
                @RequestParam(value = "function", required = false) String function,
            @Parameter(name = "The email or login of the invited user.")
                @RequestParam(value = "email", required = true) String emailOrLogin) throws RestServiceException, JsonProcessingException, AmqpException {

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
                mail.setStudyId(studyId.toString());
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
            @Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws RestServiceException {

        return new ResponseEntity<List<AccessRequest>>(this.accessRequestService.findByStudyIdAndStatus(Collections.singletonList(studyId), AccessRequest.ON_DEMAND), HttpStatus.OK);
    }
}
