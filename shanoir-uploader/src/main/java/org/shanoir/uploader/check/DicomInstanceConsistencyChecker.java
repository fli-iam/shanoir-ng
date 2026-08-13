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

package org.shanoir.uploader.check;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.dicom.DicomUtils;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compares local, on-disk DICOM instances against the DICOM instances
 * persisted for a given examination on the server, via the server's
 * DICOMWeb API.
 *
 * @author mkain
 */
public class DicomInstanceConsistencyChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DicomInstanceConsistencyChecker.class);

    private static final int METADATA_CHECK_PARALLELISM = 8;

    private final ShanoirUploaderServiceClient client;

    public DicomInstanceConsistencyChecker(ShanoirUploaderServiceClient client) {
        this.client = client;
    }

    public List<Patient> parseLocalFolder(File importJobFolder, boolean deleteGeneratedDicomDir) throws IOException {
        return ImportUtils.getPatientsFromDir(importJobFolder, deleteGeneratedDicomDir);
    }

    /**
     * Same as {@link #checkImportJob(File, String, boolean, boolean)} but
     * takes an already-parsed patient list, to avoid re-parsing the local
     * DICOM folder (and regenerating its DICOMDIR) on every retry of an
     * async server-side import.
     */
    public int checkImportJob(List<Patient> patients, File importJobFolder, String examinationUID,
            boolean compareTags, boolean deleteAfterCheck) throws Exception {
        int numberOfInstances = 0;
        if (patients != null) {
            for (Patient patient : patients) {
                for (Study study : patient.getStudies()) {
                    for (Serie serie : study.getSeries()) {
                        List<Instance> instances = serie.getInstances();
                        if (instances == null) {
                            continue;
                        }
                        for (Iterator<Instance> instancesIt = instances.iterator(); instancesIt.hasNext();) {
                            numberOfInstances = checkInstance(importJobFolder, examinationUID, numberOfInstances,
                                    serie, instancesIt, compareTags, deleteAfterCheck);
                        }
                    }
                }
            }
        }
        return numberOfInstances;
    }

    public int checkImportJob(File importJobFolder, String examinationUID, boolean compareTags,
            boolean deleteAfterCheck) throws Exception {
        List<Patient> patients = parseLocalFolder(importJobFolder, true);
        return checkImportJob(patients, importJobFolder, examinationUID, compareTags, deleteAfterCheck);
    }

    private int checkInstance(File importJobFolder, String examinationUID, int numberOfInstances, Serie serie,
            Iterator<Instance> instancesIt, boolean compareTags, boolean deleteAfterCheck) throws Exception {
        Instance instance = instancesIt.next();
        String instanceFilePath = DicomUtils.referencedFileIDToPath(
                importJobFolder.getAbsolutePath(), instance.getReferencedFileID());
        File instanceFile = new File(instanceFilePath);
        if (!instanceFile.exists()) {
            LOG.error("Serie: " + serie.getSeriesDescription()
                    + ", DICOM instance not found locally: " + instanceFilePath);
            throw new FileNotFoundException(instanceFilePath);
        }
        try (DicomInputStream dIn = new DicomInputStream(instanceFile)) {
            Attributes localInstance = dIn.readDataset();
            Attributes remoteInstance = client.getDicomInstance(
                    examinationUID, serie.getSeriesInstanceUID(), instance.getSopInstanceUID());
            if (remoteInstance == null) {
                throw new Exception("Serie: " + serie.getSeriesDescription()
                        + ", DICOM instance not found on server: " + instance.getSopInstanceUID());
            }
            boolean attributesEqual = !compareTags || compareAttributes(localInstance, remoteInstance);
            byte[] pixelDataLocal = localInstance.getBytes(Tag.PixelData);
            byte[] pixelDataRemote = remoteInstance.getBytes(Tag.PixelData);
            boolean pixelsEqual = java.util.Arrays.equals(pixelDataLocal, pixelDataRemote);
            if (!attributesEqual || !pixelsEqual) {
                LOG.error("Serie: " + serie.getSeriesDescription() + ", error in DICOM instance: " + instanceFilePath);
                throw new Exception("DICOM instance comparison issue: tags(" + attributesEqual
                        + "), pixel(" + pixelsEqual + ")");
            }
            if (deleteAfterCheck) {
                deleteInstanceFileAndSerieFolder(importJobFolder, instanceFile);
            }
            numberOfInstances++;
        }
        return numberOfInstances;
    }

    private boolean compareAttributes(Attributes localAttributes, Attributes remoteAttributes) {
        if (localAttributes.size() != remoteAttributes.size()) {
            LOG.error("Number of tags differ.");
            return false;
        }
        int[] localTags = localAttributes.tags();
        for (int tag : localTags) {
            if (!remoteAttributes.contains(tag)) {
                LOG.error("Missing tag in second file: " + TagUtils.toString(tag));
                return false;
            }
            String localValue = localAttributes.getString(tag, null);
            String remoteValue = remoteAttributes.getString(tag, null);
            if (localValue == null && remoteValue == null) {
                continue;
            }
            if (localValue == null || remoteValue == null || !localValue.equals(remoteValue)) {
                LOG.error("Tag differs: " + TagUtils.toString(tag)
                        + " | " + localValue + " != " + remoteValue);
                return false;
            }
        }
        return true;
    }

    private void deleteInstanceFileAndSerieFolder(File importJobFolder, File instanceFile) {
        if (instanceFile.getParentFile().equals(importJobFolder)) {
            FileUtils.deleteQuietly(instanceFile);
        } else {
            FileUtils.deleteQuietly(instanceFile);
            File serieFolder = instanceFile.getParentFile();
            File[] remainingFiles = serieFolder.listFiles();
            if (remainingFiles == null || remainingFiles.length == 0) {
                FileUtils.deleteQuietly(serieFolder);
            }
        }
    }

    public boolean checkImportJobMetadataOnSeries(ImportJobBase importJob, String examinationUID) throws Exception {
        Map<String, Integer> localCountsBySeriesInstanceUID = new HashMap<String, Integer>();
        for (Serie serie : importJob.getSeries()) {
            List<Instance> instances = serie.getInstances();
            if (instances == null) {
                continue;
            }
            localCountsBySeriesInstanceUID.put(serie.getSeriesInstanceUID(), instances.size());
        }
        return client.checkSeriesInstanceCounts(examinationUID, localCountsBySeriesInstanceUID);
    }

    
    /**
     * Lightweight, metadata-only counterpart to
     * {@link #checkImportJob(List, File, String, boolean, boolean)}: pings each
     * instance's DICOMWeb metadata endpoint (no PixelData, no local DICOM file
     * access at all — only the UIDs already present in the parsed tree are
     * needed) to make sure it actually landed on the server. Meant to run, by
     * default, right after ms-datasets reports FINISHED, to catch a bad import
     * immediately — well before the optional, much heavier pixel-by-pixel
     * {@link #checkImportJob} run later by ExaminationConsistencyServiceJob.
     * Checks run concurrently (bounded pool) since this sits on the upload
     * state machine's critical path and must stay fast.
     */
    public int checkImportJobMetadataOnInstances(ImportJobBase importJob, String examinationUID) throws Exception {
        List<Callable<Boolean>> checks = new ArrayList<>();
        List<String> descriptions = new ArrayList<>(); // parallel-indexed, for error messages
        for (Serie serie : importJob.getSeries()) {
            List<Instance> instances = serie.getInstances();
            if (instances == null) {
                continue;
            }
            for (Instance instance : instances) {
                checks.add(() -> client.checkDicomInstanceMetadata(
                        examinationUID, serie.getSeriesInstanceUID(), instance.getSopInstanceUID()));
                descriptions.add(serie.getSeriesDescription() + " / " + instance.getSopInstanceUID());
            }
        }
        if (checks.isEmpty()) {
            return 0;
        }
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(METADATA_CHECK_PARALLELISM, checks.size()));
        try {
            List<Future<Boolean>> futures = executor.invokeAll(checks);
            int checked = 0;
            for (int i = 0; i < futures.size(); i++) {
                boolean found;
                try {
                    found = futures.get(i).get();
                } catch (ExecutionException e) {
                    throw (e.getCause() instanceof Exception cause) ? cause : e;
                }
                if (!found) {
                    throw new Exception("DICOM instance not found on server (metadata check): " + descriptions.get(i));
                }
                checked++;
            }
            return checked;
        } finally {
            executor.shutdown();
        }
    }

}