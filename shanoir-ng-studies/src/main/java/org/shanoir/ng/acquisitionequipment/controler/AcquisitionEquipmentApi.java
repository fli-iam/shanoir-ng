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
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "acquisitionequipment", description = "the acquisitionequipment API")
@RequestMapping("/acquisitionequipments")
public interface AcquisitionEquipmentApi {

	@Operation(summary = "", description = "Deletes an acquisition equipment")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "acquisition equipment deleted"),
			@ApiResponse(responseCode = "404", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{acquisitionEquipmentId}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteAcquisitionEquipment(
			@Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") Long acquisitionEquipmentId);

	@Operation(summary = "", description = "If exists, returns the acquisition equipment corresponding to the given id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found acquisition equipment"),
			@ApiResponse(responseCode = "204", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{acquisitionEquipmentId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<AcquisitionEquipmentDTO> findAcquisitionEquipmentById(
			@Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") Long acquisitionEquipmentId);

	@Operation(summary = "", description = "If exists, returns the acquisition equipments corresponding to the given serial number")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found acquisition equipment"),
			@ApiResponse(responseCode = "204", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/bySerialNumber/{serialNumber}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsBySerialNumber(
			@Parameter(description = "serial number of the acquisition equipment", required = true) @PathVariable("serialNumber") String serialNumber);

	@Operation(summary = "", description = "If exists, returns the acquisition equipment(s) corresponding to the equipment dicom or creates a new one")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found/created acquisition equipment(s)"),
			@ApiResponse(responseCode = "204", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byDicom/{centerId}", produces = { "application/json" }, consumes = {"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsOrCreateOneByEquipmentDicom(
			@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId,
			@Parameter(description = "equipment dicom to find or create an equipment", required = true) @RequestBody EquipmentDicom equipmentDicom,
			BindingResult result);

	@Operation(summary = "", description = "Returns all the acquisition equipments for a center")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found acquisition equipments"),
			@ApiResponse(responseCode = "204", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byCenter/{centerId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsByCenter(@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId);
	
	@Operation(summary = "", description = "Returns all the acquisition equipments for a study")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found acquisition equipments"),
			@ApiResponse(responseCode = "204", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byStudy/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsByStudy(@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);
	
	@Operation(summary = "", description = "Returns all the acquisition equipments")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found acquisition equipments"),
			@ApiResponse(responseCode = "204", description = "no acquisition equipment found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipments();

	@Operation(summary = "", description = "Saves a new acquisition equipment")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "created acquisition equipment"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<AcquisitionEquipmentDTO> saveNewAcquisitionEquipment(
			@Parameter(description = "acquisition equipment to create", required = true) @RequestBody AcquisitionEquipment acquisitionEquipment,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Updates a acquisition equipment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "acquisition equipment updated"),
			@ApiResponse(responseCode = "204", description = "acquisition equipment not found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{acquisitionEquipmentId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @controlerSecurityService.idMatches(#acquisitionEquipmentId, #acquisitionEquipment)")
	ResponseEntity<Void> updateAcquisitionEquipment(
			@Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") Long acquisitionEquipmentId,
			@Parameter(description = "acquisition equipment to update", required = true) @RequestBody AcquisitionEquipment acquisitionEquipment,
			final BindingResult result) throws RestServiceException;

}
