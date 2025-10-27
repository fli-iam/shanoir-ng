package org.shanoir.ng.importer.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String UNKNOWN = "unknown";

    @Autowired
    private StudyService studyService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private DatasetAcquisitionService acquisitionService;

    @Autowired
    private DatasetAcquisitionContext acquisitionContext;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public boolean importDicom(Attributes metaInformationAttributes, Attributes datasetAttributes, String modality)
            throws Exception {
        String deIdentificationMethod = datasetAttributes.getString(Tag.DeidentificationMethod);
        Sequence deIdentificationActionSequence = datasetAttributes.getSequence(Tag.DeidentificationActionSequence);
        if (!StringUtils.isNotBlank(deIdentificationMethod)
                && deIdentificationActionSequence.isEmpty()) {
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
        Center center = manageCenter(datasetAttributes);
        Examination examination = manageExamination(datasetAttributes, study, subject, center);
        DatasetAcquisition acquisition = manageAcquisition(datasetAttributes, examination);
        // and dataset depending on volume
        // sendToPacs and index Dataset to Solr, in case new created
        return true;
    }

    private DatasetAcquisition manageAcquisition(Attributes datasetAttributes, Examination examination) throws Exception {
        DatasetAcquisition acquisition = null;
        final String userName = KeycloakUtil.getTokenUserName();
        Serie serieDICOM = new Serie(datasetAttributes);
        List<DatasetAcquisition> acquisitions = acquisitionService.findByExamination(examination.getId());
        Optional<DatasetAcquisition> existingAcquisition = acquisitions.stream()
                .filter(a -> a.getSeriesInstanceUID().equals(serieDICOM.getSeriesInstanceUID()))
                .findFirst();
        if (existingAcquisition.isPresent()) {
            acquisition = existingAcquisition.get();
        } else {
            acquisition = acquisitionContext.generateFlatDatasetAcquisitionForSerie(
                userName, serieDICOM, serieDICOM.getSeriesNumber(), datasetAttributes);
        }
        return acquisition;
    }

    /**
     * For the moment, we assume here that only pseudonymized
     * DICOM enter this import workflow. This means institution
     * name and address have been removed, so no way to create a
     * clean center, as Shanoir does normally. That is, why we assume,
     * that we do not communicate with MS Studies to create an unkown
     * center, we only keep it in MS Datasets to increase performance
     * and not too lose any significant value.
     * 
     * @param datasetAttributes
     * @return
     */
    private Center manageCenter(Attributes datasetAttributes) {
        String institutionName = datasetAttributes.getString(Tag.InstitutionName);
        String institutionAddress = datasetAttributes.getString(Tag.InstitutionAddress);
        if (StringUtils.isNotBlank(institutionName)) {
	    	return findOrCreateCenter(institutionName);
		} else {
            return findOrCreateCenter(UNKNOWN);
		}
    }

    private Center findOrCreateCenter(String institutionName) {
        Optional<Center> centerOpt = centerRepository.findFirstByNameContainingOrderByIdAsc(institutionName);
        if (!centerOpt.isEmpty()) {
            return centerOpt.get();
        } else {
            Center center = new Center();
            center.setName(institutionName);
            // @todo: create center in ms studies to remain consistent
            return centerRepository.save(center);
        }
    }

    private Examination manageExamination(Attributes datasetAttributes, Study study, Subject subject, Center center) {
        Examination examination = null;
        org.shanoir.ng.importer.dto.Study studyDICOM = new org.shanoir.ng.importer.dto.Study(datasetAttributes);
        List<Examination> examinations = examinationService.findBySubjectIdStudyId(subject.getId(), study.getId());
        Optional<Examination> existingExamination = examinations.stream()
                .filter(e -> e.getStudyInstanceUID().equals(studyDICOM.getStudyInstanceUID()))
                .findFirst();
        if (existingExamination.isPresent()) {
            examination = existingExamination.get();
        } else {
            examination = new Examination();
            examination.setStudy(study);
            examination.setSubject(subject);
            examination.setExaminationDate(studyDICOM.getStudyDate());
            examination.setStudyInstanceUID(studyDICOM.getStudyInstanceUID());
            examination.setComment(studyDICOM.getStudyDescription());
            examination.setCenterId(center.getId());
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
