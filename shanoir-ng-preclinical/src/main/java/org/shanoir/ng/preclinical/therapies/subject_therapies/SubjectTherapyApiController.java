package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyService;
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
public class SubjectTherapyApiController implements SubjectTherapyApi {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectTherapyApiController.class);

	@Autowired
	private SubjectTherapyService subtherapiesService;
	@Autowired
	private AnimalSubjectService subjectService;
	@Autowired
	private TherapyService therapyService;

	public ResponseEntity<SubjectTherapy> addSubjectTherapy(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "therapy to add to subject", required = true) @RequestBody SubjectTherapy subtherapy,
			BindingResult result) throws RestServiceException {

		// First check if given user exists
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "AnimalSubject not found", new ErrorDetails()));
		} else {
			final FieldErrorMap accessErrors = this.getCreationRightsErrors(subtherapy);
			final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
			final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subtherapy);
			/* Merge errors. */
			final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
			if (!errors.isEmpty()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
						new ErrorDetails(errors)));
			}

			// Guarantees it is a creation, not an update
			subtherapy.setId(null);
			// Just in case
			try {
				subtherapy.setAnimalSubject(animalSubject);
			} catch (Exception e) {
				LOG.error("Error while parsing subject id for Long cast " + e.getMessage(), e);
			}

			/* Save subject therapy in db. */
			try {
				final SubjectTherapy createdSubTherapy = subtherapiesService.save(subtherapy);
				return new ResponseEntity<SubjectTherapy>(createdSubTherapy, HttpStatus.OK);
			} catch (ShanoirException e) {
				throw new RestServiceException(e,
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
		}

	}

	public ResponseEntity<Void> deleteSubjectTherapy(
			@ApiParam(value = "Subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "subject therapy id to delete", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException {

		// First check if given user exists
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "AnimalSubject not found", new ErrorDetails()));
		} else {
			SubjectTherapy toDelete = subtherapiesService.findById(tid);
			if (toDelete == null) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}
			try {
				subtherapiesService.deleteById(toDelete.getId());
			} catch (ShanoirException e) {
				return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		}

	}

	public ResponseEntity<Void> deleteSubjectTherapies(
			@ApiParam(value = "animal subject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} else {
			try {
				subtherapiesService.deleteByAnimalSubject(animalSubject);
			} catch (ShanoirException e) {
				LOG.error("ERROR while deleting therapies for subject " + animalSubject.getId(), e);
				return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	public ResponseEntity<SubjectTherapy> getSubjectTherapyById(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject therapy that needs to be fetched", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "AnimalSubject not found", new ErrorDetails()));
		} else {
			final SubjectTherapy subtherapy = subtherapiesService.findById(tid);
			if (subtherapy == null) {
				return new ResponseEntity<SubjectTherapy>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<SubjectTherapy>(subtherapy, HttpStatus.OK);
		}
	}

	public ResponseEntity<List<SubjectTherapy>> getSubjectTherapies(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id) throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Subject not found", new ErrorDetails()));
		} else {
			final List<SubjectTherapy> subtherapies = subtherapiesService.findAllByAnimalSubject(animalSubject);
			return new ResponseEntity<List<SubjectTherapy>>(subtherapies, HttpStatus.OK);
		}
	}

	public ResponseEntity<List<SubjectTherapy>> getSubjectTherapiesByTherapy(
			@ApiParam(value = "therapy id", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException {
		Therapy therapy = therapyService.findById(tid);
		if (therapy == null) {
			return new ResponseEntity<List<SubjectTherapy>>(HttpStatus.NOT_FOUND);
		} else {
			final List<SubjectTherapy> subtherapies = subtherapiesService.findAllByTherapy(therapy);
			if (subtherapies.isEmpty()) {
				return new ResponseEntity<List<SubjectTherapy>>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<SubjectTherapy>>(subtherapies, HttpStatus.OK);
		}
	}

	public ResponseEntity<Void> updateSubjectTherapy(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject therapy that needs to be updated", required = true) @PathVariable("tid") Long tid,
			@ApiParam(value = "Subject therapy object that needs to be updated", required = true) @RequestBody SubjectTherapy subtherapy,
			final BindingResult result) throws RestServiceException {

		subtherapy.setId(tid);
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(subtherapy);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subtherapy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			subtherapiesService.update(subtherapy);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update subject therapy " + tid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final SubjectTherapy subtherapy) {
		final SubjectTherapy previousStateTherapy = subtherapiesService.findById(subtherapy.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<SubjectTherapy>().validate(previousStateTherapy,
				subtherapy);
		return accessErrors;
	}

	private FieldErrorMap getCreationRightsErrors(final SubjectTherapy therapies) {
		return new EditableOnlyByValidator<SubjectTherapy>().validate(therapies);
	}

	private FieldErrorMap getUniqueConstraintErrors(final SubjectTherapy therapies) {
		final UniqueValidator<SubjectTherapy> uniqueValidator = new UniqueValidator<SubjectTherapy>(
				subtherapiesService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(therapies);
		return uniqueErrors;
	}

}
