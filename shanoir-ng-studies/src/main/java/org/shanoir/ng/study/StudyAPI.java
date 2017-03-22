package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2017-01-18T15:36:13.002Z")

@Api(value = "study", description = "the study API")
public interface StudyAPI {

	@ApiOperation(value = "", notes = "Returns all studies", response = Study.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "founded studies", response = Study.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no study founded", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class)
			})
	@RequestMapping(value = "/listOfStudies", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('adminRole')")
	ResponseEntity<List<Study>> findStudies();

}
