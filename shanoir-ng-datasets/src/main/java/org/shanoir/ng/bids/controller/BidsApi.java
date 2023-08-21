
package org.shanoir.ng.bids.controller;

import java.io.IOException;

import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "bids")
@RequestMapping("/bids")
public interface BidsApi {

    @Operation(summary = "generateBids", description = "Create a BIDS structure for a given study ID and study name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studyId/{studyId}/studyName/{studyName}")
    ResponseEntity<Void> generateBIDSByStudyId(
    		@Parameter(name = "id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@Parameter(name = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException;

    @Operation(value = "", nickname = "refreshBids", notes = "Refresh the BIDS structure for a given study ID and study name", response = Resource.class, tags={})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "refreshBids/studyId/{studyId}/studyName/{studyName}")
    ResponseEntity<BidsElement>  refreshBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException;

    @Operation(value = "", nickname = "exportBIDSBySubjectId", notes = "If exists, returns a zip file of the BIDS structure corresponding to the given study id and path", response = Resource.class, tags={})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "zip file"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no dataset found"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/exportBIDS/studyId/{studyId}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_DOWNLOAD'))")
    void exportBIDSFile(
    		@Parameter(name = "Id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@Parameter(name = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath, HttpServletResponse response) throws RestServiceException, IOException;

	@Operation(summary = "getBids", description = "If exists, returns a BIDSElement structure corresponding to the given study id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "BidsElement"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/bidsStructure/studyId/{studyId}", produces = { "application/json" })
	ResponseEntity<BidsElement> getBIDSStructureByStudyId(
			@Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException, IOException;

}
