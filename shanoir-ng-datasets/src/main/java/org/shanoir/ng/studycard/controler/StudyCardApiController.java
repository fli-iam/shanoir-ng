package org.shanoir.ng.studycard.controler;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.service.StudyCardService;
import org.shanoir.ng.studycard.service.StudyCardUniqueConstraintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class StudyCardApiController implements StudyCardApi {

	@Autowired
	private StudyCardService studyCardService;
	
	@Autowired
	private StudyCardUniqueConstraintManager uniqueConstraintManager;

	@Override
	public ResponseEntity<Void> deleteStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) throws RestServiceException {
		
		try {
			studyCardService.deleteById(studyCardId);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<StudyCard> findStudyCardById(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) {
		final StudyCard studyCard = studyCardService.findById(studyCardId);
		if (studyCard == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(studyCard, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<StudyCard>> findStudyCards() {
		final List<StudyCard> studyCards = studyCardService.findAll();
		if (studyCards.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studyCards, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<StudyCard> saveNewStudyCard(
			@ApiParam(value = "study Card to create", required = true) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException {

		validate(studyCard, result);

		StudyCard createdStudyCard;
		try {
			createdStudyCard = studyCardService.save(studyCard);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<>(createdStudyCard, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<StudyCard>> searchStudyCards(
			@ApiParam(value = "study ids", required = true) @RequestBody final IdList studyIds) {
		
		final List<StudyCard> studyCards = studyCardService.search(studyIds.getIdList());
		if (studyCards.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studyCards, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId,
			@ApiParam(value = "study card to update", required = true) @RequestBody StudyCard studyCard,
			final BindingResult result) throws RestServiceException {

		validate(studyCard, result);

		try {
			studyCardService.update(studyCard);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
	}

	@Override
	public ResponseEntity<Long> searchCenterId(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) {
		
		final Long centerId = studyCardService.searchCenterId(studyCardId);
		if (centerId == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(centerId, HttpStatus.OK);
	}

	/**
	 * Validate a studyCard
	 * 
	 * @param studyCard
	 * @param result
	 * @throws RestServiceException
	 */
	protected void validate(StudyCard studyCard, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap()
				.add(new FieldErrorMap(result))
				.add(uniqueConstraintManager.validate(studyCard));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
}
