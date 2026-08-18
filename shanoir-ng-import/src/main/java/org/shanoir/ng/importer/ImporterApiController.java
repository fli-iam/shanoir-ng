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

package org.shanoir.ng.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.eeg.brainvision.BrainVisionReader;
import org.shanoir.ng.importer.eeg.edf.EDFAnnotation;
import org.shanoir.ng.importer.eeg.edf.EDFParser;
import org.shanoir.ng.importer.eeg.edf.EDFParserResult;
import org.shanoir.ng.importer.model.Channel;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.importer.model.Event;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.ImportJobStatus;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

/**
 * This is the main component of the import of Shanoir-NG. The front-end in
 * Angular only communicates with this service. The import ms itself is calling
 * the ms datasets service.
 *
 * The Import MS returns only a random ID to the outside world for one import.
 * Internally each user has its own folder in the importDirectory. So, when the
 * workFolder in the ImportJob is set to be returned, there is only the random
 * ID. When the requests arrive MS Import is adding the userId and the real path
 * value.
 *
 * @author mkain
 *
 */
@Controller
public class ImporterApiController implements ImporterApi {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterApiController.class);

    private static final String WRONG_CONTENT_FILE_UPLOAD = "Wrong content type of file upload, .zip required.";

    private static final String ERROR_WHILE_SAVING_UPLOADED_FILE = "Error while saving uploaded file.";

    private static final String NO_FILE_UPLOADED = "No file uploaded.";

    private static final String DICOMDIR = "DICOMDIR";

    private static final String APPLICATION_ZIP = "application/zip";

    private static final UIDGeneration UID_GENERATOR = new UIDGeneration();

    private static final Pattern PREFILTER_PATTERN =
            Pattern.compile("HP:(\\d+)k?Hz\\sLP:(\\d+)k?Hz(\\sN:(\\d+)k?Hz)?");

    @Value("${shanoir.import.directory}")
    private String importDir;

    @Autowired
    private DicomDirGeneratorService dicomDirGeneratorService;

    @Autowired
    private DicomDirToModelService dicomDirToModel;

    @Autowired
    private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

    @Autowired
    private ImporterManagerService importerManagerService;

    @Autowired
    private QueryPACSService queryPACSService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private ImportJobStatusService importJobStatusService;

    @Override
    public ResponseEntity<ImportJob> uploadDicomZipFile(
            @Parameter(name = "file detail") @RequestPart("file") final MultipartFile dicomZipFile)
                    throws RestServiceException {
        if (dicomZipFile == null || !ImportUtils.isZipFile(dicomZipFile)) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
        }
        LOG.info("============== NEW ZIP UPLOAD ===========================");
        File importJobFile = null;
        try {
            ImportJob importJob = new ImportJob();
            importJob.setFromDicomZip(true);
            importJobFile = ImportUtils.initImportJob(importJob, importDir, dicomZipFile);
            boolean createDicomDir = !ImportUtils.checkZipContainsFile(DICOMDIR, importJobFile);
            File importJobDir = new File(importJob.getWorkFolder());
            ImportUtils.unzip(importJobFile.getAbsolutePath(), importJobDir.getAbsolutePath());
            importJobFile.delete();
            setPatientsFromDicomDirAndCreateImages(importJob, importJobDir, createDicomDir);
            return new ResponseEntity<>(importJob, HttpStatus.OK);
        } catch (Exception e) {
            if (importJobFile != null) {
                FileUtils.deleteQuietly(importJobFile);
            }
            LOG.error(e.getMessage(), e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
        }
    }

    /**
     * Here we still use ImportJob, as any DICOM ZIP can contain multiple patients
     * and we want the user to select one of them, so we remain on the old structure.
     *
     * @param importJob
     * @param importJobDir
     * @param createDicomDir
     * @throws Exception
     */
    private void setPatientsFromDicomDirAndCreateImages(ImportJob importJob, File importJobDir, boolean createDicomDir) throws Exception {
        if (createDicomDir) {
            LOG.info("DICOMDIR missing, generating one.");
            final File dicomDir = new File(importJobDir, DICOMDIR);
            if (!dicomDir.exists()) {
                dicomDirGeneratorService.generateDicomDirFromDirectory(dicomDir, importJobDir);
                LOG.info("DICOMDIR generated at path: {}", dicomDir.getAbsolutePath());
            }
        }
        List<Patient> patients = preparePatientsForImportJob(importJobDir);
        if (patients.size() > 1) {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "ZIP with multiple DICOM patients not supported.", null));
        }
        Patient patient = patients.getFirst();
        if (patient.getStudies().size() > 1) {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "ZIP with multiple DICOM studies not supported.", null));
        }
        importJob.setPatients(patients);
        // Required here as imagesCreator is already based on new structure:
        handleLegacyPatientSubjectStudySeries(importJob);
        /**
         * STEP: split instances into non-images and images and get additional meta-data
         * from first DICOM file of each serie, meta-data missing in DICOMDIR.
         * As the user has made no serie(s) selection yet, we create Images for
         * all series.
         */
        imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(
                importJob, importJobDir.getAbsolutePath(), null, false);
    }

    /**
     * Read DICOMDIR and create Shanoir model from it (== Dicom model):
     * Patient - Study - Serie - Instance 2. STEP: split instances into non-images
     * and images and get additional meta-data from first DICOM file of each serie,
     * meta-data missing in dicomdir.
     *
     * @param dirWithDicomDir
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private List<Patient> preparePatientsForImportJob(File dirWithDicomDir) throws IOException, FileNotFoundException {
        List<Patient> patients = null;
        File dicomDirFile = new File(dirWithDicomDir.getAbsolutePath() + File.separator + DICOMDIR);
        if (dicomDirFile.exists()) {
            patients = dicomDirToModel.readDicomDirToPatients(dicomDirFile);
        }
        return patients;
    }

    @Override
    public ResponseEntity<Void> startImportJob(
            @Parameter(name = "ImportJob", required = true) @Valid @RequestBody final ImportJob importJob)
                    throws RestServiceException {
        File userImportDir = ImportUtils.getUserImportDir(importDir);
        final Long userId = KeycloakUtil.getTokenUserId();
        importJob.setUserId(userId);
        String id = ImportJobStatusService.keyOf(importJob.getWorkFolder());
        importJob.setId(id);
        final File importJobDir = new File(userImportDir, id);
        if (importJobDir.exists()) {
            importJob.setWorkFolder(importJobDir.getAbsolutePath());
            importJobStatusService.setInProgress(importJob, "Import job received, queued for processing.");
            LOG.info("============== NEW IMPORT ===========================");
            LOG.info("Import job (old) for user {} ({})", KeycloakUtil.getTokenUserName(), userId);
            LOG.info("Import type: {}", importJob.getImportType());
            LOG.info("WorkFolder: {}", importJob.getWorkFolder());
            handleLegacyPatientSubjectStudySeries(importJob);
            importerManagerService.manageImportJob(importJob);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOG.error("Missing importJobDir.");
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Missing importJobDir.", null));
        }
    }

    public void handleLegacyPatientSubjectStudySeries(final ImportJob importJob) {
        // Get subject, study and series from legacy location and put in new locations
        if (!importJob.getPatients().isEmpty()) {
            Patient patient = importJob.getPatients().getFirst();
            importJob.setPatient(patient);
            importJob.setSubject(patient.getSubject());
            if (!patient.getStudies().isEmpty()) {
                Study study = patient.getStudies().getFirst();
                importJob.setStudy(study);
                // As this is called from uploadDicomZipFile,
                // we have to take all series (not yet selected)
                importJob.setSeries(study.getSeries());
            }
        }
    }

    @Override
    public ResponseEntity<Void> startImportJobBase(
            @Parameter(name = "ImportJob", required = true) @Valid @RequestBody final ImportJobBase importJob)
            throws RestServiceException {
        final Long userId = KeycloakUtil.getTokenUserId();
        importJob.setUserId(userId);
        File userImportDir = ImportUtils.getUserImportDir(importDir);
        String id = importJob.getId();
        final File importJobDir = new File(userImportDir, id);
        if (importJobDir.exists()) {
            importJob.setWorkFolder(importJobDir.getAbsolutePath());
            importJobStatusService.setInProgress(importJob, "Import job received, queued for processing.");
            LOG.info("============== NEW IMPORT ===========================");
            LOG.info("Import job (base) for user {} ({})", KeycloakUtil.getTokenUserName(), userId);
            LOG.info("Import type: {}", importJob.getImportType());
            LOG.info("Import ID: {}", importJob.getId());
            LOG.info("WorkFolder: {}", importJob.getWorkFolder());
            importerManagerService.manageImportJob(importJob);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOG.error("Missing importJobDir.");
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Missing importJobDir.", null));
        }
    }

    @Override
    public ResponseEntity<ImportJobStatus> getImportJobStatus(@PathVariable("id") String id) {
        ImportJobStatus status = importJobStatusService.getStatus(id);
        if (status == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ImportJob> queryPACS(
            @Parameter(name = "DicomQuery", required = true) @Valid @RequestBody final DicomQuery dicomQuery)
                    throws RestServiceException, IOException {
        ImportJob importJob = new ImportJob();
        importJob.setFromPacs(true);
        ImportUtils.initImportJob(importJob, importDir);
        try {
            importJob = queryPACSService.queryCFIND(dicomQuery);
        } catch (Exception e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), null));
        }
        if (importJob.getPatients() == null || importJob.getPatients().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(importJob, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ImportJob> importDicomZipFile(
            @Parameter(name = "file detail") @RequestBody final String dicomZipFilename) throws RestServiceException {
        // We use this when coming from BRUKER upload
        if (dicomZipFilename == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
        }
        File tempFile = new File(dicomZipFilename);
        MockMultipartFile multiPartFile;
        try {
            multiPartFile = new MockMultipartFile(tempFile.getName(), tempFile.getName(), APPLICATION_ZIP, new FileInputStream(tempFile.getAbsolutePath()));
            // Import dicomfile
            return uploadDicomZipFile(multiPartFile);
        } catch (IOException e) {
            LOG.error("ERROR while loading zip file, please contact an administrator");
            LOG.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } finally {
            // Delete temp file which is useless now
            tempFile.delete();
        }
    }

    /**
     * This method load an EEG file, unzip it and load an import job with the
     * informations collected
     */
    @Override
    public ResponseEntity<EegImportJob> uploadEEGZipFile(
            @Parameter(name = "file detail") @RequestPart("file") final MultipartFile eegFile)
                    throws RestServiceException {
        try {
            // Do some checks about the file, must be != null and must be a .zip file
            if (eegFile == null) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
            }
            if (!ImportUtils.isZipFile(eegFile)) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
            }
            EegImportJob importJob = new EegImportJob();
            File importJobFile = ImportUtils.initImportJob(importJob, importDir, eegFile);
            File importJobDir = new File(importJob.getWorkFolder());
            ImportUtils.unzip(importJobFile.getAbsolutePath(), importJobDir.getAbsolutePath());
            importJobFile.delete();
            importJob.setArchive(eegFile.getOriginalFilename());
            return new ResponseEntity<>(importJob, HttpStatus.OK);
        } catch (IOException ioe) {
            throw new RestServiceException(ioe, new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Invalid file"));
        }
    }

    @Override
    public ResponseEntity<EegImportJob> analyzeEegZipFile(@Parameter(name = "EegImportJob", required = true) @RequestBody EegImportJob importJob) throws RestServiceException {
        try {
            List<EegDataset> datasets = new ArrayList<>();

            File dataFileDir = new File(importJob.getWorkFolder() + File.separator
                    + importJob.getArchive().replace(".zip", ""));

            LOG.info(dataFileDir.getAbsolutePath());

            // Get .VHDR file
            File[] bvMatchingFiles = dataFileDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.endsWith("vhdr");
                }
            });

            // Get .edf file
            File[] edfMatchingFiles = dataFileDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.endsWith("edf");
                }
            });

            if (bvMatchingFiles != null && bvMatchingFiles.length > 0) {
                // Manage multiple vhdr files
                // read .vhdr files
                readBrainvisionFiles(bvMatchingFiles, dataFileDir, datasets);
            } else if (edfMatchingFiles != null && edfMatchingFiles.length > 0) {
                // read .edf files
                readEdfFiles(edfMatchingFiles, dataFileDir, datasets);
            } else {
                throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "File does not contains a .vhdr or .edf file."));
            }

            importJob.setDatasets(datasets);
            importJob.setUsername(KeycloakUtil.getTokenUserName());

            return new ResponseEntity<>(importJob, HttpStatus.OK);
        } catch (Exception e) {
            throw new RestServiceException(e, new ErrorModel(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    private File convertAnalyzeToNifti(File imageFile, File headerFile) throws ShanoirException {
        String imageName = imageFile.getAbsolutePath();
        String newImageName = imageName.replace(".img", ".nii.gz");
        File parentFolder = imageFile.getParentFile().getAbsoluteFile();

        // Send to nifti conversion micro service
        boolean result = (boolean) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.ANIMA_CONVERSION_QUEUE, imageName);

        if (!result) {
            throw new ShanoirException("Could not convert from anima to nifti, please contact an administrator.");
        }

        return new File(parentFolder, newImageName);
    }

    @Override
    /**
     * This method imports dataset file, and converts them to nifti if necessary (in case of a Analyze file format from .hdr/.img files)
     */
    public ResponseEntity<String> uploadProcessedDataset(
            @Parameter(name = "image detail") @RequestPart("image") MultipartFile imageFile,
            @Parameter(name = "header detail", required = false) @RequestPart(value = "header", required = false) MultipartFile headerFile)
                    throws RestServiceException {

        String imageFileName = imageFile == null ? "" : imageFile.getOriginalFilename();
        String headerFileName = headerFile == null ? "" : headerFile.getOriginalFilename();
        Boolean isNifti = imageFileName.endsWith(".nii") || imageFileName.endsWith(".nii.gz");
        Boolean isAnalyze = imageFileName.endsWith(".img") && headerFileName.endsWith(".hdr");

        if (!isNifti && !isAnalyze) {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Wrong content type of file upload.", null));
        }

        try {
            // Save files to import job directory
            ImportJob importJob = new ImportJob();
            File importJobDir = ImportUtils.initImportJob(importJob, importDir);
            File destinationImageFile = new File(importJobDir.getAbsolutePath(), imageFileName);
            imageFile.transferTo(destinationImageFile);
            if (headerFile != null) {
                File destinationHeaderFile = new File(importJobDir.getAbsolutePath(), headerFileName);
                headerFile.transferTo(destinationHeaderFile);
                // Convert Analyze format to nifti format
                destinationImageFile = convertAnalyzeToNifti(destinationImageFile, destinationHeaderFile);
            }
            return new ResponseEntity<String>(destinationImageFile.getAbsolutePath(), HttpStatus.OK);
        } catch (IOException | ShanoirException e) {
            LOG.error(e.getMessage(), e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
        }
    }

    /**
     * Reads a list of .edf files to generate a bunch of datasets.
     *
     * @param datasets         the list of datasets to import
     * @param dataFileDir      the file directory where we are working
     * @param edfMatchingFiles the list of .edf files
     * @throws ShanoirImportException when parsing fails
     */
    private void readEdfFiles(final File[] edfMatchingFiles, final File dataFileDir, final List<EegDataset> datasets)
            throws ShanoirImportException {
        // List the directory once instead of once per .edf file
        final File[] allFilesInDir = dataFileDir.listFiles();

        for (File edfFile : edfMatchingFiles) {
            // Parse the file
            try (FileInputStream edfStream = new FileInputStream(edfFile)) {
                EDFParserResult result = EDFParser.parseEDF(edfStream);
                // Create channels
                List<Channel> channels = new ArrayList<>();
                for (int i = 0; i < result.getHeader().getNumberOfChannels(); i++) {
                    Channel chan = new Channel();
                    Matcher m = PREFILTER_PATTERN.matcher(result.getHeader().getPrefilterings()[i].trim());
                    if (m.matches()) {
                        chan.setHighCutoff(Integer.parseInt(m.group(1)));
                        chan.setLowCutoff(Integer.parseInt(m.group(2)));
                        if (m.group(4) != null) {
                            chan.setNotch(Integer.parseInt(m.group(4)));
                        }
                    }
                    chan.setName(result.getHeader().getChannelLabels()[i].trim());
                    chan.setReferenceUnits(result.getHeader().getDimensions()[i].trim());
                    channels.add(chan);
                }

                double samplingfrequency = result.getHeader().getNumberOfRecords()
                        / result.getHeader().getDurationOfRecords();

                // Create events
                List<Event> events = new ArrayList<>();
                for (EDFAnnotation annotation : result.getAnnotations()) {
                    Event event = new Event();

                    // This is done by default
                    event.setChannelNumber(0);
                    event.setPosition(String.valueOf((float) (samplingfrequency / annotation.getOnSet())));
                    event.setPoints((int) annotation.getDuration());
                    events.add(event);
                }

                EegDataset dataset = new EegDataset();
                dataset.setEvents(events);
                dataset.setChannels(channels);
                dataset.setChannelCount(result.getHeader().getNumberOfChannels());

                // Get dataset name from EDF file name
                String fileNameWithOutExt = FilenameUtils.removeExtension(edfFile.getName());
                dataset.setName(fileNameWithOutExt);

                dataset.setSamplingFrequency((int) samplingfrequency);

                List<String> files = new ArrayList<>();
                if (allFilesInDir != null) {
                    for (File fi : allFilesInDir) {
                        if (fi.getName().startsWith(fileNameWithOutExt)) {
                            files.add(fi.getCanonicalPath());
                        }
                    }
                }
                dataset.setFiles(files);
                datasets.add(dataset);
            } catch (IOException e) {
                throw new ShanoirImportException("Error while parsing file. Please contact an amdinistrator", e);
            }
        }
    }

    /**
     * Reads a list of .vhdr files to generate a bunch of datasets.
     *
     * @param dataFileDir     the file directory where we are working
     * @param bvMatchingFiles the list of vhdr files
     * @param datasets        the list of datasets to import
     * @return a list of datasets generated from the informations of the .vhdr files
     * @throws ShanoirImportException when parsing fails
     * @throws IOException
     */
    private void readBrainvisionFiles(final File[] bvMatchingFiles, final File dataFileDir,
                                      final List<EegDataset> datasets) throws ShanoirImportException, IOException {
        // List the directory once instead of once per .vhdr file
        final File[] allFilesInDir = dataFileDir.listFiles();

        for (File vhdrFile : bvMatchingFiles) {

            // Parse the file
            BrainVisionReader bvr = new BrainVisionReader(vhdrFile);

            EegDataset dataset = new EegDataset();
            dataset.setEvents(bvr.getEvents());
            dataset.setChannels(bvr.getChannels());
            dataset.setChannelCount(bvr.getNbchan());
            // Get dataset name from VHDR file name
            String fileNameWithOutExt = FilenameUtils.removeExtension(vhdrFile.getName());
            dataset.setName(fileNameWithOutExt);

            // Manage when we have a sampling interval but no sampling frequency
            int samplingFrequency = bvr.getSamplingFrequency();
            if (samplingFrequency == 0 && bvr.getSamplingIntervall() != 0) {
                samplingFrequency = Math.round(1000 / bvr.getSamplingIntervall());
            }

            dataset.setSamplingFrequency(samplingFrequency);
            dataset.setCoordinatesSystem(bvr.getHasPosition() ? "true" : null);

            try {
                bvr.close();
            } catch (IOException e) {
                throw new ShanoirImportException("Error while parsing file. Please contact an administrator.", e);
            }

            List<String> files = new ArrayList<>();
            if (allFilesInDir != null) {
                for (File fi : allFilesInDir) {
                    if (fi.getName().startsWith(fileNameWithOutExt)) {
                        files.add(fi.getCanonicalPath());
                    }
                }
            }
            dataset.setFiles(files);
            datasets.add(dataset);
        }
    }

    /**
     * Here we had all the informations we needed (metadata, examination, study,
     * subject, ect...) so we make a call to dataset API to create it.
     */
    @Override
    public ResponseEntity<Void> startImportEEGJob(
            @Parameter(name = "EegImportJob", required = true) @Valid @RequestBody final EegImportJob importJob)
            throws RestServiceException {
        importJobStatusService.setInProgress(importJob, "Import job received, queued for processing.");
        LOG.info("============== NEW IMPORT EEG =======================");
        LOG.info("EEG import job for user {} ({})", KeycloakUtil.getTokenUserName(), KeycloakUtil.getTokenUserId());
        LOG.info("Import type: {}", importJob.getImportType());
        LOG.info("WorkFolder: {}", importJob.getWorkFolder());
        // Comment: Anonymisation is not necessary for pure brainvision EEGs data
        try {
            importJob.setUserId(KeycloakUtil.getTokenUserId());
            importJob.setUsername(KeycloakUtil.getTokenUserName());
            ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), KeycloakUtil.getTokenUserId(), "Starting import...", ShanoirEvent.IN_PROGRESS, 0f, importJob.getStudyId());
            importJob.setShanoirEvent(event);
            cleanUpImportJob(importJob);
            Integer integg = (Integer) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.IMPORT_EEG_QUEUE, objectMapper.writeValueAsString(importJob));
            importJobStatusService.setFinished(importJob);
            return new ResponseEntity<Void>(HttpStatusCode.valueOf(integg.intValue()));
        } catch (Exception e) {
            LOG.error("Error during EEG import", e);
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void cleanUpImportJob(final ImportJobBase importJob) {
        // Clean up to send smaller json
        importJob.setPatient(null);
        importJob.setStudy(null);
    }

    @Override
    public ResponseEntity<String> createTempDir() throws RestServiceException, IOException {
        ImportJob importJob = new ImportJob();
        File importJobDir = ImportUtils.initImportJob(importJob, importDir);
        return new ResponseEntity<String>(importJobDir.getName(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> uploadFile(@PathVariable("tempDirId") String tempDirId,
                @RequestParam("file") MultipartFile file) throws RestServiceException, IOException {
        final File userImportDir = ImportUtils.getUserImportDir(importDir);
        final File importJobDir = new File(userImportDir, tempDirId);
        if (importJobDir.exists()) {
            writeUploadedFile(importJobDir, file);
        } else {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Upload file called with not existing tempDirId.", null));
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * New variant of {@link #uploadFile}, used by newer ShanoirUploader versions: instead of
     * dropping every file flat into the temp dir (requiring a later, separate split-into-series
     * step, see DatasetsCreatorService.createSerieIDFolderAndMoveFiles), the file is written
     * directly into a seriesInstanceUID-named sub-folder of the temp dir.
     */
    @Override
    public ResponseEntity<Void> uploadFileToSeries(
            @PathVariable("tempDirId") String tempDirId,
            @PathVariable("seriesInstanceUID") String seriesInstanceUID,
            @RequestParam("file") MultipartFile file)
            throws RestServiceException, IOException {
        final File userImportDir = ImportUtils.getUserImportDir(importDir);
        final File importJobDir = new File(userImportDir, tempDirId);
        if (!importJobDir.isDirectory()) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "Upload file called with not existing tempDirId.",
                            null));
        }
        final File seriesDir = new File(importJobDir, seriesInstanceUID);
        Files.createDirectories(seriesDir.toPath());
        writeUploadedFile(seriesDir, file);
        return ResponseEntity.ok().build();
    }

    private void writeUploadedFile(File targetDir, MultipartFile file)
            throws RestServiceException, IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "Uploaded file has no filename.",
                            null));
        }

        String filename = Paths.get(originalFilename)
                .getFileName()
                .toString();
        Path fileToWrite = targetDir.toPath().resolve(filename);
        try {
            Files.write(
                    fileToWrite,
                    file.getBytes(),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (FileAlreadyExistsException e) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "Duplicate file name in tempDir, could not create file as file exists already.",
                            null));
        }
    }

    /**
     * This methods returns a dicom file
     *
     * @param path
     *            the dicom file path
     * @throws ShanoirException
     *             when something gets wrong during the import
     * @throws IOException
     *             when IO fails
     * @throws RestServiceException
     */
    @Override
    public ResponseEntity<ByteArrayResource> getDicomImage(
            @Parameter(name = "path", required = true) @RequestParam(value = "path", required = true) String path)
            throws RestServiceException, IOException {
        final File userImportDir = ImportUtils.getUserImportDir(importDir);
        final String userImportDirCanonicalPath = userImportDir.getCanonicalPath();
        final String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        final File dicomFile = new File(decodedPath);
        final String dicomFileCanonicalPath = dicomFile.getCanonicalPath();
        if (!dicomFileCanonicalPath.startsWith(userImportDirCanonicalPath + File.separator)
                && !dicomFileCanonicalPath.equals(userImportDirCanonicalPath)) {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Path is not within the allowed user import directory: " + path, null));
        }
        byte[] byteArray = Files.readAllBytes(dicomFile.toPath());
        ByteArrayResource resource = new ByteArrayResource(byteArray);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/dicom"))
                .contentLength(byteArray.length)
                .body(resource);
    }

    public ResponseEntity<ImportJobBase> uploadMultipleDicom(@Parameter(name = "file detail") @RequestPart("file") MultipartFile dicomZipFile,
            @Parameter(name = "studyId", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(name = "studyName", required = true) @PathVariable("studyName") String studyName,
            @Parameter(name = "studyCardId") @PathVariable("studyCardId") Long studyCardId,
            @Parameter(name = "centerId", required = true) @PathVariable("centerId") Long centerId,
            @Parameter(name = "equipmentId", required = true) @PathVariable("equipmentId") Long equipmentId) throws RestServiceException {
        LOG.info("Multiple examination import for study {} ({})", studyName, studyId);
        if (dicomZipFile == null || !ImportUtils.isZipFile(dicomZipFile)) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
        }
        ImportJobBase importJobParent = new ImportJobBase();
        try {
            File importJobFileParent = ImportUtils.initImportJob(importJobParent, importDir, dicomZipFile);
            File importJobDirParent = new File(importJobParent.getWorkFolder());
            ImportUtils.unzip(importJobFileParent.getAbsolutePath(), importJobDirParent.getAbsolutePath());
            importJobFileParent.delete();

            // STEP 2: Analyze file structure and verify for mismatch
            File[] subjectFolders = importJobDirParent.listFiles();
            if (subjectFolders == null || subjectFolders.length != 1) {
                throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "The zip must contain a single folder named with the desired subject name.", null));
            }
            File subjectFolder = subjectFolders[0];
            File[] examinationsFolders = subjectFolder.listFiles();
            String subjectName = subjectFolder.getName();
            Subject subject = null;

            // Sort examination folders by alphabetical order
            List<File> sortedExamFolders = Arrays.asList(examinationsFolders);
            sortedExamFolders.sort(new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });

            // STEP 4: Iterate over examination folders
            for (File examFolder : sortedExamFolders) {
                // Check if it's a folder
                if (!examFolder.isDirectory()) {
                    throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "The main subject folder should only contain sub-folders and not single/data files.", null));
                }
                ImportJob importJobChild = new ImportJob();
                importJobChild.setFromDicomZip(true);
                File importJobDirChild = ImportUtils.initImportJob(importJobChild, importDir);
                ImportUtils.moveDirectoryContents(examFolder, importJobDirChild);
                boolean createDicomDir = !new File(importJobDirChild, DICOMDIR).exists();
                setPatientsFromDicomDirAndCreateImages(importJobChild, importJobDirChild, createDicomDir);
                Patient patient = importJobChild.getPatient();

                // Create subject only once.
                if (subject == null) {
                    // Update birth date to 1st of january of the year
                    LocalDate updateBirthdate = patient.getPatientBirthDate().withDayOfYear(1);
                    subject = ImportUtils.createSubject(subjectName, studyId, studyName, updateBirthdate, patient.getPatientSex(), 1);
                    Long subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE_WITH_DATASETS, objectMapper.writeValueAsString(subject));
                    if (subjectId == null) {
                        throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Subject could not be created, please check data", null));
                    }
                    subject.setId(subjectId);
                }

                // Get informations about center / study card
                // Get equipment id
                Long equipmentIdFromDicom = null;
                if (importJobChild.getSeries().get(0).getEquipment().getDeviceSerialNumber() != null) {
                    equipmentIdFromDicom = (Long) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EQUIPMENT_FROM_CODE_QUEUE, importJobChild.getSeries().get(0).getEquipment().getDeviceSerialNumber());
                    if (equipmentIdFromDicom != null) {
                        if (studyCardId != 0L) {
                            java.util.Map<String, String> params = new java.util.HashMap<>();
                            params.put("EQUIPMENT_ID_PROPERTY", "" + equipmentIdFromDicom);
                            params.put("STUDY_ID_PROPERTY", "" + studyId);
                            params.put("STUDYCARD_ID_PROPERTY", "" + studyCardId);
                            Long newStudyCardId = (Long) this.rabbitTemplate.convertSendAndReceive(
                                    RabbitMQConfiguration.IMPORT_STUDY_CARD_QUEUE,
                                    objectMapper.writeValueAsString(params));
                            if (newStudyCardId != null) {
                                studyCardId = newStudyCardId;
                            }
                        }
                        importJobChild.setAcquisitionEquipmentId(equipmentIdFromDicom);
                    } else {
                        importJobChild.setAcquisitionEquipmentId(equipmentId);
                    }
                }

                if (studyCardId != 0L)
                    importJobChild.setStudyCardId(studyCardId);

                // Create examination
                ExaminationDTO examination = ImportUtils.createExam(studyId, centerId, subject.getId(), examFolder.getName(),
                        importJobChild.getStudy().getStudyDate(), subject.getName());
                String studyInstanceUID = UID_GENERATOR.getNewUID();
                examination.setStudyInstanceUID(studyInstanceUID);

                // Create one examination for every session folder
                Long examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));
                if (examId == null) {
                    throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating examination", null));
                }
                eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EXAMINATION_EVENT, examId.toString(), KeycloakUtil.getTokenUserId(), "centerId:" + centerId + ";subjectId:" + examination.getSubject().getId(), ShanoirEvent.SUCCESS, examination.getStudyId()));

                // Complete importJob with subject / study /examination
                String anonymizationProfile = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_ANONYMISATION_PROFILE_QUEUE, studyId);

                importJobChild.setStudyId(studyId);
                importJobChild.setStudyName(studyName);
                importJobChild.setFromDicomZip(true);
                importJobChild.setSubjectName(subjectName);
                importJobChild.setSubject(subject);
                importJobChild.setExaminationId(examId);
                importJobChild.setStudyInstanceUID(studyInstanceUID);
                importJobChild.setCenterId(centerId);
                importJobChild.setAcquisitionEquipmentId(equipmentId);
                importJobChild.setAnonymisationProfileToUse(anonymizationProfile);

                // Select all series
                for (Serie serie : importJobChild.getSeries()) {
                    serie.setSelected(true);
                }

                // Send to ms-dataset for logical import
                cleanUpImportJob(importJobChild);
                this.startImportJobBase(importJobChild);
            }
            // Delete temporary file
            FileUtils.deleteQuietly(importJobDirParent);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RestServiceException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "The file could not be correctly unziped on the server. Please check consistency.", e));
        }
        return new ResponseEntity<ImportJobBase>(importJobParent, HttpStatus.OK);
    }

}
