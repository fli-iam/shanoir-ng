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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomOutputStream;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dicom.DicomUtils;
import org.shanoir.ng.shared.dicom.EchoTime;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.shared.dicom.SerieToDatasetsSeparator;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.message.CreateCenterForStudyMessage;
import org.shanoir.ng.shared.message.CreateEquipmentForCenterMessage;
import org.shanoir.ng.shared.model.AcquisitionEquipment;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.StudyCenter;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyCenterRepository;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

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

    private static final String CENTER_CREATION_ERROR = "An error occured during the center creation, please check your rights.";

    private static final String EQUIPMENT_CREATION_ERROR = "An error occured during the equipment creation, please check your rights.";

    private static final String DOUBLE_EQUAL = "==";

    private static final String SEMI_COLON = ";";

    private static final String DELIMITER = ":";

    @Value("${shanoir.import.series.seriesProperties}")
    private String seriesProperties;

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
    private StudyCenterRepository studyCenterRepository;

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolrService solrService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DatasetExpressionRepository datasetExpressionRepository;

    @Autowired
    private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${dcm4chee-arc.protocol}")
    private String dcm4cheeProtocol;

    @Value("${dcm4chee-arc.host}")
    private String dcm4cheeHost;

    @Value("${dcm4chee-arc.port.web}")
    private String dcm4cheePortWeb;

    @Value("${dcm4chee-arc.dicom.web.rs}")
    private String dicomWebRS;

    private ConcurrentHashMap<String, Integer> studyInstanceUIDNumberOfFilesCache;

    private ConcurrentHashMap<String, Integer> seriesInstanceUIDNumberOfFilesCache;

    @PostConstruct
    public void init() {
        studyInstanceUIDNumberOfFilesCache = new ConcurrentHashMap<String, Integer>(1000);
        LOG.info("DICOMWeb STOW-RS cache created: studyInstanceUIDNumberOfFilesCache");
        seriesInstanceUIDNumberOfFilesCache = new ConcurrentHashMap<String, Integer>(1000);
        LOG.info("DICOMWeb STOW-RS cache created: seriesInstanceUIDNumberOfFilesCache");
    }

    @Scheduled(cron = "0 */15 * * * *", zone = "Europe/Paris")
    public void logAndClearCaches() {
        LOG.info(studyInstanceUIDNumberOfFilesCache.toString());
        studyInstanceUIDNumberOfFilesCache.clear();
        LOG.info("DICOMWeb cache cleared: studyInstanceUIDNumberOfFilesCache");
        LOG.info(seriesInstanceUIDNumberOfFilesCache.toString());
        seriesInstanceUIDNumberOfFilesCache.clear();
        LOG.info("DICOMWeb cache cleared: seriesInstanceUIDNumberOfFilesCache");
    }

    @Transactional
    public boolean importDicom(Attributes metaInformationAttributes, Attributes attributes, String modality)
            throws Exception {
        String deIdentificationMethod = attributes.getString(Tag.DeidentificationMethod);
        Sequence deIdentificationActionSequence = attributes.getSequence(Tag.DeidentificationActionSequence);
        if (!StringUtils.isNotBlank(deIdentificationMethod)
                && (deIdentificationActionSequence == null
                || deIdentificationActionSequence.isEmpty())) {
            LOG.error("Only de-identified DICOM is allowed.");
            return false;
        }
        Long studyId = Long.parseLong(attributes.getString(Tag.ClinicalTrialProtocolID));
        Study study = studyService.findById(studyId);
        if (study == null) {
            LOG.error("Shanoir study (research project) not found with ID: {}", studyId);
            return false;
        }
        Serie serie = new Serie(attributes);
        if (!DicomUtils.checkSerieIsIgnored(attributes)) { // do nothing for files of ignored series
            Long subjectId = manageSubject(attributes, study);
            Long centerId = manageCenter(attributes, study.getId());
            Examination examination = manageExamination(attributes, study, subjectId, centerId);
            DatasetAcquisition acquisition = manageAcquisitionAndEquipment(attributes, examination, centerId, serie);
            Dataset dataset = manageDataset(attributes, studyId, subjectId, acquisition, serie);
            DatasetExpression expression = manageDatasetExpression(attributes, dataset);
            datasetExpressionRepository.save(expression);
            solrService.indexDataset(dataset.getId());
            sendToPacs(metaInformationAttributes, attributes);
            countNumberOfFiles(examination, acquisition, serie.getSeriesDescription());
        } else {
            LOG.error("DICOM file of serie {} ignored.", serie.getSeriesDescription());
            return false;
        }
        return true;
    }

    private void countNumberOfFiles(Examination examination, DatasetAcquisition acquisition, String seriesDescription) {
        String keyStudy = examination.getId().toString() + DELIMITER + examination.getExaminationDate();
        Integer numberOfFilesPerStudy = studyInstanceUIDNumberOfFilesCache.get(keyStudy);
        if (numberOfFilesPerStudy == null) {
            studyInstanceUIDNumberOfFilesCache.put(keyStudy, Integer.valueOf(1));
        } else {
            studyInstanceUIDNumberOfFilesCache.put(keyStudy, Integer.valueOf(numberOfFilesPerStudy + 1));
        }
        String keySerie = acquisition.getId() + DELIMITER + seriesDescription;
        Integer numberOfFilesPerSerie = seriesInstanceUIDNumberOfFilesCache.get(keySerie);
        if (numberOfFilesPerSerie == null) {
            seriesInstanceUIDNumberOfFilesCache.put(keySerie, Integer.valueOf(1));
        } else {
            seriesInstanceUIDNumberOfFilesCache.put(keySerie, Integer.valueOf(numberOfFilesPerSerie + 1));
        }
    }

    private Dataset manageDataset(Attributes attributes, Long studyId, Long subjectId, DatasetAcquisition acquisition, Serie serie)
            throws Exception {
        int datasetIndex = -1; // Used for single-dataset acquisitions
        Dataset currentDataset = null;
        List<Dataset> datasets = acquisition.getDatasets();
        if (datasets != null && !datasets.isEmpty()) {
            boolean serieIdentifiedForNotSeparating = checkSerieForPropertiesString(serie, seriesProperties);
            // Manage split series in the if-clause
            if (!serieIdentifiedForNotSeparating) {
                // Check if serie == acquisition: separate datasets
                currentDataset = manageDatasetSeparation(attributes, acquisition, datasets);
                datasetIndex = datasets.size() + 1;
            } else {
                currentDataset = datasets.getLast();
            }
        } else {
            datasets = new ArrayList<Dataset>();
            acquisition.setDatasets(datasets);
        }
        // Create a new dataset
        if (currentDataset == null) {
            org.shanoir.ng.importer.dto.Dataset dataset = new org.shanoir.ng.importer.dto.Dataset();
            dataset.setFirstImageSOPInstanceUID(attributes.getString(Tag.SOPInstanceUID));
            Set<EchoTime> echoTimes = new HashSet<>();
            EchoTime echoTime = new EchoTime();
            echoTime.setEchoNumber(attributes.getInt(Tag.EchoNumbers, 0));
            echoTime.setEchoTime(attributes.getDouble(Tag.EchoTime, 0.0));
            echoTimes.add(echoTime);
            dataset.setEchoTimes(echoTimes);
            currentDataset = acquisitionContext.generateFlatDataset(
                    serie, dataset, datasetIndex, subjectId,
                    attributes);
            acquisition.getDatasets().add(currentDataset);
            currentDataset.setDatasetAcquisition(acquisition);
            currentDataset.setStudyId(studyId);
            currentDataset = datasetService.create(currentDataset);
        }
        return currentDataset;
    }

    private Dataset manageDatasetSeparation(Attributes attributes, DatasetAcquisition acquisition,
            List<Dataset> datasets) {
        final HashMap<SerieToDatasetsSeparator, Dataset> existingDatasetToSeparatorMap = new HashMap<SerieToDatasetsSeparator, Dataset>();
        for (Dataset dataset : datasets) {
            SerieToDatasetsSeparator existingSeparator;
            double[] imageOrientationPatient = null;
            if (dataset.getOriginMetadata().getImageOrientationPatient() != null) {
                String iop = dataset.getOriginMetadata().getImageOrientationPatient();
                if (iop != null && !iop.isBlank()) {
                    iop = iop.trim().replaceAll("[\\\s]+", ",");
                    String[] parts = iop.split("\s*,\s*");
                    imageOrientationPatient = new double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        imageOrientationPatient[i] = Double.parseDouble(parts[i]);
                    }
                }
            }
            Set<EchoTime> echoTimes = new HashSet<EchoTime>();
            if (acquisition instanceof MrDatasetAcquisition) {
                if (dataset instanceof MrDataset) {
                    MrDataset mrDataset = (MrDataset) dataset;
                    mrDataset.getEchoTime().stream().forEach(
                            eT -> echoTimes.add(eT.getEchoTimeShared()));
                }
            }
            int acquisitionNumber = 0;
            if (acquisition.getAcquisitionNumber() != null) {
                acquisitionNumber = acquisition.getAcquisitionNumber().intValue();
            }
            existingSeparator = new SerieToDatasetsSeparator(
                    acquisitionNumber,
                    echoTimes,
                    imageOrientationPatient);
            existingDatasetToSeparatorMap.put(existingSeparator, dataset);
        }
        SerieToDatasetsSeparator currentSeriesToDatasetsSeparator = createSeriesToDatasetsSeparator(attributes);
        for (Map.Entry<SerieToDatasetsSeparator, Dataset> entry : existingDatasetToSeparatorMap.entrySet()) {
            if (entry.getKey().equals(currentSeriesToDatasetsSeparator)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private SerieToDatasetsSeparator createSeriesToDatasetsSeparator(Attributes attributes) {
        // Acquisition number
        final int acquisitionNumber = attributes.getInt(Tag.AcquisitionNumber, 0);
        // Echo times
        Set<EchoTime> echoTimes = new HashSet<>();
        EchoTime echoTime = new EchoTime();
        echoTime.setEchoNumber(attributes.getInt(Tag.EchoNumbers, 0));
        echoTime.setEchoTime(attributes.getDouble(Tag.EchoTime, 0.0));
        echoTimes.add(echoTime);
        // Image orientation patient
        List<Double> imageOrientationPatient = new ArrayList<>();
        double[] imageOrientationPatientArray = attributes.getDoubles(Tag.ImageOrientationPatient);
        if (imageOrientationPatientArray != null) {
            for (int i = 0; i < imageOrientationPatientArray.length; i++) {
                imageOrientationPatient.add(imageOrientationPatientArray[i]);
            }
        }
        double[] imageOrientationPatientsDoubleArray = imageOrientationPatient == null
                ? null
                : imageOrientationPatient.stream().mapToDouble(i -> i).toArray();
        SerieToDatasetsSeparator seriesToDatasetsSeparator =
                new SerieToDatasetsSeparator(
                acquisitionNumber, echoTimes,
                imageOrientationPatientsDoubleArray);
        return seriesToDatasetsSeparator;
    }

    private DatasetAcquisition manageAcquisitionAndEquipment(Attributes attributes, Examination examination, Long centerId, Serie serie)
            throws Exception {
        DatasetAcquisition acquisition = null;
        final String userName = KeycloakUtil.getTokenUserName();
        Optional<DatasetAcquisition> existingAcquisition = acquisitionService
                .findByExaminationAndSeriesInstanceUIDWithDatasets(
                        examination.getId(),
                        serie.getSeriesInstanceUID());
        if (existingAcquisition.isPresent()) {
            return existingAcquisition.get();
        }
        int rank = 0;
        if (serie.getSeriesNumber() > 0) {
            rank = serie.getSeriesNumber() - 1;
        }
        acquisition = acquisitionContext.generateFlatDatasetAcquisitionForSerie(
                userName, serie, rank, attributes);
        acquisition.setExamination(examination);
        acquisition.setAcquisitionNumber(attributes.getInt(Tag.AcquisitionNumber, 0));

        EquipmentDicom equipmentDicom = serie.getEquipment();
        String centerName = attributes.getString(Tag.InstitutionName);
        if (equipmentDicom.isComplete() && centerName != null && !centerName.isEmpty()) {
            String equipmentName = equipmentDicom.toStringAcquisitionEquipment(centerName);
            LOG.debug("Searching with equipment name: " + equipmentName);
            AcquisitionEquipment equipment = acquisitionEquipmentRepository.findFirstByName(equipmentName);
            if (equipment == null) {
                equipment = acquisitionEquipmentRepository.findFirstByNameContainingAndNameContaining(centerName, equipmentDicom.getDeviceSerialNumber());
            }
            if (equipment == null) {
                CreateEquipmentForCenterMessage message = new CreateEquipmentForCenterMessage(centerId,
                        equipmentDicom);
                Long equipmentId = (Long) rabbitTemplate.convertSendAndReceive(
                        RabbitMQConfiguration.ACQUISITION_EQUIPMENT_CREATE_QUEUE,
                        objectMapper.writeValueAsString(message));
                if (equipmentId == null) {
                    throw new RestServiceException(
                            new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), EQUIPMENT_CREATION_ERROR, null));
                }
                LOG.info("Equipment created in MS Studies with ID: {}, Name: {}", equipmentId, equipmentName);
                equipment = new AcquisitionEquipment();
                equipment.setId(equipmentId);
                equipment.setName(equipmentName);
                equipment = acquisitionEquipmentRepository.save(equipment);
                LOG.info("Equipment created in MS Datasets with ID: {}, Name: {}", equipment.getId(), equipment.getName());
            }
            acquisition.setAcquisitionEquipmentId(equipment.getId());
        } else {
            LOG.info("EquipmentDicom incomplete to auto-create acquisition equipment, set to 0L.");
            acquisition.setAcquisitionEquipmentId(0L);
        }
        return acquisitionService.create(acquisition, false);
    }

    /**
     * This method calls MS Studies (domain owner of centers and study_center)
     * to either create a new center and propagate it to MS Datasets, or to add
     * an existing center to study_center. To avoid thousands of RabbitMQ messages
     * a local lookup is made into the replication tables, and if the center exists
     * and is already in the study, we send no message to MS Studies to avoid overhead.
     *
     * For the moment, we assume here that only pseudonymized
     * DICOM enter this import workflow. This means institution
     * name and address have been removed, so no way to create a
     * clean center, as Shanoir does normally. That is, why we assume,
     * that we do not communicate with MS Studies to create an unkown
     * center, we only keep it in MS Datasets to increase performance
     * and not too lose any significant value.
     *
     * @param institutionDicom
     * @param studyId
     * @return
     * @throws JsonProcessingException
     * @throws AmqpException
     * @throws RestServiceException
     */
    private Long manageCenter(Attributes attributes, Long studyId) throws JsonProcessingException, AmqpException, RestServiceException {
        InstitutionDicom institutionDicom = new InstitutionDicom(attributes);
        Center center = null;
        Optional<StudyCenter> studyCenterOpt = null;
        Optional<Center> centerOpt = centerRepository.findFirstByNameContainingOrderByIdAsc(institutionDicom.getInstitutionName());
        if (!centerOpt.isEmpty()) {
            center = centerOpt.get();
            studyCenterOpt = studyCenterRepository.findByStudy_IdAndCenter_Id(studyId, center.getId());
        }
        if (studyCenterOpt == null || studyCenterOpt.isEmpty()) {
            // Communicate with MS Studies here, if not existing or not yet in study
            CreateCenterForStudyMessage message = new CreateCenterForStudyMessage();
            message.setStudyId(studyId);
            message.setInstitutionDicom(institutionDicom);
            String returnMessage = (String) rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfiguration.CENTER_CREATE_QUEUE,
                    objectMapper.writeValueAsString(message));
            if (returnMessage == null) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), CENTER_CREATION_ERROR, null));
            }
            String[] parts = returnMessage.split(DELIMITER);
            Long centerId = Long.parseLong(parts[0]);
            Long studyCenterId = Long.parseLong(parts[1]);
            LOG.info("Center created or added to MS Studies with ID: {}, Name: {}, Study ID: {}", centerId, institutionDicom.getInstitutionName(), studyId);
            if (center == null) {
                center = new Center();
                center.setId(centerId);
                center.setName(institutionDicom.getInstitutionName());
                isCenterInStudy(studyId, center, studyCenterId);
                centerRepository.save(center);
            } else {
                if (!isCenterInStudy(studyId, center, studyCenterId)) {
                    centerRepository.save(center);
                }
            }
            LOG.info("Center created or added to MS Datasets with ID: {}, Name: {}, Study ID: {}", centerId, institutionDicom.getInstitutionName(), studyId);
            return centerId;
        } else {
            return studyCenterOpt.get().getCenter().getId();
        }
    }

    private boolean isCenterInStudy(Long studyId, Center center, Long studyCenterId) {
        List<StudyCenter> studyCenterList = center.getStudyCenterList();
        if (studyCenterList != null && !studyCenterList.isEmpty()) {
            for (StudyCenter studyCenter : studyCenterList) {
                if (studyCenter.getStudy().getId().equals(studyId)) {
                    return true;
                }
            }
        }
        if (studyCenterList == null) {
            studyCenterList = new ArrayList<>();
            center.setStudyCenterList(studyCenterList);
        }
        StudyCenter studyCenter = new StudyCenter();
        studyCenter.setId(studyCenterId);
        Study study = studyService.findById(studyId);
        studyCenter.setStudy(study);
        studyCenter.setCenter(center);
        center.getStudyCenterList().add(studyCenter);
        return false;
    }


    private Examination manageExamination(Attributes attributes, Study study, Long subjectId, Long centerId) throws EntityNotFoundException {
        Examination examination = null;
        org.shanoir.ng.importer.dto.Study studyDICOM = new org.shanoir.ng.importer.dto.Study(attributes);
        List<Examination> examinations = examinationService.findBySubjectIdStudyId(subjectId, study.getId());
        Optional<Examination> existingExamination = examinations.stream()
                .filter(e -> e.getStudyInstanceUID().equals(studyDICOM.getStudyInstanceUID()))
                .findFirst();
        if (existingExamination.isPresent()) {
            examination = existingExamination.get();
        } else {
            Subject subject = new Subject();
            subject.setId(subjectId);
            examination = new Examination();
            examination.setStudy(study);
            examination.setSubject(subject);
            examination.setExaminationDate(studyDICOM.getStudyDate());
            examination.setStudyInstanceUID(studyDICOM.getStudyInstanceUID());
            examination.setComment(studyDICOM.getStudyDescription());
            examination.setCenterId(centerId);
            examination = examinationService.save(examination);
            LOG.info("Examination created: {}", examination.toString());
        } // Avoid, that the examination creation makes RabbitMQ calls
        return examination;
    }

    private Long manageSubject(Attributes attributes, Study study)
            throws JsonProcessingException, RestServiceException {
        String subjectName = attributes.getString(Tag.PatientName);
        Subject subject = subjectService.findByNameAndStudyId(subjectName, study.getId());
        if (subject == null) {
            // Communicate with MS Studies here, only if subject not existing
            // Use convertSendAndReceive to assure, that subject created in MS Studies
            org.shanoir.ng.importer.dto.Subject subjectDTO = new org.shanoir.ng.importer.dto.Subject();
            subjectDTO.setName(subjectName);
            subjectDTO.setStudy(new IdName(study.getId(), study.getName()));
            Long subjectId = (Long) rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfiguration.SUBJECTS_QUEUE,
                    objectMapper.writeValueAsString(subjectDTO));
            if (subjectId == null) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), SUBJECT_CREATION_ERROR, null));
            }
            LOG.info("Subject created in MS Studies with ID: {}, Name: {}", subjectId, subjectName);
            // We create subject here ourselves to avoid 1) transactional issues
            // and 2) to much RabbitMQ communication
            subject = new Subject();
            subject.setId(subjectId); // use same as MS Studies
            subject.setName(subjectName);
            subject.setStudy(study);
            subjectService.save(subject);
            LOG.info("Subject created in MS Datasets with ID: {}, Name: {}", subjectId, subjectName);
            return subjectId;
        } else {
            return subject.getId();
        }
    }

    /**
     * This method receives a serie object and a String from the properties
     * and checks if the tag exists with a specific value.
     *
     * @throws NoSuchFieldException
     */
    private boolean checkSerieForPropertiesString(final Serie serie, final String propertiesString)
            throws NoSuchFieldException {
        final String[] itemArray = propertiesString.split(SEMI_COLON);
        for (final String item : itemArray) {
            final String tag = item.split(DOUBLE_EQUAL)[0];
            final String value = item.split(DOUBLE_EQUAL)[1];
            LOG.debug("checkDicomFromProperties : tag={}, value={}", tag, value);
            try {
                Class<? extends Serie> aClass = serie.getClass();
                Field field = aClass.getDeclaredField(tag);
                field.setAccessible(true);
                String dicomValue = (String) field.get(serie);
                String wildcard = Utils.wildcardToRegex(value);
                if (dicomValue != null && dicomValue.matches(wildcard)) {
                    return true;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOG.error(e.getMessage());
            }
        }
        return false;
    }

    /**
     * This method writes both attributes to an output stream and converts
     * this one to an input stream, that can be used to send the manipulated
     * file to the backend pacs.
     *
     * @param metaInformationAttributes
     * @param datasetAttributes
     * @throws IOException
     * @throws Exception
     */
    public void sendToPacs(Attributes metaInformationAttributes, Attributes datasetAttributes)
            throws IOException, Exception {
        /**
         * Create a new output stream to write the changes into and use its bytes
         * to produce a new input stream to send later by http client to the DICOM
         * server.
         */
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        String tsuid = metaInformationAttributes.getString(Tag.TransferSyntaxUID);
        // close calls to the outer stream, close the inner stream
        try (DicomOutputStream dOS = new DicomOutputStream(bAOS, UID.ExplicitVRLittleEndian)) {
            dOS.writeFileMetaInformation(metaInformationAttributes);
            dOS.switchTransferSyntax(tsuid);
            dOS.writeDataset(null, datasetAttributes);
            try (InputStream finalInputStream = new ByteArrayInputStream(bAOS.toByteArray())) {
                dicomWebService.sendDicomInputStreamToPacs(finalInputStream);
            }
        }
    }

    /**
     * Create the necessary dataset expression.
     *
     * @param attributes
     * @param measurementDataset
     * @throws MalformedURLException
     * @throws ShanoirException
     */
    public DatasetExpression manageDatasetExpression(Attributes attributes, Dataset dataset)
            throws MalformedURLException, ShanoirException {
        DatasetExpression currentExpression = null;
        for (DatasetExpression expression : dataset.getDatasetExpressions()) {
            if (DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat())) {
                currentExpression = expression;
                break;
            }
        }
        if (currentExpression == null) {
            currentExpression = new DatasetExpression();
            currentExpression.setCreationDate(LocalDateTime.now());
            currentExpression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
            currentExpression.setDataset(dataset);
        }
        if (dataset.getDatasetExpressions() != null) {
            dataset.getDatasetExpressions().add(currentExpression);
        } else {
            ArrayList<DatasetExpression> datasetExpressions = new ArrayList<>();
            datasetExpressions.add(currentExpression);
            dataset.setDatasetExpressions(datasetExpressions);
        }
        addDatasetFile(attributes, currentExpression);
        return currentExpression;
    }

    /**
     * Add a dataset_file, as WADO-RS links in that case,
     * as OHIF viewer works only with new version of dcm4chee (arc-light 5.x).
     *
     * @param attributes
     * @param expression
     * @return
     * @throws MalformedURLException
     * @throws ShanoirException
     */
    private void addDatasetFile(Attributes attributes, DatasetExpression expression)
            throws MalformedURLException, ShanoirException {
        final String studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
        final String seriesInstanceUID = attributes.getString(Tag.SeriesInstanceUID);
        final String sOPInstanceUID = attributes.getString(Tag.SOPInstanceUID);
        final StringBuffer wadoStrBuf = new StringBuffer();
        wadoStrBuf.append(dcm4cheeProtocol + dcm4cheeHost + DELIMITER + dcm4cheePortWeb);
        wadoStrBuf.append(dicomWebRS + "/" + studyInstanceUID
                + "/series/" + seriesInstanceUID + "/instances/" + sOPInstanceUID);
        URL wadoURL = new URL(wadoStrBuf.toString());
        String newPath = wadoURL.toString();
        List<DatasetFile> datasetFilesDb = expression.getDatasetFiles();
        if (datasetFilesDb == null) {
            datasetFilesDb = new ArrayList<DatasetFile>();
        }
        // Check if a DatasetFile with this path already exists
        boolean pathExists = datasetFilesDb.stream()
                .anyMatch(existingFile -> newPath.equals(existingFile.getPath()));
        if (pathExists) {
            LOG.error("DatasetFile with path {} already exists in expression, skipping duplicate", newPath);
            throw new ShanoirException("DatasetFile with path " + newPath + " already exists in expression, skipping duplicate");
        }
        DatasetFile datasetFile = new DatasetFile();
        datasetFile.setPath(newPath);
        datasetFile.setPacs(true);
        datasetFile.setDatasetExpression(expression);
        datasetFilesDb.add(datasetFile);
        expression.setDatasetFiles(datasetFilesDb);
    }

}
