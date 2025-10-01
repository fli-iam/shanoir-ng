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

package org.shanoir.ng.dicom.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This implements the DICOMWeb REST-API exposed by Shanoir-NG.
 * For more informations please see:
 *
 * https://github.com/fli-iam/shanoir-ng/wiki/DICOMWeb
 *
 * @author mkain
 *
 */
@Tag(name = "dicomweb")
@RequestMapping("/dicomweb")
public interface DICOMWebApi {

    @Operation(summary = "", description = "Returns all DICOM patients/subjects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found patients/subjects"),
            @ApiResponse(responseCode = "204", description = "no patient/subject found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/patients", produces = { "application/dicom+json" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<String> findPatients() throws RestServiceException;

    @Operation(summary = "", description = "Returns all DICOM studies/examinations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found studies/examinations"),
            @ApiResponse(responseCode = "204", description = "no study/exam found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies", produces = { "application/dicom+json" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<String> findStudies(@RequestParam Map<String, String> allParams) throws RestServiceException, JsonMappingException, JsonProcessingException;

    @Operation(summary = "", description = "Returns all DICOM series/acquisitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found series/acquisitions"),
            @ApiResponse(responseCode = "204", description = "no serie/acquisition found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/series", produces = { "application/dicom+json" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<String> findSeries() throws RestServiceException;

    @Operation(summary = "", description = "Returns all DICOM series/acquisitions of an examination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found series/acquisitions"),
            @ApiResponse(responseCode = "204", description = "no serie/acquisition found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies/{examinationUID}/series", produces = { "application/dicom+json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationUID, 'CAN_SEE_ALL'))")
    ResponseEntity<String> findSeriesOfStudy(
            @Parameter(description = "examinationUID", required = true) @PathVariable("examinationUID") String examinationUID, @RequestParam Map<String, String> allParams
        ) throws RestServiceException, JsonMappingException, JsonProcessingException;

    @Operation(summary = "", description = "Returns the metadata of a DICOM serie/acquisition of an examination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found serie/acquisition metadata"),
            @ApiResponse(responseCode = "204", description = "no serie/acquisition metadata found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies/{examinationUID}/series/{serieInstanceUID}/metadata", produces = { "application/dicom+json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationUID, 'CAN_SEE_ALL'))")
    ResponseEntity<String> findSerieMetadataOfStudy(
            @Parameter(description = "examinationUID", required = true) @PathVariable("examinationUID") String examinationUID,
            @Parameter(description = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID
        ) throws RestServiceException, JsonMappingException, JsonProcessingException;

    @Operation(summary = "", description = "Returns all DICOM instances of a study and serie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found instances/datasets"),
            @ApiResponse(responseCode = "204", description = "no instance/dataset found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies/{examinationUID}/series/{serieInstanceUID}/instances", produces = { "application/dicom+json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationUID, 'CAN_SEE_ALL'))")
    ResponseEntity<String> findInstancesOfStudyOfSerie(
            @Parameter(description = "examinationUID", required = true) @PathVariable("examinationUID") String examinationUID,
            @Parameter(description = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID
        ) throws RestServiceException;

    @Operation(summary = "", description = "Returns a DICOM instance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found instance/dataset"),
            @ApiResponse(responseCode = "204", description = "no instance/dataset found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationUID, 'CAN_SEE_ALL'))")
    ResponseEntity findInstance(
            @Parameter(description = "examinationUID", required = true) @PathVariable("examinationUID") String examinationUID,
            @Parameter(description = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID,
            @Parameter(description = "sopInstanceUID", required = true) @PathVariable("sopInstanceUID") String sopInstanceUID
        ) throws RestServiceException;

    @Operation(summary = "", description = "Returns a frame of a DICOM instance, of a study and a serie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found instances/datasets"),
            @ApiResponse(responseCode = "204", description = "no instance/dataset found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}/frames/{frame}")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationUID, 'CAN_SEE_ALL'))")
    ResponseEntity findFrameOfStudyOfSerieOfInstance(
            @Parameter(description = "examinationUID", required = true) @PathVariable("examinationUID") String examinationUID,
            @Parameter(description = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID,
            @Parameter(description = "sopInstanceUID", required = true) @PathVariable("sopInstanceUID") String sopInstanceUID,
            @Parameter(description = "frame", required = true) @PathVariable("frame") String frame
        ) throws RestServiceException;

    @Operation(summary = "", description = "Returns all DICOM instances/datasets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found instances/datasets"),
            @ApiResponse(responseCode = "204", description = "no instance/dataset found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/instances")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<String> findInstances() throws RestServiceException;

    @Operation(summary = "", description = "Returns all DICOM instances/datasets of a study")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found instances/datasets"),
            @ApiResponse(responseCode = "204", description = "no instance/dataset found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/studies/{studyInstanceUID}/instances")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<String> findInstancesOfStudy(
            @Parameter(description = "studyInstanceUID", required = true) @PathVariable("studyInstanceUID") String studyInstanceUID
        ) throws RestServiceException;

    @Operation(summary = "", description = "STOW-RS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "stored"),
            @ApiResponse(responseCode = "204", description = "updated"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "422", description = "bad parameters"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/studies", consumes = { MediaType.MULTIPART_RELATED_VALUE })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<Void> stow(HttpServletRequest request) throws RestServiceException;


//    @GET
//    @NoCache
//    @Path("/patients/count")
//    @Produces("application/json")
//    public Response countPatients() {
//        return count("CountPatients", Model.PATIENT, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/count")
//    @Produces("application/json")
//    public Response countStudies() {
//        return count("CountStudies", Model.STUDY, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/series/count")
//    @Produces("application/json")
//    public Response countSeries() {
//        return count("CountSeries", Model.SERIES, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/{StudyInstanceUID}/series/count")
//    @Produces("application/json")
//    public Response countSeriesOfStudy(
//            @PathParam("StudyInstanceUID") String studyInstanceUID) {
//        return count("CountStudySeries", Model.SERIES, studyInstanceUID, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/instances/count")
//    @Produces("application/json")
//    public Response countInstances() {
//        return count("CountInstances", Model.INSTANCE, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/{StudyInstanceUID}/instances/count")
//    @Produces("application/json")
//    public Response countInstancesOfStudy(
//            @PathParam("StudyInstanceUID") String studyInstanceUID) {
//        return count("CountStudyInstances", Model.INSTANCE, studyInstanceUID, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/count")
//    @Produces("application/json")
//    public Response countInstancesOfSeries(
//            @PathParam("StudyInstanceUID") String studyInstanceUID,
//            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
//        return count("CountStudySeriesInstances", Model.INSTANCE, studyInstanceUID, seriesInstanceUID);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/size")
//    @Produces("application/json")

}
