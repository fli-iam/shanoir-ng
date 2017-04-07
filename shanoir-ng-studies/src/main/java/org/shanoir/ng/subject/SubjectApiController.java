package org.shanoir.ng.subject;

import java.util.List;


import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirSubjectException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.shanoir.ng.study.Study;
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
public class SubjectApiController implements SubjectApi  {
	
	

	private static final Logger LOG = LoggerFactory.getLogger(SubjectApiController.class);

	@Autowired
	private SubjectService subjectService;

	@Override
	public ResponseEntity<Void> deleteSubject
		(@ApiParam(value = "id of the study card",required=true ) @PathVariable("studyCardId") Long studyCardId) {
		if (subjectService.findById(studyCardId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			subjectService.deleteById(studyCardId);
		} catch (ShanoirSubjectException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Subject> findSubjectById(
				@ApiParam(value = "id of the subject",required=true ) @PathVariable("subjectId") Long subjectId) {
		final Subject subject = subjectService.findById(subjectId);
		if (subject == null) {
			return new ResponseEntity<Subject>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Subject>(subject, HttpStatus.OK);
	}

	@Override
	  public ResponseEntity<List<Subject>> findSubjects() {
		final List<Subject> subject = subjectService.findAll();
		if (subject.isEmpty()) {
			return new ResponseEntity<List<Subject>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Subject>>(subject, HttpStatus.OK);
	}

	//@Override
	  public ResponseEntity<Subject> saveNewSubject(
			@ApiParam(value = "subject to create" ,required=true ) @RequestBody Subject subject, final BindingResult result) throws RestServiceException {

		/* Validation */
		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(subject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		subject.setId(null);

		/* Save template in db. */
		try {
			final Subject createdSubject = subjectService.save(subject);
			return new ResponseEntity<Subject>(createdSubject, HttpStatus.OK);
		} catch (ShanoirSubjectException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	 public ResponseEntity<Void> updateSubject(@ApiParam(value = "id of the subject",required=true ) @PathVariable("subjectId") Long subjectId,
		        @ApiParam(value = "subject to update" ,required=true ) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		subject.setId(subjectId);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(subject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			subjectService.update(subject);
		} catch (ShanoirSubjectException e) {
			LOG.error("Error while trying to update subject " + subjectId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Subject subject) {
		final Subject previousStateTemplate = subjectService.findById(subject.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Subject>().validate(previousStateTemplate, subject);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final Subject subject) {
		return new EditableOnlyByValidator<Subject>().validate(subject);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param template
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Subject subject) {
		final UniqueValidator<Subject> uniqueValidator = new UniqueValidator<Subject>(subjectService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(subject);
		return uniqueErrors;
	}

	@Override
	public ResponseEntity<List<Subject>> findSubjectsByStudyId(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId) {
		
		final List<Subject> subjects = subjectService.findAllSubjectsOfStudy(studyId);
		if (subjects.isEmpty()) {
			return new ResponseEntity<List<Subject>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Subject>>(subjects, HttpStatus.OK);
		
	}
	
	

}
