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
import java.util.Map;
import java.util.stream.Collectors;

import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Study Card service implementation.
 *
 * @author msimon
 *
 */
@Service
public class StudyCardServiceImpl implements StudyCardService {

	@Autowired
	private StudyUserRightsRepository studyUserRepo;

	@Autowired
	private StudyCardRepository studyCardRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyCardService.class);

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException, MicroServiceCommunicationException {
		final StudyCard studyCard = studyCardRepository.findById(id).orElse(null);
		if (studyCard == null) {
			throw new EntityNotFoundException(StudyCard.class, id);
		}
		studyCardRepository.deleteById(id);
	}

	@Override
	public List<StudyCard> findAll() {
		return Utils.toList(studyCardRepository.findAll());
	}

	@Override
	public StudyCard findById(final Long id) {
		return studyCardRepository.findById(id).orElse(null);
	}

	@Override
	public StudyCard save(final StudyCard studyCard) throws MicroServiceCommunicationException {
		studyCard.setLastEditTimestamp(System.currentTimeMillis());
		StudyCard savedStudyCard = studyCardRepository.save(studyCard);
		return savedStudyCard;
	}

	@Override
	public List<StudyCard> search(final List<Long> studyIdList) {
		return studyCardRepository.findByStudyIdIn(studyIdList);
	}

	@Override
	public StudyCard update(final StudyCard studyCard) throws EntityNotFoundException, MicroServiceCommunicationException {
		final StudyCard studyCardDb = studyCardRepository.findById(studyCard.getId()).orElse(null);
		if (studyCardDb == null) throw new EntityNotFoundException(StudyCard.class, studyCard.getId());
		updateStudyCardValues(studyCardDb, studyCard);
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
		if (studyCardDb.getRules() == null) studyCardDb.setRules(new ArrayList<StudyCardRule>());
		else studyCardDb.getRules().clear();
		if (studyCard.getRules() != null) studyCardDb.getRules().addAll(studyCard.getRules());
		return studyCardDb;
	}

	@Override
	public List<StudyCard> findStudyCardsOfStudy(Long studyId) {
		List<StudyCard> studyCards = this.studyCardRepository.findByStudyId(studyId);
		StudyUser studyUser = studyUserRepo.findByUserIdAndStudyId(KeycloakUtil.getTokenUserId(), studyId);
		List<Long> centers = studyUser.getCenterIds();

		// if we have a limit by center, filter
		if (!CollectionUtils.isEmpty(centers)) {
			try {
				List<Long> equipments = new ArrayList<Long>();
				for(StudyCard studycard : studyCards) {
					equipments.add(studycard.getAcquisitionEquipmentId());
				}
				
				Map<Long, Long> centersMap = (Map<Long, Long>) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_CENTER_QUEUE, equipments);
				studyCards = studyCards.stream().filter(studyCard -> centers.contains(centersMap.get(studyCard.getAcquisitionEquipmentId()))).collect(Collectors.toList());
			} catch (Exception e) {
				LOG.error("Error while loading study cards limited by center: ", e);
				return studyCards;
			}
		}
		
		return studyCards;
	}

	@Override
	public List<StudyCard> findStudyCardsByAcqEq(Long acqEqId) {
		return this.studyCardRepository.findByAcquisitionEquipmentId(acqEqId);
	}

	@Override
	public StudyCard findByName(String name) {
		return studyCardRepository.findByName(name);
	}

}
