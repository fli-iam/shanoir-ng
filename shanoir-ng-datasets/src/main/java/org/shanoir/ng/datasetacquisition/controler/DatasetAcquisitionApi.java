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

package org.shanoir.ng.datasetacquisition.controler;

import java.util.List;

import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDatasetsDTO;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.importer.dto.ImportJob;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "datasetacquisition", description = "the datasetacquisition API")
public interface DatasetAcquisitionApi {
	
	@Operation(summary = "", description = "Creates new dataset acquisition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "created Dataset Acquitistion"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "422", description = "bad parameters"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/datasetacquisition",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#importJob.getExaminationId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> createNewDatasetAcquisition(@Parameter(description = "DatasetAcquisition to create" ,required=true ) @Valid @RequestBody ImportJob importJob) throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the dataset acquisitions corresponding to the given study card")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found dataset acquisitions"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "none found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/datasetacquisition/byStudyCard/{studyCardId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<DatasetAcquisitionDatasetsDTO>> findByStudyCard(
			@Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId);
	
	@Operation(summary = "", description = "Deletes a datasetAcquisition")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "datasetAcquisition deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no datasetAcquisition found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/datasetacquisition/{datasetAcquisitionId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#datasetAcquisitionId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteDatasetAcquisition(
			@Parameter(description = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId)
			throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the datasetAcquisition corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasetAcquisition"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no datasetAcquisition found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/datasetacquisition/{datasetAcquisitionId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or returnObject == null or returnObject.getBody() == null or @datasetSecurityService.hasRightOnTrustedExaminationDTO(returnObject.getBody().getExamination(), 'CAN_SEE_ALL')")
	ResponseEntity<DatasetAcquisitionDTO> findDatasetAcquisitionById(
			@Parameter(description = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId);
	
	@Operation(summary = "", description = "If exists, returns the datasetAcquisitions corresponding to the given examination id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasetAcquisition"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no datasetAcquisition found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/datasetacquisition/examination/{examinationId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDatasetAcquisitionDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<ExaminationDatasetAcquisitionDTO>> findDatasetAcquisitionByExaminationId(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId);
		
	@Operation(summary = "", description = "If exists, returns the datasetAcquisitions corresponding to the given dataset ids")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found datasetAcquisition"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no datasetAcquisition found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/datasetacquisition/byDatasetIds", produces = { "application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionDTOList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<DatasetAcquisitionDatasetsDTO>> findDatasetAcquisitionByDatasetIds(
			@Parameter(description = "ids of the datasets", required = true) @RequestBody Long[] datasetIds);
	
	@Operation(summary = "", description = "Returns a dataset acquisitions page")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found dataset acquisitions"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/datasetacquisition", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetAcquisitionDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<Page<DatasetAcquisitionDTO>> findDatasetAcquisitions(Pageable pageable) throws RestServiceException;

	@Operation(summary = "", description = "Updates a datasetAcquisition")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "datasetAcquisition updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/datasetacquisition/{datasetAcquisitionId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #datasetAcquisitionId == #datasetAcquisition.getId() and @datasetSecurityService.hasRightOnExamination(#datasetAcquisition.examination.id, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> updateDatasetAcquisition(
			@Parameter(description = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId,
			@Parameter(description = "datasetAcquisition to update", required = true) @Valid @RequestBody DatasetAcquisitionDTO datasetAcquisition, BindingResult result)
			throws RestServiceException;

}
