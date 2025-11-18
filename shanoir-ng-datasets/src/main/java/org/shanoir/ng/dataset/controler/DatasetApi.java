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

package org.shanoir.ng.dataset.controler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetDownloadData;
import org.shanoir.ng.dataset.dto.DatasetDownloadDataInput;
import org.shanoir.ng.dataset.dto.DatasetLight;
import org.shanoir.ng.dataset.dto.DatasetWithDependenciesDTOInterface;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "dataset")
@RequestMapping("/datasets")
public interface DatasetApi {

	@Operation(summary = "", description = "Deletes a dataset")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dataset deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/{datasetId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteDataset(
			@Parameter(description = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
            throws RestServiceException, EntityNotFoundException;

	@Operation(summary = "", description = "Deletes several datasets")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "datasets deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/delete", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnEveryDataset(#datasetIds, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteDatasets(
			@Parameter(description = "ids of the datasets", required=true) @Valid
    		@RequestBody(required = true) List<Long> datasetIds)
			throws RestServiceException;

	@Operation(summary = "", description = "Deletes nifti files from a study")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Niftis deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping(value = "/deleteNiftis", produces = { "application/json" })
	ResponseEntity<Void> deleteNiftisFromStudy(
			@Parameter(description = "Id of the study from which we want to delete the niftis", required=true) @Valid
			@RequestBody(required = true) long studyId);

	@Operation(summary = "", description = "If exists, returns the dataset corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found dataset"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{datasetId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_SEE_ALL'))")
	ResponseEntity<DatasetWithDependenciesDTOInterface> findDatasetById(
			@Parameter(description = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId);

	@Operation(summary = "", description = "Updates a dataset")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dataset updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{datasetId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("@controllerSecurityService.idMatches(#datasetId, #dataset) and hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasUpdateRightOnDataset(#dataset, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> updateDataset(
			@Parameter(description = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@Parameter(description = "dataset to update", required = true) @Valid @RequestBody Dataset dataset,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Returns a datasets page")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<DatasetDTO>> findDatasets(Pageable pageable) throws RestServiceException;

	@Operation(summary = "", description = "Returns a dataset list")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/examination/{examinationId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and  @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<DatasetDTO>> findDatasetsByExaminationId(@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
																 @Parameter(description = "return output datasets too") @RequestParam(value = "output", required = false, defaultValue = "false") Boolean output);


	@Operation(summary = "", description = "Returns a dataset list")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/acquisition/{acquisitionId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#acquisitionId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<DatasetDTO>> findDatasetsByAcquisitionId(@Parameter(description = "id of the acquisition", required = true) @PathVariable("acquisitionId") Long acquisitionId);

	@Operation(summary = "", description = "Returns a dataset list")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/studycard/{studycardId}", produces = { "application/json" })
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<DatasetDTO>> findDatasetsByStudycardId(@Parameter(description = "id of the studycard", required = true) @PathVariable("studycardId") Long studycardId);

	@Operation(summary = "", description = "Returns the list of dataset id by study id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no dataset found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/study/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<DatasetLight>> findDatasetByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Returns the number of datasets by study id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no dataset found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/study/nb-datasets/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<Integer> findNbDatasetByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Returns the list of dataset id by subject id and study id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no dataset found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/subject/{subjectId}/study/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<Long>> findDatasetIdsBySubjectIdStudyId(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Returns the list of dataset by subject id and study id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found datasets"),
			@ApiResponse(responseCode = "204", description = "no dataset found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "find/subject/{subjectId}/study/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<DatasetDTO>> findDatasetsBySubjectIdStudyId(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

    @Operation(summary = "downloadDatasetById", description = "If exists, returns a zip file of the dataset corresponding to the given id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "zip file"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/download/{datasetId}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_DOWNLOAD'))")
    void downloadDatasetById(
    		@Parameter(description = "id of the dataset", required=true) @PathVariable("datasetId") Long datasetId,
    		@Parameter(description = "Dowloading nifti, decide the nifti converter id") Long converterId,
    		@Parameter(description = "Decide if you want to download dicom (dcm) or nifti (nii) files.")
    		@Valid @RequestParam(value = "format", required = false, defaultValue="dcm") String format,
    		HttpServletResponse response) throws RestServiceException, MalformedURLException, IOException, EntityNotFoundException;


    @Operation(summary = "getDicomMetadataByDatasetId", description = "If exists, returns the dataset dicom metadata corresponding to the given id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "dicom metadata"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/dicom-metadata/{datasetId}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_DOWNLOAD'))")
    ResponseEntity<String> getDicomMetadataByDatasetId(
    		@Parameter(description = "id of the dataset", required=true) @PathVariable("datasetId") Long datasetId) throws MalformedURLException, IOException, MessagingException;

	@Operation(summary = "", description = "Creates a processed dataset")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "created Processed Dataset"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "422", description = "bad parameters"),
		@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/processedDataset",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
	ResponseEntity<Void> createProcessedDataset(@Parameter(description = "co to create" ,required=true )  @Valid @RequestBody ProcessedDatasetImportJob importJob) throws RestServiceException, IOException, Exception;

    @Operation(summary = "massiveDownloadDatasetsByIds", description = "If exists, returns a zip file of the datasets corresponding to the given ids")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "zip file"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/massiveDownload")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnEveryDataset(#datasetIds, 'CAN_DOWNLOAD'))")
    void massiveDownloadByDatasetIds(
    		@Parameter(description = "ids of the datasets", required=true) @Valid
    		@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
    		@Parameter(description = "Decide if you want to download dicom (dcm) or nifti (nii) files.") @Valid
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format,
			@Parameter(description = "If nifti, decide converter to use") @Valid
			@RequestParam(value = "converterId", required = false) Long converterId,
			HttpServletResponse response) throws RestServiceException, EntityNotFoundException, MalformedURLException, IOException;

    @Operation(summary = "massiveDownloadDatasetsByStudyId", description = "If exists, returns a zip file of the datasets corresponding to the given study ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "zip file"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/massiveDownloadByStudy")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_DOWNLOAD'))")
    void massiveDownloadByStudyId(
    		@Parameter(description = "id of the study", required=true) @Valid
    		@RequestParam(value = "studyId", required = true) Long studyId,
    		@Parameter(description = "Decide if you want to download dicom (dcm) or nifti (nii) files.") @Valid
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, IOException;

    @Operation(summary = "massiveDownloadDatasetsByExaminationId", description = "If exists, returns a zip file of the datasets corresponding to the given examination ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "zip file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/massiveDownloadByExamination")
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_DOWNLOAD'))")
	void massiveDownloadByExaminationId(
			@Parameter(description = "id of the examination", required=true) @Valid
			@RequestParam(value = "examinationId", required = true) Long examinationId,
			@Parameter(description = "Decide if you want to download dicom (dcm) or nifti (nii) files.") @Valid
			@RequestParam(value = "format", required = false, defaultValue="dcm") String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, IOException;

	@Operation(summary = "massiveDownloadDatasetsByAcquisitionId", description = "If exists, returns a zip file of the datasets corresponding to the given acquisition ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "zip file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/massiveDownloadByAcquisition")
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDatasetAcquisition(#acquisitionId, 'CAN_DOWNLOAD'))")
	void massiveDownloadByAcquisitionId(
			@Parameter(description = "id of the acquisition", required=true) @Valid
			@RequestParam(value = "acquisitionId", required = true) Long acquisitionId,
			@Parameter(description = "Decide if you want to download dicom (dcm) or nifti (nii) files.") @Valid
			@RequestParam(value = "format", required = false, defaultValue="dcm") String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, IOException;

	@Operation(summary = "downloadStatistics", description = "Download statistics from the entire database")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "exported statistics"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "404", description = "no dataset found"),
		@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/downloadStatistics")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<String> downloadStatistics(
			@Parameter(description = "Study name including regular expression", required=false) @Valid
			@RequestParam(value = "studyNameInRegExp", required = false) String studyNameInRegExp,
			@Parameter(description = "Study name excluding regular expression", required=false) @Valid
			@RequestParam(value = "studyNameOutRegExp", required = false) String studyNameOutRegExp,
			@Parameter(description = "Subject name including regular expression", required=false) @Valid
			@RequestParam(value = "subjectNameInRegExp", required = false) String subjectNameInRegExp,
			@Parameter(description = "Subject name excluding regular expression", required=false) @Valid
			@RequestParam(value = "subjectNameOutRegExp", required = false) String subjectNameOutRegExp) throws RestServiceException, IOException, InterruptedException;

	@Operation(summary = "downloadStatistics", description = "Download statistics for event")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "download ok"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "event not found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/download/event/{eventId}", produces = { "application/zip" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<ByteArrayResource> downloadStatisticsByEventId(
			@Parameter(description = "id of the event", required = true) @PathVariable("eventId") String eventId)
			throws RestServiceException, IOException;


	@Operation(summary = "", description = "If exists, returns the datasets corresponding to the given ids")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found dataset"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "/allById", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnEveryDataset(#datasetIds, 'CAN_SEE_ALL'))")
	ResponseEntity<List<DatasetLight>> findDatasetsByIds(
			@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds);


	@Operation(summary = "", description = "Get data before downloading acquisitions and examinations")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "empty response"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "422", description = "bad parameters"),
		@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/getDownloadData",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetDownloadData>> getDownloadData(@Parameter(description = "Input arguments", required = true) @Valid @RequestBody DatasetDownloadDataInput input);

}
