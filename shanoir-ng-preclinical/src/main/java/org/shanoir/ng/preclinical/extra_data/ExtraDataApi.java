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

import org.shanoir.ng.preclinical.extra_data.bloodgas_data.BloodGasData;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.preclinical.extra_data.physiological_data.PhysiologicalData;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.Resource;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "extra_data")
@RequestMapping("")
public interface ExtraDataApi {

	@ApiOperation(value = "Upload extra data", notes = "", response = ExaminationExtraData.class, tags = {
			"ExaminationExtraData", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns Extra data", response = ExaminationExtraData.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = ExaminationExtraData.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = ExaminationExtraData.class) })
	@PostMapping(value = "/examination/extradata/upload/{id}", produces = { "application/json" }, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" })
	ResponseEntity<ExaminationExtraData> uploadExtraData(
			@ApiParam(value = "extradata id", required = true) @PathVariable("id") Long id,
			@RequestParam("files") MultipartFile[] uploadfiles) throws RestServiceException;

	@ApiOperation(value = "Create extra data", notes = "", response = Void.class, tags = { "ExaminationExtraData", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns Extra data", response = ExaminationExtraData.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = ExaminationExtraData.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = ExaminationExtraData.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = ExaminationExtraData.class) })
	@PostMapping(value = "/examination/{id}/extradata", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<ExaminationExtraData> createExtraData(
			@ApiParam(value = "Examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Extra data to create", required = true) @RequestBody ExaminationExtraData extradata,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Create physiological extra data", notes = "", response = Void.class, tags = {
			"PhysiologicalData", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns Extra data", response = PhysiologicalData.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = PhysiologicalData.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = PhysiologicalData.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = PhysiologicalData.class) })
	@PostMapping(value = "/examination/{id}/physiologicaldata", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<PhysiologicalData> createPhysiologicalExtraData(
			@ApiParam(value = "Examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Physiological Extra data to create", required = true) @RequestBody PhysiologicalData extradata,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Create blood gas extra data", notes = "", response = Void.class, tags = { "BloodGasData", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns Extra data", response = BloodGasData.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = BloodGasData.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = BloodGasData.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = BloodGasData.class) })
	@PostMapping(value = "/examination/{id}/bloodgasdata", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<BloodGasData> createBloodGasExtraData(
			@ApiParam(value = "Examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "BloodGas Extra data to create", required = true) @RequestBody BloodGasData extradata,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Deletes a extradata", notes = "", response = Void.class, tags = { "ExaminationExtraData", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid Examination Extra Data  id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@DeleteMapping(value = "/examination/{id}/extradata/{eid}", produces = {
			"application/json" })
	ResponseEntity<Void> deleteExtraData(
			@ApiParam(value = "Examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Examination extra data id", required = true) @PathVariable("eid") Long eid)
			throws RestServiceException;

	@ApiOperation(value = "Get extra data by id", notes = "", response = ExaminationExtraData.class, responseContainer = "List", tags = {
			"ExaminationExtraData", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An examination extra data", response = ExaminationExtraData.class),
			@ApiResponse(code = 404, message = "Examination extra data not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = ExaminationExtraData.class) })
	@GetMapping(value = "/examination/{id}/extradata/{eid}", produces = {
			"application/json" })
	ResponseEntity<ExaminationExtraData> getExtraDataById(
			@ApiParam(value = "Examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ExaminationExtraData id", required = true) @PathVariable("eid") Long eid);

	@ApiOperation(value = "List all extra data for given examination id", notes = "", response = ExaminationExtraData.class, responseContainer = "List", tags = {
			"ExaminationExtraData", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of extra data elements", response = ExaminationExtraData.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = ExaminationExtraData.class) })
	@GetMapping(value = "/examination/{id}/extradata/all", produces = {
			"application/json" })
	ResponseEntity<List<ExaminationExtraData>> getExaminationExtraData(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@ApiOperation(value = "Download an extradata file", notes = "", response = Void.class, tags = {
			"ExaminationExtraData", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid Examination Extra Data  id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@GetMapping(value = "/examination/extradata/download/{id}", produces = {
			MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/json" })
	ResponseEntity<Resource> downloadExtraData(
			@ApiParam(value = "Examination extra data id to download", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@ApiOperation(value = "Update an existing physiologicalData", notes = "", response = Void.class, tags = {
			"ExaminationExtraData", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "ExaminationExtraData not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@PutMapping(value = "/examination/{id}/physiologicaldata/{eid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updatePhysiologicalData(
			@ApiParam(value = "ID of examination that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of physiologicaldata that needs to be updated", required = true) @PathVariable("eid") Long eid,
			@ApiParam(value = "Physiological object that needs to be updated", required = true) @RequestBody PhysiologicalData physiologicalData,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Update an existing bloodGasData", notes = "", response = Void.class, tags = {
			"ExaminationExtraData", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "BloodGasData not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@PutMapping(value = "/examination/{id}/bloodgasdata/{eid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateBloodGasData(
			@ApiParam(value = "ID of examination that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of bloodgasdata that needs to be updated", required = true) @PathVariable("eid") Long eid,
			@ApiParam(value = "BloodGasData object that needs to be updated", required = true) @RequestBody BloodGasData bloodGasData,
			final BindingResult result) throws RestServiceException;

}
