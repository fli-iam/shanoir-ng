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
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "subject")
@RequestMapping("/subjects")
public interface SubjectApi {

	@Operation(summary = "", description = "Deletes a subject")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "subject deleted"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @studySecurityService.hasRightOnSubjectForEveryStudy(#subjectId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteSubject(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@Operation(summary = "", description = "Returns all the subjects")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found subjects"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterSubjectDTOsHasRightInOneStudy(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<SubjectDTO>> findSubjects(
			@Parameter(description = "Include preclinical subject") @Valid
			@RequestParam(value = "preclinical", required = false, defaultValue = "true") boolean preclinical,
			@Parameter(description = "Include non-preclinical subject") @Valid
			@RequestParam(value = "clinical", required = false, defaultValue = "true") boolean clinical);

	@Operation(summary = "", description = "Returns the clinical subjects as Pageable with corresponding name")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found subjects"),
		@ApiResponse(responseCode = "204", description = "no subject found"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/filter", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Page<SubjectDTO>> findClinicalSubjectsPageByName(Pageable page, String name);

	@Operation(summary = "", description = "Returns id and name for all the subjects")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found subjects"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/names", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findAllSubjectsNames();

	@Operation(summary = "", description = "Returns id and name for the given subject ids")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found subjects"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "/names", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findSubjectsNames(@RequestParam(value = "subjectIds", required = true) List<Long> subjectIds);

	@Operation(summary = "", description = "If exists, returns the subject corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found bubject"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectForEveryStudies(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<SubjectDTO> findSubjectById(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	// Attention: this method is used by ShanoirUploader!!!
	@Operation(summary = "", description = "Saves a new subject")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created subject"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnEverySubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	ResponseEntity<SubjectDTO> saveNewSubject(
			@Parameter(description = "subject to create", required = true) @RequestBody Subject subject,
			@Parameter(description = "request param centerId as flag for auto-increment common name", required = false) @RequestParam(required = false) Long centerId,
			final BindingResult result) throws RestServiceException;
	
	// Attention: this method is used by ShanoirUploader!!!
	@Operation(summary = "", description = "Updates a subject")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "subject updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{subjectId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnEverySubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	ResponseEntity<Void> updateSubject(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@Parameter(description = "subject to update", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException, MicroServiceCommunicationException;

	@Operation(summary = "", description = "If exists, returns the subjects of a study")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found subjects"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{studyId}/allSubjects", produces = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<SimpleSubjectDTO>> findSubjectsByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "preclinical", required = false) @RequestParam(value="preclinical", required = false) String preclinical);

	@Operation(summary = "", description = "If exists, returns the subject corresponding to the given identifier")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found subject"),
			@ApiResponse(responseCode = "204", description = "no subject found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/findByIdentifier/{subjectIdentifier}", produces = {"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	// PostAuthorize removed here: only a subject can be returned from studies with correct rights
	ResponseEntity<SubjectDTO> findSubjectByIdentifier(
			@Parameter(description = "identifier of the subject", required = true) @PathVariable("subjectIdentifier") String subjectIdentifier);

}
