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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/controler/StudyCardApi.java
package org.shanoir.ng.studycard.controler;
=======
package org.shanoir.ng.studycard;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/StudyCardApi.java

import java.util.List;

import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.model.StudyCard;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-29T08:41:16.372Z")

@Api(value = "studycards", description = "the studyCard API")
@RequestMapping("/studycards")
public interface StudyCardApi {

	@ApiOperation(value = "", notes = "Deletes a study card", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study card deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study card found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyCardId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> deleteStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the study card corresponding to the given id", response = StudyCard.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found study card", response = StudyCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = StudyCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = StudyCard.class),
			@ApiResponse(code = 404, message = "no study card found", response = StudyCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = StudyCard.class) })
	@RequestMapping(value = "/{studyCardId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<StudyCard> findStudyCardById(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId);

	@ApiOperation(value = "", notes = "Returns all the study Cards", response = StudyCard.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found study cards", response = StudyCard.class),
			@ApiResponse(code = 204, message = "no study card found", response = StudyCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = StudyCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = StudyCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = StudyCard.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<List<StudyCard>> findStudyCards();

	@ApiOperation(value = "", notes = "Saves a new study card", response = StudyCard.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created study card", response = StudyCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = StudyCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = StudyCard.class),
			@ApiResponse(code = 422, message = "bad parameters", response = StudyCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = StudyCard.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<StudyCard> saveNewStudyCard(
			@ApiParam(value = "study Card to create", required = true) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns searched study cards", response = StudyCard.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found study cards", response = StudyCard.class),
			@ApiResponse(code = 401, message = "unauthorized", response = StudyCard.class),
			@ApiResponse(code = 403, message = "forbidden", response = StudyCard.class),
			@ApiResponse(code = 404, message = "no study card found", response = StudyCard.class),
			@ApiResponse(code = 500, message = "unexpected error", response = StudyCard.class) })
	@RequestMapping(value = "/search", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<List<StudyCard>> searchStudyCards(
			@ApiParam(value = "study ids", required = true) @RequestBody IdList studyIds);
	
    @ApiOperation(value = "", notes = "If exists, returns center id", response = Long.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found center id", response = Long.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Long.class),
        @ApiResponse(code = 403, message = "forbidden", response = Long.class),
        @ApiResponse(code = 404, message = "no center id found", response = Long.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Long.class) })
    @RequestMapping(value = "/centerid/{studyCardId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> searchCenterId(@ApiParam(value = "id of the study card",required=true ) @PathVariable("studyCardId") Long studyCardId);

	@ApiOperation(value = "", notes = "Updates a study card", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study card updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{studyCardId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> updateStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId,
			@ApiParam(value = "study card to update", required = true) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException;

}
