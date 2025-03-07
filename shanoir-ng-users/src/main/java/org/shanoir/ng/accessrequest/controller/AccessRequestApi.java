package org.shanoir.ng.accessrequest.controller;

import java.util.List;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Api for access request, to make a demand on 
 * @author jcome
 *
 */
@Tag(name = "accessrequest")
@RequestMapping("/accessrequest")
public interface AccessRequestApi {

	@Operation(summary = "", description = "Saves a new access request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "created access request"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<AccessRequest> saveNewAccessRequest(
			@Parameter(name = "access request to create", required = true) @RequestBody AccessRequest request,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Resolves a new access request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "resolved access request"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "resolve/{accessRequestId}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> resolveNewAccessRequest(
			@Parameter(name = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId,
			@Parameter(name = "Accept or refuse the request", required = true) @RequestBody boolean validation,
			BindingResult result) throws RestServiceException, AccountNotOnDemandException, EntityNotFoundException, JsonProcessingException, AmqpException;

	@Operation(summary = "byAdmin", description = "Find all the access request managed by the given adminstrator")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "resolved access request"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "byAdmin", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<List<AccessRequest>> findAllByAdminId() throws RestServiceException;
	
	@Operation(summary = "byUser", description = "Find all the access request by the given user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "resolved access request"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "422", description = "bad parameters"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "byUser", produces = { "application/json" }, consumes = {
            "application/json" })
    ResponseEntity<List<AccessRequest>> findAllByUserId() throws RestServiceException;

	@Operation(summary = "byStudy", description = "Find all the access request for the given study")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "resolved access request"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "byStudy/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<List<AccessRequest>> findAllByStudyId(
			@Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId
			) throws RestServiceException;

	@Operation(summary = "get by id", description = "Find the access request for the given id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "resolved access request"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{accessRequestId}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<AccessRequest> getByid(@Parameter(name = "id of the access request to resolve", required = true) @PathVariable("accessRequestId") Long accessRequestId) throws RestServiceException;

	@Operation(summary = "", description = "Invite an user to a study")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User invited, reponse message"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/invitation/")
	ResponseEntity<AccessRequest> inviteUserToStudy(
			@Parameter(name = "Study the user is invited in", required = true) 
				@RequestParam(value = "studyId", required = true) Long studyId,
			@Parameter(name = "Study name the user is invited in", required = true) 
				@RequestParam(value = "studyName", required = true) String studyName,
			@Parameter(name = "Issuer of the invitation", required = true) 
				@RequestParam(value = "issuer", required = false) String issuer,
			@Parameter(name = "The future role of the user in the study he is invited in", required = true) 
				@RequestParam(value = "function", required = false) String function,
			@Parameter(name = "The email or login of the invited user.") 
				@RequestParam(value = "email", required = true) String emailOrLogin) throws RestServiceException, JsonProcessingException, AmqpException;

}
