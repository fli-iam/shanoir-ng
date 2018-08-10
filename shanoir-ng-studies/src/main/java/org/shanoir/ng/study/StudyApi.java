package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.studyuser.StudyUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T10:35:29.288Z")

@Api(value = "studies", description = "the studies API")
@RequestMapping("/studies")
public interface StudyApi {

	@ApiOperation(value = "", notes = "Adds a member to a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "member added", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyId}/members", produces = { "application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> addMember(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "relation between the study and an user", required = true) @RequestBody StudyUser studyUser);

	@ApiOperation(value = "", notes = "Deletes a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "Find all members (study_user) of a study", response = StudyUser.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found members", response = StudyUser.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{studyId}/members", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<StudyUser>> findMembers(@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);
	
	@ApiOperation(value = "", notes = "If exists, returns the studies that the user is allowed to see", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found studies", response = StudyDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Study.class),
			@ApiResponse(code = 403, message = "forbidden", response = Study.class),
			@ApiResponse(code = 404, message = "no study found", response = Study.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<StudyDTO>> findStudies();

	@ApiOperation(value = "", notes = "Returns id and name for all the studies", response = IdNameDTO.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies", response = IdNameDTO.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no study found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/names", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<IdNameDTO>> findStudiesNames();

	@ApiOperation(value = "", notes = "If exists, returns the studies with theirs study cards that the user is allowed to see", response = SimpleStudyDTO.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies with studycards", response = SimpleStudyDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = SimpleStudyDTO.class),
			@ApiResponse(code = 403, message = "forbidden", response = SimpleStudyDTO.class),
			@ApiResponse(code = 404, message = "no study found", response = SimpleStudyDTO.class),
			@ApiResponse(code = 500, message = "unexpected error", response = SimpleStudyDTO.class) })
	@RequestMapping(value = "/listwithcards", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<List<SimpleStudyDTO>> findStudiesWithStudyCardsByUserAndEquipment(
			@ApiParam(value = "equipment", required = true) @RequestBody EquipmentDicom equipment, BindingResult result)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the study corresponding to the given id", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found study", response = Study.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Study.class),
			@ApiResponse(code = 403, message = "forbidden", response = Study.class),
			@ApiResponse(code = 404, message = "no study found", response = Study.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<StudyDTO> findStudyById(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "Removes a membre from a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "member removed", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyId}/member/{memberId}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> removeMember(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "id of the member", required = true) @PathVariable("memberId") Long userId);

	@ApiOperation(value = "", notes = "Saves a new study", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created study", response = Study.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Study.class),
			@ApiResponse(code = 403, message = "forbidden", response = Study.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Study.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<StudyDTO> saveNewStudy(
			@ApiParam(value = "study to create", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> updateStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "study to update", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;

}
