package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.ApiParam;

@Controller
public class StudyApiController implements StudyApi {

	@Autowired
	private StudyService studyService;

	@Override
	public ResponseEntity<Void> deleteStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		try {
			studyService.deleteById(studyId);
		} catch (ShanoirStudiesException e) {
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<Study>> findStudies() {
		final List<Study> studies = studyService.findAll();
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studies, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Study>> findStudiesByUserId() {
		List<Study> studies;
		try {
			studies = studyService.findStudiesByUserId(KeycloakUtil.getTokenUserId());
		} catch (ShanoirStudiesException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
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
	public ResponseEntity<Study> findStudyById(final Long studyId) {
		final Study study = studyService.findById(studyId);
		if (study == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(study, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Study> saveNewStudy(final Study study) throws RestServiceException {
		// Guarantees it is a creation, not an update
		study.setId(null);

		/* Save user in db. */
		try {
			final Study createdStudy = studyService.save(study);
			return new ResponseEntity<>(createdStudy, HttpStatus.OK);
		} catch (final ShanoirStudiesException e) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Void> updateStudy(final Long studyId, final Study study) throws RestServiceException {
		study.setId(studyId);
		
		/* Update study in db. */
		try {
			studyService.update(study);
		} catch (final ShanoirStudiesException e) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
