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

package org.shanoir.ng.preclinical.extra_data;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.shanoir.ng.preclinical.extra_data.bloodgas_data.BloodGasData;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.preclinical.extra_data.physiological_data.PhysiologicalData;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "extra_data")
@RequestMapping("")
public interface ExtraDataApi {

	@Operation(summary = "Upload extra data", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns Extra data"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "/examination/extradata/upload/{id}", produces = { "application/json" }, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" })
	ResponseEntity<ExaminationExtraData> uploadExtraData(
			@Parameter(name = "extradata id", required = true) @PathVariable("id") Long id,
			@RequestParam("files") MultipartFile[] uploadfiles) throws RestServiceException;

	@Operation(summary = "Create extra data", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns Extra data"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "/examination/{id}/extradata", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<ExaminationExtraData> createExtraData(
			@Parameter(name = "Examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "Extra data to create", required = true) @RequestBody ExaminationExtraData extradata,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "Create physiological extra data", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns Extra data"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "/examination/{id}/physiologicaldata", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<PhysiologicalData> createPhysiologicalExtraData(
			@Parameter(name = "Examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "Physiological Extra data to create", required = true) @RequestBody PhysiologicalData extradata,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "Create blood gas extra data", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns Extra data"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "/examination/{id}/bloodgasdata", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<BloodGasData> createBloodGasExtraData(
			@Parameter(name = "Examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "BloodGas Extra data to create", required = true) @RequestBody BloodGasData extradata,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "Deletes a extradata", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid Examination Extra Data  id"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@DeleteMapping(value = "/examination/{id}/extradata/{eid}", produces = {
			"application/json" })
	ResponseEntity<Void> deleteExtraData(
			@Parameter(name = "Examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "Examination extra data id", required = true) @PathVariable("eid") Long eid)
			throws RestServiceException;

	@Operation(summary = "Get extra data by id", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An examination extra data"),
			@ApiResponse(responseCode = "404", description = "Examination extra data not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/examination/{id}/extradata/{eid}", produces = {
			"application/json" })
	ResponseEntity<ExaminationExtraData> getExtraDataById(
			@Parameter(name = "Examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "ExaminationExtraData id", required = true) @PathVariable("eid") Long eid);

	@Operation(summary = "List all extra data for given examination id", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of extra data elements"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/examination/{id}/extradata/all", produces = {
			"application/json" })
	ResponseEntity<List<ExaminationExtraData>> getExaminationExtraData(
			@Parameter(name = "examination id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@Operation(summary = "Download an extradata file", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid Examination Extra Data  id"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@GetMapping(value = "/examination/extradata/download/{id}")
	void downloadExtraData(
			@Parameter(name = "Examination extra data id to download", required = true) @PathVariable("id") Long id,
			HttpServletResponse response)
			throws RestServiceException;

	@Operation(summary = "Update an existing physiologicalData", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "404", description = "ExaminationExtraData not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PutMapping(value = "/examination/{id}/physiologicaldata/{eid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updatePhysiologicalData(
			@Parameter(name = "ID of examination that needs to be updated", required = true) @PathVariable("id") Long id,
			@Parameter(name = "ID of physiologicaldata that needs to be updated", required = true) @PathVariable("eid") Long eid,
			@Parameter(name = "Physiological object that needs to be updated", required = true) @RequestBody PhysiologicalData physiologicalData,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "Update an existing bloodGasData", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "404", description = "BloodGasData not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PutMapping(value = "/examination/{id}/bloodgasdata/{eid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateBloodGasData(
			@Parameter(name = "ID of examination that needs to be updated", required = true) @PathVariable("id") Long id,
			@Parameter(name = "ID of bloodgasdata that needs to be updated", required = true) @PathVariable("eid") Long eid,
			@Parameter(name = "BloodGasData object that needs to be updated", required = true) @RequestBody BloodGasData bloodGasData,
			final BindingResult result) throws RestServiceException;

}
