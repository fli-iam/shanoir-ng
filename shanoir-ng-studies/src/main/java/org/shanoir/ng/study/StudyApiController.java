package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudyApiController implements StudyApi {

	@Autowired
	private StudyService studyService;

	@Autowired
	private StudyMapper studyMapper;

	@Override
	public ResponseEntity<Void> deleteStudy(@PathVariable("studyId") Long studyId) {
		try {
			studyService.deleteById(studyId, KeycloakUtil.getTokenUserId());
		} catch (ShanoirStudiesException e) {
			if (ErrorModelCode.NO_RIGHT_FOR_ACTION.equals(e.getErrorCode())) {
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<StudyDTO>> findStudies() {
		List<Study> studies;
		try {
			if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
				// Return all studies if user has role "admin"
				studies = studyService.findAll();
			} else {
				studies = studyService.findStudiesByUserId(KeycloakUtil.getTokenUserId());
			}
		} catch (ShanoirStudiesException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studyMapper.studiesToStudyDTOs(studies), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdNameDTO>> findStudiesNames() {
		final List<IdNameDTO> studies = studyService.findIdsAndNames();
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studies, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SimpleStudyDTO>> findStudiesWithStudyCardsByUserId() {
		List<SimpleStudyDTO> studies;
		try {
			studies = studyService.findStudiesWithStudyCardsByUserId(KeycloakUtil.getTokenUserId());
		} catch (ShanoirStudiesException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studies, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<StudyDTO> findStudyById(@PathVariable("studyId") final Long studyId,
			@RequestParam(value = "withdata", required = false) Boolean withdata) {
		Study study;
		try {
			if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
				study = studyService.findById(studyId);
			} else {
				study = studyService.findById(studyId, KeycloakUtil.getTokenUserId());
			}
		} catch (ShanoirStudiesException e) {
			if (ErrorModelCode.NO_RIGHT_FOR_ACTION.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (study == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (withdata) {
			return new ResponseEntity<>(studyMapper.studyToStudyDTO(study), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(studyMapper.studyToSimpleStudyDTO(study), HttpStatus.OK);

		}
	}

	@Override
	public ResponseEntity<StudyDTO> saveNewStudy(@RequestBody final Study study, final BindingResult result)
			throws RestServiceException {
		/* Validation */
		// A basic study can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(study);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constraint
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(study);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		study.setId(null);

		/* Save study in db. */
		try {
			final Study createdStudy = studyService.save(study);
			return new ResponseEntity<>(studyMapper.studyToStudyDTO(createdStudy), HttpStatus.OK);
		} catch (final ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Void> updateStudy(@PathVariable("studyId") final Long studyId, @RequestBody final Study study,
			final BindingResult result) throws RestServiceException {
		try {
			// Check if user can update study
			if (!studyService.canUserUpdateStudy(studyId, KeycloakUtil.getTokenUserId())) {
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
			}
		} catch (ShanoirStudiesException e1) {
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		study.setId(studyId);

		// A basic study can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(study);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constraint
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(study);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update study in db. */
		try {
			studyService.update(study);
		} catch (final ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param study study.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Study study) {
		final Study previousStateStudy = studyService.findById(study.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Study>().validate(previousStateStudy, study);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param study study.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final Study study) {
		return new EditableOnlyByValidator<Study>().validate(study);
	}

	/*
	 * Get unique constraint errors.
	 *
	 * @param study study.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Study study) {
		final UniqueValidator<Study> uniqueValidator = new UniqueValidator<Study>(studyService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(study);
		return uniqueErrors;
	}

}
