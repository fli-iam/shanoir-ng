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

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.studycard.service.QualityCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;

@Controller
public class QualityCardApiController implements QualityCardApi {

    private static final String PACS_COMMUNICATION_ERROR = "Error during PACS communication while applying quality card on study";

    private static final Logger LOG = LoggerFactory.getLogger(QualityCardApiController.class);

    @Autowired
    private QualityCardService service;

    @Autowired
    private QualityCardRepository repository;

    public ResponseEntity<Void> deleteQualityCard(Long qualityCardId) {
        repository.deleteById(qualityCardId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<QualityCard> findQualityCardById(Long qualityCardId) {
        QualityCard qualityCard = repository.findById(qualityCardId).orElse(null);
        if (qualityCard == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(qualityCard, HttpStatus.OK);
    }


    public ResponseEntity<List<QualityCard>> findQualityCardByStudyId(Long studyId) {
        List<QualityCard> qualityCards = repository.findByStudyId(studyId);
        if (qualityCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(qualityCards, HttpStatus.OK);
    }

    public ResponseEntity<List<QualityCard>> findQualityCards() {
        List<QualityCard> qualityCards = repository.findAll();
        if (qualityCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(qualityCards, HttpStatus.OK);
    }

    public ResponseEntity<QualityCard> saveNewQualityCard(QualityCard qualityCard, BindingResult result) throws RestServiceException {
        service.validate(qualityCard, result);
        service.resetIdsForFreshInsert(qualityCard);
        qualityCard = repository.save(qualityCard);
        return new ResponseEntity<>(qualityCard, HttpStatus.OK);
    }

    public ResponseEntity<Void> updateQualityCard(Long qualityCardId, QualityCard qualityCard, BindingResult result) throws RestServiceException {
        service.validate(qualityCard, result);
        try {
            service.update(qualityCard);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<QualityCardResult> applyQualityCardOnStudy(Long qualityCardId) throws RestServiceException {
        QualityCard qualityCard = repository.findById(qualityCardId).orElse(null);
        if (qualityCard == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        LOG.info("apply quality card: name:" + qualityCard.getName() + ", studyId: " + qualityCard.getStudyId());
        QualityCardResult results = null;
        try {
            results = service.applyQualityCardOnStudy(qualityCard, true);
        } catch (PacsException e) {
            LOG.error(PACS_COMMUNICATION_ERROR, e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), PACS_COMMUNICATION_ERROR, null));
        }
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    public ResponseEntity<QualityCardResult> testQualityCardOnStudy(Long qualityCardId, Integer from, Integer to) throws RestServiceException {
        QualityCard qualityCard = repository.findById(qualityCardId).orElse(null);
        if (qualityCard == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        QualityCardResult results = null;
        LOG.info("test quality card: name:" + qualityCard.getName() + ", studyId: " + qualityCard.getStudyId());
        try {
            results = service.applyQualityCardOnStudy(qualityCard, false, from, to);
        } catch (PacsException e) {
            LOG.error(PACS_COMMUNICATION_ERROR, e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), PACS_COMMUNICATION_ERROR, e));
        }
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
