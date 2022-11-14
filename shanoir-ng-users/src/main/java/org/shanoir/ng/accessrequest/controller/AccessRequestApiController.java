package org.shanoir.ng.accessrequest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

/**
 * Api for access request, to make a demand on 
 * @author jcome
 *
 */
@Service
public class AccessRequestApiController implements AccessRequestApi {

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	AccessRequestService accessRequestService;

	@Autowired
	EmailService emailService;

	@Autowired
	UserService userService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ObjectMapper mapper;

	private static final Logger LOG = LoggerFactory.getLogger(AccessRequestApiController.class);
	
	public ResponseEntity<AccessRequest> saveNewAccessRequest(
			@ApiParam(value = "uaccess request to create", required = true) @RequestBody AccessRequest request,
			BindingResult result) throws RestServiceException {
		// Create a new access request
		User user = userService.findById(KeycloakUtil.getTokenUserId());
		request.setUser(user);
		request.setStatus(AccessRequest.ON_DEMAND);
		AccessRequest createdRequest = accessRequestService.createAllowed(request);
		createdRequest.setUser(user);
		
		// Send event
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.ACCESS_REQUEST_EVENT, "" + createdRequest.getId(), KeycloakUtil.getTokenUserId(), "", 1, createdRequest.getStudyId()));
		
		// Send notification to study admin
		try {
			emailService.notifyStudyManagerAccessRequest(createdRequest);
		} catch (ShanoirException e) {
			throw new RestServiceException(e, new ErrorModel(ErrorModelCode.BAD_REQUEST));
		}

		return new ResponseEntity<AccessRequest>(createdRequest, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<AccessRequest>> findAllByUserId() throws RestServiceException {
		// Get all studies I administrate
		List<Long> studiesId;
		try {
			studiesId = (List<Long>) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, KeycloakUtil.getTokenUserId());
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException("Error while retrieving studies I can admin. Please contact an admin.", e);			
		}
		if (CollectionUtils.isEmpty(studiesId)) {
			return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
		}
		// Get all access requests
		List<AccessRequest> accessRequests = this.accessRequestService.findByStudyId(studiesId);
		
		List<AccessRequest> unresolvedRequests = new ArrayList<AccessRequest>();
		// Filter by status
		for (AccessRequest request : accessRequests) {
			if (AccessRequest.ON_DEMAND == request.getStatus()) {
				unresolvedRequests.add(request);
			}
		}

		if (CollectionUtils.isEmpty(unresolvedRequests)) {
			return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<List<AccessRequest>>(unresolvedRequests, HttpStatus.OK);
	}

	public ResponseEntity<Void> resolveNewAccessRequest(
			@ApiParam(value = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId,
			@ApiParam(value = "Accept or refuse the request", required = true) @RequestBody boolean validation,
			BindingResult result) throws RestServiceException, AccountNotOnDemandException, EntityNotFoundException {
		AccessRequest resolvedRequest = accessRequestService.findById(accessRequestId).get();
		if (resolvedRequest == null) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
		if (validation) {
			resolvedRequest.setStatus(AccessRequest.APPROVED);
		} else {
			resolvedRequest.setStatus(AccessRequest.REFUSED);
		}
		try {
			accessRequestService.update(resolvedRequest);
		} catch (EntityNotFoundException e) {
			LOG.error("Could not resolve access request, please try later.", e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (validation) {
			// Update study to add a new user
			ShanoirEvent subscription = new ShanoirEvent(
					ShanoirEventType.USER_ADD_TO_STUDY_EVENT,
					resolvedRequest.getStudyId().toString(),
					resolvedRequest.getUser().getId(),
					resolvedRequest.getUser().getUsername(),
					ShanoirEvent.SUCCESS);
			
			try {
				this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE, mapper.writeValueAsString(subscription));
			} catch (JsonProcessingException | AmqpException e) {
				LOG.error("Could not subscribe user to study.", e);
			}

			// If the user is not currently enabled, enable it.
			if(resolvedRequest.getUser().isAccountRequestDemand()) {
				userService.confirmAccountRequest(resolvedRequest.getUser());
			}
			StudyInvitationEmail email = new StudyInvitationEmail();
			email.setInvitedMail(resolvedRequest.getUser().getEmail());
			email.setStudyId(resolvedRequest.getStudyId().toString());
			email.setStudyName(resolvedRequest.getStudyName());
			emailService.notifyUserAddedToStudy(resolvedRequest);
		} else {
			emailService.notifyUserRefusedFromStudy(resolvedRequest);	
		}

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<AccessRequest> getByid(@ApiParam(value = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId) throws RestServiceException {
		AccessRequest acceReq = this.accessRequestService.findById(accessRequestId).get();
		return new ResponseEntity<AccessRequest>(acceReq, HttpStatus.OK);
	}

	public 	ResponseEntity<String> inviteUserToStudy(
			@ApiParam(value = "Study the user is invited in", required = true) 
			@RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "Study name the user is invited in", required = true) 
			@RequestParam(value = "studyName", required = true) String studyName,
			@ApiParam(value = "The email of the invited user.") 
    		@RequestParam(value = "email", required = true) String email) throws RestServiceException {
		
		// Check if user with such email exists
		Optional<User> user = this.userService.findByEmail(email);
		
		// User exists => directly add it to the study
		if (user.isPresent()) {
			// Direct call to study MS to add the user
			
			ShanoirEvent subscription = new ShanoirEvent(
					ShanoirEventType.USER_ADD_TO_STUDY_EVENT,
					studyId.toString(),
					user.get().getId(),
					user.get().getUsername(),
					ShanoirEvent.SUCCESS);
			
			boolean subResult = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE, subscription);
			
			if (subResult) {
				// create a new access request for history / logging purpose
				AccessRequest request = new AccessRequest();
				request.setUser(user.get());
				request.setStudyId(studyId);
				request.setStudyName(studyName);
				request.setStudyId(studyId);
				request.setMotivation("From study manager");
				request.setStatus(AccessRequest.APPROVED);
				this.accessRequestService.createAllowed(request);
				this.emailService.notifyUserAddedToStudy(request);
			}
			
			return new ResponseEntity<String>("User " + user.get().getUsername() + " was added to the study with success", HttpStatus.OK);
		}

		StudyInvitationEmail mail = new StudyInvitationEmail();
		mail.setInvitedMail(email);
		mail.setStudyId(studyId.toString());
		mail.setStudyName(studyName);
		
		// User does not exists, just send an email
		this.emailService.inviteToStudy(mail);
		
		return new ResponseEntity<String>("No existing user with this email, an invitation to join Shanoir was sent.", HttpStatus.OK);
	}
}
