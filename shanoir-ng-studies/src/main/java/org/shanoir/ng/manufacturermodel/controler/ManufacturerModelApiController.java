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

package org.shanoir.ng.manufacturermodel.controler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelService;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityLinkedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ManufacturerModelApiController implements ManufacturerModelApi {

	@Autowired
	private ManufacturerModelService manufacturerModelService;

	@Override
	public ResponseEntity<ManufacturerModel> findManufacturerModelById(
			@PathVariable("manufacturerModelId") final Long manufacturerModelId) {
		final Optional<ManufacturerModel> manufacturerModelOpt = manufacturerModelService.findById(manufacturerModelId);
		if (manufacturerModelOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModelOpt.orElseThrow(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ManufacturerModel>> findManufacturerModels() {
		List<ManufacturerModel> manufacturerModels = manufacturerModelService.findAll();
		// Remove "unknown" manufacturer models
		manufacturerModels = manufacturerModels.stream().filter(manufacturer -> manufacturer.getId() != 0).collect(Collectors.toList());
		if (manufacturerModels.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModels, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdName>> findManufacturerModelsNames() {
		List<IdName> manufacturerModels = manufacturerModelService.findIdsAndNames();
		// Remove "unknown" manufacturer models
		manufacturerModels = manufacturerModels.stream().filter(manufacturer -> manufacturer.getId() != 0).collect(Collectors.toList());
		if (manufacturerModels.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModels, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdName>> findCenterManufacturerModelsNames(@PathVariable("centerId") final Long centerId) {
		final List<IdName> manufacturerModels = manufacturerModelService.findIdsAndNamesForCenter(centerId);
		if (manufacturerModels.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModels, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ManufacturerModel> saveNewManufacturerModel(
			@RequestBody final ManufacturerModel manufacturerModel, final BindingResult result)
					throws RestServiceException {

		/* Validation */
		validate(result);

		/* Save center in db. */
		return new ResponseEntity<ManufacturerModel>(manufacturerModelService.create(manufacturerModel), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateManufacturerModel(
			@PathVariable("manufacturerModelId") final Long manufacturerModelId,
			@RequestBody final ManufacturerModel manufacturerModel, final BindingResult result)
					throws RestServiceException {
		try {
			if (manufacturerModelId.equals(0L)) {
				throw new EntityNotFoundException("Cannot update unknown manufacturer model");
			}
			manufacturerModel.setId(manufacturerModelId);

			/* Validation */
			validate(result);

			/* Update user in db. */
			manufacturerModelService.update(manufacturerModel);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<Void> deleteManufacturerModel(Long manufacturerModelId) throws RestServiceException {
		try {
			if (manufacturerModelId.equals(0L)) {
				throw new EntityNotFoundException("Cannot update unknown manufacturer model");
			}
			manufacturerModelService.deleteById(manufacturerModelId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (EntityLinkedException e) {
			throw new RestServiceException(
					new ErrorModel(
							HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"This manufacturer is still linked to manufacturer models."
					)
			);
		}
	}

	private void validate(BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}
}
