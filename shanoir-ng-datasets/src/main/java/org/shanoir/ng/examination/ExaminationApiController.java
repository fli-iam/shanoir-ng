package org.shanoir.ng.examination;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirDatasetException;
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
public class ExaminationApiController implements ExaminationApi {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationApiController.class);

	@Autowired
	private ExaminationService examinationService;

	@Override
	public ResponseEntity<Void> deleteExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId) {
		if (examinationService.findById(examinationId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			examinationService.deleteById(examinationId);
		} catch (ShanoirDatasetException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Examination> findExaminationById(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId) {
		final Examination examination = examinationService.findById(examinationId);
		if (examination == null) {
			return new ResponseEntity<Examination>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Examination>(examination, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Examination>> findExaminations() {
		// TODO: filter by user!!!
		final List<Examination> examinations = examinationService.findAll();
		if (examinations.isEmpty()) {
			return new ResponseEntity<List<Examination>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Examination>>(examinations, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Examination> saveNewExamination(
			@ApiParam(value = "the examination to create", required = true) @RequestBody @Valid final Examination examination,
			final BindingResult result) throws RestServiceException {

		/* Validation */
		// A basic examination can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(examination);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(examination);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		examination.setId(null);

		/* Save examination in db. */
		try {
			final Examination createdExamination = examinationService.save(examination);
			return new ResponseEntity<Examination>(createdExamination, HttpStatus.OK);
		} catch (ShanoirDatasetException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Void> updateExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId,
			@ApiParam(value = "the examination to update", required = true) @RequestBody @Valid final Examination examination,
			final BindingResult result) throws RestServiceException {

		examination.setId(examinationId);

		// A basic examination can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(examination);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(examination);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update examination in db. */
		try {
			examinationService.update(examination);
		} catch (ShanoirDatasetException e) {
			LOG.error("Error while trying to update examination " + examinationId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param examination examination.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Examination examination) {
		final Examination previousStateExamination = examinationService.findById(examination.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Examination>().validate(previousStateExamination,
				examination);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param examination examination.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final Examination examination) {
		return new EditableOnlyByValidator<Examination>().validate(examination);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param examination
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Examination examination) {
		final UniqueValidator<Examination> uniqueValidator = new UniqueValidator<Examination>(examinationService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(examination);
		return uniqueErrors;
	}

}
