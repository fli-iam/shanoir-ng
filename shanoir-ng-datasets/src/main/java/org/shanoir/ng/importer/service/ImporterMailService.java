package org.shanoir.ng.importer.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.EmailBase;
import org.shanoir.ng.shared.email.EmailDatasetImportFailed;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
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

    @Autowired
    private StudyUserRightsRepository studyUserRightRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Sens the import email through rabbitMQ to user MS
     * @param importJob the import job
     * @param userId the userID
     * @param examination the exam ID
     * @param generatedAcquisitions
     */
    public void sendImportEmail(ImportJob importJob, Long userId, Examination examination, Set<DatasetAcquisition> generatedAcquisitions) {
        EmailDatasetsImported generatedMail = new EmailDatasetsImported();

        LinkedHashMap<Long, String> datasets = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(generatedAcquisitions)) {
            return;
        }
        generatedMail.setExamDate(examination.getExaminationDate().toString());
        generatedMail.setExaminationId(examination.getId().toString());
        generatedMail.setStudyId(importJob.getStudyId().toString());
        generatedMail.setSubjectName(importJob.getSubjectName());
        generatedMail.setStudyName(importJob.getStudyName());
        generatedMail.setUserId(userId);
        generatedMail.setStudyCard(importJob.getStudyCardName());

        for (DatasetAcquisition acq : generatedAcquisitions.stream().sorted(Comparator.comparingInt(DatasetAcquisition::getSortingIndex)).toList()) {
            if (!CollectionUtils.isEmpty(acq.getDatasets())) {
                for (Dataset dataset : acq.getDatasets()) {
                    datasets.put(dataset.getId(), dataset.getName());
                }
            }
        }

        generatedMail.setDatasets(datasets);
        sendMail(importJob, generatedMail, RabbitMQConfiguration.IMPORT_DATASET_MAIL_QUEUE);
    }

    public void sendFailureMail(ImportJob importJob, Long userId, String errorMessage) {
        EmailDatasetImportFailed generatedMail = new EmailDatasetImportFailed();
        generatedMail.setExaminationId(importJob.getExaminationId().toString());
        generatedMail.setStudyId(importJob.getStudyId().toString());
        generatedMail.setStudyCardId(importJob.getStudyCardId() != null ? importJob.getStudyCardId().toString() : "");
        generatedMail.setSubjectName(importJob.getSubjectName());
        generatedMail.setStudyName(importJob.getStudyName());
        generatedMail.setUserId(userId);

        generatedMail.setErrorMessage(errorMessage != null ? errorMessage : "An unexpected error occured, please contact Shanoir support.");

        sendMail(importJob, generatedMail, RabbitMQConfiguration.IMPORT_DATASET_FAILED_MAIL_QUEUE);
    }

    /**
     * Sends the given mail in entry to all recipients in a given study
     * @param job the imprt job
     * @param email the recipients
     * @param queue
     */
    private void sendMail(ImportJob job, EmailBase email, String queue) {
        List<Long> recipients = new ArrayList<>();

        // Get all recpients
        List<StudyUser> users = (List<StudyUser>) studyUserRightRepo.findByStudyId(job.getStudyId());

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
            LOG.error("Could not send email for this import. ", e);
        }
    }



}
