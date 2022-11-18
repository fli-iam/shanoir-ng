package org.shanoir.ng.accessrequest.controller;

import java.util.List;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Api for access request, to make a demand on 
 * @author jcome
 *
 */
@Api(value = "accessrequest")
@RequestMapping("/accessrequest")
public interface AccessRequestApi {

	@ApiOperation(value = "", notes = "Saves a new access request", response = AccessRequest.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "created access request", response = AccessRequest.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AccessRequest.class),
			@ApiResponse(code = 403, message = "forbidden", response = AccessRequest.class),
			@ApiResponse(code = 422, message = "bad parameters", response = AccessRequest.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AccessRequest.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<AccessRequest> saveNewAccessRequest(
			@ApiParam(value = "access request to create", required = true) @RequestBody AccessRequest request,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Resolves a new access request", response = AccessRequest.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "resolved access request", response = AccessRequest.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AccessRequest.class),
			@ApiResponse(code = 403, message = "forbidden", response = AccessRequest.class),
			@ApiResponse(code = 422, message = "bad parameters", response = AccessRequest.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AccessRequest.class) })
	@PutMapping(value = "resolve/{accessRequestId}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> resolveNewAccessRequest(
			@ApiParam(value = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId,
			@ApiParam(value = "Accept or refuse the request", required = true) @RequestBody boolean validation,
			BindingResult result) throws RestServiceException, AccountNotOnDemandException, EntityNotFoundException;

	@ApiOperation(value = "byUser", notes = "Find all the access request for the given user", response = AccessRequest.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "resolved access request", response = AccessRequest.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AccessRequest.class),
			@ApiResponse(code = 403, message = "forbidden", response = AccessRequest.class),
			@ApiResponse(code = 422, message = "bad parameters", response = AccessRequest.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AccessRequest.class) })
	@GetMapping(value = "byUser", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<List<AccessRequest>> findAllByUserId() throws RestServiceException;

	@ApiOperation(value = "get by id", notes = "Find the access request for the given id", response = AccessRequest.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "resolved access request", response = AccessRequest.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AccessRequest.class),
			@ApiResponse(code = 403, message = "forbidden", response = AccessRequest.class),
			@ApiResponse(code = 422, message = "bad parameters", response = AccessRequest.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AccessRequest.class) })
	//TODO: @PostAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(returnObject.getBody().getStudyId(), 'CAN_ADMINISTRATE')")
	@GetMapping(value = "/{accessRequestId}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<AccessRequest> getByid(@ApiParam(value = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId) throws RestServiceException;

	@ApiOperation(value = "", notes = "Invite an user to a study", response = AccessRequest.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "User invited, reponse message", response = String.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@PutMapping(value = "/invitation/")
	ResponseEntity<String> inviteUserToStudy(
			@ApiParam(value = "Study the user is invited in", required = true) 
			@RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "Study name the user is invited in", required = true) 
			@RequestParam(value = "studyName", required = true) String studyName,
			@ApiParam(value = "The email of the invited user.") 
    		@RequestParam(value = "email", required = true) String email) throws RestServiceException;
}
