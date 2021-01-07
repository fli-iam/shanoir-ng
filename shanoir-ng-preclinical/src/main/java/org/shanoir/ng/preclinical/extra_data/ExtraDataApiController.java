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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.preclinical.extra_data.bloodgas_data.BloodGasData;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.preclinical.extra_data.physiological_data.PhysiologicalData;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiParam;

@Controller
public class ExtraDataApiController implements ExtraDataApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(ExtraDataApiController.class);

	@Autowired
	private ExtraDataService<ExaminationExtraData> extraDataService;
	@Autowired
	private ExtraDataService<PhysiologicalData> physioDataService;
	@Autowired
	private ExtraDataService<BloodGasData> bloodGasDataService;
	@Autowired
	private ShanoirPreclinicalConfiguration preclinicalConfig;
	@Value("${preclinical.uploadExtradataFolder}")
	private String extraDataPath;

	@Override
	public ResponseEntity<ExaminationExtraData> uploadExtraData(
			@ApiParam(value = "extra data id", required = true) @PathVariable("id") Long id,
			@RequestParam("files") MultipartFile[] uploadfiles) throws RestServiceException {

		if (uploadfiles.length == 0) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded", null));
		}
		if (id == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad Arguments", null));
		}

		ExaminationExtraData extradata = extraDataService.findById(id);

		try {
			extradata = saveUploadedFile(extradata, uploadfiles[0]);
			extraDataService.save(extradata);
			return new ResponseEntity<>(extradata, HttpStatus.OK);
		} catch (IOException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file", null));
		} catch (ShanoirException e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while saving updated extradata", null));
		}
	}

	@Override
	public ResponseEntity<ExaminationExtraData> createExtraData(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ExaminationExtraData to create", required = true) @RequestBody ExaminationExtraData extradata,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(extradata);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(extradata);

		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}
		// Guarantees it is a creation, not an update
		extradata.setId(null);
		extradata.setExtradatatype("Extra data");
		try {
			final ExaminationExtraData createdExtraData = extraDataService.save(extradata);
			return new ResponseEntity<>(createdExtraData, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<PhysiologicalData> createPhysiologicalExtraData(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "PhysiologicalData to create", required = true) @RequestBody PhysiologicalData extradata,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(extradata);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(extradata);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		extradata.setId(null);
		extradata.setExtradatatype("Physiological data");

		/* Save extradata in db. */
		try {
			final PhysiologicalData createdExtraData = physioDataService.save(extradata);
			return new ResponseEntity<>(createdExtraData, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<BloodGasData> createBloodGasExtraData(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "BloodGasData to create", required = true) @RequestBody BloodGasData extradata,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(extradata);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(extradata);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}
		// Guarantees it is a creation, not an update
		extradata.setId(null);
		extradata.setExtradatatype("Blood gas data");
		try {
			final BloodGasData createdExtraData = bloodGasDataService.save(extradata);
			return new ResponseEntity<>(createdExtraData, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<Void> deleteExtraData(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ExaminationExtraData id to delete", required = true) @PathVariable("eid") Long eid)
			throws RestServiceException {
		ExaminationExtraData toDelete = extraDataService.findById(eid);
		if (toDelete == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			// Find and delete corresponding file
			if (Paths.get(toDelete.getFilepath()).toFile().exists()) {
				Files.delete(Paths.get(toDelete.getFilepath()));
			}
		} catch (Exception e) {
			LOG.error("There was an error trying to delete files from " + toDelete.getFilepath()
					+ toDelete.getFilename() + " " + e.getMessage(), e);
		}
		try {
			extraDataService.deleteById(toDelete.getId());
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		} catch (Exception e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.NOT_FOUND.value(),
					"Error trying to delete file " + toDelete.getFilename(), null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ExaminationExtraData> getExtraDataById(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of exam extra data that needs to be fetched", required = true) @PathVariable("eid") Long eid) {
		final ExaminationExtraData extradata = extraDataService.findById(eid);
		if (extradata == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(extradata, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ExaminationExtraData>> getExaminationExtraData(
			@ApiParam(value = "ID of examination from which we get extradata", required = true) @PathVariable("id") Long id) {
		final List<ExaminationExtraData> extradatas = extraDataService.findAllByExaminationId(id);
		return new ResponseEntity<>(extradatas, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Resource> downloadExtraData(
			@ApiParam(value = "ID of exam extra data file to download", required = true) @PathVariable("id") Long id)
			throws RestServiceException {

		final ExaminationExtraData extradata = extraDataService.findById(id);
		if (extradata != null) {
			try {
				File toDownload = new File(extradata.getFilepath());
				Path path = Paths.get(toDownload.getAbsolutePath());
				ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

				HttpHeaders header = new HttpHeaders();
				header.setContentType(MediaType.APPLICATION_PDF);
				header.set(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + extradata.getFilename().replace(" ", "_"));

				return ResponseEntity.ok().headers(header).contentLength(toDownload.length())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body((Resource) resource);
			} catch (IOException ioe) {
				LOG.error("Error while getting file to download " + ioe.getMessage(), ioe);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> updatePhysiologicalData(
			@ApiParam(value = "ID of examination that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of physiologicalData that needs to be updated", required = true) @PathVariable("eid") Long eid,
			@ApiParam(value = "PhysiologicalData object that needs to be updated", required = true) @RequestBody PhysiologicalData physioData,
			final BindingResult result) throws RestServiceException {
		// IMPORTANT : avoid any confusion that could lead to security breach
		physioData.setId(eid);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(physioData);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(physioData);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			physioDataService.update(physioData);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update extradata " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateBloodGasData(
			@ApiParam(value = "ID of examination that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of bloodGasData that needs to be updated", required = true) @PathVariable("eid") Long eid,
			@ApiParam(value = "BloodGasData object that needs to be updated", required = true) @RequestBody BloodGasData bloodGasData,
			final BindingResult result) throws RestServiceException {
		// IMPORTANT : avoid any confusion that could lead to security breach
		bloodGasData.setId(eid);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(bloodGasData);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(bloodGasData);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			bloodGasDataService.update(bloodGasData);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update extradata " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FieldErrorMap getCreationRightsErrors(final ExaminationExtraData extradata) {
		return new EditableOnlyByValidator<ExaminationExtraData>().validate(extradata);
	}

	private FieldErrorMap getUpdateRightsErrors(final ExaminationExtraData extraData) {
		final ExaminationExtraData previousStateExtraData = extraDataService.findById(extraData.getId());
		return new EditableOnlyByValidator<ExaminationExtraData>()
				.validate(previousStateExtraData, extraData);
	}

	@SuppressWarnings("unchecked")
	private FieldErrorMap getUniqueConstraintErrors(final ExaminationExtraData extradata) {
		final UniqueValidator<ExaminationExtraData> uniqueValidator = new UniqueValidator<>(
				extraDataService);
		return uniqueValidator.validate(extradata);
	}

	private ExaminationExtraData saveUploadedFile(ExaminationExtraData extradata, MultipartFile file)
			throws IOException {
		// Create corresponding folders
		Path path = Paths.get(extraDataPath + extradata.getExaminationId() + File.pathSeparator
				+ extradata.getClass().getSimpleName());
		Files.createDirectories(path);
		// Path to file
		Path pathToFile = Paths.get(path.toString() + File.pathSeparator + file.getOriginalFilename());
		byte[] bytes = file.getBytes();
		Files.write(pathToFile, bytes);
		extradata.setFilename(file.getOriginalFilename());
		extradata.setFilepath(pathToFile.toString());
		return extradata;
	}

}
