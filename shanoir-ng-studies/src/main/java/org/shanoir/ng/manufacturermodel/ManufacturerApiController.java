package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudyException;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class ManufacturerApiController implements ManufacturerApi {

	@Autowired
	private ManufacturerService manufacturerService;

	public ResponseEntity<Manufacturer> findManufacturerById(
			@ApiParam(value = "id of the manufacturer", required = true) @PathVariable("manufacturerId") final Long manufacturerId) {
		final Manufacturer manufacturer = manufacturerService.findById(manufacturerId);
		if (manufacturer == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturer, HttpStatus.OK);
	}

	public ResponseEntity<List<Manufacturer>> findManufacturers() {
		final List<Manufacturer> manufacturers = manufacturerService.findAll();
		if (manufacturers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturers, HttpStatus.OK);
	}

	public ResponseEntity<Manufacturer> saveNewManufacturer(
			@ApiParam(value = "manufacturer to create", required = true) @RequestBody final Manufacturer manufacturer,
			final BindingResult result) throws RestServiceException {
		/* Validation */
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(manufacturer);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		manufacturer.setId(null);

		/* Save center in db. */
		try {
			return new ResponseEntity<Manufacturer>(manufacturerService.save(manufacturer), HttpStatus.OK);
		} catch (ShanoirStudyException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	/*
	 * Get unique constraint errors.
	 *
	 * @param manufacturer manufacturer.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Manufacturer manufacturer) {
		final UniqueValidator<Manufacturer> uniqueValidator = new UniqueValidator<Manufacturer>(manufacturerService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(manufacturer);
		return uniqueErrors;
	}

}
