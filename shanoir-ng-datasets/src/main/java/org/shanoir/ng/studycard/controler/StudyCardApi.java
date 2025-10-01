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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.dto.DicomTag;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardApply;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;

@Tag(name = "studycards", description = "the studyCard API")
@RequestMapping("/studycards")
public interface StudyCardApi {
    
	@Operation(summary = "", description = "Deletes a study card")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "study card deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyCardId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudyCard(#studyCardId, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> deleteStudyCard(
			@Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the study card corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study card"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyCardId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudyId(), 'CAN_SEE_ALL')")
	ResponseEntity<StudyCard> findStudyCardById(
			@Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId);
	
	@Operation(summary = "", description = "If exists, returns the study cards corresponding to the given study id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study cards"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byStudy/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<StudyCard>> findStudyCardByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);
		
	@Operation(summary = "", description = "If exists, returns the study cards corresponding to the given equipment id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study cards"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/byAcqEq/{acqEqId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<StudyCard>> findStudyCardByAcqEqId(
			@Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acqEqId") Long acqEqId);
	
	@Operation(summary = "", description = "Returns all the study Cards")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study cards"),
			@ApiResponse(responseCode = "204", description = "no study card found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<StudyCard>> findStudyCards();
	
	@Operation(summary = "", description = "Saves a new study card")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created study card"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyCard.getStudyId(), 'CAN_ADMINISTRATE'))")
	ResponseEntity<StudyCard> saveNewStudyCard(
			@Parameter(description = "study Card to create", required = true) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException;
		
	// Attention: used by ShanoirUploader!
	@Operation(summary = "", description = "If exists, returns searched study cards")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study cards"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study card found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/search", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.filterCardList(returnObject.getBody(), 'CAN_SEE_ALL') )")
	ResponseEntity<List<StudyCard>> searchStudyCards(
			@Parameter(description = "study ids", required = true) @RequestBody IdList studyIds);	
	
	@Operation(summary = "", description = "Updates a study card")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "study card updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyCardId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and #studyCardId == #studyCard.getId() and @datasetSecurityService.hasUpdateRightOnStudyCard(#studyCard, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> updateStudyCard(
			@Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId,
			@Parameter(description = "study card to update", required = true) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Returns all the dicom tags")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "available tags"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/dicomTags", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DicomTag>> findDicomTags() throws RestServiceException;
	
	@Operation(summary = "", description = "Apply a study card")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "apply a study card to the given acquisitions"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/apply", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnEveryDatasetAcquisition(#studyCardApplyObject.datasetAcquisitionIds, 'CAN_ADMINISTRATE'))")
	ResponseEntity<Void> applyStudyCard(
			@Parameter(description = "study card id and acquisition ids", required = true) @RequestBody StudyCardApply studyCardApplyObject) throws RestServiceException, PacsException, SolrServerException, IOException;

}
