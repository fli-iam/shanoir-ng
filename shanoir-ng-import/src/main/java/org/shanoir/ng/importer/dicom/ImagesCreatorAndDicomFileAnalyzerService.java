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

package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.emf.MultiframeExtractor;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dicom.EchoTime;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class reads all instances. A FileInputStream in form of a DicomInputStream is opened
 * to all files to read additional informations, e.g. missing in the DicomDir.
 * This class splits the instances array into two different array nodes: non-images and images,
 * on using the SOPClassUID. Before the instances are numbered with their instance number
 * and added like this by DicomDirToModelReader. DicomFileAnalyzer removes/deletes
 * the instances node and creates an image. As this class is reading the content of
 * each dicom file already it adds as well the informations,
 * which are later necessary to separate datasets inside each serie:
 * acquisitionNumber, echoNumbers and imageOrientationsPatient.
 *
 * In case of the import from pacs, the files are accessed using the STORAGE_PATTERN defined
 * in DicomStoreSCPServer.
 *
 * @author mkain
 *
 */
@Service
public class ImagesCreatorAndDicomFileAnalyzerService {

    private static final Logger LOG = LoggerFactory.getLogger(ImagesCreatorAndDicomFileAnalyzerService.class);

    private static final String YES = "YES";

    private static final String SERIES_NUMBER_0 = "0";

    private static final String SLASH = "/";

    private MultiframeExtractor emf = new MultiframeExtractor();

    @Autowired
    private ShanoirEventService eventService;

    public void createImagesAndAnalyzeDicomFiles(
            ImportJobBase importJob, String folderFileAbsolutePath, ShanoirEvent event, boolean isFromPACS)
            throws FileNotFoundException {
        int cpt = 1;
        int nbSeries = importJob.getSeries().size();
        for (Iterator<Serie> seriesIt = importJob.getSeries().iterator(); seriesIt.hasNext();) {
            Serie serie = seriesIt.next();
            if (!serie.isIgnored()) {
                if (event != null) {
                    event.setMessage("Creating images and analyzing DICOM files for serie [" + (serie.getSeriesDescription() == null ? serie.getSeriesInstanceUID() : serie.getSeriesDescription()) + "] " + cpt + "/" + nbSeries + ")");
                    eventService.publishEvent(event);
                }
                try {
                    filterAndCreateImages(folderFileAbsolutePath, serie, isFromPACS);
                } catch (Exception e) { // one serie/file could cause problems, log and mark as erroneous, but continue with next serie
                    handleError(event, nbSeries, cpt, serie, e);
                }
                if (!serie.isIgnored()) {
                    // use a second try here, in case error is on serie, to get at least the serie name for error tracing
                    try {
                        getAdditionalMetaDataFromFirstInstanceOfSerie(folderFileAbsolutePath, importJob.getPatient(), importJob.getStudy(), serie, isFromPACS);
                    } catch (Exception e) {
                        handleError(event, nbSeries, cpt, serie, e);
                    }
                }
            }
            // Images created, no need for instances anymore
            serie.setInstances(null);
            cpt++;
        }
        /**
         * We apply an additional sort here on the series list to base everything on
         * seriesNumbers finally. It might happen, while using ShanoirUploader, that
         * some PACS will not share the seriesNumber, so we can not correctly sort in
         * ShanoirUploader without going into the files itself, what we do not do,
         * therefore we have the ImagesCreatorAndDicomFileAnalyzerService, that is
         * called on the server.
         */
        importJob.getSeries().sort(new SeriesNumberOrAcquisitionTimeOrDescriptionSorter());
    }

    private void handleError(ShanoirEvent event, int nbSeries, int cpt, Serie serie, Exception e) {
        LOG.error("Error while processing serie: {} {} {}", serie.toString(), e.getMessage(), e.getStackTrace());
        serie.setErroneous(true);
        serie.setErrorMessage(e.getMessage() + ", " + e.toString());
        serie.setSelected(false);
        if (event != null) {
            event.setMessage("Error with serie [" + (serie.getSeriesDescription() == null ? serie.getSeriesInstanceUID() : serie.getSeriesDescription()) + "] " + cpt + "/" + nbSeries + ")");
            eventService.publishEvent(event);
        }
    }

    /**
     * @param folderFileAbsolutePath
     * @param serie
     * @throws FileNotFoundException
     */
    public void getAdditionalMetaDataFromFirstInstanceOfSerie(String folderFileAbsolutePath, Patient patient, Study study, Serie serie, boolean isFromPACS)
            throws FileNotFoundException {
        List<Instance> instances = serie.getInstances();
        if (instances != null && !instances.isEmpty()) {
            Instance firstInstance = instances.getFirst();
            File firstInstanceFile = null;
            if (isFromPACS) {
                firstInstanceFile = ImportUtils.getInstanceFileByUIDs(firstInstance, serie, folderFileAbsolutePath);
            } else {
                firstInstanceFile = ImportUtils.getInstanceFileByReferencedFileID(firstInstance, folderFileAbsolutePath);
            }
            processDicomFileForFirstInstance(firstInstanceFile, patient, study, serie);
        }
    }

    /**
     * This method iterates over all instances, filters only the images
     * and puts them into a new list: images.
     *
     * @param folderFileAbsolutePath
     * @param serie
     * @throws FileNotFoundException
     */
    private void filterAndCreateImages(String folderFileAbsolutePath, Serie serie, boolean isFromPACS) throws Exception {
        List<Image> images = new ArrayList<Image>();
        List<Instance> instances = serie.getInstances();
        if (instances != null) {
            for (Iterator<Instance> instancesIt = instances.iterator(); instancesIt.hasNext();) {
                Instance instance = instancesIt.next();
                File instanceFile = null;
                if (isFromPACS) {
                    // Old versions of ShUp always set the ReferencedFileID
                    // and upload all files into the same import job folder
                    // TODO to remove after migration phase with old ShUps
                    if (!ArrayUtils.isEmpty(instance.getReferencedFileID())) {
                        instanceFile = ImportUtils.getInstanceFileByReferencedFileID(instance, folderFileAbsolutePath);
                    } else {
                        instanceFile = ImportUtils.getInstanceFileByUIDs(instance, serie, folderFileAbsolutePath);
                    }
                } else {
                    instanceFile = ImportUtils.getInstanceFileByReferencedFileID(instance, folderFileAbsolutePath);
                }
                LOG.debug("filterAndCreateImages: processing file {}", instanceFile.getAbsolutePath());
                processDicomFilePerInstanceAndCreateImage(instanceFile, images, folderFileAbsolutePath);
            }
            /**
             * Old versions of ShUp v7.0.1, still installed and running, send "ignored" series.
             * The method processDicomFilePerInstanceAndCreateImage will ignore those instances,
             * so the images remain empty, that is why we tag these series as ignored now.
             */
            if (!images.isEmpty()) {
                serie.setImages(images);
                serie.setImagesNumber(images.size());
            } else {
                serie.setIgnored(true);
                serie.setSelected(false);
            }
        }
    }


    /**
     * This method opens the connection to each dcm file and reads its attributes
     * and extracts meta-data from the dicom, that will be used later.
     *
     * @param dicomFile
     * @param serie
     * @param instances
     * @param instance
     * @param index
     * @param nonImages
     * @param images
     */
    private void processDicomFilePerInstanceAndCreateImage(File dicomFile, List<Image> images,
            String folderFileAbsolutePath) throws Exception {
        try (DicomInputStream dIS = new DicomInputStream(dicomFile)) {
            Attributes attributes = dIS.readDatasetUntilPixelData();
            // Some DICOM files with a particular SOPClassUID are ignored: such as Raw Data Storage etc.
            if (!DicomSerieAndInstanceAnalyzer.checkInstanceIsIgnored(attributes)) {
                Image image = new Image();
                String sopInstanceUID = attributes.getString(Tag.SOPInstanceUID);
                if (sopInstanceUID == null || sopInstanceUID.isEmpty()) {
                    UIDGeneration generator = new UIDGeneration();
                    sopInstanceUID = generator.getNewUID();
                    LOG.warn("DICOM file without SOPInstanceUID: " + dicomFile.getAbsolutePath());
                }
                image.setSOPInstanceUID(sopInstanceUID);
                /**
                 * Attention: the path of each image is always relative: either to the temporary folder created
                 * with dicom zip import during the upload or with the DicomStoreSCPServer folder for PACS import.
                 * If the import is from ShanoirUploader, the path stays absolute to allow the execution of quality control.
                 */
                String relativeFilePath = dicomFile.getAbsolutePath().replace(folderFileAbsolutePath + SLASH, "");
                image.setPath(relativeFilePath);
                addImageSeparateDatasetsInfo(image, attributes, dicomFile);
                images.add(image);
            }
        } catch (IOException iOE) {
            throw iOE;
        } catch (Exception e) {
            LOG.error("Error while processing DICOM file, one for entire serie: " + dicomFile.getAbsolutePath());
            throw e;
        }
    }

    /**
     * This method reads the first dicom file of a serie to complete missing informations.
     *
     * @param dicomFile
     * @param serie
     * @param patient
     */
    private void processDicomFileForFirstInstance(File dicomFile, Patient patient, Study study, Serie serie) {
        try (DicomInputStream dIS = new DicomInputStream(dicomFile)) {
            LOG.debug("Process first DICOM file of serie {} path {}", serie.getSeriesInstanceUID() + " " + serie.getSeriesDescription(), dicomFile.getAbsolutePath());
            Attributes attributes = dIS.readDatasetUntilPixelData();
            checkPatientData(patient, attributes);
            checkStudyData(study, attributes);
            checkSerieData(serie, attributes);
            addSeriesInstitution(serie, attributes);
            addSeriesEquipment(serie, attributes);
        } catch (IOException e) {
            LOG.error("Error during processing of DICOM file " + dicomFile.getAbsolutePath() + ":", e);
        }
    }

    /**
     * This method adds all required infos to separate datasets within series for
     * each image.
     *
     * @param image
     * @param datasetAttributes
     */
    private void addImageSeparateDatasetsInfo(Image image, Attributes attributes, File dicomFile) throws Exception {
        final String sopClassUID = attributes.getString(Tag.SOPClassUID);
        if (MultiframeExtractor.isSupportedSOPClass(sopClassUID)) {
            // MultiframeExtractor needs the actual PixelData bytes to slice out a frame,
            // which readDatasetUntilPixelData() deliberately omits. Re-read this file in
            // full — but only for enhanced multi-frame instances, which are the exception,
            // not the rule, so this doesn't undo the optimization for normal series.
            try (DicomInputStream fullDIS = new DicomInputStream(dicomFile)) {
                attributes = fullDIS.readDataset();
            }
            attributes = emf.extract(attributes, 0);
        }
        // acquisition number
        image.setAcquisitionNumber(attributes.getInt(Tag.AcquisitionNumber, 0));
        // image orientation patient
        List<Double> imageOrientationPatient = new ArrayList<>();
        double[] imageOrientationPatientArray = attributes.getDoubles(Tag.ImageOrientationPatient);
        if (imageOrientationPatientArray != null) {
            for (int i = 0; i < imageOrientationPatientArray.length; i++) {
                imageOrientationPatient.add(imageOrientationPatientArray[i]);
            }
            image.setImageOrientationPatient(imageOrientationPatient);
        } else {
            LOG.debug("imageOrientationPatientArray in dcm file null: {}", image.getPath());
        }
        // repetition time
        image.setRepetitionTime(attributes.getDouble(Tag.RepetitionTime, 0));
        // inversion time
        image.setInversionTime(attributes.getDouble(Tag.InversionTime, 0));
        // flip angle
        String flipAngle = attributes.getString(Tag.FlipAngle);
        if (flipAngle == null) {
            flipAngle = "0";
        }
        image.setFlipAngle(flipAngle);
        // echo times
        Set<EchoTime> echoTimes = new HashSet<>();
        EchoTime echoTime = new EchoTime();
        echoTime.setEchoNumber(attributes.getInt(Tag.EchoNumbers, 0));
        echoTime.setEchoTime(attributes.getDouble(Tag.EchoTime, 0.0));
        echoTimes.add(echoTime);
        image.setEchoTimes(echoTimes);
    }

    /**
     * Adds the equipment information. We suppose here that the info coming
     * from the first file is more reliable than the infos coming from the
     * dicomdir or the pacs querying.
     *
     * @param serie
     * @param attributes
     */
    private void addSeriesEquipment(Serie serie, Attributes attributes) {
        if (!serie.getEquipment().isKnown()) {
            serie.setEquipment(new EquipmentDicom(attributes));
        }
    }

    /**
     * Adds the equipment information.
     * Used by ShanoirUploader in case of a DICOM Pushed study
     *
     * @param serie
     * @param datasetAttributes
     */
    public void addSeriesInstitution(Serie serie, Attributes attributes) {
        if (!serie.getInstitution().isKnown()) {
            InstitutionDicom institution = new InstitutionDicom(attributes);
            serie.setInstitution(institution);
        }
    }

    /**
     * Get DICOM study information from .dcm file.
     *
     * @param study
     * @param attributes
     */
    private void checkStudyData(Study study, Attributes attributes) {
        if (study != null && attributes != null) {
            // always use StudyDescription from .dcm file, e.g. in Q/R encoding errors
            // can happen, so we trust the local .dcm file the most to avoid further issues
            String studyDescriptionDicomFile = attributes.getString(Tag.StudyDescription);
            if (StringUtils.isNotEmpty(studyDescriptionDicomFile)) {
                study.setStudyDescription(studyDescriptionDicomFile);
            }
        }
    }

    /**
     * Normally we get the seriesDescription from the DicomDir, if not: null or
     * empty, get the seriesDescription from the .dcm file, if existing in .dcm file.
     *
     * @param serie
     * @param attributes
     */
    private void checkSerieData(Serie serie, Attributes attributes) {
        if (StringUtils.isEmpty(serie.getSopClassUID())) {
            // has not been found in dicomdir or before in other file, so we get it from .dcm file:
            String sopClassUIDDicomFile = attributes.getString(Tag.SOPClassUID);
            if (StringUtils.isNotEmpty(sopClassUIDDicomFile)) {
                serie.setSopClassUID(sopClassUIDDicomFile);
            }
        }
        if (StringUtils.isEmpty(serie.getSeriesNumber()) || SERIES_NUMBER_0.equals(serie.getSeriesNumber())) {
            // has not been sent by PACS (case for Telemis), get it from .dcm file:
            String seriesNumberDicomFile = attributes.getString(Tag.SeriesNumber);
            if (StringUtils.isNotEmpty(seriesNumberDicomFile)) {
                serie.setSeriesNumber(seriesNumberDicomFile);
            }
        }
        // always use seriesDescription from .dcm file, e.g. in Q/R encoding errors
        // can happen, so we trust the local .dcm file the most to avoid further issues
        String seriesDescriptionDicomFile = attributes.getString(Tag.SeriesDescription);
        if (StringUtils.isNotEmpty(seriesDescriptionDicomFile)) {
            serie.setSeriesDescription(seriesDescriptionDicomFile);
        }
        DicomSerieAndInstanceAnalyzer.checkSerieIsEnhanced(serie, attributes);
        DicomSerieAndInstanceAnalyzer.checkSerieIsSpectroscopy(serie, attributes);
        if (serie.getSeriesDate() == null) {
            serie.setSeriesDate(DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.SeriesDate)));
        }
        if (serie.getIsCompressed() == null) {
            String transferSyntaxUID = attributes.getString(Tag.TransferSyntaxUID);
            serie.setIsCompressed(transferSyntaxUID != null && transferSyntaxUID.startsWith("1.2.840.10008.1.2.4"));
        }
        if (StringUtils.isEmpty(serie.getProtocolName())) {
            serie.setProtocolName(attributes.getString(Tag.ProtocolName));
        }
        // keep this check at this place: enhanced Dicom needs to be checked first
        DicomSerieAndInstanceAnalyzer.checkSerieIsMultiFrame(serie, attributes);
        if (StringUtils.isEmpty(serie.getModality())) {
            serie.setModality(attributes.getString(Tag.Modality));
        }
    }

    /**
     * Normally we get the Patient BirthDate from the DicomDir, if not: null or
     * empty, get the Patient BirthDate from the .dcm file, if existing in .dcm file
     * add it in JsonNode tree.
     *
     * @param serie
     * @param attributes
     */
    private void checkPatientData(Patient patient, Attributes attributes) {
        if (patient != null) {
            if (patient.getPatientBirthDate() == null) {
                // has not been found in dicomdir, so we get it from .dcm file:
                patient.setPatientBirthDate(DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.PatientBirthDate)));
            }
            if (StringUtils.isEmpty(patient.getPatientSex())) {
                // has not been found in dicomdir, so we get it from .dcm file:
                patient.setPatientSex(attributes.getString(Tag.PatientSex));
            }
            // we can not display this information for the pacs in select series: as info not available
            String patientIdentityRemoved = attributes.getString(Tag.PatientIdentityRemoved);
            if (StringUtils.isNotBlank(patientIdentityRemoved)) {
                if (YES.equals(patientIdentityRemoved)) {
                    patient.setPatientIdentityRemoved(true);
                    String deIdentificationMethod = attributes.getString(Tag.DeidentificationMethod);
                    patient.setDeIdentificationMethod(deIdentificationMethod);
                }
            }
        }
    }

}
