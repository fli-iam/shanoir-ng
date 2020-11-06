
package org.shanoir.ng.exporter.controller;

import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "bids")
@RequestMapping("/bids")
public interface BidsApi {

    @ApiOperation(value = "", nickname = "generateBids", notes = "Create a BIDS structure for a given study ID and study name", response = Resource.class, tags={})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "/studyId/{studyId}/studyName/{studyName}")
    ResponseEntity<Void> generateBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException;

    @ApiOperation(value = "", nickname = "exportBIDSBySubjectId", notes = "If exists, returns a zip file of the BIDS structure corresponding to the given subject id", response = Resource.class, tags={})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @GetMapping(value = "/exportBIDS/studyId/{studyId}")
    void exportBIDSFile(
    		@ApiParam(value = "Id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath, HttpServletResponse response) throws RestServiceException, IOException;
  
}
