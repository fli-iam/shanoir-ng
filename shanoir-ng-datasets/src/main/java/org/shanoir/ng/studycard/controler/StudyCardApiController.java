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

import io.swagger.v3.oas.annotations.Parameter;
import org.apache.solr.client.solrj.SolrServerException;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.studycard.dto.DicomTag;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardApply;
import org.shanoir.ng.studycard.model.VM;
import org.shanoir.ng.studycard.service.CardsProcessingService;
import org.shanoir.ng.studycard.service.StudyCardService;
import org.shanoir.ng.studycard.service.StudyCardUniqueConstraintManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Controller
public class StudyCardApiController implements StudyCardApi {

    private static final String MICROSERVICE_COMMUNICATION_ERROR = "Microservice communication error";

    private static final Logger LOG = LoggerFactory.getLogger(StudyCardApiController.class);

    @Autowired
    private StudyCardService studyCardService;

    @Autowired
    private StudyCardUniqueConstraintManager uniqueConstraintManager;

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
    private CardsProcessingService cardProcessingService;
    
    @Autowired
    private SolrService solrService;

    @Override
    public ResponseEntity<Void> deleteStudyCard(
		    @Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) throws RestServiceException {
        try {
            if (datasetAcquisitionService.existsByStudyCardId(studyCardId)) {
                throw new RestServiceException(
                        new ErrorModel(
                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                "This study card is linked to at least one dataset acquisition."
                        ));
            }
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
			@Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) {
        final StudyCard studyCard = studyCardService.findById(studyCardId);
        if (studyCard == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studyCard, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<StudyCard>> findStudyCardByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
        final List<StudyCard> studyCards = studyCardService.findByStudy(studyId);
        if (studyCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyCards, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<StudyCard>> findStudyCardByAcqEqId(
			@Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acqEqId") Long acqEqId) {
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
			@Parameter(description = "study Card to create", required = true) @RequestBody StudyCard studyCard,
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

    // Attention: used by ShanoirUploader!
    @Override
    public ResponseEntity<List<StudyCard>> searchStudyCards(
			@Parameter(description = "study ids", required = true) @RequestBody final IdList studyIds) {
        final List<StudyCard> studyCards = studyCardService.search(studyIds.getIdList());
        if (studyCards.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyCards, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateStudyCard(
			@Parameter(description = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId,
			@Parameter(description = "study card to update", required = true) @RequestBody StudyCard studyCard,
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

    @Override
    public ResponseEntity<Void> applyStudyCard(
			@Parameter(description = "study card id and dataset ids", required = true) @RequestBody StudyCardApply studyCardApplyObject) throws PacsException, SolrServerException, IOException {
        if (studyCardApplyObject == null
                || studyCardApplyObject.getDatasetAcquisitionIds() == null
                || studyCardApplyObject.getDatasetAcquisitionIds().isEmpty()
                || studyCardApplyObject.getStudyCardId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        StudyCard studyCard = studyCardService.findById(studyCardApplyObject.getStudyCardId());
        LOG.debug("re-apply studycard n° " + studyCard.getId());
        List<DatasetAcquisition> acquisitions = datasetAcquisitionService.findById(studyCardApplyObject.getDatasetAcquisitionIds());
        cardProcessingService.applyStudyCard(studyCard, acquisitions);
        
        // Get all updated dataset ids
        List<Long> datasetIds = new ArrayList<Long>();
        for (DatasetAcquisition acquisition : acquisitions) {
        	for (Dataset ds : acquisition.getDatasets()) {
        		datasetIds.add(ds.getId());
        	}
        }
        
        // Update solr metadata
        try {
            solrService.updateDatasetsAsync(datasetIds);
        }catch (Exception e){
            LOG.error("Solr update failed for datasets {}", datasetIds, e);
        }

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
