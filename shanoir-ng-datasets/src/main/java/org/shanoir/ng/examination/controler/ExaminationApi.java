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

package org.shanoir.ng.examination.controler;

import java.io.IOException;
import java.util.List;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.shared.exception.RestServiceException;
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
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "examination")
@RequestMapping("/examinations")
public interface ExaminationApi {

	@Operation(summary = "", description = "Deletes an examination")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "examination deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no examination found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/{examinationId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteExamination(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId);

	@Operation(summary = "", description = "If exists, returns the examination corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found examination"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no examination found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{examinationId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL')")
	ResponseEntity<ExaminationDTO> findExaminationById(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId)
			throws RestServiceException;

	@Operation(summary = "", description = "Returns all the examinations")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found examinations"),
			@ApiResponse(responseCode = "204", description = "no examination found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<ExaminationDTO>> findExaminations(Pageable pageable, String searchStr, String searchField);

	@Operation(summary = "", description = "Returns all the examinations")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found examinations"),
			@ApiResponse(responseCode = "204", description = "no examination found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/preclinical/{isPreclinical}", produces = { "application/json" })
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<ExaminationDTO>> findPreclinicalExaminations(
			@Parameter(description = "preclinical", required = true) @PathVariable("isPreclinical") Boolean isPreclinical, Pageable pageable);

	@Operation(summary = "", description = "Returns the list of examinations by subject id and study id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found examinations"),
			@ApiResponse(responseCode = "204", description = "no examination found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/subject/{subjectId}/study/{studyId}", produces = { "application/json" })
	ResponseEntity<List<SubjectExaminationDTO>> findExaminationsBySubjectIdStudyId(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Returns the list of examinations by study id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found examinations"),
			@ApiResponse(responseCode = "204", description = "no examination found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/study/{studyId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<Long>> findExaminationsByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Returns the list of examinations by subject id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found examinations"),
			@ApiResponse(responseCode = "204", description = "no examination found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/subjectid/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<ExaminationDTO>> findExaminationsBySubjectId(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	// Attention: this method is used by ShanoirUploader!!!
	@Operation(summary = "", description = "Saves a new examination")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created examination"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = {"", "/"}, produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudyCenter(#examinationDTO.getCenterId(), #examinationDTO.getStudyId(), 'CAN_IMPORT'))")
	ResponseEntity<ExaminationDTO> saveNewExamination(
			@Parameter(description = "examination to create", required = true) @Valid @RequestBody ExaminationDTO examinationDTO,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Updates an examination")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "examination updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{examinationId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and #examination.getId().equals(#examinationId) and @datasetSecurityService.hasRightOnExamination(#examination.getId(), 'CAN_IMPORT'))")
	ResponseEntity<Void> updateExamination(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@Parameter(description = "examination to update", required = true) @Valid @RequestBody ExaminationDTO examination,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Add extra data to an examination")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "examination updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "extra-data-upload/{examinationId}",
	produces = { "application/json" },
    consumes = { "multipart/form-data" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_IMPORT'))")
	ResponseEntity<Void> addExtraData(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException;

	@Operation(summary = "", description = "Create an examination and add extra data")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "examination created and data added"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "extra-data-upload/new-exam/subject/{subjectName}/center/{centerId}",
	produces = { "application/json" },
    consumes = { "multipart/form-data" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnSubjectName(#subjectName, 'CAN_IMPORT'))")
	ResponseEntity<Void> createExaminationAndAddExtraData(
			@Parameter(description = "name of the subject", required = true) @PathVariable("subjectName") String subjectName,
			@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId,
			@Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException;
	
	@Operation(summary = "", description = "Download extra data from an examination", tags = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "examination updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "extra-data-download/{examinationId}/{fileName:.+}/")
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	void downloadExtraData(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@Parameter(description = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException;

}
