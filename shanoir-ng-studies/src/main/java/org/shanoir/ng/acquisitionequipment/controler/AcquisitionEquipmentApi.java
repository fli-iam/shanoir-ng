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

package org.shanoir.ng.acquisitionequipment.controler;

import java.util.List;

import org.shanoir.ng.acquisitionequipment.dto.AcquisitionEquipmentDTO;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-04-03T09:59:20.168Z")

@Api(value = "acquisitionequipment", description = "the acquisitionequipment API")
@RequestMapping("/acquisitionequipments")
public interface AcquisitionEquipmentApi {

	@ApiOperation(value = "", notes = "Deletes an acquisition equipment", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "acquisition equipment deleted", response = Void.class),
			@ApiResponse(code = 404, message = "no acquisition equipment found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{acquisitionEquipmentId}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteAcquisitionEquipment(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") Long acquisitionEquipmentId);

	@ApiOperation(value = "", notes = "If exists, returns the acquisition equipment corresponding to the given id", response = AcquisitionEquipment.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found acquisition equipment", response = AcquisitionEquipment.class),
			@ApiResponse(code = 204, message = "no acquisition equipment found", response = AcquisitionEquipment.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AcquisitionEquipment.class),
			@ApiResponse(code = 403, message = "forbidden", response = AcquisitionEquipment.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AcquisitionEquipment.class) })
	@RequestMapping(value = "/{acquisitionEquipmentId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<AcquisitionEquipmentDTO> findAcquisitionEquipmentById(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") Long acquisitionEquipmentId);

	@ApiOperation(value = "", notes = "Returns all the acquisition equipments for a center", response = AcquisitionEquipment.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found acquisition equipments", response = AcquisitionEquipment.class),
			@ApiResponse(code = 204, message = "no acquisition equipment found", response = AcquisitionEquipment.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AcquisitionEquipment.class),
			@ApiResponse(code = 403, message = "forbidden", response = AcquisitionEquipment.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AcquisitionEquipment.class) })
	@RequestMapping(value = "/byCenter/{centerId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipments(@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId);
	
	@ApiOperation(value = "", notes = "Returns all the acquisition equipments", response = AcquisitionEquipment.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found acquisition equipments", response = AcquisitionEquipment.class),
			@ApiResponse(code = 204, message = "no acquisition equipment found", response = AcquisitionEquipment.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AcquisitionEquipment.class),
			@ApiResponse(code = 403, message = "forbidden", response = AcquisitionEquipment.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AcquisitionEquipment.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipments();

	@ApiOperation(value = "", notes = "Saves a new acquisition equipment", response = AcquisitionEquipment.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "created acquisition equipment", response = AcquisitionEquipment.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AcquisitionEquipment.class),
			@ApiResponse(code = 403, message = "forbidden", response = AcquisitionEquipment.class),
			@ApiResponse(code = 422, message = "bad parameters", response = AcquisitionEquipment.class),
			@ApiResponse(code = 500, message = "unexpected error", response = AcquisitionEquipment.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<AcquisitionEquipmentDTO> saveNewAcquisitionEquipment(
			@ApiParam(value = "acquisition equipment to create", required = true) @RequestBody AcquisitionEquipment acquisitionEquipment,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a acquisition equipment", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "acquisition equipment updated", response = Void.class),
			@ApiResponse(code = 204, message = "acquisition equipment not found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{acquisitionEquipmentId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#acquisitionEquipmentId, #acquisitionEquipment)")
	ResponseEntity<Void> updateAcquisitionEquipment(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") Long acquisitionEquipmentId,
			@ApiParam(value = "acquisition equipment to update", required = true) @RequestBody AcquisitionEquipment acquisitionEquipment,
			final BindingResult result) throws RestServiceException;

}
