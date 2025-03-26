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

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Study Card service implementation.
 *
 * @author msimon
 *
 */
@Service
public class QualityCardServiceImpl implements QualityCardService {

    @Autowired
    private QualityCardRepository qualityCardRepository;

    @Override
    public void deleteById(final Long id) throws EntityNotFoundException, MicroServiceCommunicationException {
        final QualityCard qualityCard = qualityCardRepository.findById(id).orElse(null);
        if (qualityCard == null) {
            throw new EntityNotFoundException(QualityCard.class, id);
        }
        qualityCardRepository.deleteById(id);
    }

    @Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject, 'CAN_SEE_ALL')")
    public List<QualityCard> findAll() {
        return Utils.toList(qualityCardRepository.findAll());
    }

    @Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("returnObject == null || @datasetSecurityService.hasRightOnStudy(returnObject.getStudyId(), 'CAN_SEE_ALL')")
    public QualityCard findById(final Long id) {
        return qualityCardRepository.findById(id).orElse(null);
    }

    @Override
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#card.getStudyId(), 'CAN_ADMINISTRATE'))")
    public QualityCard save(final QualityCard card) throws MicroServiceCommunicationException {
        QualityCard savedQualityCard = qualityCardRepository.save(card);
        return savedQualityCard;
    }

    @Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject, 'CAN_SEE_ALL')")
    public List<QualityCard> search(final List<Long> studyIdList) {
        return qualityCardRepository.findByStudyIdIn(studyIdList);
    }

    @Override
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasUpdateRightOnCard(#card, 'CAN_ADMINISTRATE'))")
    public QualityCard update(final QualityCard card) throws EntityNotFoundException, MicroServiceCommunicationException {
        QualityCard qualityCardDb = qualityCardRepository.findById(card.getId()).orElse(null);
        if (qualityCardDb == null) throw new EntityNotFoundException(QualityCard.class, card.getId());
        qualityCardDb = updateQualityCardValues(qualityCardDb, card);
        qualityCardRepository.save(qualityCardDb);
        return qualityCardDb;
    }


    /**
     * Update some values of template to save them in database.
     *
     * @param templateDb template found in database.
     * @param template template with new values.
     * @return database template with new values.
     */
    private QualityCard updateQualityCardValues(final QualityCard qualityCardDb, final QualityCard qualityCard) {
        qualityCardDb.setName(qualityCard.getName());
        qualityCardDb.setId(qualityCard.getId());
        qualityCardDb.setStudyId(qualityCard.getStudyId());
        qualityCardDb.setToCheckAtImport(qualityCard.isToCheckAtImport());
        if (qualityCardDb.getRules() == null) qualityCardDb.setRules(new ArrayList<QualityExaminationRule>());
        else qualityCardDb.getRules().clear();
        if (qualityCard.getRules() != null) qualityCardDb.getRules().addAll(qualityCard.getRules());
        return qualityCardDb;
    }

    @Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterCardList(returnObject, 'CAN_SEE_ALL')")
    public List<QualityCard> findByStudy(Long studyId) {
        return this.qualityCardRepository.findByStudyId(studyId);
    }

    @Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    public QualityCard findByName(String name) {
        return qualityCardRepository.findByName(name);
    }

}
