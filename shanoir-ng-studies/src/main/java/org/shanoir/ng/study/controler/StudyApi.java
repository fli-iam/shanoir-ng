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

package org.shanoir.ng.study.controler;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.model.Study;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T10:35:29.288Z")

@Api(value = "studies", description = "the studies API")
@RequestMapping("/studies")
public interface StudyApi {

	@ApiOperation(value = "", notes = "Deletes a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "If exists, returns the studies that the user is allowed to see", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found studies", response = StudyDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Study.class),
			@ApiResponse(code = 403, message = "forbidden", response = Study.class),
			@ApiResponse(code = 404, message = "no study found", response = Study.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterStudyDTOsHasRight(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<StudyDTO>> findStudies();

	@ApiOperation(value = "", notes = "Returns id and name for all the studies", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies", response = IdName.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no study found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/names", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterStudyIdNameDTOsHasRight(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<IdName>> findStudiesNames() throws RestServiceException;

	@ApiOperation(value = "", notes = "Returns id, name and centers for all the studies", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies", response = IdName.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no study found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/namesAndCenters", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterStudyIdNameDTOsHasRight(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<IdNameCenterStudyDTO>> findStudiesNamesAndCenters() throws RestServiceException;
	
	@ApiOperation(value = "", notes = "If exists, returns the study corresponding to the given id", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found study", response = Study.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Study.class),
			@ApiResponse(code = 403, message = "forbidden", response = Study.class),
			@ApiResponse(code = 404, message = "no study found", response = Study.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedStudyDTO(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<StudyDTO> findStudyById(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

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
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#studyId, #study) and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> updateStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "study to update", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Get my rights on this study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "here are your rights", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/rights/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<StudyUserRight>> rights(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Know if I'm in one study at least with CAN_IMPORT", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/hasOneStudy", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Boolean> hasOneStudyToImport() throws RestServiceException;

	@ApiOperation(value = "", notes = "Add protocol file to a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "examination updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "protocol-file-upload/{studyId}",
	produces = { "application/json" },
    consumes = { "multipart/form-data" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> uploadProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException;

	@ApiOperation(value = "", notes = "Download protocol file from a study", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "examination updated"),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "protocol-file-download/{studyId}/{fileName:.+}/")
	@PreAuthorize("hasRole('ADMIN')")
	void downloadProtocolFile(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("studyId") Long examinationId,
			@ApiParam(value = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException;

	@ApiOperation(value = "", notes = "Deletes the protocol file of a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "protocol-file-delete/{studyId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws IOException;

    @ApiOperation(value = "", nickname = "exportBIDSByStudyId", notes = "If exists, returns a zip file of the BIDS structure corresponding to the given study id", response = Resource.class, tags={})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "/exportBIDS/studyId/{studyId}")
    void exportBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId, HttpServletResponse response) throws RestServiceException, IOException;


    @ApiOperation(value = "", nickname = "getBids", notes = "If exists, returns a BIDSElement structure corresponding to the given study id", response = BidsElement.class, tags={})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BidsElement", response = BidsElement.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "/bidsStructure/studyId/{studyId}",
        produces = { "application/json" })
    ResponseEntity<BidsElement> getBIDSStructureByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId) throws RestServiceException, IOException;
}
