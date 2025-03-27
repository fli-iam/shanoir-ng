package org.shanoir.ng.challenge;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "challenges", description = "the challenge API")
@RequestMapping("/challenges")
public interface ChallengeApi {


    @Operation(summary = "", description = "Returns id and name for all available challenges")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found challenges"),
            @ApiResponse(responseCode = "204", description = "no challenges found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<IdName>> findChallenges() throws RestServiceException;
}
