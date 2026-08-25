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

import org.apache.commons.collections4.CollectionUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.studycard.model.*;
import org.shanoir.ng.studycard.model.rule.DatasetAcquisitionRule;
import org.shanoir.ng.studycard.model.rule.DatasetRule;
import org.shanoir.ng.studycard.model.rule.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

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

    private static final Logger LOG = LoggerFactory.getLogger(StudyCardServiceImpl.class);

    @Autowired
    private StudyCardRepository studyCardRepository;

    @Autowired
    private UniqueConstraintManager<StudyCard> uniqueConstraintManager;

    @Autowired
    private SolrService solrService;

    @Autowired
    private DatasetAcquisitionRepository acquisitionRepository;

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
    private WADODownloaderService downloader;

    public void update(final StudyCard card) throws EntityNotFoundException {
        final StudyCard studyCardDb = studyCardRepository.findById(card.getId()).orElse(null);
        if (studyCardDb == null) throw new EntityNotFoundException(StudyCard.class, card.getId());
        updateStudyCardValues(studyCardDb, card);
        studyCardRepository.save(studyCardDb);
    }

    public void validate(StudyCard studyCard, BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap()
                .add(new FieldErrorMap(result))
                .add(uniqueConstraintManager.validate(studyCard));
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

    public void applyStudyCard(StudyCard studyCard, StudyCardApply studyCardApplyObject) throws PacsException, EntityNotFoundException {
        LOG.debug("re-apply studycard n° " + studyCard.getId());

        List<DatasetAcquisition> acquisitions = acquisitionRepository.findByIdsWithDatasetExpressions(studyCardApplyObject.getDatasetAcquisitionIds());
        applyStudyCard(studyCard, acquisitions);

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
        } catch (Exception e) {
            LOG.error("Solr update failed for datasets {}", datasetIds, e);
        }
    }

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
                    } else if (field.getType().getName() == "long") {
                        // longs actually code a date and a time, see Tag.class
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

    public boolean applyStudyCardOnAcquisition(StudyCard studyCard, DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes) throws IllegalStateException {
        boolean changeInAtLeastOneAcquisition = false;
        if (studyCard.getRules() != null) {
            for (StudyCardRule<?> rule : studyCard.getRules()) {
                if (rule instanceof DatasetAcquisitionRule) {
                    changeInAtLeastOneAcquisition = true;
                    ((DatasetAcquisitionRule) rule).apply(acquisition, dicomAttributes);
                } else if (rule instanceof DatasetRule && acquisition.getDatasets() != null) {
                    for (Dataset dataset : acquisition.getDatasets()) {
                        changeInAtLeastOneAcquisition = true;
                        Attributes attributes;
                        if (String.class.equals(dicomAttributes.getParametrizedType())) {
                            // @SuppressWarnings("unchecked") doesn't work ...
                            attributes = ((AcquisitionAttributes<String>) dicomAttributes).getDatasetAttributes(dataset.getSOPInstanceUID());
                        } else if (Long.class.equals(dicomAttributes.getParametrizedType())) {
                            attributes = ((AcquisitionAttributes<Long>) dicomAttributes).getDatasetAttributes(dataset.getId());
                        } else {
                            throw new IllegalStateException("the parametrized type of AcquisitionAttributes is not implemented, use String or Long");
                        }
                        ((DatasetRule) rule).apply(dataset, attributes);
                    }
                } else {
                    throw new IllegalStateException("unknown type of rule");
                }
            }
        }
        acquisition.setStudyCard(studyCard);
        acquisition.setStudyCardTimestamp(studyCard.getLastEditTimestamp());
        return changeInAtLeastOneAcquisition;
    }

    /**
     * Apply study card on given acquisitions
     */
    private void applyStudyCard(StudyCard studyCard, List<DatasetAcquisition> acquisitions) throws PacsException, EntityNotFoundException {

        if (CollectionUtils.isEmpty(studyCard.getRules())) {
            return;
        }

        boolean changeInAtLeastOneAcquisition = false;
        for (DatasetAcquisition acquisition : acquisitions) {
            if (CollectionUtils.isNotEmpty(acquisition.getDatasets())) {
                try {
                    AcquisitionAttributes<Long> dicomAttributes = downloader.getDicomAttributesForAcquisition(acquisition);
                    changeInAtLeastOneAcquisition |= applyStudyCardOnAcquisition(studyCard, acquisition, dicomAttributes);
                } catch (PacsException e) {
                    LOG.error("Error during PACS communication while applying study card on dataset acquisition " + acquisition.getId(), e);
                }
            }
        }
        if (changeInAtLeastOneAcquisition) { // no need to update, if nothing happened
            datasetAcquisitionService.update(acquisitions);
        }
    }

    private void updateStudyCardValues(final StudyCard studyCardDb, final StudyCard studyCard) {
        studyCardDb.setName(studyCard.getName());
        studyCardDb.setDisabled(studyCard.isDisabled());
        studyCardDb.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
        studyCardDb.setId(studyCard.getId());
        studyCardDb.setNiftiConverterId(studyCard.getNiftiConverterId());
        studyCardDb.setStudyId(studyCard.getStudyId());
        studyCardDb.setLastEditTimestamp(System.currentTimeMillis());
        if (studyCardDb.getRules() == null) studyCardDb.setRules(new ArrayList<StudyCardRule<?>>());
        else studyCardDb.getRules().clear();
        if (studyCard.getRules() != null) studyCardDb.getRules().addAll(studyCard.getRules());
    }
}
