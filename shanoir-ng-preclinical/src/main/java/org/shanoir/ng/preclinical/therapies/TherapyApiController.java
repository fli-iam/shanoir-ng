package org.shanoir.ng.preclinical.therapies;

import java.util.List;

import org.shanoir.ng.preclinical.references.RefsService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class TherapyApiController implements TherapyApi {

	private static final Logger LOG = LoggerFactory.getLogger(TherapyApiController.class);

	@Autowired
	private TherapyService therapiesService;
	@Autowired
	private RefsService referencesService;

	public ResponseEntity<Therapy> createTherapy(
			@ApiParam(value = "therapy to create", required = true) @RequestBody Therapy therapy, BindingResult result)
			throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(therapy);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(therapy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		therapy.setId(null);

		/* Save therapy in db. */
		try {
			final Therapy createdTherapy = therapiesService.save(therapy);
			return new ResponseEntity<Therapy>(createdTherapy, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

	}

	public ResponseEntity<Void> deleteTherapy(
			@ApiParam(value = "Therapy id to delete", required = true) @PathVariable("id") Long id) {
		if (therapiesService.findById(id) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			therapiesService.deleteById(id);
		} catch (ShanoirException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<Therapy> getTherapyById(
			@ApiParam(value = "ID of therapy that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final Therapy therapy = therapiesService.findById(id);
		if (therapy == null) {
			return new ResponseEntity<Therapy>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Therapy>(therapy, HttpStatus.OK);
	}

	public ResponseEntity<List<Therapy>> getTherapyByType(
			@ApiParam(value = "Type of therapies that needs to be fetched", required = true) @PathVariable("type") String type)
			throws RestServiceException {
		try {
			final List<Therapy> therapies = therapiesService.findByTherapyType(TherapyType.valueOf(type.toUpperCase()));
			if (therapies.isEmpty()) {
				return new ResponseEntity<List<Therapy>>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<Therapy>>(therapies, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Therapy>>(HttpStatus.NO_CONTENT);
		}
	}

	public ResponseEntity<List<Therapy>> getTherapies() {
		final List<Therapy> therapies = therapiesService.findAll();
		if (therapies.isEmpty()) {
			return new ResponseEntity<List<Therapy>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Therapy>>(therapies, HttpStatus.OK);
	}

	public ResponseEntity<Void> updateTherapy(
			@ApiParam(value = "ID of therapy that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Therapy object that needs to be updated", required = true) @RequestBody Therapy therapy,
			final BindingResult result) throws RestServiceException {

		therapy.setId(id);

		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(therapy);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(therapy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			therapiesService.update(therapy);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update therapy " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final Therapy therapy) {
		final Therapy previousStateTherapy = therapiesService.findById(therapy.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Therapy>().validate(previousStateTherapy,
				therapy);
		return accessErrors;
	}

	private FieldErrorMap getCreationRightsErrors(final Therapy therapy) {
		return new EditableOnlyByValidator<Therapy>().validate(therapy);
	}

	private FieldErrorMap getUniqueConstraintErrors(final Therapy therapy) {
		final UniqueValidator<Therapy> uniqueValidator = new UniqueValidator<Therapy>(therapiesService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(therapy);
		return uniqueErrors;
	}

}
