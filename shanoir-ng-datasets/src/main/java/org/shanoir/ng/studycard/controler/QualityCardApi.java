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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "qualitycards", description = "the qualitycard API")
@RequestMapping("/qualitycards")
public interface QualityCardApi {

	@ApiOperation(value = "", notes = "Deletes a quality card", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "quality card deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no quality card found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{qualityCardId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteQualityCard(
			@ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the quality card corresponding to the given id", response = QualityCard.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found quality card", response = QualityCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = QualityCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = QualityCard.class),
			@ApiResponse(code = 404, message = "no quality card found", response = QualityCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = QualityCard.class) })
	@RequestMapping(value = "/{qualityCardId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudyId(), 'CAN_SEE_ALL')")
	ResponseEntity<QualityCard> findQualityCardById(
			@ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId);
	
	@ApiOperation(value = "", notes = "If exists, returns the quality cards corresponding to the given study id", response = QualityCard.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found quality cards", response = QualityCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = QualityCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = QualityCard.class),
			@ApiResponse(code = 404, message = "no quality card found", response = QualityCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = QualityCard.class) })
	@RequestMapping(value = "/byStudy/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<QualityCard>> findQualityCardByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);
	
	@ApiOperation(value = "", notes = "Returns all the quality Cards", response = QualityCard.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found quality cards", response = QualityCard.class),
			@ApiResponse(code = 204, message = "no quality card found", response = QualityCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = QualityCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = QualityCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = QualityCard.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<QualityCard>> findQualityCards();

	@ApiOperation(value = "", notes = "Saves a new quality card", response = QualityCard.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created quality card", response = QualityCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = QualityCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = QualityCard.class),
			@ApiResponse(code = 422, message = "bad parameters", response = QualityCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = QualityCard.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#qualityCard.getStudyId(), 'CAN_ADMINISTRATE'))")
	ResponseEntity<QualityCard> saveNewQualityCard(
			@ApiParam(value = "Quality Card to create", required = true) @RequestBody QualityCard QualityCard,
			final BindingResult result) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Updates a quality card", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "quality card updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{qualityCardId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') or ( hasRole('EXPERT') and #qualityCardId == #qualityCard.getId() and @datasetSecurityService.hasUpdateRightOnQualityCard(#qualityCard, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> updateQualityCard(
			@ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId,
			@ApiParam(value = "quality card to update", required = true) @RequestBody QualityCard qualityCard,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Apply a quality card on a study for quality control", response = Void.class, tags = {})
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "applied a quality card on its study for quality control", response = Void.class),
		@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
		@ApiResponse(code = 403, message = "forbidden", response = Void.class),
		@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
		@ApiResponse(code = 500, message = "unexpected error", response = Void.class)
	})
	@RequestMapping(value = "/apply/{qualityCardId}", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<QualityCardResult> applyQualityCardOnStudy(
		@ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException, MicroServiceCommunicationException;
	
	@ApiOperation(value = "", notes = "Test a quality card on a study for quality control", response = Void.class, tags = {})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "applied a quality card on its study for quality control", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Void.class)
    })
    @RequestMapping(value = "/test/{qualityCardId}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnQualityCard(#qualityCardId, 'CAN_ADMINISTRATE'))")
    ResponseEntity<QualityCardResult> testQualityCardOnStudy(
        @ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException, MicroServiceCommunicationException;
	
}
