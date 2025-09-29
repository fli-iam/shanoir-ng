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

package org.shanoir.ng.studycard.controler;

import java.util.List;

import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
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

@Tag(name = "qualitycards", description = "the qualitycard API")
@RequestMapping("/qualitycards")
public interface QualityCardApi {

	@Operation(summary = "", description = "Deletes a quality card")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "quality card deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no quality card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{qualityCardId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteQualityCard(
			@Parameter(description = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the quality card corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found quality card"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no quality card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{qualityCardId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudyId(), 'CAN_SEE_ALL')")
	ResponseEntity<QualityCard> findQualityCardById(
			@Parameter(description = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId);
	
	@Operation(summary = "", description = "If exists, returns the quality cards corresponding to the given study id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found quality cards"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no quality card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byStudy/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<QualityCard>> findQualityCardByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);
	
	@Operation(summary = "", description = "Returns all the quality Cards")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found quality cards"),
			@ApiResponse(responseCode = "204", description = "no quality card found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<QualityCard>> findQualityCards();

	@Operation(summary = "", description = "Saves a new quality card")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created quality card"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#qualityCard.getStudyId(), 'CAN_ADMINISTRATE'))")
	ResponseEntity<QualityCard> saveNewQualityCard(
			@Parameter(description = "Quality Card to create", required = true) @RequestBody QualityCard QualityCard,
			final BindingResult result) throws RestServiceException;
	
	@Operation(summary = "", description = "Updates a quality card")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "quality card updated"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "422", description = "bad parameters"),
		@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{qualityCardId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and #qualityCardId == #qualityCard.getId() and @datasetSecurityService.hasUpdateRightOnQualityCard(#qualityCard, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> updateQualityCard(
			@Parameter(description = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId,
			@Parameter(description = "quality card to update", required = true) @RequestBody QualityCard qualityCard,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Apply a quality card on a study for quality control")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "applied a quality card on its study for quality control"),
		@ApiResponse(responseCode = "401", description = "unauthorized"),
		@ApiResponse(responseCode = "403", description = "forbidden"),
		@ApiResponse(responseCode = "422", description = "bad parameters"),
		@ApiResponse(responseCode = "500", description = "unexpected error")
	})
	@RequestMapping(value = "/apply/{qualityCardId}", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<QualityCardResult> applyQualityCardOnStudy(
			@Parameter(description = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException, MicroServiceCommunicationException;
	
	@Operation(summary = "", description = "Test a quality card on a study for quality control")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "applied a quality card on its study for quality control"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "422", description = "bad parameters"),
        @ApiResponse(responseCode = "500", description = "unexpected error")
    })
    @RequestMapping(value = "/test/{qualityCardId}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
    ResponseEntity<QualityCardResult> testQualityCardOnStudy(
			@Parameter(description = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException, MicroServiceCommunicationException;

	@Operation(summary = "", description = "Test a quality card on a study for quality control")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "applied a quality card on its study for quality control"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "422", description = "bad parameters"),
        @ApiResponse(responseCode = "500", description = "unexpected error")
    })

    @RequestMapping(value = "/test/{qualityCardId}/{start}/{stop}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
    ResponseEntity<QualityCardResult> testQualityCardOnStudy(
			@Parameter(description = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId,
			@Parameter(description = "examination number start ", required = true) @PathVariable("start") int start,
			@Parameter(description = "examination number stop", required = true) @PathVariable("stop") int stop) throws RestServiceException, MicroServiceCommunicationException;
}