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

import org.apache.commons.collections4.CollectionUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectStudyService;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.StudyCard;
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
                AcquisitionAttributes dicomAttributes = downloader.getDicomAttributesForAcquisition(acquisition);
                changeInAtLeastOneAcquisition = studyCard.apply(acquisition, dicomAttributes);
            }
        }
        if (changeInAtLeastOneAcquisition) { // no need to update, if nothing happened
            datasetAcquisitionService.update(acquisitions);
        }
    }	
	
    /**
	 * Study cards for quality control: apply on entire study.
	 * 
	 * @param studyCard
	 * @throws MicroServiceCommunicationException 
	 */
	public QualityCardResult applyQualityCardOnExamination(QualityCard qualityCard, Long examinationId, boolean updateTags) throws MicroServiceCommunicationException {
        if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");
        long startTs = new Date().getTime();
        Examination examination = examinationService.findById(examinationId);
         LOG.debug("Quality check for examination " + examination.getId() + " : getting exam from db tool " + (new Date().getTime() - startTs) + "ms");
        return applyQualityCardOnExamination(qualityCard, examination, updateTags);
	}

    /**
	 * Study cards for quality control: apply on entire study.
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
            // For now, just take the first DICOM instance
            // Later, use DICOM json to have a hierarchical structure of DICOM metata (study -> serie -> instance) 
            try {
                ExaminationAttributes examinationDicomAttributes;
                if (qualityCard.hasDicomConditions()) { // don't query pacs if not needed
                    long dicomStartTs = new Date().getTime();
                    examinationDicomAttributes = downloader.getDicomAttributesForExamination(examination);
                    LOG.debug("Getting DICOM data for examination " + examination.getId() + " took " + (new Date().getTime() - dicomStartTs)  + "ms");
                } else {
                    LOG.debug("No need to get DICOM data for examination " + examination.getId());
                    examinationDicomAttributes = null;
                }
                List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
                // today study cards are only used for MR modality
                // acquisitions = acquisitions.stream().filter(a -> a instanceof MrDatasetAcquisition).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(acquisitions)) {
                    LOG.debug(acquisitions.size() + " acquisitions found for examination with id: " + examination.getId());
                    LOG.debug(qualityCard.getRules().size() + " rules found for study card with id: " + qualityCard.getId() + " and name: " + qualityCard.getName());
                    long rulesStartTs = new Date().getTime();
                    for (QualityExaminationRule rule : qualityCard.getRules()) {
                        rule.apply(examination, examinationDicomAttributes, result);
                    }
                    LOG.debug("Quality check for examination " + examination.getId() + " : rules application took " + (new Date().getTime() - rulesStartTs) + "ms");
                }
            } catch (PacsException e) {
                long ts = new Date().getTime();
                LOG.warn("Examination" + examination.getId() + " metadata could not be retreived from the Shanoir pacs (ts:" + ts + ")");
                QualityCardResultEntry resultEntry = initResult(examination);
                resultEntry.setTagSet(QualityTag.ERROR);
                resultEntry.setMessage("Examination " + examination.getId() + " could not be checked because its metadata could not be retreived from the Shanoir pacs (ts:" + ts + ")");
                result.add(resultEntry);
            }
			//result.removeUnchanged(study);
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
			throw new RestClientException("Study card used with emtpy rules.");
		}
	}

	/**
	 * Study cards for quality control: apply on entire study.
	 * 
	 * @param studyCard
	 * @throws MicroServiceCommunicationException 
	 */
	public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags) throws MicroServiceCommunicationException {
        long start = new Date().getTime();
        if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.CHECK_QUALITY_EVENT, null, KeycloakUtil.getTokenUserId(), "Quality check started on study " + qualityCard.getStudyId() , 4);
		eventService.publishEvent(event);
        Study study = studyService.findById(qualityCard.getStudyId());
		if (study == null ) throw new IllegalArgumentException("study can't be null");
		if (qualityCard.getStudyId() != study.getId()) throw new IllegalStateException("study and studycard ids don't match");
		if (CollectionUtils.isNotEmpty(qualityCard.getRules())) {	    
		    QualityCardResult result = new QualityCardResult();
            Float i = 0f;
			for (Examination examination : study.getExaminations()) {
                event.setStatus(2);
                event.setProgress(i / study.getExaminations().size());
                eventService.publishEvent(event);
                result.merge(applyQualityCardOnExamination(qualityCard, examination, updateTags));
                i++;
			};
            event.setProgress(1f);
            event.setStatus(1);
            event.setMessage("Quality card applied on study " + study.getName() + " in " + (new Date().getTime() - start) + " ms.");
            event.setReport(result.toString());
            eventService.publishEvent(event);
			return result;
		} else {
            event.setStatus(-1);
            event.setMessage("Study card used with emtpy rules.");
            event.setProgress(1f);
            eventService.publishEvent(event);
			throw new RestClientException("Study card used with emtpy rules.");
		}
	}

    private void resetSubjectStudies(List<SubjectStudy> subjectStudies) {
        if (subjectStudies != null) {
            for (SubjectStudy subjectStudy : subjectStudies) {
                subjectStudy.setQualityTag(null);
            }
        }
    }

    private QualityCardResultEntry initResult(Examination examination) {
        QualityCardResultEntry result = new QualityCardResultEntry();
        result.setSubjectName(examination.getSubject().getName());
        result.setExaminationDate(examination.getExaminationDate());
        result.setExaminationComment(examination.getComment());
        return result;
    }
	
}
