package org.shanoir.ng.dataset;

import javax.validation.Valid;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.DatasetsErrorModelCode;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
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
public class DatasetApiController implements DatasetApi {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetApiController.class);

	@Autowired
	private DatasetService datasetService;

	public ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
			throws RestServiceException {
		if (datasetService.findById(datasetId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			datasetService.deleteById(datasetId);
		} catch (ShanoirException e) {
			if (DatasetsErrorModelCode.DATASET_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else if (e.getErrorMap() != null) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Forbidden",
						new ErrorDetails(e.getErrorMap())));
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Dataset> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId) {
		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(dataset, HttpStatus.OK);
	}

	public ResponseEntity<Dataset> saveNewDataset(
			@ApiParam(value = "dataset to create", required = true) @Valid @RequestBody Dataset dataset,
			final BindingResult result) throws RestServiceException {
		/* Validation */
		// A basic user can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(dataset);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(dataset);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		dataset.setId(null);

		/* Save dataset in db. */
		try {
			final Dataset createdDataset = datasetService.save(dataset);
			return new ResponseEntity<>(createdDataset, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	public ResponseEntity<Void> updateDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody Dataset dataset,
			final BindingResult result) throws RestServiceException {
		// IMPORTANT : avoid any confusion that could lead to security breach
		dataset.setId(datasetId);

		// A basic dataset can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(dataset);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(dataset);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update dataset in db. */
		try {
			datasetService.update(dataset);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update dataset " + datasetId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param dataset dataset.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Dataset dataset) {
		final Dataset previousStateDataset = datasetService.findById(dataset.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Dataset>().validate(previousStateDataset,
				dataset);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param dataset dataset.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final Dataset center) {
		return new EditableOnlyByValidator<Dataset>().validate(center);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param dataset dataset.
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Dataset dataset) {
		final UniqueValidator<Dataset> uniqueValidator = new UniqueValidator<Dataset>(datasetService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(dataset);
		return uniqueErrors;
	}

}
