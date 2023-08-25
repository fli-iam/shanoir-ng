package org.shanoir.ng.processing.carmin.controller;

import io.swagger.annotations.*;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.dto.CarminDatasetProcessingDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(value = "carminDatasetProcessing")
@RequestMapping("/carminDatasetProcessing")
public interface CarminDatasetProcessingApi {

        @ApiOperation(value = "", notes = "Saves a new carmin dataset processing", response = CarminDatasetProcessing.class, tags = {})
        @ApiResponses(value = {
                        @ApiResponse(code = 200, message = "created dataset processing", response = CarminDatasetProcessing.class),
                        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
                        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
                        @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
                        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
        @PostMapping(value = "", produces = { "application/json" }, consumes = {
                        "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
        ResponseEntity<CarminDatasetProcessingDTO> saveNewCarminDatasetProcessing(
                        @ApiParam(value = "carmin dataset processing to create", required = true) @Valid @RequestBody CarminDatasetProcessingDTO carminDatasetProcessing,
                        @ApiParam(value = "start monitoring job once created") @RequestParam(value = "start", required = false, defaultValue="false") boolean start,
                        BindingResult result) throws RestServiceException, EntityNotFoundException, SecurityException;

        @ApiOperation(value = "", notes = "Updates a dataset processing", response = Void.class, tags = {})
        @ApiResponses(value = { @ApiResponse(code = 204, message = "dataset processing updated", response = Void.class),
                @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
                @ApiResponse(code = 403, message = "forbidden", response = Void.class),
                @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
                @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
        @PutMapping(value = "/{datasetProcessingId}", produces = { "application/json" }, consumes = {
                "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
        ResponseEntity<Void> updateCarminDatasetProcessing(
                @ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
                @ApiParam(value = "carmin dataset processing to update", required = true) @Valid @RequestBody CarminDatasetProcessingDTO carminDatasetProcessing,
                @ApiParam(value = "start monitoring job once updated") @RequestParam(value = "start", required = false, defaultValue="false") boolean start,
                BindingResult result)
                throws RestServiceException, SecurityException;

        @ApiOperation(value = "", notes = "If exists, returns the carmin dataset processing corresponding to the given id", response = CarminDatasetProcessingDTO.class, tags = {})
        @ApiResponses(value = {
                        @ApiResponse(code = 200, message = "found dataset processing", response = CarminDatasetProcessing.class),
                        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
                        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
                        @ApiResponse(code = 404, message = "no dataset processing found", response = Void.class),
                        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
        @GetMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<CarminDatasetProcessingDTO> findCarminDatasetProcessingById(
                        @ApiParam(value = "id of the carmin dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

        @ApiOperation(value = "", notes = "If exists, returns the carmin dataset processing corresponding to the given execution identifier with carmin new fields", response = CarminDatasetProcessing.class, tags = {})
        @ApiResponses(value = {
                        @ApiResponse(code = 200, message = "found dataset processing", response = CarminDatasetProcessingDTO.class),
                        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
                        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
                        @ApiResponse(code = 404, message = "no dataset processing found", response = Void.class),
                        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
        @GetMapping(value = "", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<CarminDatasetProcessingDTO> findCarminDatasetProcessingByIdentifier(
                        @ApiParam(value = "identifier of the execution", required = true) @RequestParam("identifier") String identifier);

        @ApiOperation(value = "", notes = "If exists, returns the dataset processing corresponding to the given id with carmin new fields", response = CarminDatasetProcessing.class, responseContainer = "List", tags = {})
        @ApiResponses(value = {
                        @ApiResponse(code = 200, message = "found dataset processing", response = CarminDatasetProcessingDTO.class, responseContainer = "List"),
                        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
                        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
                        @ApiResponse(code = 404, message = "no dataset processing found", response = Void.class),
                        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
        @GetMapping(value = "carminDatasetProcessings", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<List<CarminDatasetProcessingDTO>> findCarminDatasetProcessings();


        @ApiOperation(value = "", notes = "Returns all the carmin dataset processings with given study and subject", response = CarminDatasetProcessingDTO.class, responseContainer = "List", tags = {})
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "found carmin dataset processings", response = CarminDatasetProcessingDTO.class, responseContainer = "List"),
                @ApiResponse(code = 204, message = "no dataset processing found", response = Void.class),
                @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
                @ApiResponse(code = 403, message = "forbidden", response = Void.class),
                @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
        @GetMapping(value = "/study/{studyId}/subject/{subjectId}", produces = { "application/json" })
        @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
        ResponseEntity<List<CarminDatasetProcessingDTO>> findCarminDatasetProcessingsByStudyIdAndSubjectId(
                @ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
                @ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);
}
