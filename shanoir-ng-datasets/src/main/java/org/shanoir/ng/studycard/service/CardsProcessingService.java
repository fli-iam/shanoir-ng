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

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.StreamExceptionWrapper;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectStudyService;
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
    private ExaminationService examinationService;

	@Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
    private WADODownloaderService downloader;

	@Autowired
	private SubjectStudyService subjectStudyService;

    @Autowired
    private ShanoirEventService eventService;


	/**
	 * Apply study card on given acquisitions
	 * 
	 * @param studyCard
	 * @param acquisitions
	 * @throws PacsException 
	 */
	public void applyStudyCard(StudyCard studyCard, List<DatasetAcquisition> acquisitions) throws PacsException {
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
	 * Study cards for quality control: apply on entire exam.
	 *
	 * @param studyCard
	 * @throws MicroServiceCommunicationException
	 */
	public QualityCardResult applyQualityCardOnExamination(QualityCard qualityCard, Examination examination, boolean updateTags) throws MicroServiceCommunicationException {
        long startTs = new Date().getTime();
        if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");
		if (examination == null ) throw new IllegalArgumentException("examination can't be null");
        LOG.debug("Quality check for examination " + examination.getId() + " started");
		if (qualityCard.getStudyId() != examination.getStudy().getId()) throw new IllegalStateException("study and studycard ids don't match");
		if (CollectionUtils.isNotEmpty(qualityCard.getRules())) {
		    QualityCardResult result = new QualityCardResult();
            if (updateTags) {
                List<SubjectStudy> subjectsStudies = subjectStudyService.get(examination.getSubject().getId(), examination.getStudy().getId());
                resetSubjectStudies(subjectsStudies);
                try {
                    subjectStudyService.update(subjectsStudies);
                } catch (EntityNotFoundException e) {} // too bad
            }
            List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
            if (acquisitions != null && !acquisitions.isEmpty()) {
                LOG.debug(acquisitions.size() + " acquisitions found for examination with id: " + examination.getId());
                LOG.debug(qualityCard.getRules().size() + " rules found for study card with id: " + qualityCard.getId() + " and name: " + qualityCard.getName());
                long rulesStartTs = new Date().getTime();
                for (QualityExaminationRule rule : qualityCard.getRules()) {
                    rule.apply(examination, result, downloader);
                }
                LOG.debug("Quality check for examination " + examination.getId() + " : rules application took " + (new Date().getTime() - rulesStartTs) + "ms");
            }
			if (updateTags) {
			    try {
			        subjectStudyService.update(result.getUpdatedSubjectStudies());
			    } catch (EntityNotFoundException e) {
                    throw new IllegalStateException("Could not update subject-studies", e);
			    }
			}
            LOG.info("Quality check for examination " + examination.getId() + " finished in " + (new Date().getTime() - startTs) + " ms");
            return result;
		} else {
			throw new RestClientException("Quality card used with emtpy rules.");
		}
	}

	/**
	 * Study cards for quality control: apply on entire study.
	 * 
	 * @param studyCard
	 * @throws MicroServiceCommunicationException 
	 */
	public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags, Integer start, Integer stop) throws MicroServiceCommunicationException {
        long startTs = new Date().getTime();
        if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.CHECK_QUALITY_EVENT, null, KeycloakUtil.getTokenUserId(), "Quality check started on study " + qualityCard.getStudyId() , 4, qualityCard.getStudyId());
        eventService.publishEvent(event);
        Study study = studyService.findById(qualityCard.getStudyId());
        if (study == null ) throw new IllegalArgumentException("study can't be null");
        if (qualityCard.getStudyId() != study.getId()) throw new IllegalStateException("study and studycard ids don't match");
        if (CollectionUtils.isNotEmpty(qualityCard.getRules())) {
            if (updateTags) { // first reset subject studies
                event.setMessage("resetting quality subject tags");
                eventService.publishEvent(event);
                resetSubjectStudies(study.getSubjectStudyList());
                try {
                    subjectStudyService.update(study.getSubjectStudyList());
                } catch (EntityNotFoundException e) {} // too bad
            }
            QualityCardResult result = new QualityCardResult();
            AtomicInteger i = new AtomicInteger(0);
            List<Examination> examinations;
            if (start != null && stop != null) {
                examinations = study.getExaminations().subList(start, stop < study.getExaminations().size() ? stop : study.getExaminations().size());
            } else {
                examinations = study.getExaminations();
            }
            // Load lazy data before go parallel
            loadExaminationsLazyCollections(study.getExaminations(), event);
            loadRulesLazyCollections(qualityCard.getRules(), event);
            // main loop
            try {
                examinations.parallelStream().forEach(examination -> {
                    LOG.error("quality examination: " + examination.getId());
                    event.setStatus(2);
                    event.setProgress(0.5f + (i.floatValue() * 0.5f / examinations.size()));
                    event.setMessage("checking quality for examination " + examination.getComment());
                    //event.setReport(result.toString()); // too heavy, too slow
                    eventService.publishEvent(event);
                    try {
                        result.merge(applyQualityCardOnExamination(qualityCard, examination, false));
                    } catch (MicroServiceCommunicationException e) {
                        throw new StreamExceptionWrapper(e);
                    }
                    i.incrementAndGet();
                });
            } catch (StreamExceptionWrapper e) {
                throw (MicroServiceCommunicationException)(e.getCause());
            }
            if (updateTags) { // update subject studies
			    try {
                    event.setMessage("setting quality subject tags");
                    eventService.publishEvent(event);
			        subjectStudyService.update(result.getUpdatedSubjectStudies());
			    } catch (EntityNotFoundException e) {
                    throw new IllegalStateException("Could not update subject-studies", e);
			    }	    
			}
            event.setProgress(1f);
            event.setStatus(1);
            event.setMessage("Quality card applied on study " + study.getName() + " in " + (new Date().getTime() - startTs) + " ms.");
            event.setReport(result.toString());
            eventService.publishEvent(event);
            return result;
        } else {
            event.setStatus(-1);
            event.setMessage("Quality card used with emtpy rules.");
            event.setProgress(1f);
            eventService.publishEvent(event);
            throw new RestClientException("Quality card used with emtpy rules.");
        }
	}

    private void loadExaminationsLazyCollections(List<Examination> examinations, ShanoirEvent event) {
        if (examinations != null) {
            int i = 0;
            for (Examination examination : examinations) {
                event.setMessage("Loading examination " + examination.getComment() + " data from Shanoir database");
                event.setProgress(i * 0.4f / examinations.size());
                eventService.publishEvent(event);
                if (examination.getSubject() != null) {
                    Hibernate.initialize(examination.getSubject().getSubjectStudyList());
                }
                if (examination.getDatasetAcquisitions() != null) {
                    for(DatasetAcquisition acquisition : examination.getDatasetAcquisitions()) {
                        if (acquisition.getDatasets() != null) {
                            for (Dataset dataset : acquisition.getDatasets()) {
                                if (dataset.getDatasetExpressions() != null) {
                                    for (DatasetExpression expression : dataset.getDatasetExpressions()) {
                                        Hibernate.initialize(expression.getDatasetFiles());
                                    }
                                }
                            }
                        }
                    }
                }
                i++;
            }
        }
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
	 * Study cards for quality control: apply on entire study.
	 *
	 * @param studyCard
	 * @throws MicroServiceCommunicationException
	 */
	public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags) throws MicroServiceCommunicationException {
        return applyQualityCardOnStudy(qualityCard, updateTags, null, null);
	}

        /**
	 * Study cards for quality control: apply on entire study.
	 *
	 * @param studyCard
	 * @throws MicroServiceCommunicationException
	 */
	public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, Integer start, Integer stop) throws MicroServiceCommunicationException {
        return applyQualityCardOnStudy(qualityCard, false, start, stop);
	}

    private void resetSubjectStudies(List<SubjectStudy> subjectStudies) {
        if (subjectStudies != null) {
            for (SubjectStudy subjectStudy : subjectStudies) {
                subjectStudy.setQualityTag(null);
            }
        }
    }
}
