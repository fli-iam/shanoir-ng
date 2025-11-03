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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.emf.MultiframeExtractor;
import org.dcm4che3.io.DicomOutputStream;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
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
import org.shanoir.ng.shared.dicom.EchoTime;
import org.shanoir.ng.shared.dicom.SerieToDatasetsSeparator;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String DOUBLE_EQUAL = "==";

    private static final String SEMI_COLON = ";";

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
    private DICOMWebService dicomWebService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolrService solrService;

    @Autowired
    private DatasetService datasetService;

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

    @Transactional
    public boolean importDicom(Attributes metaInformationAttributes, Attributes attributes, String modality)
            throws Exception {
        // DicomEnhanced: get attributes differently
        final String sopClassUID = attributes.getString(Tag.SOPClassUID);
        if (UID.EnhancedMRImageStorage.equals(sopClassUID)
                || UID.EnhancedMRColorImageStorage.equals(sopClassUID)
                || UID.EnhancedCTImageStorage.equals(sopClassUID)
                || UID.EnhancedPETImageStorage.equals(sopClassUID)) {
            MultiframeExtractor emf = new MultiframeExtractor();
            attributes = emf.extract(attributes, 0);
        }
        String deIdentificationMethod = attributes.getString(Tag.DeidentificationMethod);
        Sequence deIdentificationActionSequence = attributes.getSequence(Tag.DeidentificationActionSequence);
        if (!StringUtils.isNotBlank(deIdentificationMethod)
                && (deIdentificationActionSequence == null    
                || deIdentificationActionSequence.isEmpty())) {
            LOG.error("Only de-identified DICOM is allowed.");
            return false;
        }
        Long studyId = attributes.getLong(Tag.ClinicalTrialProtocolID, 0L);
        Study study = studyService.findById(studyId);
        if (study == null) {
            LOG.error("Shanoir study (research project) not found with ID: {}", studyId);
            return false;
        }
        Subject subject = manageSubject(attributes, study);
        Center center = manageCenter(attributes);
        Examination examination = manageExamination(attributes, study, subject, center);
        DatasetAcquisition acquisition = manageAcquisition(attributes, examination);
        Dataset dataset = manageDataset(attributes, subject, acquisition);
        manageDatasetExpression(attributes, dataset);
        Dataset createdDataset = datasetService.create(dataset);
        solrService.indexDataset(createdDataset.getId());
        sendToPacs(metaInformationAttributes, attributes);
        return true;
    }

    private Dataset manageDataset(Attributes attributes, Subject subject, DatasetAcquisition acquisition)
            throws Exception {
        Dataset currentDataset = null;
        Serie serieDICOM = new Serie(attributes);
        List<Dataset> datasets = acquisition.getDatasets();
        if (!datasets.isEmpty()) {
            boolean serieIdentifiedForNotSeparating = checkSerieForPropertiesString(serieDICOM, seriesProperties);
            // Manage split series in the if-clause
            if (!serieIdentifiedForNotSeparating) {
                // Check if serie == acquisition: separate datasets
                currentDataset = manageDatasetSeparation(attributes, acquisition, datasets);
            }
        }
        // Create a new dataset
        if (currentDataset == null) {
            org.shanoir.ng.importer.dto.Dataset dataset = new org.shanoir.ng.importer.dto.Dataset();
            dataset.setFirstImageSOPInstanceUID(attributes.getString(Tag.SOPInstanceUID));
            // dataset.setEchoTimes(seriesToDatasetsSeparator);
            currentDataset = acquisitionContext.generateFlatDataset(serieDICOM, dataset, 0, subject.getId(),
                    attributes);
            acquisition.getDatasets().add(currentDataset);
            currentDataset.setDatasetAcquisition(acquisition);
        }
        return currentDataset;
    }

    private Dataset manageDatasetSeparation(Attributes attributes, DatasetAcquisition acquisition,
            List<Dataset> datasets) {
        final HashMap<SerieToDatasetsSeparator, Dataset> existingDatasetToSeparatorMap = new HashMap<SerieToDatasetsSeparator, Dataset>();
        for (Dataset dataset : datasets) {
            SerieToDatasetsSeparator existingSeparator;
            String[] parts = dataset.getOriginMetadata().getImageOrientationPatient().split("\\s*,\\s*");
            double[] imageOrientationPatient = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                imageOrientationPatient[i] = Double.parseDouble(parts[i]);
            }
            Set<EchoTime> echoTimes = new HashSet<EchoTime>();
            if (acquisition instanceof MrDatasetAcquisition) {
                MrDataset mrDataset = (MrDataset) dataset;
                mrDataset.getEchoTime().stream().forEach(
                        eT -> echoTimes.add(eT.getEchoTimeShared()));
            }
            existingSeparator = new SerieToDatasetsSeparator(
                    acquisition.getAcquisitionNumber().intValue(),
                    echoTimes,
                    imageOrientationPatient);
            existingDatasetToSeparatorMap.put(existingSeparator, dataset);
        }
        SerieToDatasetsSeparator seriesToDatasetsSeparator = createSeriesToDatasetsSeparator(attributes);
        for (Map.Entry<SerieToDatasetsSeparator, Dataset> entry : existingDatasetToSeparatorMap.entrySet()) {
            if (entry.getKey().equals(seriesToDatasetsSeparator)) {
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
        SerieToDatasetsSeparator seriesToDatasetsSeparator = new SerieToDatasetsSeparator(acquisitionNumber, echoTimes,
                imageOrientationPatientsDoubleArray);
        return seriesToDatasetsSeparator;
    }

    private DatasetAcquisition manageAcquisition(Attributes attributes, Examination examination) throws Exception {
        DatasetAcquisition acquisition = null;
        final String userName = KeycloakUtil.getTokenUserName();
        Serie serieDICOM = new Serie(attributes);
        List<DatasetAcquisition> acquisitions = acquisitionService.findByExamination(examination.getId());
        Optional<DatasetAcquisition> existingAcquisition = acquisitions.stream()
                .filter(a -> a.getSeriesInstanceUID().equals(serieDICOM.getSeriesInstanceUID()))
                .findFirst();
        if (existingAcquisition.isPresent()) {
            acquisition = existingAcquisition.get();
        } else {
            acquisition = acquisitionContext.generateFlatDatasetAcquisitionForSerie(
                    userName, serieDICOM, serieDICOM.getSeriesNumber(), attributes);
        }
        return acquisitionService.create(acquisition);
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
     * @param attributes
     * @return
     */
    private Center manageCenter(Attributes attributes) {
        String institutionName = attributes.getString(Tag.InstitutionName);
        String institutionAddress = attributes.getString(Tag.InstitutionAddress);
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

    private Examination manageExamination(Attributes attributes, Study study, Subject subject, Center center) {
        Examination examination = null;
        org.shanoir.ng.importer.dto.Study studyDICOM = new org.shanoir.ng.importer.dto.Study(attributes);
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

    private Subject manageSubject(Attributes attributes, Study study)
            throws JsonProcessingException, RestServiceException {
        String subjectName = attributes.getString(Tag.PatientName);
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
        // close calls to the outer stream, close the inner stream
        try (DicomOutputStream dOS = new DicomOutputStream(bAOS,
                metaInformationAttributes.getString(Tag.TransferSyntaxUID))) {
            dOS.writeDataset(metaInformationAttributes, datasetAttributes);
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
     */
    public void manageDatasetExpression(Attributes attributes, Dataset dataset)
            throws MalformedURLException {
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
            dataset.setDatasetExpressions(Collections.singletonList(currentExpression));
        }
        addDatasetFile(attributes, currentExpression);
    }

    /**
     * Add a dataset_file, as WADO-RS links in that case,
     * as OHIF viewer works only with new version of dcm4chee (arc-light 5.x).
     * 
     * @param attributes
     * @param expression
     * @return
     * @throws MalformedURLException
     */
    private void addDatasetFile(Attributes attributes, DatasetExpression expression)
            throws MalformedURLException {
        DatasetFile datasetFile = new DatasetFile();
        final String studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
        final String seriesInstanceUID = attributes.getString(Tag.SeriesInstanceUID);
        final String sOPInstanceUID = attributes.getString(Tag.SOPInstanceUID);
        final StringBuffer wadoStrBuf = new StringBuffer();
        wadoStrBuf.append(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb);
        wadoStrBuf.append(dicomWebRS + "/" + studyInstanceUID
                + "/series/" + seriesInstanceUID + "/instances/" + sOPInstanceUID);
        URL wadoURL = new URL(wadoStrBuf.toString());
        datasetFile.setPath(wadoURL.toString());
        datasetFile.setPacs(true);
        datasetFile.setDatasetExpression(expression);
        List<DatasetFile> datasetFilesDb = expression.getDatasetFiles();
        if (datasetFilesDb == null) {
            datasetFilesDb = new ArrayList<DatasetFile>();
        }
        datasetFilesDb.add(datasetFile);
        expression.setDatasetFiles(datasetFilesDb);
    }

}
