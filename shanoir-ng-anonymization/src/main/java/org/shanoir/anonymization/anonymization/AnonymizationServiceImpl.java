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

package org.shanoir.anonymization.anonymization;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anonymization serviceImpl. mkain: bug fixing done for multi-threading errors,
 * e.g. when used by server; bug fixed for identical media storage sop instance
 * uid and sop instance uid; bug fixed for invalid uid generation.
 *
 * @author ifakhfakh
 * @author mkain
 *
 */
public class AnonymizationServiceImpl implements AnonymizationService {

    private static final Logger LOG = LoggerFactory.getLogger(AnonymizationServiceImpl.class);

    private static final String PRIVATE_TAGS = "0xggggeeee";

    private static final String CURVE_DATA_TAGS = "0x50xxxxxx";

    private static final String OVERLAY_COMMENTS_TAGS = "0x60xx4000";

    private static final String OVERLAY_DATA_TAGS = "0x60xx3000";

    private static final char[] STUDY_ID_CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final UIDGeneration UID_GENERATOR = new UIDGeneration();

    private Random rand = new Random();

    private static Map<String, List<String>> tagsToDeleteForManufacturer;

    /**
     * Per-directory locks used when checking/deleting an original series folder
     * once it has been emptied by moving all of its (now renamed) files into
     * their new seriesInstanceUID folder. Needed because several files that
     * originally lived in the same folder can be processed concurrently by
     * different threads of a batch.
     */
    private static final Map<String, Object> DIR_LOCKS = new ConcurrentHashMap<>();

    @Override
    public AnonymizationResult anonymize(ArrayList<File> dicomFiles, String profile, File importJobDir) throws Exception {
        long startTime = System.currentTimeMillis();
        final int totalAmount = dicomFiles.size();
        LOG.info("Start pseudonymization: profile {} on {} DICOM files.", profile, totalAmount);
        Map<String, Profile> profiles = AnonymizationRulesSingleton.getInstance().getProfiles();
        Map<String, String> anonymizationMap = profiles.get(profile).getAnonymizationMap();
        tagsToDeleteForManufacturer = AnonymizationRulesSingleton.getInstance().getTagsToDeleteForManufacturer();

        // init here for multi-threading reasons
        Map<String, String> seriesInstanceUIDs = new HashMap<>();
        Map<String, String> frameOfReferenceUIDs = new HashMap<>();
        Map<String, String> studyInstanceUIDs = new HashMap<>();
        Map<String, String> studyIds = new HashMap<>();
        Map<String, String> sopInstanceUIDsByFilePath = new HashMap<>();

        AnonymizationStats stats = new AnonymizationStats();
        LOG.debug("anonymize : totalAmount={}", totalAmount);
        int current = 0;
        for (int i = 0; i < dicomFiles.size(); ++i) {
            final File file = dicomFiles.get(i);
            performAnonymization(file, importJobDir, anonymizationMap, false, "", "", null, seriesInstanceUIDs, frameOfReferenceUIDs,
                    studyInstanceUIDs, studyIds, sopInstanceUIDsByFilePath, stats);
            current++;
            final int currentPercent = current * 100 / totalAmount;
            LOG.debug("anonymize : anonymization current percent= {} %", currentPercent);
        }
        logInfos("End pseudonymization", startTime);
        stats.logSummary();
        return new AnonymizationResult(seriesInstanceUIDs, studyInstanceUIDs, frameOfReferenceUIDs, sopInstanceUIDsByFilePath);
    }

    @Override
    public AnonymizationResult anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientLastName,
            String patientFirstName, String patientID, String studyInstanceUID, File importJobDir) throws Exception {
        String patientName = patientLastName + "^" + patientFirstName + "^^^";
        return anonymizeForShanoir(dicomFiles, profile, patientName, patientID, studyInstanceUID, importJobDir);
    }

    @Override
    public AnonymizationResult anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientName,
            String patientID, String studyInstanceUID, File importJobDir) throws Exception {
        long startTime = System.currentTimeMillis();
        final int totalAmount = dicomFiles.size();
        LOG.info("Start pseudonymization: profile {} on {} DICOM files.", profile, totalAmount);
        LOG.info("StudyInstanceUID used from ImportJob: {}", studyInstanceUID);
        Map<String, Profile> profiles = AnonymizationRulesSingleton.getInstance().getProfiles();
        Map<String, String> anonymizationMap = profiles.get(profile).getAnonymizationMap();
        tagsToDeleteForManufacturer = AnonymizationRulesSingleton.getInstance().getTagsToDeleteForManufacturer();

        // init here for multi-threading reasons
        Map<String, String> seriesInstanceUIDs = new HashMap<>();
        Map<String, String> frameOfReferenceUIDs = new HashMap<>();
        Map<String, String> studyInstanceUIDs = new HashMap<>();
        Map<String, String> studyIds = new HashMap<>();
        Map<String, String> sopInstanceUIDs = new HashMap<>();

        AnonymizationStats stats = new AnonymizationStats();
        LOG.debug("anonymize : totalAmount={}", totalAmount);
        int current = 0;
        for (int i = 0; i < dicomFiles.size(); ++i) {
            final File file = dicomFiles.get(i);
            performAnonymization(file, importJobDir, anonymizationMap, true, patientName, patientID, studyInstanceUID,
                    seriesInstanceUIDs, frameOfReferenceUIDs, studyInstanceUIDs, studyIds,
                    sopInstanceUIDs, stats);
            current++;
            final int currentPercent = current * 100 / totalAmount;
            LOG.debug("anonymize : anonymization current percent= {} %", currentPercent);
        }
        logInfos("End pseudonymization", startTime);
        stats.logSummary();
        return new AnonymizationResult(seriesInstanceUIDs, studyInstanceUIDs, frameOfReferenceUIDs, sopInstanceUIDs);
    }

    private void logInfos(final String methodName, long startTime) {
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        LOG.info("{}, duration (ms): {}", methodName, elapsedTime);
    }

    private void anonymizePatientMetaData(Attributes attributes, String patientName, String patientID,
            String patientBirthDate, AnonymizationStats stats) {
        String oldPatientName = getStringValueSafe(attributes, Tag.PatientName);
        anonymizeTagAccordingToVR(attributes, Tag.PatientName, patientName);
        recordAndTrace(stats, false, Tag.PatientName, "PATIENT_INFO", oldPatientName, patientName);

        String oldPatientID = getStringValueSafe(attributes, Tag.PatientID);
        anonymizeTagAccordingToVR(attributes, Tag.PatientID, patientID);
        recordAndTrace(stats, false, Tag.PatientID, "PATIENT_INFO", oldPatientID, patientID);

        // patient birth date
        String oldPatientBirthDate = getStringValueSafe(attributes, Tag.PatientBirthDate);
        String newPatientBirthDate;
        if (patientBirthDate != null && patientBirthDate.length() >= 4) {
            newPatientBirthDate = patientBirthDate.substring(0, 4) + "01" + "01";
        } else {
            newPatientBirthDate = "19000101";
        }
        anonymizeTagAccordingToVR(attributes, Tag.PatientBirthDate, newPatientBirthDate);
        recordAndTrace(stats, false, Tag.PatientBirthDate, "PATIENT_INFO", oldPatientBirthDate, newPatientBirthDate);
    }

    /**
     * Perform the anonymization for an DICOM image according to chosen profile. To
     * have a consistent DICOM file: the attribute in the "header" (0002,0003) Media
     * Storage SOP Instance UID and the attribute in the "body" (0008 0018) SOP
     * Instance UID have to match. If they do not match the PACS returns the
     * following error: SOP Instance UID in Dataset [xxx] differs from Affected SOP
     * Instance UID [yyy]. The problem is, that when doing the dcmSend, the tool
     * reads the SOP Instance UID from the meta-information/header and sends the
     * file with a C-STORE request and an Affected SOP Instance UID (== header) in
     * the request header. If the file arrives in the PACS, the SOP Instance UID in
     * the file does not match with the request header and this is refused.
     *
     * Further does each part of an UID has to start with a non-zero value, see
     * UIDGeneration code.
     *
     * Once anonymization of the DICOM attributes is complete and the file has
     * been written back to disk, the file itself is renamed to
     * "<SOPInstanceUID>.dcm" AND moved into a folder named after its (possibly
     * newly generated) SeriesInstanceUID, created directly under importJobDir
     * (skipping the move if a file with that name already exists in the target
     * folder). Once a file has been moved out, if its original parent folder is
     * left empty, that folder is deleted. sopInstanceUIDsByFilePath is keyed by
     * the file's ORIGINAL SOPInstanceUID so callers can still resolve it from an
     * untouched, pre-rename referencedFileID.
     *
     * Overload kept for backward compatibility with existing external callers: it
     * simply runs the anonymization with a fresh, single-file stats tracker that
     * is discarded afterwards. Prefer the overload below in batch contexts, so
     * counts (and the final report) can be aggregated across all files.
     *
     * @param dicomFile
     *                  the image path
     * @param importJobDir
     *                  the root folder of the import job; new per-series folders
     *                  are created directly under it
     * @param profile
     *                  anonymization profile
     * @throws Exception
     */
    public void performAnonymization(final File dicomFile, final File importJobDir, Map<String, String> anonymizationMap,
            boolean isShanoirAnonymization,
            String patientName, String patientID, String studyInstanceUID, Map<String, String> seriesInstanceUIDs,
            Map<String, String> frameOfReferenceUIDs,
            Map<String, String> studyInstanceUIDs, Map<String, String> studyIds,
            Map<String, String> sopInstanceUIDs) throws Exception {
        performAnonymization(dicomFile, importJobDir, anonymizationMap, isShanoirAnonymization, patientName, patientID,
                studyInstanceUID, seriesInstanceUIDs, frameOfReferenceUIDs, studyInstanceUIDs, studyIds,
                sopInstanceUIDs, new AnonymizationStats());
    }

    /**
     * Same as the overload above, but accepts an {@link AnonymizationStats} so
     * that modification counts can be accumulated across an entire batch of
     * files and reported once at the end (see {@link #anonymize} /
     * {@link #anonymizeForShanoir}).
     */
    public void performAnonymization(final File dicomFile, final File importJobDir, Map<String, String> anonymizationMap,
            boolean isShanoirAnonymization,
            String patientName, String patientID, String studyInstanceUID, Map<String, String> seriesInstanceUIDs,
            Map<String, String> frameOfReferenceUIDs,
            Map<String, String> studyInstanceUIDs, Map<String, String> studyIds,
            Map<String, String> sopInstanceUIDs, AnonymizationStats stats)
            throws Exception {
        DicomInputStream din = null;
        DicomOutputStream dos = null;
        try {
            din = new DicomInputStream(dicomFile);

            /**
             * DICOM "header"/meta-information fields: read tags
             */
            Attributes metaInformationAttributes = din.readFileMetaInformation();
            for (int tagInt : metaInformationAttributes.tags()) {
                String tagString = tagToHexString(tagInt);
                if (anonymizationMap.containsKey(tagString)) {
                    final String action = anonymizationMap.get(tagString);
                    anonymizeTag(tagInt, action, metaInformationAttributes, false, stats);
                }
            }
            final String mediaStorageSOPInstanceUIDGenerated = metaInformationAttributes
                    .getString(Tag.MediaStorageSOPInstanceUID);

            /**
             * MK: Read entire dataset with PixelData. Attention: do NOT change here
             * to only readDatasetUntilPixelData(), as the modified DICOM image will
             * MISS the PixelData!
             * It writes into the image, what has been read from the image,
             * that is why we read the entire dataset here
             * (I made the mistake already twice...).
             */
            Attributes datasetAttributes = din.readDataset();
            String sopInstanceUID = datasetAttributes.getString(Tag.SOPInstanceUID);

            // Make sure the PatientName and PatientID exist in the dataset attributes.
            if (!datasetAttributes.contains(Tag.PatientID))
                datasetAttributes.setNull(Tag.PatientID, VR.LO);
            if (!datasetAttributes.contains(Tag.PatientName))
                datasetAttributes.setNull(Tag.PatientName, VR.PN);

            // temporarily keep the patient credentials in memory to search in private tags
            String patientNameAttr = datasetAttributes.getString(Tag.PatientName);
            String[] patientNameArrayAttr = null;
            if (patientNameAttr != null && !patientNameAttr.isEmpty()) {
                patientNameArrayAttr = patientNameAttr.split("\\^");
            }

            String patientIDAttr = datasetAttributes.getString(Tag.PatientID);
            String patientBirthNameAttr = datasetAttributes.getString(Tag.PatientBirthName);
            // temporarily keep the patient birth date for isShanoirAnonymization
            String patientBirthDateAttr = datasetAttributes.getString(Tag.PatientBirthDate);

            String studyInstanceUIDVendor = datasetAttributes.getString(Tag.StudyInstanceUID);
            if (studyInstanceUID != null && !studyInstanceUID.isEmpty()) {
                LOG.debug("StudyInstanceUID used from ImportJob: {}", studyInstanceUID);
                studyInstanceUIDs.put(studyInstanceUIDVendor, studyInstanceUID);
            }

            String manufacturer = datasetAttributes.getString(Tag.Manufacturer);
            Set<String> tagsToDeleteForCurrentManufacturer = getTagsToDeleteForManufacturer(manufacturer);

            // anonymize DICOM files according to selected profile
            for (int tagInt : datasetAttributes.tags()) {
                // Group number is the top 16 bits of the tag; odd group => private tag.
                // Avoids String.format + substring + Integer.decode per tag.
                int group = tagInt >>> 16;
                if ((group & 1) == 1) {
                    String action = anonymizationMap.get(PRIVATE_TAGS);
                    String value = datasetAttributes.getString(tagInt);
                    // only act below in case of K: keep, if X: delete for private tags, no need
                    if (value != null && !value.isEmpty() && action.equals("K")) {
                        action = checkForPHIInPrivateTags(patientNameArrayAttr, patientIDAttr, patientBirthNameAttr,
                                patientBirthDateAttr, tagInt, value, action);
                        action = handleTagsToDeleteForManufacturer(tagToHexString(tagInt),
                                tagsToDeleteForCurrentManufacturer, action);
                    }
                    anonymizeTag(tagInt, action, datasetAttributes, true, stats);
                    // even: public tags
                } else {
                    String tagString = tagToHexString(tagInt);
                    if (anonymizationMap.containsKey(tagString)) {
                        switch (tagInt) {
                            case Tag.SOPInstanceUID ->
                                anonymizeSOPInstanceUID(tagInt, datasetAttributes, mediaStorageSOPInstanceUIDGenerated,
                                        stats);
                            case Tag.SeriesInstanceUID ->
                                anonymizeUID(tagInt, datasetAttributes, seriesInstanceUIDs, stats);
                            case Tag.FrameOfReferenceUID ->
                                anonymizeUID(tagInt, datasetAttributes, frameOfReferenceUIDs, stats);
                            case Tag.StudyInstanceUID ->
                                anonymizeUID(tagInt, datasetAttributes, studyInstanceUIDs, stats);
                            case Tag.StudyID -> anonymizeStudyId(tagInt, datasetAttributes, studyIds, stats);
                            default -> {
                                final String action = anonymizationMap.get(tagString);
                                anonymizeTag(tagInt, action, datasetAttributes, false, stats);
                            }
                        }
                    } else {
                        if (0x50000000 <= tagInt && tagInt <= 0x50FFFFFF) {
                            final String action = anonymizationMap.get(CURVE_DATA_TAGS);
                            anonymizeTag(tagInt, action, datasetAttributes, false, stats);
                        } else if (0x60004000 <= tagInt && tagInt <= 0x60FF4000) {
                            final String action = anonymizationMap.get(OVERLAY_COMMENTS_TAGS);
                            anonymizeTag(tagInt, action, datasetAttributes, false, stats);
                        } else if (0x60003000 <= tagInt && tagInt <= 0x60FF3000) {
                            final String action = anonymizationMap.get(OVERLAY_DATA_TAGS);
                            anonymizeTag(tagInt, action, datasetAttributes, false, stats);
                        }
                    }
                }
            }

            // Special anonymization of patient data if isShanoirAnonymization
            if (isShanoirAnonymization) {
                anonymizePatientMetaData(datasetAttributes, patientName, patientID, patientBirthDateAttr, stats);
            }

            // Determine the file's FINAL SOPInstanceUID (whatever action applied, or
            // none at all). Used to correlate this file back to the Instance
            // it came from.
            String finalSopInstanceUID = datasetAttributes.getString(Tag.SOPInstanceUID);
            // Determine the file's FINAL SeriesInstanceUID (regenerated if the tag was
            // anonymized above, unchanged otherwise). Used to pick/create the target
            // per-series folder under importJobDir.
            String finalSeriesInstanceUID = datasetAttributes.getString(Tag.SeriesInstanceUID);

            LOG.debug("finish anonymization: begin storage");
            dos = new DicomOutputStream(dicomFile);
            dos.writeDataset(metaInformationAttributes, datasetAttributes);
            dos.close();
            dos = null;
            // Rename the file to "<SOPInstanceUID>.dcm" and move it into
            // "<importJobDir>/<SeriesInstanceUID>/", now that the anonymized content
            // has been fully written and flushed to disk.
            if (finalSopInstanceUID != null) {
                sopInstanceUIDs.put(sopInstanceUID, finalSopInstanceUID);
                File originalParentDir = dicomFile.getParentFile();
                File targetDir = originalParentDir;
                if (finalSeriesInstanceUID != null && !finalSeriesInstanceUID.isEmpty() && importJobDir != null) {
                    File seriesDir = new File(importJobDir, finalSeriesInstanceUID);
                    if (!seriesDir.exists()) {
                        seriesDir.mkdirs();
                    }
                    if (seriesDir.exists()) {
                        targetDir = seriesDir;
                    } else {
                        LOG.error("performAnonymization : could not create series folder {}, keeping file in {}",
                                seriesDir.getAbsolutePath(), originalParentDir.getAbsolutePath());
                    }
                }
                File renamedFile = new File(targetDir, finalSopInstanceUID + ".dcm");
                if (renamedFile.exists()) {
                    LOG.warn(
                            "performAnonymization : skipping rename, target file {} already exists (SOPInstanceUID collision for {})",
                            renamedFile.getAbsolutePath(), dicomFile.getAbsolutePath());
                } else if (!dicomFile.renameTo(renamedFile)) {
                    LOG.error("performAnonymization : could not rename/move file {} to {}",
                            dicomFile.getAbsolutePath(), renamedFile.getAbsolutePath());
                } else if (!targetDir.equals(originalParentDir)) {
                    deleteDirectoryIfEmpty(originalParentDir, importJobDir);
                }
            }
            LOG.debug("finish anonymization: end storage");
        } catch (final IOException exc) {
            LOG.error("performAnonymization : error while anonymizing file " + dicomFile.toString() + " : ", exc);
        } finally {
            try {
                if (din != null) {
                    din.close();
                }
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Deletes {@code dir} if it is now empty, once a file has been moved out of
     * it into its new per-series folder. Synchronized per-directory (via
     * {@link #DIR_LOCKS}) because several files originally living in the same
     * folder can be processed concurrently by different threads of a batch, and
     * we must avoid one thread deleting the folder while another is still about
     * to move a sibling file out of it, or double-deleting it.
     *
     * @param dir
     *            the file's original parent folder, now possibly empty
     * @param importJobDir
     *            the import job root; never deleted even if "empty"
     */
    private void deleteDirectoryIfEmpty(File dir, File importJobDir) {
        if (dir == null || dir.equals(importJobDir)) {
            return;
        }
        String key = dir.getAbsolutePath();
        Object lock = DIR_LOCKS.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            File[] remaining = dir.listFiles();
            if (remaining != null && remaining.length == 0) {
                if (dir.delete()) {
                    LOG.debug("performAnonymization : deleted now-empty folder {}", dir.getAbsolutePath());
                    DIR_LOCKS.remove(key);
                } else {
                    LOG.warn("performAnonymization : could not delete now-empty folder {}", dir.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Builds the "0xAAAABBBB" hex representation of a tag.
     * NOTE: uppercase hex digits are used deliberately, to match the casing of
     * the tag keys defined in the anonymization properties files
     * (AnonymizationRulesSingleton).
     */
    private static String tagToHexString(int tagInt) {
        String hex = Integer.toHexString(tagInt).toUpperCase();
        StringBuilder sb = new StringBuilder(10).append("0x");
        for (int i = hex.length(); i < 8; i++) {
            sb.append('0');
        }
        return sb.append(hex).toString();
    }

    /**
     * Resolves the set of tags to delete for a given manufacturer once, so the
     * per-tag hot path only does an O(1) Set lookup instead of a List scan plus
     * repeated Attributes.getString(Manufacturer) calls.
     */
    private Set<String> getTagsToDeleteForManufacturer(String manufacturer) {
        List<String> tagsToDelete = tagsToDeleteForManufacturer.get(manufacturer);
        return tagsToDelete != null ? new HashSet<>(tagsToDelete) : null;
    }

    /**
     * Handle tags to delete for manufacturer here
     *
     * @param tagString
     * @param tagsToDeleteForCurrentManufacturer
     * @param action
     * @return
     */
    private String handleTagsToDeleteForManufacturer(String tagString, Set<String> tagsToDeleteForCurrentManufacturer,
            String action) {
        if (tagsToDeleteForCurrentManufacturer != null && tagsToDeleteForCurrentManufacturer.contains(tagString)) {
            return "X";
        }
        return action;
    }

    private String checkForPHIInPrivateTags(String[] patientNameArrayAttr, String patientIDAttr,
            String patientBirthNameAttr,
            String patientBirthDateAttr, int tagInt, String value, String action) throws Exception {
        // check for patient name elements
        for (int i = 0; i < patientNameArrayAttr.length; i++) {
            String patientNamePart = patientNameArrayAttr[i];
            if (checkTagContainsValuePHI(tagInt, value, patientNamePart)) {
                return "X";
            }
        }
        if (checkTagContainsValuePHI(tagInt, value, patientIDAttr)
                || checkTagContainsValuePHI(tagInt, value, patientBirthNameAttr)
                || checkTagContainsValuePHI(tagInt, value, patientBirthDateAttr)) {
            return "X";
        }
        return action;
    }

    private boolean checkTagContainsValuePHI(int tagInt, String value, String compareValuePHI) throws Exception {
        if (compareValuePHI != null && !compareValuePHI.isEmpty() && compareValuePHI.length() > 2
                && value.contains(compareValuePHI)) {
            LOG.warn("Potential PHI found in private tag (--> remove/delete): " + tagInt + ": " + value);
            return true;
        }
        return false;
    }

    /**
     * Tag Anonymization
     *
     * @param tagInt
     *                   : the tag to anonymize
     * @param action
     *                   : the action letter to apply
     * @param attributes
     *                   : the list of dicom attributes to modify
     * @param isPrivate
     *                   : whether tagInt is a private (odd group) DICOM tag, for
     *                   reporting purposes
     * @param stats
     *                   : accumulates modification counts for the summary report
     */
    private void anonymizeTag(Integer tagInt, String action, Attributes attributes, boolean isPrivate,
            AnonymizationStats stats) {
        String value = getFinalValueForTag(action);
        if (value == null) {
            String oldValue = getStringValueSafe(attributes, tagInt);
            attributes.remove(tagInt);
            recordAndTrace(stats, isPrivate, tagInt, "DELETED", oldValue, null);
        } else if (!"KEEP".equals(value)) {
            String oldValue = getStringValueSafe(attributes, tagInt);
            anonymizeTagAccordingToVR(attributes, tagInt, value);
            recordAndTrace(stats, isPrivate, tagInt, typeLabelForAction(action), oldValue, value);
        }
        // action "K" (KEEP): tag left untouched on purpose, not counted as a
        // modification, and no old/new value to trace
    }

    /**
     * Maps an anonymization action letter to a human-readable modification type,
     * used both for the summary report and the per-tag trace log.
     */
    private String typeLabelForAction(String action) {
        if ("Z".equals(action)) {
            return "BLANKED";
        } else if ("D".equals(action)) {
            return "DUMMY";
        } else if ("U".equals(action)) {
            return "UID_REGENERATED";
        }
        return "MODIFIED";
    }

    /**
     * Reads a tag's current value as a String for tracing purposes, without
     * throwing for VRs that don't support a meaningful String representation
     * (e.g. binary VRs like OB). Only used right before a tag is overwritten or
     * removed, so it's always evaluated against the tag's original,
     * pre-anonymization
     * value.
     */
    private static String getStringValueSafe(Attributes attributes, int tagInt) {
        try {
            return attributes.getString(tagInt);
        } catch (Exception e) {
            return "<unreadable value>";
        }
    }

    /**
     * Records the modification in the stats (INFO-level, type only, no PHI) and,
     * if DEBUG is enabled, additionally traces the actual old -> new value change.
     * The old/new value trace is DEBUG-only and off by default; enable DEBUG for
     * this logger only when you actually need to inspect specific value changes,
     * since it will surface PHI (e.g. patient name, birth date) in the logs.
     */
    private void recordAndTrace(AnonymizationStats stats, boolean isPrivate, int tagInt, String type,
            String oldValue, String newValue) {
        stats.record(isPrivate, type);
        LOG.debug("Tag {} ({}) modified - {}", tagToHexString(tagInt), isPrivate ? "private" : "public", type);
        LOG.debug("Tag {} ({}) [{}] value changed: '{}' -> '{}'", tagToHexString(tagInt),
                    isPrivate ? "private" : "public", type, oldValue, newValue);
    }

    private void anonymizeSOPInstanceUID(int tagInt, Attributes attributes, String mediaStorageSOPInstanceUID,
            AnonymizationStats stats) {
        String oldValue = getStringValueSafe(attributes, tagInt);
        anonymizeTagAccordingToVR(attributes, tagInt, mediaStorageSOPInstanceUID);
        recordAndTrace(stats, false, tagInt, "UID_REGENERATED", oldValue, mediaStorageSOPInstanceUID);
    }

    private void anonymizeStudyId(int tagInt, Attributes attributes, Map<String, String> studyIds,
            AnonymizationStats stats) {
        String oldValue = getStringValueSafe(attributes, tagInt);
        String value;
        if (studyIds != null && studyIds.size() != 0 && studyIds.get(attributes.getString(tagInt)) != null) {
            value = studyIds.get(attributes.getString(tagInt));
        } else {
            StringBuilder sb = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                char c = STUDY_ID_CHARS[rand.nextInt(STUDY_ID_CHARS.length)];
                sb.append(c);
            }
            value = sb.toString();
            LOG.info("New StudyID generated for DICOM study/exam: {}", value);
            studyIds.put(attributes.getString(tagInt), value);
        }
        anonymizeTagAccordingToVR(attributes, tagInt, value);
        recordAndTrace(stats, false, tagInt, "STUDY_ID_REGENERATED", oldValue, value);
    }

    /**
     * Get the anonymized value of the tag
     *
     * @param action
     *               : the action letter to apply
     * @return
     */
    private String getFinalValueForTag(final String action) {
        String result = "";
        if (action != null) {
            if (action.equals("X")) {
                result = null;
            } else if (action.equals("Z")) {
                result = "";
            } else if (action.equals("D")) {
                result = new BigInteger(130, SECURE_RANDOM).toString(32);
            } else if (action.equals("U")) {
                String newUID = null;
                try {
                    newUID = UID_GENERATOR.getNewUID();
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
                result = newUID;
            } else if (action.equals("K")) {
                result = "KEEP";
            }
        }
        return result;
    }

    /**
     * anonymize Tag According To its VR
     *
     * @param attributes
     *                   : the list of dicom attributes to modify
     * @param tag
     *                   : the tag to anonymize
     * @param value
     *                   : the new value of the tag after anonymization
     */
    private void anonymizeTagAccordingToVR(Attributes attributes, int tag, String value) {
        VR vr = attributes.getVR(tag);
        if (vr == null) {
            return;
        }
        // VR.AT = Attribute Tag
        // VR.SL = Signed Long || VR.UL = Unsigned Long
        // VR.SS = Signed Short || VR.US = Unsigned Short
        if (vr.equals(VR.SL) || vr.equals(VR.UL) || vr.equals(VR.AT) || vr.equals(VR.SS) || vr.equals(VR.US)) {
            Integer iValue = Integer.decode(value);
            attributes.setInt(tag, vr, iValue);
        } else if (vr.equals(VR.FD)) { // VR.FD = Floating Point Double
            Double dValue = Double.valueOf(value);
            attributes.setDouble(tag, vr, dValue);
        } else if (vr.equals(VR.FL)) { // VR.FL = Floating Point Single
            Float fValue = Float.valueOf(value);
            attributes.setFloat(tag, vr, fValue);
        } else if (vr.equals(VR.OB)) { // VR.OB = Other Byte String
            byte[] b = new byte[1];
            attributes.setBytes(tag, vr, b);
        } else if (vr.equals(VR.SQ) || vr.equals(VR.UN)) { // VR.SQ = Sequence of Items || VR.UN = Unknown
            attributes.setNull(tag, vr);
        } else if (vr.equals(VR.AE) || vr.equals(VR.AS) || vr.equals(VR.CS) || vr.equals(VR.DA) || vr.equals(VR.DS)
                || vr.equals(VR.DT) || vr.equals(VR.IS) || vr.equals(VR.LO) || vr.equals(VR.LT) || vr.equals(VR.OW)
                || vr.equals(VR.PN) || vr.equals(VR.SH) || vr.equals(VR.ST) || vr.equals(VR.TM) || vr.equals(VR.UI)
                || vr.equals(VR.UT) || vr.equals(VR.OF)) {
            // Unlimited string:
            // VR.AE = Age String
            // VR.AS = Application Entity
            // VR.CS = Code String
            // VR.DA = Date
            // VR.DS = Date Time
            // VR.DT = Decimal String
            // VR.IS = Integer String
            // VR.LO = Long String
            // VR.LT = Long Text
            // VR.OF = Other Float String
            // VR.OW = Other Word String
            // VR.PN = Person Name
            // VR.SH = Short String
            // VR.ST = Short Text
            // VR.TM = Time
            // VR.UI = Unique Identifier (UID)
            // VR.UT = Unlimited Text
            attributes.setString(tag, vr, value);
        } else {
            attributes.setString(tag, vr, value);
        }

        // N.B.: Doesn't exist in the library:
        // VR.UR = Universal Resource Identifier or Universal
        // Resource Locator (URI/URL)
        // VR.OD = Other Double String
    }

    private void anonymizeUID(int tagInt, Attributes attributes, Map<String, String> uids, AnonymizationStats stats) {
        String oldValue = getStringValueSafe(attributes, tagInt);
        String value;
        if (uids != null && uids.size() != 0
                && uids.get(oldValue) != null) {
            value = uids.get(oldValue);
            // We log only concerning the studyInstanceUID
            if (Tag.StudyInstanceUID == tagInt) {
                LOG.debug("Existing StudyInstanceUID reused: {}", value);
            }
        } else {
            String newUID = null;
            try {
                newUID = UID_GENERATOR.getNewUID();
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
            value = newUID;
            if (Tag.StudyInstanceUID == tagInt) {
                LOG.info("New StudyInstanceUID generated for DICOM study/exam: {}", newUID);
            }
            uids.put(oldValue, value);
        }
        anonymizeTagAccordingToVR(attributes, tagInt, value);
        recordAndTrace(stats, false, tagInt, "UID_REGENERATED", oldValue, value);
    }

    /**
     * Thread-safe counters tracking how many DICOM tags were modified during an
     * anonymization run, broken down by modification type (DELETED, BLANKED,
     * DUMMY, UID_REGENERATED, STUDY_ID_REGENERATED, PATIENT_INFO) and by whether
     * the tag was a public or a private DICOM tag. A single instance can be
     * shared across all files of a batch (see
     * {@link AnonymizationServiceImpl#anonymize}) to produce one aggregated
     * report at the end of the job.
     *
     * Uses AtomicLong/ConcurrentHashMap rather than plain counters because
     * {@link AnonymizationServiceImpl#performAnonymization} is public API and
     * may be invoked concurrently by external multi-threaded callers (see the
     * class-level multi-threading note above).
     */
    public static final class AnonymizationStats {

        private final AtomicLong totalModified = new AtomicLong();
        private final Map<String, AtomicLong> publicByType = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> privateByType = new ConcurrentHashMap<>();

        private void record(boolean isPrivate, String type) {
            totalModified.incrementAndGet();
            (isPrivate ? privateByType : publicByType)
                    .computeIfAbsent(type, t -> new AtomicLong())
                    .incrementAndGet();
        }

        public long getTotalModified() {
            return totalModified.get();
        }

        /**
         * Logs the aggregated report at INFO level: total tags modified, then a
         * breakdown by type for public tags and for private tags.
         */
        public void logSummary() {
            LOG.info("Pseudonymization report: {} tag(s) modified in total.", totalModified.get());
            logScope("public", publicByType);
            logScope("private", privateByType);
        }

        private void logScope(String scope, Map<String, AtomicLong> byType) {
            long scopeTotal = byType.values().stream().mapToLong(AtomicLong::get).sum();
            LOG.info("  {} tags modified: {}", scope, scopeTotal);
            byType.forEach((type, count) -> LOG.info("    - {}: {}", type, count.get()));
        }
    }

}