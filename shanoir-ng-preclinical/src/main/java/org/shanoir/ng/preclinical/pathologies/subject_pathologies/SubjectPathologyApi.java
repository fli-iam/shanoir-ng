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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "subject_pathology", description = "the subject pathologies API")
public interface SubjectPathologyApi {

    @Operation(summary = "Add a new subject pathology", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success returns subject pathology"),
            @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
            @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @RequestMapping(value = "/subject/{id}/pathology", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<SubjectPathology> addSubjectPathology(
            @Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "pathology to add to subject", required = true) @RequestBody SubjectPathology pathos,
            BindingResult result) throws RestServiceException;

    @Operation(summary = "Deletes a pathology from subject", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid subject pathology id"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @RequestMapping(value = "/subject/{id}/pathology/{pid}", produces = {
            "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteSubjectPathology(
            @Parameter(name = "animal subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "pathology id", required = true) @PathVariable("pid") Long pid)
            throws RestServiceException;

    @Operation(summary = "Deletes all pathology linked to a given subject", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid subject id"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @RequestMapping(value = "/subject/{id}/pathology/all", produces = {
            "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteSubjectPathologies(
            @Parameter(name = "animal subject id", required = true) @PathVariable("id") Long id)
            throws RestServiceException;

    @Operation(summary = "Get subject pathology by id", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A subject pathology"),
            @ApiResponse(responseCode = "404", description = "Subjet pathology not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @RequestMapping(value = "/subject/{id}/pathology/{pid}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<SubjectPathology> getSubjectPathologyById(
            @Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "Subject pathology id", required = true) @PathVariable("pid") Long pid)
            throws RestServiceException;

    @Operation(summary = "List all pathologies for subject", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of subject pathologies"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @RequestMapping(value = "/subject/{id}/pathology/all", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<SubjectPathology>> getSubjectPathologies(
            @Parameter(name = "subject id", required = true) @PathVariable("id") Long id) throws RestServiceException;

    @Operation(summary = "List all subjects for pathology", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of subject pathologies"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @RequestMapping(value = "/subject/all/pathology/{pid}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathology(
            @Parameter(name = "pathology id", required = true) @PathVariable("pid") Long pid)
            throws RestServiceException;

    @Operation(summary = "List all subjects for pathology model", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of subject pathologies"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @RequestMapping(value = "/subject/all/pathology/model/{pathoModelId}/", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathologyModel(
            @Parameter(name = "pathology model id", required = true) @PathVariable("pathoModelId") Long pathoModelId);

    @Operation(summary = "Update an existing subject pathology", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
            @ApiResponse(responseCode = "404", description = "Subject Pathology not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @RequestMapping(value = "/subject/{id}/pathology/{pid}", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.PUT)
    ResponseEntity<Void> updateSubjectPathology(
            @Parameter(name = "ID of subject", required = true) @PathVariable("id") Long id,
            @Parameter(name = "ID of subject pathology that needs to be updated", required = true) @PathVariable("pid") Long pid,
            @Parameter(name = "Subject Pathology that will be be updated", required = true) @RequestBody SubjectPathology pathos,
            final BindingResult result) throws RestServiceException;

}
