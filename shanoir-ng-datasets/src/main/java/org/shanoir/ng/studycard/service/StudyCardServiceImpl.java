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

import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.dto.DicomTag;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.VM;
import org.shanoir.ng.studycard.model.rule.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
		final StudyCard studyCard = studyCardRepository.findById(id).orElse(null);
		if (studyCard == null) {
			throw new EntityNotFoundException(StudyCard.class, id);
		}
		studyCardRepository.deleteById(id);
	}

	@Override
	public List<DicomTag> findDicomTags() throws RestServiceException {
		Field[] declaredFields = Tag.class.getDeclaredFields();
		List<DicomTag> dicomTags = new ArrayList<DicomTag>();
		try {
			for (Field field : declaredFields) {
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
					if (field.getType().getName() == "int") {
						int tagCode = field.getInt(null);
						VR tagVr = StandardElementDictionary.INSTANCE.vrOf(tagCode);
						VM tagVm = VM.of(tagCode);
						DicomTagType tagType = DicomTagType.valueOf(tagVr, tagVm);
						dicomTags.add(new DicomTag(tagCode, field.getName(), tagType, tagVm));
					}
					// longs actually code a date and a time, see Tag.class
					else if (field.getType().getName() == "long") {
						String name = field.getName().replace("DateAndTime", "");
						String hexStr = String.format("%016X", field.getLong(null));
						String dateStr = hexStr.substring(0, 8);
						String timeStr = hexStr.substring(8);
						int dateTagCode = Integer.parseInt(dateStr, 16);
						int timeTagCode = Integer.parseInt(timeStr, 16);
						VM dateVm = VM.of(dateTagCode);
						VM timeVm = VM.of(timeTagCode);
						DicomTagType dateTagType = DicomTagType.valueOf(StandardElementDictionary.INSTANCE.vrOf(dateTagCode), dateVm);
						DicomTagType timeTagType = DicomTagType.valueOf(StandardElementDictionary.INSTANCE.vrOf(timeTagCode), timeVm);
						dicomTags.add(new DicomTag(dateTagCode, name + "Date", dateTagType, dateVm));
						dicomTags.add(new DicomTag(timeTagCode, name + "Time", timeTagType, timeVm));
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Cannot parse the dcm4che lib Tag class static fields", e));
		}
		return dicomTags;
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject, 'CAN_SEE_ALL')")
	public List<StudyCard> findAll() {
		return Utils.toList(studyCardRepository.findAll());
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("returnObject == null || @datasetSecurityService.hasRightOnStudy(returnObject.getStudyId(), 'CAN_SEE_ALL')")
	public StudyCard findById(final Long id) {
		return studyCardRepository.findById(id).orElse(null);
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#card.getStudyId(), 'CAN_ADMINISTRATE'))")
	public StudyCard save(final StudyCard card) throws MicroServiceCommunicationException {
	    card.setLastEditTimestamp(System.currentTimeMillis());
		StudyCard savedStudyCard = studyCardRepository.save(card);
		return savedStudyCard;
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject, 'CAN_SEE_ALL')")
	public List<StudyCard> search(final List<Long> studyIdList) {
		return studyCardRepository.findByStudyIdIn(studyIdList);
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasUpdateRightOnCard(#card, 'CAN_ADMINISTRATE'))")
	public StudyCard update(final StudyCard card) throws EntityNotFoundException, MicroServiceCommunicationException {
		final StudyCard studyCardDb = studyCardRepository.findById(card.getId()).orElse(null);
		if (studyCardDb == null) throw new EntityNotFoundException(StudyCard.class, card.getId());
		updateStudyCardValues(studyCardDb, card);
		studyCardDb.setLastEditTimestamp(System.currentTimeMillis());
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
		if (studyCardDb.getRules() == null) studyCardDb.setRules(new ArrayList<StudyCardRule<?>>());
		else studyCardDb.getRules().clear();
		if (studyCard.getRules() != null) studyCardDb.getRules().addAll(studyCard.getRules());
		return studyCardDb;
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject, 'CAN_SEE_ALL')")
	public List<StudyCard> findByStudy(Long studyId) {
		return this.studyCardRepository.findByStudyId(studyId);
	}

	@Override
	public List<StudyCard> findStudyCardsByAcqEq(Long acqEqId) {
		return this.studyCardRepository.findByAcquisitionEquipmentId(acqEqId);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	public StudyCard findByName(String name) {
		return studyCardRepository.findByName(name);
	}

}
