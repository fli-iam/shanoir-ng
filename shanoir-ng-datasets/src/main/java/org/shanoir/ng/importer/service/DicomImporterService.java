package org.shanoir.ng.importer.service;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
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
        String subjectName = datasetAttributes.getString(Tag.PatientName);
        Subject subject = subjectService.findByNameAndStudyId(subjectName, studyId);
        if (subject == null) {
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
        }
        return true;
    }

}
