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

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelService;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-04-04T08:00:17.206Z")

@Controller
public class ManufacturerModelApiController implements ManufacturerModelApi {

	@Autowired
	private ManufacturerModelService manufacturerModelService;

	@Override
	public ResponseEntity<ManufacturerModel> findManufacturerModelById(
			@PathVariable("manufacturerModelId") final Long manufacturerModelId) {
		final ManufacturerModel manufacturerModel = manufacturerModelService.findById(manufacturerModelId);
		if (manufacturerModel == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModel, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ManufacturerModel>> findManufacturerModels() {
		final List<ManufacturerModel> manufacturerModels = manufacturerModelService.findAll();
		if (manufacturerModels.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModels, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<List<IdName>> findManufacturerModelsNames() {
		final List<IdName> manufacturerModels = manufacturerModelService.findIdsAndNames();
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
		manufacturerModel.setId(manufacturerModelId);

		/* Validation */
		validate(result);

		/* Update user in db. */
		try {
			manufacturerModelService.update(manufacturerModel);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
