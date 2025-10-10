package org.shanoir.ng.importer.service;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The DicomImporterService is used by the STOWRSMultipartRequestFilter.
 * 
 * Single DICOM files are sent to the filter via POST requests and the
 * 4 standard modalities MR,CT,PT,NM are managed by this service.
 * 
 * Attention: All DICOM files are pseudonymized outside Shanoir, e.g. in
 * Karnak or elsewhere. We do not apply any additional pseudonymization
 * on the files here, even the contrary we fully rely on the DICOM info.
 * A DeidentificationMethod needs to be present to continue with the import.
 * 
 * To map the Shanoir study, the DICOM attribute ClinicalTrialProtocolID
 * (0012,0020) is used. No new Shanoir study (research project) is created,
 * it must be created manually before by its PI.
 * 
 * We assume, that the patientName is already matching the subject name and
 * rely on this, e.g. and create new subjects in case not yet existing.
 * 
 * @author mkain
 */
@Service
public class DicomImporterService {

    private static final Logger LOG = LoggerFactory.getLogger(DicomImporterService.class);

    private static final String SUBJECT_CREATION_ERROR = "An error occured during the subject creation, please check your rights.";

    @Autowired
    private StudyService studyService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public boolean importDicom(Attributes metaInformationAttributes, Attributes datasetAttributes, String modality)
            throws RestServiceException, JsonProcessingException, AmqpException {
        String deIdentificationMethod = datasetAttributes.getString(Tag.DeidentificationMethod);
        if (!StringUtils.isNotBlank(deIdentificationMethod)) {
            LOG.error("Only de-identified DICOM is allowed.");
            return false;
        }
        Long studyId = datasetAttributes.getLong(Tag.ClinicalTrialProtocolID, 0L);
        Study study = studyService.findById(studyId);
        if (study == null) {
            LOG.error("Shanoir study (research project) not found with ID: {}", studyId);
            return false;
        }
        Subject subject = manageSubject(datasetAttributes, study);
        Examination examination = manageExamination(datasetAttributes, study, subject);
        return true;
    }

    private Examination manageExamination(Attributes datasetAttributes, Study study, Subject subject) {
        Examination examination = null;
        LocalDate examinationDate = DateTimeUtils.dateToLocalDate(datasetAttributes.getDate(Tag.StudyDate));
        String examinationComment = datasetAttributes.getString(Tag.StudyDescription);
        List<Examination> examinations = examinationService.findBySubjectIdStudyId(subject.getId(), study.getId());
        examinations = examinations.stream().filter(e -> e.getExaminationDate().equals(examinationDate)).toList();
        if (examinations.size() == 1) {
            examination = examinations.iterator().next();
        } else { // Create new examination
            examination = new Examination();
            examination.setStudy(study);
            examination.setSubject(subject);
            examination.setExaminationDate(examinationDate);
            examination.setComment(examinationComment);
            // @todo: findOrCreateCenter
            examination.setCenterId(null);
            examination = examinationService.save(examination);
        } // Avoid, that the examination creation makes RabbitMQ calls
        return examination;
    }

    private Subject manageSubject(Attributes datasetAttributes, Study study)
            throws JsonProcessingException, RestServiceException {
        String subjectName = datasetAttributes.getString(Tag.PatientName);
        Subject subject = subjectService.findByNameAndStudyId(subjectName, study.getId());
        if (subject == null) { // Communicate with MS Studies here, only if not existing
            subject = new Subject();
            subject.setName(subjectName);
            subject.setStudy(study);
            Long subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE,
                    objectMapper.writeValueAsString(subject));
            if (subjectId == null) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), SUBJECT_CREATION_ERROR, null));
            }
            LOG.info("Subject created with ID: {}, Name: {}", subjectId, subjectName);
        } // We need to assure here: subject created in ms studies + ms datasets
        return subject;
    }

}
