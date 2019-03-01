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

package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class AcquisitionEquipmentApiController implements AcquisitionEquipmentApi {

	private static final Logger LOG = LoggerFactory.getLogger(AcquisitionEquipmentApiController.class);

	@Autowired
	private AcquisitionEquipmentMapper acquisitionEquipmentMapper;

	@Autowired
	private AcquisitionEquipmentService acquisitionEquipmentService;

	public ResponseEntity<Void> deleteAcquisitionEquipment(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId) {
		try {
			acquisitionEquipmentService.deleteById(acquisitionEquipmentId);
		} catch (ShanoirStudiesException e) {
			if (StudiesErrorModelCode.ACQ_EQPT_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<AcquisitionEquipmentDTO> findAcquisitionEquipmentById(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId) {
		final AcquisitionEquipment equipment = acquisitionEquipmentService.findById(acquisitionEquipmentId);
		if (equipment == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(acquisitionEquipmentMapper.acquisitionEquipmentToAcquisitionEquipmentDTO(equipment),
				HttpStatus.OK);
	}

	public ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipments() {
		final List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAll();
		if (equipments.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(
				acquisitionEquipmentMapper.acquisitionEquipmentsToAcquisitionEquipmentDTOs(equipments), HttpStatus.OK);
	}

	public ResponseEntity<AcquisitionEquipmentDTO> saveNewAcquisitionEquipment(
			@ApiParam(value = "acquisition equipment to create", required = true) @RequestBody final AcquisitionEquipment acquisitionEquipment,
			final BindingResult result) throws RestServiceException {
		/* Validation */
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(acquisitionEquipment);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		acquisitionEquipment.setId(null);

		/* Save acquisition equipment in db. */
		try {
			return new ResponseEntity<>(acquisitionEquipmentMapper.acquisitionEquipmentToAcquisitionEquipmentDTO(
					acquisitionEquipmentService.save(acquisitionEquipment)), HttpStatus.OK);
		} catch (final ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	public ResponseEntity<Void> updateAcquisitionEquipment(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId,
			@ApiParam(value = "acquisition equipment to update", required = true) @RequestBody final AcquisitionEquipment acquisitionEquipment,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		acquisitionEquipment.setId(acquisitionEquipmentId);

		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(acquisitionEquipment);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update user in db. */
		try {
			acquisitionEquipmentService.update(acquisitionEquipment);
		} catch (final ShanoirStudiesException e) {
			LOG.error("Error while trying to update acquisition equipment " + acquisitionEquipmentId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Get unique constraint errors
	 * 
	 * @param result
	 * @return an error map
	 * @author yyao
	 */
	private FieldErrorMap getUniqueConstraintErrors(final AcquisitionEquipment acquisitionEquipment) {
		final UniqueValidator<AcquisitionEquipment> uniqueValidator = new UniqueValidator<AcquisitionEquipment>(
				acquisitionEquipmentService);
		FieldErrorMap uniqueErrorsFromField = uniqueValidator.validate(acquisitionEquipment);
		FieldErrorMap uniqueErrorsFromTable = uniqueValidator.validateFromTable(acquisitionEquipment);
		final FieldErrorMap uniqueErrors = new FieldErrorMap(uniqueErrorsFromField, uniqueErrorsFromTable);
		return uniqueErrors;
	}

}
