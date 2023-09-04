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

import org.apache.commons.collections4.CollectionUtils;
import org.dcm4che3.data.Attributes;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.quality.SubjectStudyQualityTagDTO;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectStudyService;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CardsProcessingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CardsProcessingService.class);
	
	@Autowired
	private StudyService studyService;
	
	@Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	@Autowired
    private WADODownloaderService downloader;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private SubjectStudyService subjectStudyService;

	
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
                Attributes dicomAttributes = downloader.getDicomAttributesForAcquisition(acquisition);
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
	public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags) throws MicroServiceCommunicationException {
	    if (qualityCard == null) throw new IllegalArgumentException("qualityCard can't be null");
		Study study = studyService.findById(qualityCard.getStudyId());
		if (study == null ) throw new IllegalArgumentException("study can't be null");
		if (qualityCard.getStudyId() != study.getId()) throw new IllegalStateException("study and studycard ids don't match");
		if (CollectionUtils.isNotEmpty(qualityCard.getRules())) {	    
		    QualityCardResult result = new QualityCardResult();
		    resetSubjectStudies(study);
            try {
                subjectStudyService.update(study.getSubjectStudyList());
            } catch (EntityNotFoundException e) {} // too bad
			for (Examination examination : study.getExaminations()) {
			    // For now, just take the first DICOM instance
			    // Later, use DICOM json to have a hierarchical structure of DICOM metata (study -> serie -> instance) 
                try {
                    Attributes examinationDicomAttributes = downloader.getDicomAttributesForExamination(examination);
                    List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
                    // today study cards are only used for MR modality
                    // acquisitions = acquisitions.stream().filter(a -> a instanceof MrDatasetAcquisition).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(acquisitions)) {
                        LOG.info(acquisitions.size() + " acquisitions found for examination with id: " + examination.getId());
                        LOG.info(qualityCard.getRules().size() + " rules found for study card with id: " + qualityCard.getId() + " and name: " + qualityCard.getName());
                        for (QualityExaminationRule rule : qualityCard.getRules()) {
                            rule.apply(examination, examinationDicomAttributes, result);
                        }
                    }				
                } catch (PacsException e) {
                    long ts = new Date().getTime();
                    LOG.warn("Examination" + examination.getId() + " metadata could not be retreived from the Shanoir pacs (ts:" + ts + ")");
                    QualityCardResultEntry resultEntry = initResult(examination);
                    resultEntry.setTagSet(QualityTag.ERROR);
                    resultEntry.setMessage("Examination " + examination.getId() + " could not be checked because its metadata could not be retreived from the Shanoir pacs (ts:" + ts + ")");
                    result.add(resultEntry);
                }
			};
			//result.removeUnchanged(study);
			if (updateTags) {
			    try {
			        subjectStudyService.update(result.getUpdatedSubjectStudies());
			    } catch (EntityNotFoundException e) {
			        throw new IllegalStateException("Could not update subject-studies", e);
			    }	    
			}
			return result;
		} else {
			throw new RestClientException("Study card used with emtpy rules.");
		}
	}
	
	private void resetSubjectStudies(Study study) {
        if (study != null && study.getSubjectStudyList() != null) {
            for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
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
