/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.studycard.controler;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.service.CardsProcessingService;
import org.shanoir.ng.studycard.service.QualityCardService;
import org.shanoir.ng.studycard.service.QualityCardUniqueConstraintManager;
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
public class QualityCardApiController implements QualityCardApi {

	private static final String MICROSERVICE_COMMUNICATION_ERROR = "Microservice communication error";
	
	private static final Logger LOG = LoggerFactory.getLogger(QualityCardApiController.class);
	
    @Autowired
    private QualityCardService qualityCardService;
	
	@Autowired
	private QualityCardUniqueConstraintManager uniqueConstraintManager;

	@Autowired
	private CardsProcessingService cardProcessingService;

	@Override
	public ResponseEntity<Void> deleteQualityCard(
			@ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException {
		try {
			qualityCardService.deleteById(qualityCardId);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), MICROSERVICE_COMMUNICATION_ERROR, null));
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<QualityCard> findQualityCardById(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("qualityCardId") Long qualityCardId) {
		final QualityCard qualityCard = qualityCardService.findById(qualityCardId);
		if (qualityCard == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(qualityCard, HttpStatus.OK);
	}
	

	@Override
	public ResponseEntity<List<QualityCard>> findQualityCardByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		final List<QualityCard> qualityCards = qualityCardService.findByStudy(studyId);
		if (qualityCards.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(qualityCards, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<QualityCard>> findQualityCards() {
		final List<QualityCard> qualityCards = qualityCardService.findAll();
		if (qualityCards.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(qualityCards, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<QualityCard> saveNewQualityCard(
			@ApiParam(value = "Quality Card to create", required = true) @RequestBody QualityCard qualityCard,
			final BindingResult result) throws RestServiceException {
		validate(qualityCard, result);
		QualityCard createdQualityCard;
		try {
			createdQualityCard = qualityCardService.save(qualityCard);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), MICROSERVICE_COMMUNICATION_ERROR, null));
		}
		return new ResponseEntity<>(createdQualityCard, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateQualityCard(
			@ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId,
			@ApiParam(value = "quality card to update", required = true) @RequestBody QualityCard qualityCard,
			final BindingResult result) throws RestServiceException {
		validate(qualityCard, result);
		try {
			qualityCardService.update(qualityCard);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), MICROSERVICE_COMMUNICATION_ERROR, null));
		}
	}

	/**
	 * Validate a quality card
	 * 
	 * @param qualityCard
	 * @param result
	 * @throws RestServiceException
	 */
	protected void validate(QualityCard qualityCard, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap()
				.add(new FieldErrorMap(result))
				.add(uniqueConstraintManager.validate(qualityCard));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}

	@Override
    public ResponseEntity<QualityCardResult> applyQualityCardOnStudy(
	        @ApiParam(value = "id of the quality card", required = true) @PathVariable("qualityCardId") Long qualityCardId) throws RestServiceException, MicroServiceCommunicationException {
		
	    final QualityCard qualityCard = qualityCardService.findById(qualityCardId);
	    if (qualityCard == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		LOG.info("apply quality card: name:" + qualityCard.getName() + ", studyId: " + qualityCard.getStudyId());
		QualityCardResult results = cardProcessingService.applyQualityCardOnStudy(qualityCard);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

}
