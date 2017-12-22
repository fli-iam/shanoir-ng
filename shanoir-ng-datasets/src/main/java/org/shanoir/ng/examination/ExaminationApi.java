package org.shanoir.ng.examination;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "examination", description = "the examination API")
@RequestMapping("/examinations")
public interface ExaminationApi {

	@ApiOperation(value = "", notes = "Deletes an examination", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "examination deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no examination found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{examinationId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId)
			throws ShanoirException;

	@ApiOperation(value = "", notes = "If exists, returns the examination corresponding to the given id", response = Examination.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found examination", response = Examination.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no examination found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{examinationId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<ExaminationDTO> findExaminationById(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId)
			throws ShanoirException;

	@ApiOperation(value = "", notes = "Returns all the examinations", response = Examination.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<ExaminationDTO>> findExaminations();

	@ApiOperation(value = "", notes = "Returns the list of examinations by subject id", response = Examination.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/subjects/{subjectId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<SubjectExaminationDTO>> findExaminationsBySubjectId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@ApiOperation(value = "", notes = "Saves a new examination", response = Examination.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created examination", response = Examination.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Examination> saveNewExamination(
			@ApiParam(value = "examination to create", required = true) @Valid @RequestBody Examination examination,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates an examination", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "examination updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{examinationId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> updateExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@ApiParam(value = "examination to update", required = true) @Valid @RequestBody Examination examination,
			final BindingResult result) throws RestServiceException;

}
