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
package org.shanoir.ng.datasetacquisition.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "datasetacquisition", description = "the datasetacquisition API")
public interface DatasetAcquisitionApi {
	
	@ApiOperation(value = "", notes = "Creates new dataset acquisition", response = Void.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "created Dataset Acquitistion", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @RequestMapping(value = "/datasetacquisition",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> createNewDatasetAcquisition(@ApiParam(value = "DatasetAcquisition to create" ,required=true )  @Valid @RequestBody ImportJob importJob) throws RestServiceException;

	@ApiOperation(value = "", notes = "Creates new EEG dataset acquisition", response = Void.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "created EEG Dataset Acquitistion", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @RequestMapping(value = "/datasetacquisition_eeg",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> createNewEegDatasetAcquisition(@ApiParam(value = "DatasetAcquisition to create" ,required=true )  @Valid @RequestBody EegImportJob importJob);
	
	@ApiOperation(value = "", notes = "If exists, returns the dataset acquisitions corresponding to the given study card", response = DatasetAcquisition.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found dataset acquisitions", response = DatasetAcquisition.class, responseContainer = "List"),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "none found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/datasetacquisition/byStudyCard/{studyCardId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<DatasetAcquisitionDTO>> findByStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId);
	
	@ApiOperation(value = "", notes = "Deletes a datasetAcquisition", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "datasetAcquisition deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no datasetAcquisition found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/datasetacquisition/{datasetAcquisitionId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#datasetAcquisitionId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteDatasetAcquisition(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the datasetAcquisition corresponding to the given id", response = DatasetAcquisition.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found datasetAcquisition", response = DatasetAcquisition.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no datasetAcquisition found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/datasetacquisition/{datasetAcquisitionId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or returnObject == null or @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getExamination().getStudyId(), 'CAN_SEE_ALL')")
	ResponseEntity<DatasetAcquisitionDTO> findDatasetAcquisitionById(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId);
	
	@ApiOperation(value = "", notes = "Returns a dataset acquisitions page", response = Page.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found dataset acquisitions", response = Page.class),
			@ApiResponse(code = 204, message = "no user found", response = ErrorModel.class),
			@ApiResponse(code = 401, message = "unauthorized", response = ErrorModel.class),
			@ApiResponse(code = 403, message = "forbidden", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/datasetacquisition", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetAcquisitionDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<DatasetAcquisitionDTO>> findDatasetAcquisitions(Pageable pageable) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a datasetAcquisition", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "datasetAcquisition updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/datasetacquisition/{datasetAcquisitionId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #datasetAcquisitionId == #datasetAcquisition.getId() and @datasetSecurityService.hasUpdateRightOnDatasetAcquisition(#datasetAcquisition, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> updateDatasetAcquisition(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId,
			@ApiParam(value = "datasetAcquisition to update", required = true) @Valid @RequestBody DatasetAcquisitionDTO datasetAcquisition, BindingResult result)
			throws RestServiceException;

}
