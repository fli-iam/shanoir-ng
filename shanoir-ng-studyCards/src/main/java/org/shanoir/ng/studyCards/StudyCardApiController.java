package org.shanoir.ng.studyCards;

import java.util.List;


import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudyCardsException;
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
public class StudyCardApiController implements StudyCardApi  {

	private static final Logger LOG = LoggerFactory.getLogger(StudyCardApiController.class);

	@Autowired
	private StudyCardService studyCardService;

	@Override
	public ResponseEntity<Void> deleteStudyCard
		(@ApiParam(value = "id of the study card",required=true ) @PathVariable("studyCardId") Long studyCardId) {
		if (studyCardService.findById(studyCardId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			studyCardService.deleteById(studyCardId);
		} catch (ShanoirStudyCardsException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<StudyCard> findStudyCardById(
				@ApiParam(value = "id of the study card",required=true ) @PathVariable("studyCardId") Long studyCardId) {
		final StudyCard studyCard = studyCardService.findById(studyCardId);
		if (studyCard == null) {
			return new ResponseEntity<StudyCard>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<StudyCard>(studyCard, HttpStatus.OK);
	}

	@Override
	  public ResponseEntity<List<StudyCard>> findStudyCards() {
		final List<StudyCard> studyCards = studyCardService.findAll();
		if (studyCards.isEmpty()) {
			return new ResponseEntity<List<StudyCard>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<StudyCard>>(studyCards, HttpStatus.OK);
	}

	//@Override
	  public ResponseEntity<StudyCard> saveNewStudyCard(
			@ApiParam(value = "study Card to create" ,required=true ) @RequestBody StudyCard studyCard, final BindingResult result) throws RestServiceException {

		/* Validation */
		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(studyCard);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(studyCard);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		studyCard.setId(null);

		/* Save template in db. */
		try {
			final StudyCard createdStudyCard = studyCardService.save(studyCard);
			return new ResponseEntity<StudyCard>(createdStudyCard, HttpStatus.OK);
		} catch (ShanoirStudyCardsException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	 public ResponseEntity<Void> updateStudyCard(@ApiParam(value = "id of the study card",required=true ) @PathVariable("studyCardId") Long studyCardId,
		        @ApiParam(value = "study card to update" ,required=true ) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		studyCard.setId(studyCardId);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(studyCard);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(studyCard);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			studyCardService.update(studyCard);
		} catch (ShanoirStudyCardsException e) {
			LOG.error("Error while trying to update template " + studyCardId + " : ", e);
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
	private FieldErrorMap getUpdateRightsErrors(final StudyCard studyCard) {
		final StudyCard previousStateTemplate = studyCardService.findById(studyCard.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<StudyCard>().validate(previousStateTemplate, studyCard);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final StudyCard studyCard) {
		return new EditableOnlyByValidator<StudyCard>().validate(studyCard);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param template
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final StudyCard studyCard) {
		final UniqueValidator<StudyCard> uniqueValidator = new UniqueValidator<StudyCard>(studyCardService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(studyCard);
		return uniqueErrors;
	}

	@Override
    public ResponseEntity<StudyCard> findStudyCardsByStudyId(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId) {
        // do some magic!
        return new ResponseEntity<StudyCard>(HttpStatus.OK);
    }
	
	

}
