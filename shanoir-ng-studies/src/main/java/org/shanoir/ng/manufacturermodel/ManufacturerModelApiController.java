package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-04-04T08:00:17.206Z")

@Controller
public class ManufacturerModelApiController implements ManufacturerModelApi {

	@Autowired
	private ManufacturerModelService manufacturerModelService;

	public ResponseEntity<ManufacturerModel> findManufacturerModelById(
			@ApiParam(value = "id of the manufacturer model", required = true) @PathVariable("manufacturerModelId") final Long manufacturerModelId) {
		final ManufacturerModel manufacturerModel = manufacturerModelService.findById(manufacturerModelId);
		if (manufacturerModel == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModel, HttpStatus.OK);
	}

	public ResponseEntity<List<ManufacturerModel>> findManufacturerModels() {
		final List<ManufacturerModel> manufacturerModels = manufacturerModelService.findAll();
		if (manufacturerModels.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerModels, HttpStatus.OK);
	}

	public ResponseEntity<ManufacturerModel> saveNewManufacturerModel(
			@ApiParam(value = "manufacturer model to create", required = true) @RequestBody final ManufacturerModel manufacturerModel,
			final BindingResult result) throws RestServiceException {
		/* Validation */
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(manufacturerModel);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		manufacturerModel.setId(null);

		/* Save center in db. */
		try {
			return new ResponseEntity<ManufacturerModel>(manufacturerModelService.save(manufacturerModel), HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	/*
	 * Get unique constraint errors.
	 *
	 * @param manufacturerModel manufacturer model.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUniqueConstraintErrors(final ManufacturerModel manufacturerModel) {
		final UniqueValidator<ManufacturerModel> uniqueValidator = new UniqueValidator<ManufacturerModel>(manufacturerModelService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(manufacturerModel);
		return uniqueErrors;
	}

}
