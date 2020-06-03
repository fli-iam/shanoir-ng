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

package org.shanoir.ng.studycard.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Study Card service implementation.
 *
 * @author msimon
 *
 */
@Service
public class StudyCardServiceImpl implements StudyCardService {

	@Autowired
	private StudyCardRepository studyCardRepository;

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException, MicroServiceCommunicationException {
		final StudyCard studyCard = studyCardRepository.findOne(id);
		if (studyCard == null) {
			throw new EntityNotFoundException(StudyCard.class, id);
		}
		studyCardRepository.delete(id);
	}

	@Override
	public List<StudyCard> findAll() {
		return Utils.toList(studyCardRepository.findAll());
	}

	@Override
	public StudyCard findById(final Long id) {
		return studyCardRepository.findOne(id);
	}

	@Override
	public StudyCard save(final StudyCard studyCard) throws MicroServiceCommunicationException {
		StudyCard savedStudyCard = studyCardRepository.save(studyCard);
		return savedStudyCard;
	}

	@Override
	public List<StudyCard> search(final List<Long> studyIdList) {
		return studyCardRepository.findByStudyIdIn(studyIdList);
	}

	@Override
	public StudyCard update(final StudyCard studyCard) throws EntityNotFoundException, MicroServiceCommunicationException {
		final StudyCard studyCardDb = studyCardRepository.findOne(studyCard.getId());
		if (studyCardDb == null) throw new EntityNotFoundException(StudyCard.class, studyCard.getId());
		updateStudyCardValues(studyCardDb, studyCard);
		studyCardRepository.save(studyCardDb);
		return studyCardDb;
	}


	/**
	 * Update some values of template to save them in database.
	 *
	 * @param templateDb template found in database.
	 * @param template template with new values.
	 * @return database template with new values.
	 */
	private StudyCard updateStudyCardValues(final StudyCard studyCardDb, final StudyCard studyCard) {
		studyCardDb.setName(studyCard.getName());
		studyCardDb.setDisabled(studyCard.isDisabled());
		studyCardDb.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		studyCardDb.setId(studyCard.getId());
		studyCardDb.setNiftiConverterId(studyCard.getNiftiConverterId());
		studyCardDb.setStudyId(studyCard.getStudyId());
		if (studyCardDb.getRules() == null) studyCardDb.setRules(new ArrayList<StudyCardRule>());
		else studyCardDb.getRules().clear();
		if (studyCard.getRules() != null) studyCardDb.getRules().addAll(studyCard.getRules());
		return studyCardDb;
	}

	@Override
	public List<StudyCard> findStudyCardsOfStudy(Long studyId) {
		return this.studyCardRepository.findByStudyId(studyId);
	}

	@Override
	public List<StudyCard> findStudyCardsByAcqEq(Long acqEqId) {
		return this.studyCardRepository.findByAcquisitionEquipmentId(acqEqId);
	}

}
