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
import java.util.Map;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.StudyLightDTO;
import org.shanoir.ng.study.dto.StudyStatisticsDTO;
import org.shanoir.ng.study.dto.StudyStorageVolumeDTO;
import org.shanoir.ng.study.dua.DataUserAgreement;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@Tag(name = "studies", description = "the studies API")
@RequestMapping("/studies")
public interface StudyApi {

	@Operation(summary = "", description = "Deletes a study")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "study deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteStudy(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "If exists, returns the studies that the user is allowed to see")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<StudyDTO>> findStudies();
	
	@Operation(summary = "", description = "If exists, returns the studies that the user is allowed to see")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/light", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<StudyLightDTO>> findStudiesLight();

	@Operation(summary = "", description = "If exists, returns the studies that are publicly available")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/public/data", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<StudyLightDTO>> findPublicStudiesData();

	@Operation(summary = "", description = "Returns the total number of studies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/count", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Long> countAllStudies();

	// @Operation(summary = "", description = "Returns aggregated statistics on all studies")
	// @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found global statistics"),
	// 		@ApiResponse(responseCode = "401", description = "unauthorized"),
	// 		@ApiResponse(responseCode = "403", description = "forbidden"),
	// 		@ApiResponse(responseCode = "404", description = "no study found"),
	// 		@ApiResponse(responseCode = "500", description = "unexpected error") })
	// @RequestMapping(value = "/public/statistics", produces = { "application/json" }, method = RequestMethod.GET)
	// ResponseEntity<PublicStudiesStatisticsDTO> getPublicStatistics();

	@Operation(summary = "", description = "Returns id and name for all the studies")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "204", description = "no study found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/names", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findStudiesNames() throws RestServiceException;

	@Operation(summary = "", description = "Returns id, name and centers for all the studies")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "204", description = "no study found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/namesAndCenters", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdNameCenterStudyDTO>> findStudiesNamesAndCenters() throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the study corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedStudyDTO(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<StudyDTO> findStudyById(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "Fetch detailed storage volume of study")
			@Valid
			@RequestParam(value = "withStorageVolume", required = false, defaultValue="false") boolean withStorageVolume);

	@Operation(summary = "", description = "Saves a new study")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created study"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<StudyDTO> saveNewStudy(
			@Parameter(description = "study to create", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "Copy a list of dataset to a study")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "copy worked"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/copyDatasets", produces = { "application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<String> copyDatasetsToStudy(
			@Parameter(description = "Dataset ids to copy", required = true)
			@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
			@Parameter(description = "Study id to copy in", required = true)
			@RequestParam(value = "studyId", required = true) String studyId,
			@Parameter(description = "center id of datasets", required = true)
			@RequestParam(value = "centerIds", required = true) List<Long> centerIds,
			@Parameter(description = "subject id of datasets", required = true)
			@RequestParam(value = "subjectIds", required = true) List<String> subjectIdStudyId);


	@Operation(summary = "", description = "If exists, returns the sizes of the study files detailed by format corresponding to the given id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Sizes of the study files in bytes by format"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error")})
	@GetMapping(value = "/detailedStorageVolume/{studyId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<StudyStorageVolumeDTO> getDetailedStorageVolume(@PathVariable("studyId") Long studyId) throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the sizes of the study files detailed by format corresponding to the given id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Sizes of the study files in bytes by format"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "/detailedStorageVolume", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.filterVolumesHasRightOnStudies(#studyIds, 'CAN_SEE_ALL'))")
	ResponseEntity<Map<Long, StudyStorageVolumeDTO>> getDetailedStorageVolumeByStudy(
			@Parameter(description = "study ids") @RequestParam List<Long> studyIds
	) throws RestServiceException;


	@Operation(summary = "", description = "Updates a study")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "study updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controllerSecurityService.idMatches(#studyId, #study) and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> updateStudy(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "study to update", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "Get my rights on this study")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "here are your rights"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/rights/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<StudyUserRight>> rights(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException;

	@Operation(summary = "", description = "Get tags from this study")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "here are your tags"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/tags/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasAnyRightOnStudy(#studyId, {'CAN_SEE_ALL', 'CAN_IMPORT', 'CAN_ADMINISTRATE'}))")
	ResponseEntity<List<org.shanoir.ng.tag.model.Tag>> tags(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException;
	
	@Operation(summary = "", description = "Get my rights")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "here are your rights"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/rights/all", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Map<Long, List<StudyUserRight>>> rights() throws RestServiceException;

	@Operation(summary = "", description = "Know if I'm in one study at least with CAN_IMPORT")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = ""),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/hasOneStudy", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Boolean> hasOneStudyToImport() throws RestServiceException;

	@Operation(summary = "", description = "Add protocol file to a study")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "protocol file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "protocol-file-upload/{studyId}", produces = { "application/json" }, consumes = {
			"multipart/form-data" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> uploadProtocolFile(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
			throws RestServiceException;

	@Operation(summary = "", description = "Download protocol file from a study")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "protocol file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "protocol-file-download/{studyId}/{fileName:.+}/")
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_DOWNLOAD'))")
	void downloadProtocolFile(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException;
	
	@Operation(summary = "", description = "If one or more exist, return a list of data user agreements (DUAs) waiting for the given user id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found duas"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no duas found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/dua", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DataUserAgreement>> getDataUserAgreements()
			throws RestServiceException, IOException;
	
	@Operation(summary = "", description = "Updates a data user agreement (DUA)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "dua updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/dua/{duaId}", produces = { "application/json" }, consumes = {"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @studySecurityService.checkUserOnDUA(#duaId)")
	ResponseEntity<Void> acceptDataUserAgreement(
			@Parameter(description = "id of the dua", required = true) @PathVariable("duaId") Long duaId)
			throws RestServiceException, MicroServiceCommunicationException;

	@Operation(summary = "", description = "Returns the data user agreement (DUA) of a specific study")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "dua confirmed"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/dua/study/{studyId}", produces = { "application/json" }, consumes = {"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Boolean> hasDUAByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException, ShanoirException;


	@Operation(summary = "", description = "Add DUA to a study")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "dua uploaded"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "dua-upload/{studyId}", produces = { "application/json" }, consumes = {
			"multipart/form-data" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> uploadDataUserAgreement(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
			throws RestServiceException;

	@Operation(summary = "", description = "Download DUA of a study")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "dua downloaded"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "dua-download/{studyId}/{fileName:.+}/")
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	void downloadDataUserAgreement(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException;

	@Operation(summary = "", description = "Deletes the user of a study")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "user removed from study"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study or user found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "studyUser/{studyId}/{userId}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteStudyUser(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "id of the user", required = true) @PathVariable("userId") Long userId)
			throws IOException;

	@Operation(summary = "", description = "If exists, returns the studies that are publicly available for a given user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/public/connected", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<IdName>> findPublicStudiesConnected();

	@Operation(summary = "", description = "If exists, returns a list of StudyUser corresponding to the given studyId")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "List of study users"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/studyUser/{studyId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER'))")
	ResponseEntity<List<StudyUser>> getStudyUserByStudyId(@PathVariable("studyId") Long studyId);

	@Operation(summary = "getStudyStatistics", description = "Returns study imaging statistics corresponding to the given study id")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "exported statistics"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "404", description = "no study found"),
		@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/statistics/{studyId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('EXPERT')")
	ResponseEntity<List<StudyStatisticsDTO>> getStudyStatistics(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws RestServiceException, IOException;


	@Operation(summary = "", description = "If exists, returns a list of Study corresponding to the given right for current user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "List of study"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/studyUser/right/{right}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER'))")
	ResponseEntity<List<Long>> getStudiesByRightForCurrentUser(
			@Parameter(description = "right requested", required = true) @PathVariable("right") StudyUserRight right) throws RestServiceException;


}