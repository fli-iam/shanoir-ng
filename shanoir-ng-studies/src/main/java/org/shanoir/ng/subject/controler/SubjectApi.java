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

package org.shanoir.ng.subject.controler;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "subject")
@RequestMapping("/subjects")
public interface SubjectApi {

	@ApiOperation(value = "", notes = "Deletes a subject", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "subject deleted", response = Void.class),
			@ApiResponse(code = 204, message = "no subject found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@DeleteMapping(value = "/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @studySecurityService.hasRightOnSubjectForEveryStudy(#subjectId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@ApiOperation(value = "", notes = "Returns all the subjects", response = Subject.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found subjects", response = Subject.class),
			@ApiResponse(code = 204, message = "no subject found", response = Subject.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
			@ApiResponse(code = 403, message = "forbidden", response = Subject.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterSubjectDTOsHasRightInOneStudy(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<SubjectDTO>> findSubjects();
	
	@ApiOperation(value = "", notes = "Returns id and name for all the subjects", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found subjects", response = IdName.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no subject found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/names", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasAnyRole('ADMIN', 'EXPERT') or @studySecurityService.filterSubjectIdNamesDTOsHasRightInOneStudy(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<IdName>> findSubjectsNames();

	@ApiOperation(value = "", notes = "If exists, returns the subject corresponding to the given id", response = Subject.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found bubject", response = Subject.class),
			@ApiResponse(code = 204, message = "no subject found", response = Subject.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
			@ApiResponse(code = 403, message = "forbidden", response = Subject.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
	@GetMapping(value = "/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectForOneStudy(returnObject.getBody().getId(), 'CAN_SEE_ALL')")
	ResponseEntity<SubjectDTO> findSubjectById(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@ApiOperation(value = "", notes = "Saves a new subject", response = Subject.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created subject", response = Subject.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
			@ApiResponse(code = 403, message = "forbidden", response = Subject.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Subject.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnEverySubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	ResponseEntity<SubjectDTO> saveNewSubject(
			@ApiParam(value = "subject to create", required = true) @RequestBody Subject subject,
			@ApiParam(value = "request param centerId as flag for auto-increment common name", required = false) @RequestParam(required = false) Long centerId,
			final BindingResult result) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Updates a subject", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "subject updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@PutMapping(value = "/{subjectId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> updateSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "subject to update", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException, MicroServiceCommunicationException;

	@ApiOperation(value = "", notes = "If exists, returns the subjects of a study", response = Subject.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found subjects", response = Subject.class),
			@ApiResponse(code = 204, message = "no subject found", response = Subject.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
			@ApiResponse(code = 403, message = "forbidden", response = Subject.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
	@GetMapping(value = "/{studyId}/allSubjects", produces = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterSimpleSubjectDTOsHasRightInOneStudy(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<SimpleSubjectDTO>> findSubjectsByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "preclinical", required = false) @RequestParam(value="preclinical", required = false) String preclinical);

	@ApiOperation(value = "", notes = "If exists, returns the subject corresponding to the given identifier", response = Subject.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found subject", response = SubjectDTO.class),
			@ApiResponse(code = 204, message = "no subject found", response = SubjectDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = SubjectDTO.class),
			@ApiResponse(code = 403, message = "forbidden", response = SubjectDTO.class),
			@ApiResponse(code = 500, message = "unexpected error", response = SubjectDTO.class) })
	@GetMapping(value = "/findByIdentifier/{subjectIdentifier}", produces = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterSubjectDTOsHasRightInOneStudy(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<SubjectDTO> findSubjectByIdentifier(
			@ApiParam(value = "identifier of the subject", required = true) @PathVariable("subjectIdentifier") String subjectIdentifier);

}
