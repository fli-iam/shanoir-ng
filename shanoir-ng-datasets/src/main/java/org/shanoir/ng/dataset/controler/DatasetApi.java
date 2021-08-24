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

/**
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.shanoir.ng.dataset.controler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.shanoir.ng.dataset.dto.DatasetAndProcessingsDTOInterface;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "dataset")
@RequestMapping("/datasets")
public interface DatasetApi {
	
	@ApiOperation(value = "", notes = "Creates new dataset", response = Void.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "created Datasets", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @PostMapping(value = "new",
        produces = { "application/json" },
        consumes = { "application/json" })
	@PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Dataset> saveNewDataset(
    		@ApiParam(value = "Dataset to create", required=true) @RequestBody Dataset dataset,
    		final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Deletes a dataset", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no dataset found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@DeleteMapping(value = "/{datasetId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
			throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Deletes several datasets", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "datasets deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no dataset found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@DeleteMapping(value = "/delete", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnEveryDataset(#datasetIds, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteDatasets(
			@ApiParam(value = "ids of the datasets", required=true) @Valid
    		@RequestBody(required = true) List<Long> datasetIds)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the dataset corresponding to the given id", response = Dataset.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found dataset", response = Dataset.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{datasetId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_SEE_ALL'))")
	ResponseEntity<DatasetAndProcessingsDTOInterface> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId);

	@ApiOperation(value = "", notes = "Updates a dataset", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PutMapping(value = "/{datasetId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("@controlerSecurityService.idMatches(#datasetId, #dataset) and hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasUpdateRightOnDataset(#dataset, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> updateDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody Dataset dataset,
			BindingResult result) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Returns a datasets page", response = Page.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found datasets", response = Page.class),
			@ApiResponse(code = 204, message = "no user found", response = ErrorModel.class),
			@ApiResponse(code = 401, message = "unauthorized", response = ErrorModel.class),
			@ApiResponse(code = 403, message = "forbidden", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<DatasetDTO>> findDatasets(Pageable pageable) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Returns a dataset list", response = List.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found datasets", response = Page.class),
			@ApiResponse(code = 204, message = "no user found", response = ErrorModel.class),
			@ApiResponse(code = 401, message = "unauthorized", response = ErrorModel.class),
			@ApiResponse(code = 403, message = "forbidden", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/acquisition/{acquisitionId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#acquisitionId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<DatasetDTO>> findDatasetsByAcquisitionId(@ApiParam(value = "id of the acquisition", required = true) @PathVariable("acquisitionId") Long acquisitionId);

	@ApiOperation(value = "", notes = "Returns the list of dataset id by subject id and study id", response = Long.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found datasets", response = Long.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no dataset found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/subject/{subjectId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and  @datasetSecurityService.hasRightOnSubjectForEveryStudy(#subjectId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<Long>> findDatasetIdsBySubjectId(@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@ApiOperation(value = "", notes = "Returns the list of dataset id by subject id and study id", response = Long.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found datasets", response = Long.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no dataset found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/subject/{subjectId}/study/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<Long>> findDatasetIdsBySubjectIdStudyId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

    @ApiOperation(value = "", nickname = "downloadDatasetById", notes = "If exists, returns a zip file of the dataset corresponding to the given id", response = Resource.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "/download/{datasetId}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_DOWNLOAD'))")
    void downloadDatasetById(
    		@ApiParam(value = "id of the dataset", required=true) @PathVariable("datasetId") Long datasetId,
    		@ApiParam(value = "Dowloading nifti, decide the nifti converter id") Long converterId,
    		@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.",
    			allowableValues = "dcm, nii", defaultValue = "dcm")
    		@Valid
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format, HttpServletResponse response) throws RestServiceException, MalformedURLException, IOException;

    @ApiOperation(value = "", nickname = "massiveDownloadDatasetsByIds", notes = "If exists, returns a zip file of the datasets corresponding to the given ids", response = Resource.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @PostMapping(value = "/massiveDownload")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasAtLeastRightOnOneDataset(#datasetIds, 'CAN_DOWNLOAD'))")
    void massiveDownloadByDatasetIds(
    		@ApiParam(value = "ids of the datasets", required=true) @Valid
    		@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
    		@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii", defaultValue = "dcm") @Valid
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, MalformedURLException, IOException;

    @ApiOperation(value = "", nickname = "massiveDownloadDatasetsByStudyId", notes = "If exists, returns a zip file of the datasets corresponding to the given study ID", response = Resource.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "/massiveDownloadByStudy")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_DOWNLOAD'))")
    void massiveDownloadByStudyId(
    		@ApiParam(value = "id of the study", required=true) @Valid
    		@RequestParam(value = "studyId", required = true) Long studyId,
    		@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii", defaultValue = "dcm") @Valid
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, IOException;

	@ApiOperation(value = "", nickname = "downloadStatistics", notes = "Download statistics from the entire database", response = Resource.class, tags={  })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "exported statistics", response = Resource.class),
		@ApiResponse(code = 401, message = "unauthorized"),
		@ApiResponse(code = 403, message = "forbidden"),
		@ApiResponse(code = 404, message = "no dataset found"),
		@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/downloadStatistics", produces = { "application/zip" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<ByteArrayResource> downloadStatistics(
			@ApiParam(value = "Study name including regular expression", required=false) @Valid
			@RequestParam(value = "studyNameInRegExp", required = false) String studyNameInRegExp,
			@ApiParam(value = "Study name excluding regular expression", required=false) @Valid
			@RequestParam(value = "studyNameOutRegExp", required = false) String studyNameOutRegExp,
			@ApiParam(value = "Subject name including regular expression", required=false) @Valid
			@RequestParam(value = "subjectNameInRegExp", required = false) String subjectNameInRegExp,
			@ApiParam(value = "Subject name excluding regular expression", required=false) @Valid
			@RequestParam(value = "subjectNameOutRegExp", required = false) String subjectNameOutRegExp) throws RestServiceException, IOException;
		
}
