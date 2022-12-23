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
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
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
    public List<QualityCard> findAll() {
        return Utils.toList(qualityCardRepository.findAll());
    }

    @Override
    public QualityCard findById(final Long id) {
        return qualityCardRepository.findById(id).orElse(null);
    }

    @Override
    public QualityCard save(final QualityCard qualityCard) throws MicroServiceCommunicationException {
        QualityCard savedQualityCard = qualityCardRepository.save(qualityCard);
        return savedQualityCard;
    }

    @Override
    public List<QualityCard> search(final List<Long> studyIdList) {
        return qualityCardRepository.findByStudyIdIn(studyIdList);
    }

    @Override
    public QualityCard update(final QualityCard qualityCard) throws EntityNotFoundException, MicroServiceCommunicationException {
        final QualityCard qualityCardDb = qualityCardRepository.findById(qualityCard.getId()).orElse(null);
        if (qualityCardDb == null) throw new EntityNotFoundException(QualityCard.class, qualityCard.getId());
        updateQualityCardValues(qualityCardDb, qualityCard);
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
        if (qualityCardDb.getRules() == null) qualityCardDb.setRules(new ArrayList<QualityExaminationRule>());
        else qualityCardDb.getRules().clear();
        if (qualityCard.getRules() != null) qualityCardDb.getRules().addAll(qualityCard.getRules());
        return qualityCardDb;
    }

    @Override
    public List<QualityCard> findByStudy(Long studyId) {
        return this.qualityCardRepository.findByStudyId(studyId);
    }

    @Override
    public QualityCard findByName(String name) {
        return qualityCardRepository.findByName(name);
    }

}
