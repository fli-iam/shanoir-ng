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

import javax.validation.Valid;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "examination")
@RequestMapping("/examinations")
public interface ExaminationApi {

	@ApiOperation(value = "", notes = "Deletes an examination", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "examination deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no examination found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@DeleteMapping(value = "/{examinationId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the examination corresponding to the given id", response = Examination.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found examination", response = Examination.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no examination found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{examinationId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudy().getId(), 'CAN_SEE_ALL')")
	ResponseEntity<ExaminationDTO> findExaminationById(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Returns all the examinations", response = Examination.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<ExaminationDTO>> findExaminations(Pageable pageable);

	@ApiOperation(value = "", notes = "Returns all the examinations", response = Examination.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/preclinical/{isPreclinical}", produces = { "application/json" })
	ResponseEntity<Page<ExaminationDTO>> findPreclinicalExaminations(
			@ApiParam(value = "preclinical", required = true) @PathVariable("isPreclinical") Boolean isPreclinical, Pageable pageable);

	@ApiOperation(value = "", notes = "Returns the list of examinations by subject id and study id", response = Examination.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/subject/{subjectId}/study/{studyId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<SubjectExaminationDTO>> findExaminationsBySubjectIdStudyId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "Returns the list of examinations by subject id", response = Examination.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/subjectid/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<ExaminationDTO>> findExaminationsBySubjectId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@ApiOperation(value = "", notes = "Saves a new examination", response = Examination.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created examination", response = Examination.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#examinationDTO.getStudy().getId(), 'CAN_IMPORT'))")
	ResponseEntity<ExaminationDTO> saveNewExamination(
			@ApiParam(value = "examination to create", required = true) @Valid @RequestBody ExaminationDTO examinationDTO,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates an examination", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "examination updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PutMapping(value = "/{examinationId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> updateExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@ApiParam(value = "examination to update", required = true) @Valid @RequestBody ExaminationDTO examination,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the bruker archive of the examination corresponding to the given id", response = Resource.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found examination", response = Examination.class),
	        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
	        @ApiResponse(code = 401, message = "unauthorized"),
	        @ApiResponse(code = 403, message = "forbidden"),
	        @ApiResponse(code = 404, message = "no dataset found"),
	        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/preclinical/examinationId/{examinationId}/export",
		produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<ByteArrayResource> exportExaminationById(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId)
			throws RestServiceException, IOException;

}
