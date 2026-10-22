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

package org.shanoir.ng.importer.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.email.EmailBase;
import org.shanoir.ng.shared.email.EmailDatasetImportFailed;
import org.shanoir.ng.shared.email.EmailDatasetsCopied;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.shared.email.EmailExtraDataAdded;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ImporterMailService {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterMailService.class);

    /** Maximum number of examinations listed in the datasets copied email. */
    static final int MAX_COPIED_EXAMINATIONS_IN_MAIL = 50;

    @Autowired
    private StudyUserRightsRepository studyUserRightRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ExaminationRepository examinationRepository;

    /**
     * Sends the import email through rabbitMQ to user MS
     * @param importJob the import job
     * @param userId the userID
     * @param examination the exam ID
     * @param generatedAcquisitions
     */
    public void sendImportEmail(ImportJob importJob, Long userId, Examination examination, Set<DatasetAcquisition> generatedAcquisitions, List<String> qualityCardNames) {
        EmailDatasetsImported generatedMail = new EmailDatasetsImported();

        LinkedHashMap<Long, String> datasets = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(generatedAcquisitions)) {
            return;
        }
        generatedMail.setExamDate(examination.getExaminationDate().toString());
        generatedMail.setExaminationId(examination.getId().toString());
        generatedMail.setStudyId(importJob.getStudyId());
        generatedMail.setSubjectName(importJob.getSubjectName());
        generatedMail.setStudyName(importJob.getStudyName());
        generatedMail.setUserId(userId);
        generatedMail.setStudyCard(importJob.getStudyCardName());
        generatedMail.setQualityCards(qualityCardNames);

        for (DatasetAcquisition acq : generatedAcquisitions.stream().sorted(Comparator.comparingInt(DatasetAcquisition::getSortingIndex)).toList()) {
            if (!CollectionUtils.isEmpty(acq.getDatasets())) {
                for (Dataset dataset : acq.getDatasets()) {
                    datasets.put(dataset.getId(), dataset.getName());
                }
            }
        }

        generatedMail.setDatasets(datasets);
        sendMail(importJob.getStudyId(), generatedMail, RabbitMQConfiguration.IMPORT_DATASET_MAIL_QUEUE);
    }

    public void sendFailureMail(ImportJob importJob, Long userId, String errorMessage) {
        EmailDatasetImportFailed generatedMail = new EmailDatasetImportFailed();
        generatedMail.setExaminationId(importJob.getExaminationId().toString());
        generatedMail.setStudyId(importJob.getStudyId());
        generatedMail.setStudyCardId(importJob.getStudyCardId() != null ? importJob.getStudyCardId().toString() : "");
        generatedMail.setSubjectName(importJob.getSubjectName());
        generatedMail.setStudyName(importJob.getStudyName());
        generatedMail.setUserId(userId);

        generatedMail.setErrorMessage(errorMessage != null ? errorMessage : "An unexpected error occured, please contact Shanoir support.");

        sendMail(importJob.getStudyId(), generatedMail, RabbitMQConfiguration.IMPORT_DATASET_FAILED_MAIL_QUEUE);
    }

    /**
     * Sends the extra data email through rabbitMQ to user MS
     * @param examination the examination the files were added to, or the examination of the acquisition
     * @param datasetAcquisitionId the acquisition the files were added to, null if added to the examination
     * @param fileNames the names of the files successfully added
     * @param userId the user who added the files
     */
    public void sendExtraDataEmail(Examination examination, Long datasetAcquisitionId, List<String> fileNames, Long userId) {
        if (CollectionUtils.isEmpty(fileNames)) {
            return;
        }
        EmailExtraDataAdded generatedMail = new EmailExtraDataAdded();
        generatedMail.setStudyId(examination.getStudyId());
        generatedMail.setStudyName(examination.getStudy() != null ? examination.getStudy().getName() : null);
        generatedMail.setSubjectName(examination.getSubject() != null ? examination.getSubject().getName() : null);
        generatedMail.setExaminationId(examination.getId().toString());
        generatedMail.setExamDate(examination.getExaminationDate() != null ? examination.getExaminationDate().toString() : null);
        generatedMail.setDatasetAcquisitionId(datasetAcquisitionId != null ? datasetAcquisitionId.toString() : null);
        generatedMail.setFileNames(fileNames);
        generatedMail.setUserId(userId);

        sendMail(examination.getStudyId(), generatedMail, RabbitMQConfiguration.EXTRA_DATA_MAIL_QUEUE);
    }

    /**
     * Sends the datasets copied email through rabbitMQ to user MS
     * @param studyId the study the datasets were copied into
     * @param copiedDatasetCount the number of datasets successfully copied
     * @param examinationIds the examinations created in the study by the copy
     * @param userId the user who copied the datasets
     */
    public void sendDatasetsCopiedEmail(Long studyId, int copiedDatasetCount, List<Long> examinationIds, Long userId) {
        if (copiedDatasetCount <= 0) {
            return;
        }
        List<Long> distinctExamIds = examinationIds == null ? List.of()
                : examinationIds.stream().filter(Objects::nonNull).distinct().toList();
        List<Long> listedExamIds = distinctExamIds.stream().limit(MAX_COPIED_EXAMINATIONS_IN_MAIL).toList();

        // Keep the copy order, the repository does not guarantee it
        Map<Long, Examination> examsById = new LinkedHashMap<>();
        for (Examination exam : examinationRepository.findAllById(listedExamIds)) {
            examsById.put(exam.getId(), exam);
        }
        LinkedHashMap<Long, String> examinations = new LinkedHashMap<>();
        for (Long examId : listedExamIds) {
            Examination exam = examsById.get(examId);
            if (exam != null) {
                examinations.put(examId, examinationLabel(exam));
            }
        }

        EmailDatasetsCopied generatedMail = new EmailDatasetsCopied();
        generatedMail.setStudyId(studyId);
        generatedMail.setStudyName(studyRepository.findById(studyId).map(Study::getName).orElse(null));
        generatedMail.setCopiedDatasetCount(copiedDatasetCount);
        generatedMail.setExaminations(examinations);
        generatedMail.setTotalExaminations(distinctExamIds.size());
        generatedMail.setUserId(userId);

        sendMail(studyId, generatedMail, RabbitMQConfiguration.DATASETS_COPIED_MAIL_QUEUE);
    }

    private String examinationLabel(Examination exam) {
        String subjectName = exam.getSubject() != null ? exam.getSubject().getName() : "Examination " + exam.getId();
        return exam.getExaminationDate() != null ? subjectName + " - " + exam.getExaminationDate() : subjectName;
    }

    /**
     * Sends the given mail in entry to all recipients in a given study
     * @param studyId the study id
     * @param email the recipients
     * @param queue
     */
    private void sendMail(Long studyId, EmailBase email, String queue) {
        List<Long> recipients = new ArrayList<>();

        // Get all recipients
        List<StudyUser> users = (List<StudyUser>) studyUserRightRepo.findByStudyId(studyId);

        for (StudyUser user : users) {
            if (user.isReceiveNewImportReport()) {
                recipients.add(user.getUserId());
            }
        }
        if (recipients.isEmpty()) {
            // Do not send any mail if no recipients
            return;
        }
        email.setRecipients(recipients);

        try {
            rabbitTemplate.convertAndSend(queue, objectMapper.writeValueAsString(email));
        } catch (AmqpException | JsonProcessingException e) {
            LOG.error("Could not send email to queue {}. ", queue, e);
        }
    }



}
