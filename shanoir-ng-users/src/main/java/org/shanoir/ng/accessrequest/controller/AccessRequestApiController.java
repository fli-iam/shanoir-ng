package org.shanoir.ng.accessrequest.controller;

import java.util.List;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(AccessRequestApiController.class);
	
	public ResponseEntity<AccessRequest> saveNewAccessRequest(
			@ApiParam(value = "uaccess request to create", required = true) @RequestBody AccessRequest request,
			BindingResult result) throws RestServiceException {
		// Create a new access request
		User user = userService.findById(KeycloakUtil.getTokenUserId());
		request.setUser(user);
		AccessRequest createdRequest = accessRequestService.create(request);
		
		// Send event
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.ACCESS_REQUEST_EVENT, "" + createdRequest.getId(), KeycloakUtil.getTokenUserId(), "", 1, createdRequest.getStudyId()));
		
		// Send notification to study admin
		// TODO:
		//emailService.notifyStudyManagerAccessRequest(createdRequest);

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

		if (CollectionUtils.isEmpty(accessRequests)) {
			return new ResponseEntity<List<AccessRequest>>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<List<AccessRequest>>(accessRequests, HttpStatus.OK);
	}

	public ResponseEntity<Void> resolveNewAccessRequest(
			@ApiParam(value = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId,
			@ApiParam(value = "Accept or refuse the request", required = true) @RequestBody boolean validation,
			BindingResult result) throws RestServiceException, AccountNotOnDemandException, EntityNotFoundException {
		AccessRequest resolvedRequest = accessRequestService.findById(accessRequestId).get();
		if (resolvedRequest == null) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
		
		resolvedRequest.setStatus(Boolean.valueOf(validation));
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
			eventService.publishEvent(subscription);
			
			// If the user is not currently enabled, enable it.
			if(resolvedRequest.getUser().isAccountRequestDemand()) {
				userService.confirmAccountRequest(resolvedRequest.getUser());
			}
			//emailService.notifyUserAddedToStudy(accessRequestService);	
		} else {
			//emailService.notifyUserRefusedFromStudy(accessRequestService);	
		}

		// Send email to user
		// TODO:
		//emailService.notifyUserAddedToStudy(accessRequestService);	

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<AccessRequest> getByid(@ApiParam(value = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId) throws RestServiceException {
		AccessRequest acceReq = this.accessRequestService.findById(accessRequestId).get();
		return new ResponseEntity<AccessRequest>(acceReq, HttpStatus.OK);
	}
}
