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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.shanoir.anonymization.anonymization.AnonymizationResult;
import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.DicomStoreSCPServer;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.EmailBase;
import org.shanoir.ng.shared.email.EmailDatasetImportFailed;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This class actually does the import work and introduces the asynchronous
 * aspect into the import, that the ImporterApiController can directly answer.
 *
 * @author mkain
 *
 */
@Service
public class ImporterManagerService {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterManagerService.class);

    /**
     * For the moment Spring is not used here to autowire, as we try to keep the
     * anonymization project as simple as it is, without Spring annotations, to
     * be usable outside a Spring context, as e.g. in ShanoirUploader.
     * Maybe to change and think about deeper afterwards.
     */
    private static final AnonymizationServiceImpl ANONYMIZER = new AnonymizationServiceImpl();

    @Autowired
    private QueryPACSService queryPACSService;

    @Autowired
    private DicomStoreSCPServer dicomStoreSCPServer;

    @Autowired
    private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private DatasetsCreatorService datasetsCreatorService;

    @Autowired
    private ImportJobStatusService importJobStatusService;

    @Autowired
    private StudyUserRightsRepository studyUserRightRepo;

    @Async
    public void manageImportJob(final ImportJobBase importJob) {
        importJobStatusService.setInProgress(importJob, "Starting import");
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT,
                importJob.getExaminationId().toString(), importJob.getUserId(),
                "Starting import", ShanoirEvent.IN_PROGRESS,
                0f, importJob.getStudyId());
        event.setTimestamp(importJob.getTimestamp());
        eventService.publishEvent(event);
        importJob.setShanoirEvent(event);
        try {
            // 1. call to cleanSeries: remove ignored series, that have been detected to be
            // ignored by the uploadDicomZipFile (DicomDirToModelService) or the QueryPACSService
            // (either from ShUp or the web-gui-pacs import), see usage of
            // DicomSerieAndInstanceAnalyzer and e.g. missing instances
            cleanSeries(importJob.getSeries());
            // In PACS import the DICOM files are still in the PACS, we have to download
            // them first and then analyze them:
            // what gives us a list of images for each serie.
            final File importJobDir = new File(importJob.getWorkFolder());
            if (importJob.isFromPacs()) {
                // At first all DICOM files arrive normally in /tmp/shanoir-dcmrcv
                // (see config DicomStoreSCPServer)
                downloadAndMoveDicomFilesToImportJobDir(importJobDir,
                        importJob.getStudy().getStudyInstanceUID(), importJob.getSeries(), event);
                imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(importJob,
                        importJobDir.getAbsolutePath(), event, true);
            // Pseudonymization already done and UIDs updated in importJob
            // createImages already based on correct instances post-pseudo
            } else if (importJob.isFromShanoirUploader()) {
                imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(importJob,
                        importJobDir.getAbsolutePath(), event, true);
            // isFromDicomZip: do nothing, as images creation and analyze of DICOM files
            // have been done after upload of ZIP file(s) already
            } else if (!importJob.isFromDicomZip()) {
                throw new ShanoirException("Unsupported type of import.");
            }

            // 2. call to cleanSeries: at this point we are sure for all imports, that the
            // ImagesCreatorAndDicomFileAnalyzer has been run and correctly classified
            // everything. So no need to check afterwards for erroneous series.
            // So two possibilities to remove series:
            // 1. call above, via the info from the DICOMDIR or the info from the pacs
            // 2. call, via analysis of DICOM files itself and their content (PACS)
            cleanSeries(importJob.getSeries());

            event.setProgress(0.25F);
            eventService.publishEvent(event);
            importJobStatusService.setInProgress(importJob, "Pseudonymizing and creating datasets");
            if (!importJob.isFromShanoirUploader()) {
                pseudonymize(importJob, event, importJobDir);
            }
            datasetsCreatorService.createDatasets(importJob, importJobDir);
            importJobStatusService.setFinished(importJob);

            this.rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMPORTER_QUEUE_DATASET,
                    objectMapper.writeValueAsString(importJob));
            long importJobDirSize = ImportUtils.getDirectorySize(importJobDir.toPath());
            LOG.info("user=" + KeycloakUtil.getTokenUserName() + ",size="
                    + ImportUtils.readableFileSize(importJobDirSize) + "," + importJob.toString());
        } catch (Exception e) {
            LOG.error("Error during import for study {} and examination {}", importJob.getStudyId(),
                    importJob.getExaminationId(), e);
            event.setMessage("ERROR while importing data for study " + importJob.getStudyId() + " for examination "
                    + importJob.getExaminationId());
            event.setStatus(ShanoirEvent.ERROR);
            event.setProgress(-1f);
            eventService.publishEvent(event);
            importJobStatusService.setError(importJob, event.getMessage());
            sendFailureMail(importJob, e.getMessage());
        }
    }

    /**
     * As the DicomSerieAndInstanceAnalyzer can declare a serie as ignored as well, we clean twice.
     * cleanSeries is important for import-from-zip file: when the ImagesCreatorAndDicomFileAnalyzer
     * has declared some series as e.g. erroneous, we have to remove them from the import. For import-from
     * pacs or from-sh-up it is different, as the ImagesCreatorAndDicomFileAnalyzer is called afterwards (startImportJob).
     * Same here for multi-exam-imports: it calls uploadDicomZipFile method, where series could be classed
     * as erroneous and when startImportJob is called, we want them to be removed from the import.
     *
     * @param importJob
     */
    private void cleanSeries(List<Serie> series) {
        for (Iterator<Serie> serieIt = series.iterator(); serieIt.hasNext();) {
            Serie serie = serieIt.next();
            if (!serie.getSelected() || serie.isIgnored() || serie.isErroneous()) {
                LOG.info("Serie {} cleaned from import (not selected, ignored, erroneous).", serie.getSeriesDescription());
                serieIt.remove();
            }
        }
    }

    private void pseudonymize(final ImportJobBase importJob, ShanoirEvent event, final File importJobDir)
            throws FileNotFoundException, ShanoirException {
        if (importJob.getAnonymisationProfileToUse() != null && !importJob.getAnonymisationProfileToUse().isEmpty()) {
            ArrayList<File> dicomFiles = getDicomFilesForImportJob(importJob, importJobDir.getAbsolutePath());
            final Subject subject = importJob.getSubject();
            if (subject == null) {
                LOG.error("Error: subject == null in importJob.");
                throw new ShanoirException("Error: subject == null in importJob.");
            }
            final String subjectName = subject.getName();
            event.setMessage("Pseudonymizing DICOM files for subject [" + subjectName + "]...");
            eventService.publishEvent(event);
            try {
                AnonymizationResult anonymizationResult = ANONYMIZER.anonymizeForShanoir(dicomFiles, importJob.getAnonymisationProfileToUse(), subjectName, subjectName, importJob.getStudyInstanceUID());
                ImportUtils.updateImportJobWithPseudonymizedUIDs(importJob, importJobDir, anonymizationResult);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new ShanoirException("Error during pseudonymization.");
            }
        } else {
            throw new ShanoirException("Error during pseudonymization: missing profile.");
        }
    }

    private void sendFailureMail(ImportJobBase importJob, String errorMessage) {
        EmailDatasetImportFailed generatedMail = new EmailDatasetImportFailed();
        generatedMail.setExaminationId(importJob.getExaminationId().toString());
        generatedMail.setStudyId(importJob.getStudyId().toString());
        generatedMail.setSubjectName(importJob.getSubjectName());
        generatedMail.setStudyName(importJob.getStudyName());
        generatedMail.setUserId(importJob.getUserId());
        generatedMail.setErrorMessage(errorMessage != null ? errorMessage : "An unexpected error occured, please contact Shanoir support.");
        sendMail(importJob, generatedMail);
    }

    /**
     * Sends the given mail in entry to all recipients in a given study
     * @param job the imprt job
     * @param email the recipients
     */
    private void sendMail(ImportJobBase job, EmailBase email) {
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
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMPORT_DATASET_FAILED_MAIL_QUEUE, objectMapper.writeValueAsString(email));
        } catch (AmqpException | JsonProcessingException e) {
            LOG.error("Could not send email for this import. ", e);
        }
    }

    /**
     * Calls a c-move for each serie involved, files are received via DicomStoreSCPServer.
     *
     * @param patients
     * @throws ShanoirException
     */
    private void downloadAndMoveDicomFilesToImportJobDir(final File importJobDir, String studyInstanceUID,
            List<Serie> series, ShanoirEvent event) throws Exception {
        int cpt = 1;
        int nbSeries = series.size();
        for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
            Serie serie = seriesIt.next();
            event.setMessage("Downloading DICOM files from PACS for serie [" + (serie.getProtocolName() == null ? serie.getSeriesInstanceUID() : serie.getProtocolName()) + "] (" + cpt + "/" + nbSeries + ")");
            eventService.publishEvent(event);
            String seriesInstanceUID = serie.getSeriesInstanceUID();
            queryPACSService.queryCFINDInstances(studyInstanceUID, serie);
            queryPACSService.queryCMOVE(studyInstanceUID, serie);
            File serieIDFolderDir = new File(importJobDir + File.separator + seriesInstanceUID);
            if (!serieIDFolderDir.exists()) {
                serieIDFolderDir.mkdirs();
            } else {
                throw new ShanoirException("Error while creating serie id folder: folder already exists.");
            }
            for (Iterator<Instance> iterator = serie.getInstances().iterator(); iterator.hasNext();) {
                Instance instance = iterator.next();
                String sopInstanceUID = instance.getSopInstanceUID();
                File oldFile = new File(dicomStoreSCPServer.getStorageDirPath() + File.separator + seriesInstanceUID + File.separator + sopInstanceUID + DicomStoreSCPServer.DICOM_FILE_SUFFIX);
                if (oldFile.exists()) {
                    File newFile = new File(importJobDir.getAbsolutePath() + File.separator + seriesInstanceUID + File.separator + oldFile.getName());
                    oldFile.renameTo(newFile);
                    LOG.debug("Moving file: {} to ", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                } else {
                    throw new ShanoirException("Error while creating serie id folder: file to copy does not exist.");
                }
            }
            cpt++;
        }
    }

    /**
     * Using Java HashSet here to avoid duplicate files for Pseudonymization.
     * For performance reasons already init with 100000 buckets, assuming,
     * that we will normally never have more than 100000 files to process.
     *
     * @param importJob
     * @param patient
     * @param workFolderPath
     * @return list of files
     * @throws FileNotFoundException
     */
    private ArrayList<File> getDicomFilesForImportJob(final ImportJobBase importJob, final String workFolderPath) throws FileNotFoundException {
        Set<File> pathsSet = new HashSet<>(100000);
        List<Serie> series = importJob.getSeries();
        for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
            Serie serie = seriesIt.next();
            handleSerie(workFolderPath, pathsSet, serie);
        }
        return new ArrayList<>(pathsSet);
    }

    /**
     * This method walks trough the images of a serie, gets the path,
     * creates a file for it and adds it to pathsSet.
     *
     * @param workFolderPath
     * @param pathsSet
     * @param serie
     * @param importJob
     * @throws FileNotFoundException
     */
    private void handleSerie(final String workFolderPath, Set<File> pathsSet, Serie serie) throws FileNotFoundException {
        List<Image> images = serie.getImages();
        for (Iterator<Image> imagesIt = images.iterator(); imagesIt.hasNext();) {
            Image image = imagesIt.next();
            String path = image.getPath();
            File file = new File(path);
            if (file.exists()) {
                pathsSet.add(file);
            } else {
                throw new FileNotFoundException("File not found: " + path);
            }
        }
    }

}
