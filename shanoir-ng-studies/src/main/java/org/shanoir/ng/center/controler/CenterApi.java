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

package org.shanoir.ng.center.controler;

import java.util.List;

import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
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

@Tag(name = "center", description = "the center API")
@RequestMapping("/centers")
public interface CenterApi {

	@Operation(summary = "", description = "Deletes a center")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "center deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no center found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{centerId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteCenter(
			@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId)
			throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the center corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found center"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no center found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{centerId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<CenterDTO> findCenterById(
			@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId);
	
	@Operation(summary = "", description = "If exists, returns the center corresponding to the given InstitutionDicom or creates a new center")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found center"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no center found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byDicom/{studyId}", produces = { "application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_IMPORT')")
	ResponseEntity<CenterDTO> findCenterOrCreateByInstitutionDicom(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(description = "institution dicom to find or create a center", required = true)
			@RequestBody InstitutionDicom institutionDicom, BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Returns all the centers")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found centers"),
			@ApiResponse(responseCode = "204", description = "no center found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<CenterDTO>> findCenters();

	@Operation(summary = "", description = "Returns the centers associated to a study")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found centers"),
			@ApiResponse(responseCode = "204", description = "no center found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/study/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<List<CenterDTO>> findCentersByStudy (
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Returns id and name for all the centers")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found centers"),
			@ApiResponse(responseCode = "204", description = "no center found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/names", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<IdName>> findCentersNames();
	
	@Operation(summary = "", description = "Returns id and name for all the centers")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found centers"),
			@ApiResponse(responseCode = "204", description = "no center found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/names/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<IdName>> findCentersNames(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@Operation(summary = "", description = "Saves a new center")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created center"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<CenterDTO> saveNewCenter(
			@Parameter(description = "center to create", required = true) @RequestBody Center center, BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "Updates a center")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "center updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{centerId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controllerSecurityService.idMatches(#centerId, #center)")
	ResponseEntity<Void> updateCenter(
			@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId,
			@Parameter(description = "center to update", required = true) @RequestBody Center center, BindingResult result)
			throws RestServiceException;

}
