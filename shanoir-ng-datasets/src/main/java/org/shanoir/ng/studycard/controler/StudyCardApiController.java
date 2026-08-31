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

import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.model.DicomTag;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardApply;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.StudyCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;

@Controller
public class StudyCardApiController implements StudyCardApi {

    private static final String PACS_COMMUNICATION_ERROR = "Error during PACS communication while applying quality card on study";

    private static final Logger LOG = LoggerFactory.getLogger(StudyCardApiController.class);

    @Autowired
    private StudyCardService service;

    @Autowired
    private StudyCardRepository repository;

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    public ResponseEntity<Void> deleteStudyCard(Long studyCardId) throws RestServiceException {
        if (datasetAcquisitionService.existsByStudyCardId(studyCardId)) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "This study card is linked to at least one dataset acquisition."
                    ));
        }
        repository.deleteById(studyCardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<StudyCard> findStudyCardById(Long studyCardId) {
        StudyCard studyCard = repository.findById(studyCardId).orElse(null);
        if (studyCard == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studyCard, HttpStatus.OK);
    }

    public ResponseEntity<List<StudyCard>> findStudyCardByStudyId(Long studyId) {
        final List<StudyCard> studyCards = repository.findByStudyId(studyId);
        if (studyCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyCards, HttpStatus.OK);
    }

    public ResponseEntity<List<StudyCard>> findStudyCardByAcqEqId(Long acqEqId) {
        List<StudyCard> studyCards = repository.findByAcquisitionEquipmentId(acqEqId);
        if (studyCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyCards, HttpStatus.OK);
    }

    public ResponseEntity<List<StudyCard>> findStudyCards() {
        List<StudyCard> studyCards = repository.findAll();
        if (studyCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyCards, HttpStatus.OK);
    }

    public ResponseEntity<StudyCard> saveNewStudyCard(StudyCard studyCard, BindingResult result) throws RestServiceException {
        service.validate(studyCard, result);
        studyCard.setLastEditTimestamp(System.currentTimeMillis());
        studyCard = repository.save(studyCard);
        return new ResponseEntity<>(studyCard, HttpStatus.OK);
    }

    // Attention: used by ShanoirUploader!
    public ResponseEntity<List<StudyCard>> searchStudyCards(IdList studyIds) {
        List<StudyCard> studyCards = repository.findByStudyIdIn(studyIds.getIdList());
        if (studyCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyCards, HttpStatus.OK);
    }

    public ResponseEntity<Void> updateStudyCard(Long studyCardId, StudyCard studyCard, BindingResult result) throws RestServiceException {
        service.validate(studyCard, result);
        try {
            service.update(studyCard);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<List<DicomTag>> findDicomTags() throws RestServiceException {
        return new ResponseEntity<>(service.findDicomTags(), HttpStatus.OK);
    }

    public ResponseEntity<Void> applyStudyCard(StudyCardApply studyCardApplyObject) throws RestServiceException {
        if (studyCardApplyObject == null
                || studyCardApplyObject.getDatasetAcquisitionIds() == null
                || studyCardApplyObject.getDatasetAcquisitionIds().isEmpty()
                || studyCardApplyObject.getStudyCardId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        StudyCard studyCard = repository.findById(studyCardApplyObject.getStudyCardId()).orElse(null);
        if (studyCard == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            service.applyStudyCard(studyCard, studyCardApplyObject);
        } catch (PacsException | EntityNotFoundException e) {
            LOG.error("Study card could not be applied for acquisitions {}", studyCardApplyObject.getDatasetAcquisitionIds(), e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), PACS_COMMUNICATION_ERROR, e));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
