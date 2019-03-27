package org.shanoir.ng.manufacturermodel.controler;

import java.util.List;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelService;
import org.shanoir.ng.shared.dto.IdNameDTO;
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
	public ResponseEntity<List<IdNameDTO>> findManufacturerModelsNames() {
		final List<IdNameDTO> manufacturerModels = manufacturerModelService.findIdsAndNames();
		if (manufacturerModels.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModels, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<List<IdNameDTO>> findCenterManufacturerModelsNames(@PathVariable("centerId") final Long centerId) {
		final List<IdNameDTO> manufacturerModels = manufacturerModelService.findIdsAndNamesForCenter(centerId);
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
		final FieldErrorMap errors = new FieldErrorMap()
				.checkBindingContraints(result)
				.checkUniqueConstraints(manufacturerModel, manufacturerModelService);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

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
		final FieldErrorMap errors = new FieldErrorMap()
				.checkBindingContraints(result)
				.checkUniqueConstraints(manufacturerModel, manufacturerModelService);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update user in db. */
		try {
			manufacturerModelService.update(manufacturerModel);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
