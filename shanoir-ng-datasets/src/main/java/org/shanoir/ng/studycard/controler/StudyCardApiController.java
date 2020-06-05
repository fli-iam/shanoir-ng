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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.dto.DicomTag;
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
import org.dcm4che3.data.Tag;

import io.swagger.annotations.ApiParam;

@Controller
public class StudyCardApiController implements StudyCardApi {

	private static final String MICROSERVICE_COMMUNICATION_ERROR = "Microservice communication error";

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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), MICROSERVICE_COMMUNICATION_ERROR, null));
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
	public ResponseEntity<List<StudyCard>> findStudyCardByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		final List<StudyCard> studyCards = studyCardService.findStudyCardsOfStudy(studyId);
		if (studyCards.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studyCards, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<StudyCard>> findStudyCardByAcqEqId(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acqEqId") Long acqEqId) {
		final List<StudyCard> studyCards = studyCardService.findStudyCardsByAcqEq(acqEqId);
		if (studyCards.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(studyCards, HttpStatus.OK);
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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), MICROSERVICE_COMMUNICATION_ERROR, null));
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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), MICROSERVICE_COMMUNICATION_ERROR, null));
		}
	}
	

	
	@Override
	public ResponseEntity<List<DicomTag>> findDicomTags() throws RestServiceException {
		Field[] declaredFields = Tag.class.getDeclaredFields();
		List<DicomTag> dicomTags = new ArrayList<DicomTag>();
		try {
			for (Field field : declaredFields) {
			    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			    	if (field.getType().getName() == "int")
						dicomTags.add(new DicomTag(field.getInt(null), field.getName()));
			    	// longs actually code a date and a time, see Tag.class
			    	if (field.getType().getName() == "long") {
			    		String name = field.getName().replace("DateAndTime", "");
			    		String hexStr = String.format("%016X", field.getLong(null));
			    		String dateStr = hexStr.substring(0, 8);
			    		String timeStr = hexStr.substring(8);
			    		dicomTags.add(new DicomTag(Integer.parseInt(dateStr, 16), name + "Date"));
			    		dicomTags.add(new DicomTag(Integer.parseInt(timeStr, 16), name + "Time"));
			    	}
			    }
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Cannot parse the dcm4che lib Tag class static fields", e));
		}
		return new ResponseEntity<>(dicomTags, HttpStatus.OK);
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
