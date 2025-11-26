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


package org.shanoir.ng.bids.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Tag(name = "bids")
@RequestMapping("/bids")
public interface BidsApi {

    @Operation(summary = "generateBids", description = "Create a participants.tsv file for a given study ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset found"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/neurobagel/studyId/{studyId}")
    ResponseEntity<ByteArrayResource> generateParticipantsByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws RestServiceException, IOException;

    @Operation(summary = "generateBids", description = "Create a BIDS structure for a given study ID and study name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studyId/{studyId}/studyName/{studyName}")
    ResponseEntity<Void> generateBIDSByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "name of the study", required = true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException;

    @Operation(summary = "refreshBids", description = "Refresh the BIDS structure for a given study ID and study name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no study found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "refreshBids/studyId/{studyId}/studyName/{studyName}")
    ResponseEntity<BidsElement>  refreshBIDSByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "name of the study", required = true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException;

    @Operation(summary = "exportBIDSBySubjectId", description = "If exists, returns a zip file of the BIDS structure corresponding to the given study id and path")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "zip file"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/exportBIDS/studyId/{studyId}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_DOWNLOAD'))")
    void exportBIDSFile(
            @Parameter(description = "Id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath, HttpServletResponse response) throws RestServiceException, IOException;

    @Operation(summary = "getBids", description = "If exists, returns a BIDSElement structure corresponding to the given study id")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "BidsElement"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset found"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/bidsStructure/studyId/{studyId}", produces = { "application/json" })
    ResponseEntity<BidsElement> getBIDSStructureByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
            throws RestServiceException, IOException;

}
