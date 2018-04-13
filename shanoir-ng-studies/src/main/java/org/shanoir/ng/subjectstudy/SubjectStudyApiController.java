package org.shanoir.ng.subjectstudy;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
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
public class SubjectStudyApiController implements SubjectStudyApi {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectStudyApiController.class);
	
	@Autowired
	private SubjectStudyService subjectStudyService;

	/*
	 * Get access rights errors.
	 *
	 * @param SubjectStudy subjectStudy.
	 *
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final SubjectStudy subjectStudy) {
		final SubjectStudy previousStateTemplate = subjectStudyService.findById(subjectStudy.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<SubjectStudy>().validate(previousStateTemplate,
				subjectStudy);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param SubjectStudy subjectStudy.
	 *
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final SubjectStudy subjectStudy) {
		return new EditableOnlyByValidator<SubjectStudy>().validate(subjectStudy);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param SubjectStudy subjectStudy
	 *
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final SubjectStudy subjectStudy) {
		final UniqueValidator<SubjectStudy> uniqueValidator = new UniqueValidator<SubjectStudy>(subjectStudyService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(subjectStudy);
		return uniqueErrors;
	}

	@Override
	public ResponseEntity<Void> updateSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId,
			@ApiParam(value = "subject study to update", required = true) @RequestBody SubjectStudy subjectStudy,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		subjectStudy.setId(subjectStudyId);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(subjectStudy);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subjectStudy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			subjectStudyService.update(subjectStudy);
		} catch (ShanoirStudiesException e) {
			LOG.error("Error while trying to update subject study " + subjectStudyId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	public ResponseEntity<SubjectStudy> saveNewSubjectStudy(
			@ApiParam(value = "subject study to create", required = true) @RequestBody SubjectStudy subjectStudy,
			final BindingResult result) throws RestServiceException {

		/* Validation */
		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(subjectStudy);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subjectStudy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			final SubjectStudy createdSubjectStudy = subjectStudyService.save(subjectStudy);
			//subjectStudyService.updateShanoirOld(createdSubject);
			return new ResponseEntity<SubjectStudy>(createdSubjectStudy, HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}
	
	@Override
	public ResponseEntity<Void> deleteSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId) {
		if (subjectStudyService.findById(subjectStudyId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			subjectStudyService.deleteById(subjectStudyId);
		} catch (ShanoirStudiesException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<SubjectStudy> findSubjectStudyById(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectStudyId) {
		final SubjectStudy subjectStudy = subjectStudyService.findById(subjectStudyId);
		if (subjectStudy == null) {
			return new ResponseEntity<SubjectStudy>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(subjectStudy, HttpStatus.OK);
	}


}
