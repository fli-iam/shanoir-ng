package org.shanoir.ng.bids.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.DatasetDescription;
import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.modality.*;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocolSCMetadata;
import org.shanoir.ng.datasetacquisition.model.mr.MrSequenceApplication;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is a service class for BIDS purpose.
 * Create BIDS files.
 * Export Data in BIDS format at different levels: Study, Subject, Examination, Dataset.
 * In a possible future: import Data from BIDS format
 * @author JcomeD
 *
 */
@Service
public class BIDSServiceImpl implements BIDSService {

    private static final String SESSIONS_TSV = "_sessions.tsv";

    private static final String TABULATION = "\t";

    private static final String NEW_LINE = "\n";

    private static final String SCANS_FILE_EXTENSION = "_scans.tsv";

    private static final String SESSION_PREFIX = "ses-";

    private static final String SUBJECT_PREFIX = "sub-";

    private static final String STUDY_PREFIX = "stud-";

    private static final Logger LOG = LoggerFactory.getLogger(BIDSServiceImpl.class);

    private static final String TASK = "_task_";

    private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

    private static final String README_FILE = "README";

    private static final String SUBJECT_IDENTIFIER = "subject_identifier";

    private static final String PARTICIPANT_ID = "participant_id";

    private static final String CSV_SEPARATOR = "\t";

    private static final String CSV_SPLITTER = "\n";

    private static final String[] CSV_PARTICIPANTS_HEADER = {
            PARTICIPANT_ID,
            SUBJECT_IDENTIFIER
    };

    private static final Map<String, String> natureMap;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put(MrDatasetNature.T1_WEIGHTED_MR_DATASET.name(), "T1w");
        aMap.put(MrDatasetNature.T2_WEIGHTED_MR_DATASET.name(), "T2w");
        aMap.put(MrDatasetNature.T2_STAR_WEIGHTED_MR_DATASET.name(), "T2starw");
        aMap.put(MrDatasetNature.PROTON_DENSITY_WEIGHTED_MR_DATASET.name(), "PDw");
        aMap.put(MrDatasetNature.H1_SPECTROSCOPIC_IMAGING_DATASET.name(), "UNIT1");
        aMap.put(MrDatasetNature.VELOCITY_ENCODED_ANGIO_MR_DATASET.name(), "angio");
        natureMap = Collections.unmodifiableMap(aMap);
    }

    @Value("${bids-data-folder}")
    private String bidsStorageDir;

    @Autowired
    private ExaminationService examService;

    @Autowired
    private StudyRepository studyRepo;

    @Autowired
    private SubjectStudyRepository subjectStudyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DatasetSecurityService datasetSecurityService;

    @Autowired
    private WADODownloaderService downloader;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @RabbitListener(queues = RabbitMQConfiguration.RELOAD_BIDS)
    public void deleteBidsForStudy(String studyId) {
        Study studyDeleted = studyRepo.findById(Long.valueOf(studyId)).orElse(null);
        this.deleteBidsFolder(studyDeleted.getId(), studyDeleted.getName());
    }

    @Override
    public void deleteBidsFolder(Long studyId, String studyName) {
        try {
            if (studyName == null) {
                Optional<Study> study = studyRepo.findById(studyId);
                if (!study.isEmpty())
                    studyName = study.get().getName();
            }
            // Try to delete the BIDS folder recursively if possible
            File bidsDir = new File(bidsStorageDir + File.separator + STUDY_PREFIX + studyId + studyName);
            if (bidsDir.exists()) {
                FileUtils.deleteDirectory(bidsDir);
            }
        } catch (Exception e) {
            LOG.error("ERROR when deleting BIDS folder: please delete it manually: {}, {}", studyId, studyName, e);
        }
    }

    @Override
    public File getBidsFolderpath(final Long studyId, String studyName) {
        studyName = this.formatLabel(studyName);
        String tmpFilePath = bidsStorageDir + File.separator + STUDY_PREFIX + studyId + studyName;
        return new File(tmpFilePath);
    }

    /**
     * Returns data from the study formatted as BIDS in a .zip file.
     * @param studyId the study ID we want to export as BIDS
     * @param studyName the study name
     * @return data from the study formatted as BIDS in a .zip file.
     * @throws IOException
     */
    @Override
    public File exportAsBids(final Long studyId, String studyName) throws IOException {
        // Get folder
        File workFolder = getBidsFolderpath(studyId, studyName);
        if (workFolder.exists()) {
            // If the file already exists, just return it
            return workFolder;
        }

        // Otherwise, create it from scratch
        File baseDir = createBaseBidsFolder(workFolder, studyName);

        // Iterate over subjects got from call to SubjectApiController.findSubjectsByStudyId() and get list of subjects
        List<Subject> subjs = getSubjectsForStudy(studyId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(subjs)) {
            return baseDir;
        }

        // Sort by ID
        subjs.sort(Comparator.comparing(Subject::getId));

        // Create participants.tsv
        participantsSerializer(baseDir, subjs);

        int index = 1;
        for (Subject subj : subjs) {
            exportAsBids(subj, studyName, studyId, baseDir, index);
            index++;
        }

        return baseDir;
    }

    /**
     * Get a list of subject from the study ID.
     * @param studyId the study ID to get the subject for
     * @return a list of users associated to the study
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private List<Subject> getSubjectsForStudy(final Long studyId) throws JsonParseException, JsonMappingException, IOException {
        // Get the list of subjects
        List<SubjectStudy> subjectStudies = subjectStudyRepository.findByStudy_Id(studyId);
        return subjectStudies.stream().map(SubjectStudy::getSubject).collect(Collectors.toList());
    }

    /**
     * Create the study/BASE BIDS folder.
     * @param studyName the study name
     * @return the base folder newly created
     */
    private File createBaseBidsFolder(File workFolder, String studyName) {
        workFolder.mkdirs();

        // 2. Create dataset_description.json and README
        DatasetDescription datasetDescription = new DatasetDescription();
        datasetDescription.setName(studyName);
        try {
            objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDescription);
            objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + README_FILE), studyName);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return workFolder;
    }

    /**
     * Create all the data in a BIDS folder for a given subject
     * @param subject the subject we want to export as BIDS
     * @param studyName the study name
     * @param workDir Subject BIDS directory where we are working. Will be created if null.
     * @param index subject index
     * @return data from the subject formatted as BIDS in a .zip file.
     * @throws IOException
     */
    private void exportAsBids(final Subject subject, final String studyName, Long studyId, final File workDir, int index) throws IOException {
        File subjDir = createSubjectFolder(subject.getName(), index, workDir);

        // Get subject examinations and filter on the one with adapted study only
        List<Examination> examinationList = examService.findBySubjectIdStudyId(subject.getId(), studyId);

        // Create session folder only if there is multiple exmainations
        boolean useSessionFolder = (examinationList != null && examinationList.size() > 1) ;
        File sessionFile = null;
        try {

            sessionFile = new File(subjDir.getAbsolutePath() + "/" + subjDir.getName() + SESSIONS_TSV);
            if (useSessionFolder) {
                // Generate  sub-<label>_sessions.tsv file
                sessionFile.getParentFile().mkdirs();
                sessionFile.createNewFile();
                StringBuilder buffer = new StringBuilder();
                buffer.append("session_id").append(TABULATION)
                .append("acq_time").append(TABULATION)
                .append(NEW_LINE);
                
                for (Examination examination : examinationList) {
                    String sessionLabel = this.getSessionLabel(examination);
            
                    buffer.append(sessionLabel).append(TABULATION)
                    .append(examination.getExaminationDate()).append(TABULATION)
                    .append(NEW_LINE);
                }
                
                Files.write(Paths.get(sessionFile.getAbsolutePath()), buffer.toString().getBytes());
            }
            // Iterate over examinations to export them as BIDS
            File examDir = subjDir;
            for (Examination exam : examinationList) {
                if (useSessionFolder) {
                    examDir = createExaminationFolder(exam, subjDir);
                }
                exportAsBids(exam, examDir, studyName, subject.getName());
            }
        } catch (Exception e) {
            LOG.error("Fail to process sessionFile [{}] for study [{}]", sessionFile, studyName, e);
        }
    }

    /**
     * Create the subject/patient BIDS folder
     * @param subjectName the subject name for which we want to create the folder
     * @param baseDir the parent folder
     * @param index the subject id
     * @return the newly created folder
     * @throws IOException
     */
    private File createSubjectFolder(String subjectName, final int index, final File baseDir) throws IOException {
        // Generate another ID here ?
        subjectName = this.formatLabel(subjectName);

        File subjectFolder = new File(baseDir.getAbsolutePath() + File.separator + SUBJECT_PREFIX + index + subjectName);
        if (!subjectFolder.exists()) {
            subjectFolder.mkdirs();
        }
        return subjectFolder;
    }

    /**
     * Returns data from the examination formatted as BIDS in a .zip file.
     * @param examination the examination we want to export as BIDS
     * @param studyName the study name
     * @param subjectName the subject name
     * @return data from the examination formatted as BIDS in a .zip file.
     * @throws IOException
     */
    private void exportAsBids(final Examination examination, final File examDir, final String studyName, final String subjectName) throws IOException {
        // Add examination extra-data
        for (String filePath : examination.getExtraDataFilePathList()) {
            File file = new File(this.examService.getExtraDataFilePath(examination.getId(), filePath));
            if (file.exists()) {
                Path bidsExtraFilePath = Path.of(examDir.getAbsolutePath() + "/" + file.getName());
                Files.createLink(bidsExtraFilePath, file.toPath());
            }
        }

        // Iterate over acquisitions/datasets        
        for (DatasetAcquisition acq : examination.getDatasetAcquisitions()) {
            List<Dataset> datasets = acq.getDatasets();
            if (CollectionUtils.isEmpty(datasets)) {
                continue;
            }
            for (Dataset ds : datasets) {
                try {
                    getScansFile(examDir, subjectName);
                    createDatasetBidsFiles(ds, examDir, studyName, subjectName);
                } catch (IOException e) {
                    LOG.error("Error while creating BIDS dataset file for dataset [{}]", ds.getName(), e);
                }
            }
        }
    }

    /**
     * Create the session/examination BIDS folder
     * @param examination the examination for which we want to create the folder
     * @param subjectDir the parent folder
     * @return the newly created folder
     * @throws IOException
     */
    private File createExaminationFolder(final Examination examination, final File subjectDir) throws IOException {
        String sessionLabel = this.getSessionLabel(examination);

        // Create exam/session folder
        File examFolder = new File(subjectDir.getAbsolutePath() + File.separator + SESSION_PREFIX +  sessionLabel);
        if (!examFolder.exists()) {
            examFolder.mkdirs();
        }
        return examFolder;
    }

    private String getSessionLabel(Examination examination) {
        String label = "" + examination.getId();
        if (!StringUtils.isBlank(examination.getComment())) {
            label += examination.getComment();
        }

        return formatLabel(label);
    }

    /**
     * Create the list of BIDS files associated to a dataset.
     * @param dataset the dataset from which we want the specific BIDS files to be created
     * @param workDir the working directory where files will be created
     * @param studyName the study name
     * @param subjectName the subject name
     * @return A list of newly created specific BIDS files associated to the dataset in entry
     * @throws IOException when we fail to create a file
     */
    private void createDatasetBidsFiles(final Dataset dataset, final File workDir, final String studyName, final String subjectName) throws IOException {
        File dataFolder = null;

        if (dataset.getDatasetProcessing() != null) {
            LOG.warn("Submitted dataset is a processed dataset.");
            return;
        }
        String subjectNameUpdated = this.formatLabel(subjectName);
        String datasetFilePrefix = workDir.getName().contains(SESSION_PREFIX) ? workDir.getParentFile().getName() + "_" + workDir.getName() : workDir.getName();
        
        dataFolder = createSpecificDataFolder(dataset, workDir, dataFolder, subjectNameUpdated, studyName);

        // Copy dataset files in the directory AS hard link to avoid duplicating files
        List<URL> pathURLs = new ArrayList<>();

        if (!"Eeg".equals(dataset.getType()) && !"BIDS".equals(dataset.getType()) && onlyHasDicom(dataset)) {
            // DCM2NIIX
            Long converterId = 6L;
            if ("CT".equals(dataset.getType())) {
                converterId = 8L;
            }
            File userDir = DatasetFileUtils.getUserImportDir("/tmp");
            String tmpFilePath = userDir + File.separator + dataset.getId() + "_DCM";
            File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()));
            try {
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, null);

                // Create temporary workfolder with dicom files, to be able to convert them
                workFolder.mkdirs();

                downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset, null);

                // Convert them, sending to import microservice
                boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + workFolder.getAbsolutePath() + ";" + dataFolder.getAbsolutePath());

                if (!result) {
                    throw new ShanoirException("Could not convert from dicom to nifti.");
                }

                File[] newFiles = dataFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().startsWith(dataset.getId() + "_");
                    }
                });

                if (newFiles != null && newFiles.length != 0) {
                    // Add the file to the scans.tsv reference
                    File scansTsvFile = getScansFile(workDir, subjectName);
                    for (File fileResult : newFiles) {
                        String buffer = fileResult.getParentFile().getName() + File.separator + fileResult.getName() + TABULATION +
                                format.format(dataset.getDatasetAcquisition().getExamination().getExaminationDate().atStartOfDay()) + TABULATION +
                                dataset.getDatasetAcquisition().getExamination().getId() +
                                NEW_LINE;

                        Files.write(Paths.get(scansTsvFile.getAbsolutePath()), buffer.getBytes(), StandardOpenOption.APPEND);
                    }
                }

            } catch (Exception e) {
                LOG.error("Could not convert from dicom to nifti", e);
                File errorfile = new File(dataFolder.getAbsolutePath() + "/error.txt");
                Files.createFile(errorfile.toPath());
                Files.write(errorfile.toPath(), "Could not convert data from dicom to nifti for this dataset.".getBytes());
            } finally {
                FileUtils.deleteQuietly(workFolder);
            }
            return;
        }

        getDatasetFilePathURLs(dataset, pathURLs, null);

        for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
            URL url =  iterator.next();
            File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));
            String fileName = datasetFilePrefix + "_";
            
            String nature = null;

            if (dataset instanceof MrDataset) {
                MrDataset mrDataset = (MrDataset) dataset;
                if (mrDataset.getUpdatedMrMetadata() != null && mrDataset.getUpdatedMrMetadata().getMrDatasetNature() != null) {
                    nature = mrDataset.getUpdatedMrMetadata().getMrDatasetNature().name();
                    fileName += natureMap.get(nature) + "_";
                } else if (mrDataset.getOriginMrMetadata() != null && mrDataset.getOriginMrMetadata().getMrDatasetNature() != null) {
                    nature = mrDataset.getOriginMrMetadata().getMrDatasetNature().name();
                    fileName += natureMap.get(nature) + "_";
                }
            }

            String fileSuffix = srcFile.getName();
            if (srcFile.getName().startsWith(subjectName)) {
                fileSuffix = srcFile.getName().substring(fileSuffix.indexOf(subjectName) + subjectName.length());
            }
            fileName += fileSuffix;

            try {
            Path pathToGo = Paths.get(dataFolder.getAbsolutePath() + File.separator + fileName);
                // Use link to avoid file duplication
                deleteIfExists(pathToGo.toAbsolutePath().toString());
                Files.createLink(pathToGo, srcFile.toPath());

                // Add the file to the scans.tsv reference
                File scansTsvFile = getScansFile(workDir, subjectName);
                StringBuilder buffer = new StringBuilder();
                buffer.append(pathToGo.getParent().getFileName() + File.separator + pathToGo.getFileName()).append(TABULATION)
                .append(format.format(dataset.getDatasetAcquisition().getExamination().getExaminationDate().atStartOfDay())).append(TABULATION)
                .append(dataset.getDatasetAcquisition().getExamination().getId())
                .append(NEW_LINE);

                Files.write(Paths.get(scansTsvFile.getAbsolutePath()), buffer.toString().getBytes(), StandardOpenOption.APPEND);

            } catch (IOException exception) {
                LOG.error("File could not be created: {}", srcFile.getAbsolutePath(), exception);
            }
        }
    }

    private boolean onlyHasDicom(Dataset dataset) {
        for (DatasetExpression expression : dataset.getDatasetExpressions()) {
            if (DatasetExpressionFormat.NIFTI_SINGLE_FILE.equals(expression.getDatasetExpressionFormat())) {
                return false;
            }
        }
        return true;
    }

    private File createSpecificDataFolder(Dataset dataset, File workDir, File dataFolder, String subjectName, String studyName) throws IOException {



        // Create specific files (EEG, MS, MEG, etc..)
        if (dataset instanceof EegDataset) {
            dataFolder = createDataFolder("eeg", workDir);
            String examComment = dataset.getDatasetAcquisition().getExamination().getComment();
            String sessionLabel = examComment != null ? examComment : dataset.getDatasetAcquisition().getExamination().getId().toString();
            exportSpecificEegFiles((EegDataset) dataset, subjectName, sessionLabel, studyName, dataset.getName(), dataFolder);
        } else if (dataset instanceof PetDataset) {
            dataFolder = createDataFolder("pet", workDir);
        } else if (dataset instanceof MrDataset) {
            MrDataset mrDataset = (MrDataset) dataset;
            // Here we want to know whether we have anat/func/dwi/fmap
            // We base ourselves on SeriesDescription here
            MrProtocol protocol = ((MrDatasetAcquisition) dataset.getDatasetAcquisition()).getMrProtocol();
            if (protocol != null) {
                if (protocol.getUpdatedMetadata() != null
                        && protocol.getUpdatedMetadata().getBidsDataType() != null) {
                    dataFolder = createDataFolder(protocol.getUpdatedMetadata().getBidsDataType(), workDir);
                } else {
                    MrProtocolSCMetadata metadata = protocol.getUpdatedMetadata();
                    if (metadata != null) {
                        MrSequenceApplication application = metadata.getMrSequenceApplication();
                        if (application != null) {
                            // CALIBRATION(1), --> fieldmap
                            if (application.equals(MrSequenceApplication.CALIBRATION)) {
                                dataFolder = createDataFolder("fmap", workDir);
                            }
                            //MORPHOMETRY(2), ==> anat
                            else if (application.equals(MrSequenceApplication.MORPHOMETRY)) {
                                dataFolder = createDataFolder("anat", workDir);
                            }
                            // DIFFUSION(8), , ==> diffusion
                            else if (application.equals(MrSequenceApplication.DIFFUSION)) {
                                dataFolder = createDataFolder("dwi", workDir);
                            }
                            // BOLD(9), , ==> functional
                            else if (application.equals(MrSequenceApplication.BOLD)) {
                                dataFolder = createDataFolder("func", workDir);
                            }
                        }
                    }
                }
            }
            // default case, dataFolder is still null => undefined folder
            if (dataFolder == null) {
                dataFolder = createDataFolder("undefined", workDir);
            }
        } else if (dataset instanceof BidsDataset) {
            BidsDataset bidsdataset = (BidsDataset) dataset;
            dataFolder = createDataFolder(bidsdataset.getBidsDataType(), workDir);
        } else {
            dataFolder = createDataFolder("undefined", workDir);
        }
        return dataFolder;
    }

    private void deleteIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private File getScansFile(File parentFile, String subjectName) throws IOException {
        String fileName = parentFile.getName() + SCANS_FILE_EXTENSION;
        subjectName = this.formatLabel(subjectName);
        if (!parentFile.getName().contains(subjectName)) {
            fileName = SUBJECT_PREFIX + subjectName + "_" + parentFile.getName() + SCANS_FILE_EXTENSION;
        }
        File scansFile = new File(parentFile.getAbsolutePath() + File.separator + fileName);
        if (!scansFile.exists()) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("filename").append(TABULATION)
            .append("acq_time").append(TABULATION)
            .append("session_id")
            .append(NEW_LINE);
            Files.write(Paths.get(scansFile.getAbsolutePath()), buffer.toString().getBytes());
        }
        return scansFile;
    }

    /**
     * Create /eeg, /func ,[...] folder in BIDS file if not existing.
     * @return the folder newly created.
     */
    private File createDataFolder(final String folderName, final File workDir) {
        File dataFolder = new File(workDir.getAbsolutePath() + File.separator + folderName);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    /**
     * Reads all dataset files depending on the format attached to one dataset.
     * @param dataset
     * @param pathURLs
     * @throws MalformedURLException
     */
    private void getDatasetFilePathURLs(final Dataset dataset, final List<URL> pathURLs, final DatasetExpressionFormat format) throws MalformedURLException {
        List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
        for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
            DatasetExpression datasetExpression = itExpressions.next();
            if (format == null || datasetExpression.getDatasetExpressionFormat().equals(format)) {
                List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
                for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
                    DatasetFile datasetFile = itFiles.next();
                    if (!datasetFile.isPacs()) {
                        URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
                        pathURLs.add(url);
                    }
                }
            }
        }
    }

    /**
     * This methods export specific EEG files for BIDS export.
     * - channel.tsv -> A list of channels from dataset.channels
     * - event.tsv -> A list of events from dataset.events
     * - [..]_eeg.json -> Description of EEG methods used
     * - ? electrodes.tsv -> list of electrodes positions if existing
     * - ? coordsystem.json -> if electrodes are defined, sets the reference
     * See https://bids-specification.readthedocs.io/en/latest/04-modality-specific-files/03-electroencephalography.html
     * for more informations
     * @param dataset the dataset we want to export in BIDS
     * @param studyName the name of associated study
     * @param subjectName the subject name associated
     * @param sessionId the session ID / examination ID associated
     * @param runId The run ID
     * @param dataFolder
     * @throws RestServiceException
     * @throws IOException
     */
    private void exportSpecificEegFiles(final EegDataset dataset, final String subjectName, final String sessionId, final String studyName, final String runId, File dataFolder) throws IOException {
        // Create _eeg.json
        
        String datasetFilePrefix = dataFolder.getParentFile().getName().contains(SESSION_PREFIX) ? dataFolder.getParentFile().getParentFile().getName() + "_" + dataFolder.getParentFile().getName() : dataFolder.getParentFile().getName();
        
        String fileName = TASK + studyName + "_eeg.json";
        String destFile = dataFolder.getAbsolutePath() + File.separator + datasetFilePrefix + "_" + fileName;

        EegDataSetDescription datasetDescription = new EegDataSetDescription();
        datasetDescription.setTaskName(studyName);
        datasetDescription.setSamplingFrequency(String.valueOf(dataset.getSamplingFrequency()));
        objectMapper.writeValue(new File(destFile), datasetDescription);

        // Create the folder where we are currently working if necessary.
        File destWorkFolderFile = dataFolder;
        if (!destWorkFolderFile.exists()) {
            destWorkFolderFile.mkdirs();
        }

        // Create channels.tsv file
        fileName = datasetFilePrefix + TASK + studyName + "_" + runId + "_channel.tsv";
        destFile = destWorkFolderFile.getAbsolutePath() + File.separator + fileName;

        StringBuilder buffer = new StringBuilder();
        buffer.append("name \t type \t units \t sampling_frequency \t low_cutoff \t high_cutoff \t notch \n");

        for (Channel chan: dataset.getChannels()) {
            buffer.append(chan.getName()).append(TABULATION)
            .append(chan.getReferenceType().name()).append(TABULATION)
            .append(chan.getReferenceUnits()).append(TABULATION)
            .append(dataset.getSamplingFrequency()).append(TABULATION)
            .append(chan.getLowCutoff() == 0 ? "n/a" : chan.getLowCutoff()).append(TABULATION)
            .append(chan.getHighCutoff() == 0 ? "n/a" : chan.getHighCutoff()).append(TABULATION)
            .append(chan.getNotch() == 0 ? "n/a" : chan.getNotch()).append(NEW_LINE);
        }
        Files.write(Paths.get(destFile), buffer.toString().getBytes());

        // add to scans.tsv
        addToTsvFile(dataFolder.getParentFile(), fileName, dataset, subjectName);

        // Create events.tsv file
        fileName = datasetFilePrefix + TASK + studyName + "_" + runId + "_event.tsv";
        destFile = destWorkFolderFile.getAbsolutePath() + File.separator + fileName;

        buffer = new StringBuilder();
        buffer.append("onset \t duration \t sample \n");

        for (Event event: dataset.getEvents()) {
            float sample = Float.parseFloat(event.getPosition());
            float samplingFrequency = dataset.getSamplingFrequency();
            float onset = sample / samplingFrequency;
            int duration = event.getPoints();
            buffer.append(onset).append(TABULATION)
            .append(duration == 0 ? "n/a" : String.valueOf(duration)).append(TABULATION)
            .append(sample).append(NEW_LINE);
        }
        Files.write(Paths.get(destFile), buffer.toString().getBytes());

        // add to scans.tsv
        addToTsvFile(dataFolder.getParentFile(), fileName, dataset, subjectName);

        // If no coordinates system, don't create electrode.csv & _coordsystem.json files
        if (dataset.getCoordinatesSystem() == null) {
            return;
        }

        // Create electrode.csv file
        fileName = datasetFilePrefix + TASK + studyName + "_" + runId + "_electrodes.tsv";
        destFile = destWorkFolderFile.getAbsolutePath() + File.separator + fileName;

        buffer = new StringBuilder();
        buffer.append("name \t x \t y \t z \n");

        for (Channel chan: dataset.getChannels()) {
            buffer.append(chan.getName()).append(TABULATION)
            .append(chan.getX()).append(TABULATION)
            .append(chan.getY()).append(TABULATION)
            .append(chan.getZ()).append(NEW_LINE);
        }
        Files.write(Paths.get(destFile), buffer.toString().getBytes());

        // add to scans.tsv
        addToTsvFile(dataFolder.getParentFile(), fileName, dataset, subjectName);

        // Create _coordsystem.json file
        fileName = datasetFilePrefix + TASK + studyName + "_" + runId + "_coordsystem.json";
        destFile = destWorkFolderFile.getAbsolutePath() + File.separator + fileName;

        buffer = new StringBuilder();
        buffer.append("{\n")
        .append("\"EEGCoordinateSystem\": ").append("\"" + dataset.getCoordinatesSystem()).append("\",\n")
        .append("\"EEGCoordinateUnits\": ").append("\"" + DatasetApiController.CoordinatesSystem.valueOf(dataset.getCoordinatesSystem()).getUnit()).append("\"\n")
        .append("}");

        Files.write(Paths.get(destFile), buffer.toString().getBytes());

        // add to scans.tsv
        addToTsvFile(dataFolder.getParentFile(), fileName, dataset, subjectName);
    }

    private void addToTsvFile(File parentFolder, String fileName, Dataset dataset, String subjectName) throws IOException {
        File scansTsvFile = getScansFile(parentFolder, subjectName);

        StringBuilder buffer = new StringBuilder();
        buffer.append(fileName).append(TABULATION)
        .append(dataset.getDatasetAcquisition().getExamination().getExaminationDate()).append(TABULATION)
        .append(dataset.getDatasetAcquisition().getExamination().getId())
        .append(NEW_LINE);

        Files.write(Paths.get(scansTsvFile.getAbsolutePath()), buffer.toString().getBytes(), StandardOpenOption.APPEND);
    }

    /**
     * Creates the participants.tsv and participants.json file from the study
     */
    private void participantsSerializer(File parentFolder, List<Subject> subjs) {
        File csvFile = new File(parentFolder.getAbsolutePath() + File.separator + "participants.tsv");
        int index = 1;

        if (csvFile.exists()) {
            // Recreate it everytime
            FileUtils.deleteQuietly(csvFile);
        }
        StringBuilder buffer =  new StringBuilder();
        // Headers
        for (String columnHeader : CSV_PARTICIPANTS_HEADER) {
            buffer.append(columnHeader).append(CSV_SEPARATOR);
        }
        buffer.append(CSV_SPLITTER);

        for (Subject subject : subjs) {
            String subjectName = subject.getName();
            subjectName = this.formatLabel(subjectName);
            // Write in the file the values
            buffer.append(SUBJECT_PREFIX).append(index++).append("_").append(subjectName).append(CSV_SEPARATOR)
            .append(subject.getId()).append(CSV_SEPARATOR)
            .append(CSV_SPLITTER);
        }

        try {
            Files.write(Paths.get(csvFile.getAbsolutePath()), buffer.toString().getBytes());
        } catch (IOException e) {
            LOG.error("Error while creating particpants.tsv file: {}", e);
        }
    }

    private String formatLabel(String label) {
        return label.replaceAll("[^a-zA-Z0-9]+", "");
    }

}
