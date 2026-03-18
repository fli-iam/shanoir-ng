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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.StreamExceptionWrapper;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class CardsProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(CardsProcessingService.class);

    @Autowired
    private StudyService studyService;

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
    private WADODownloaderService downloader;

    @Autowired
    private ShanoirEventService eventService;


    /**
     * Apply study card on given acquisitions
     *
     * @param studyCard
     * @param acquisitions
     * @throws PacsException
     * @throws EntityNotFoundException 
     */
    public void applyStudyCard(StudyCard studyCard, List<DatasetAcquisition> acquisitions) throws PacsException, EntityNotFoundException {
        boolean changeInAtLeastOneAcquisition = false;
        for (DatasetAcquisition acquisition : acquisitions) {
            if (CollectionUtils.isNotEmpty(acquisition.getDatasets()) && CollectionUtils.isNotEmpty(studyCard.getRules())) {
                AcquisitionAttributes<Long> dicomAttributes = downloader.getDicomAttributesForAcquisition(acquisition);
                changeInAtLeastOneAcquisition = studyCard.apply(acquisition, dicomAttributes);
            }
        }
        if (changeInAtLeastOneAcquisition) { // no need to update, if nothing happened
            datasetAcquisitionService.update(acquisitions);
        }
    }

    /**
     * Quality Cards : apply on entire exam.
     *
     * @param qualityCard
     * @param examination
     * @param updateTags
     * @throws MicroServiceCommunicationException
     */
    public QualityCardResult applyQualityCardOnDatasetAcquisition(QualityCard qualityCard, DatasetAcquisition acquisition) throws MicroServiceCommunicationException, PacsException {
        long startTs = new Date().getTime();
        if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");
        if (acquisition == null) throw new IllegalArgumentException("dataset acquisition can't be null");
        LOG.debug("Quality check for dataset acquisition " + acquisition.getId() + " started");

        if (CollectionUtils.isEmpty(qualityCard.getRules())) {
            throw new RestClientException("Quality card used with empty rules.");
        }

        QualityCardResult result = new QualityCardResult();

        LOG.debug(qualityCard.getRules().size() + " rules found for quality card with id: " + qualityCard.getId() + " and name: " + qualityCard.getName());

        // We retrieve dicom attributes for this dataset acquisition/DICOM serie
        AcquisitionAttributes<Long> dicomAttributes = downloader.getDicomAttributesForAcquisition(acquisition);

        // We apply each rule of the quality card on the acquisition
        for (QualityExaminationRule rule : qualityCard.getRules()) {
            rule.apply(acquisition, dicomAttributes, result, downloader);
        }

        LOG.debug("Quality check for acquisition " + acquisition.getId() + " finished in " + (new Date().getTime() - startTs) + " ms");
        
        return result;
    }

    /**
     * Quality Cards : apply on entire study.
     *
     * @param qualityCard
     * @param updateTags
     * @param start
     * @param stop
     * @throws MicroServiceCommunicationException
     */
    public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags, Integer start, Integer stop) throws MicroServiceCommunicationException, PacsException {
        long startTs = new Date().getTime();
        if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");

        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.CHECK_QUALITY_EVENT, null, KeycloakUtil.getTokenUserId(), "Quality check started on study " + qualityCard.getStudyId(), 4, qualityCard.getStudyId());
        eventService.publishEvent(event);

        Study study = studyService.findById(qualityCard.getStudyId());
        if (study == null) throw new IllegalArgumentException("study can't be null");
        if (!Objects.equals(qualityCard.getStudyId(), study.getId())) throw new IllegalStateException("study and qualityCard study ids don't match");

        if (CollectionUtils.isEmpty(qualityCard.getRules())) {
            event.setStatus(-1);
            event.setMessage("Quality card used with empty rules.");
            event.setProgress(1f);
            eventService.publishEvent(event);
            throw new RestClientException("Quality card used with empty rules.");
        }

        // Load lazy data before go parallel
        loadRulesLazyCollections(qualityCard.getRules(), event);

        List<Examination> examinations = study.getExaminations();
        // if start and stop are specified, we apply quality card only on a subset of examinations, for testing purpose
        if (start != null && stop != null) {
            examinations = examinations.subList(start, stop < examinations.size() ? stop : examinations.size());
        }

        QualityCardResult result = new QualityCardResult();
        AtomicInteger examinationIndex = new AtomicInteger(0);
        int totalExaminations = examinations.size();

        for (Examination examination : examinations) {
            event.setMessage("Processing examination " + examination.getComment());
            event.setProgress(examinationIndex.floatValue() / totalExaminations);
            eventService.publishEvent(event);

            // We only load the DatasetAcquisitions from one examination at a time
            List<DatasetAcquisition> datasetAcquisitions = datasetAcquisitionService
                .findByExamination(examination.getId());

            if (updateTags) {
                resetDatasetAcquisitions(datasetAcquisitions);
            }

            // We apply the quality card on DatasetAcquisitions in parallel for one examination only
            List<DatasetAcquisition> updatedAcquisitions = new ArrayList<>();
            try {
                datasetAcquisitions.parallelStream().forEach(datasetAcquisition -> {
                    event.setStatus(2);
                    event.setMessage("Checking quality for acquisition " + datasetAcquisition.getId() 
                        + " in examination " + examination.getComment());
                    eventService.publishEvent(event);
                    try {
                        QualityCardResult acquisitionResult = applyQualityCardOnDatasetAcquisition(
                            qualityCard, datasetAcquisition);
                        result.merge(acquisitionResult);
                        synchronized (updatedAcquisitions) {
                            updatedAcquisitions.addAll(acquisitionResult.getUpdatedDatasetAcquisitions());
                        }
                    } catch (MicroServiceCommunicationException | PacsException e) {
                        throw new StreamExceptionWrapper(e);
                    }
                });
            } catch (StreamExceptionWrapper e) {
                throw (MicroServiceCommunicationException) e.getCause();
            }

            if (updateTags && !updatedAcquisitions.isEmpty()) {
                try {
                    datasetAcquisitionService.update(updatedAcquisitions);
                } catch (EntityNotFoundException e) {
                    throw new IllegalStateException("Could not update dataset acquisitions for examination " + examination.getComment(), e);
                }
            }

            datasetAcquisitions.clear();
            updatedAcquisitions.clear();
            examinationIndex.incrementAndGet();
        }

            event.setProgress(1f);
            event.setStatus(1);
            event.setMessage("Quality card applied on study " + study.getName() + " in " + (new Date().getTime() - startTs) + " ms.");
            event.setReport(result.toString());
            eventService.publishEvent(event);
            return result;
    }

    private void loadRulesLazyCollections(List<QualityExaminationRule> rules, ShanoirEvent event) {
        event.setMessage("Loading rules");
        event.setProgress(0.5f);
        eventService.publishEvent(event);
        if (rules != null) {
            for (QualityExaminationRule rule : rules) {
                if (rule.getConditions() != null) {
                    for (StudyCardCondition condition : rule.getConditions()) {
                        Hibernate.initialize(condition.getValues());
                    }
                }
            }
        }
    }

    /**
     * Quality cards for quality control: apply on entire study.
     *
     * @param qualityCard
     * @throws MicroServiceCommunicationException
     */
    public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags) throws MicroServiceCommunicationException, PacsException {
        return applyQualityCardOnStudy(qualityCard, updateTags, null, null);
    }

        /**
     * Quality cards for quality control: apply on entire study.
     *
     * @param qualityCard
     * @throws MicroServiceCommunicationException
     */
    public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, Integer start, Integer stop) throws MicroServiceCommunicationException, PacsException {
        return applyQualityCardOnStudy(qualityCard, false, start, stop);
    }

    private void resetDatasetAcquisitions(List<DatasetAcquisition> datasetAcquisitions) {
        if (datasetAcquisitions != null) {
            for (DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
            datasetAcquisition.setQualityTag(null);
            }
        }
    }

}
