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

import javax.annotation.Resource;
import javax.validation.Valid;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
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

@Api(value = "dataset", description = "the dataset API")
@RequestMapping("/datasets")
public interface DatasetApi {

	@ApiOperation(value = "", notes = "Deletes a dataset", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no dataset found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{datasetId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the dataset corresponding to the given id", response = Dataset.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found dataset", response = Dataset.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{datasetId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_SEE_ALL'))")
	ResponseEntity<DatasetDTO> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId);

	@ApiOperation(value = "", notes = "Updates a dataset", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{datasetId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
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
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<DatasetDTO>> findDatasets(Pageable pageable) throws RestServiceException;

    @ApiOperation(value = "", nickname = "downloadDatasetById", notes = "If exists, returns a zip file of the dataset corresponding to the given id", response = Resource.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @RequestMapping(value = "/download/{datasetId}", produces = { "application/zip" }, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_DOWNLOAD'))")
    ResponseEntity<ByteArrayResource> downloadDatasetById(
    		@ApiParam(value = "id of the dataset", required=true) @PathVariable("datasetId") Long datasetId,
    		@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", 
    			allowableValues = "dcm, nii", defaultValue = "dcm") @Valid 
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format) throws RestServiceException, MalformedURLException, IOException;
    
    @ApiOperation(value = "", nickname = "exportBIDSBySubjectId", notes = "If exists, returns a zip file of the BIDS structure corresponding to the given subject id", response = Resource.class, tags={})
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @RequestMapping(value = "/exportBIDS/subjectId/{subjectId}/subjectName/{subjectName}/studyName/{studyName}",
        produces = { "application/zip" }, 
        method = RequestMethod.GET)
    ResponseEntity<ByteArrayResource> exportBIDSBySubjectId(
    		@ApiParam(value = "id of the subject", required=true) @PathVariable("subjectId") Long subjectId,
    		@ApiParam(value = "name of the subject", required=true) @PathVariable("subjectName") String subjectName,
    		@ApiParam(value = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, MalformedURLException, IOException;
	
}
