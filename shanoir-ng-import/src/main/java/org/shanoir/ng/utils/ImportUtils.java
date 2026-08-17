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

package org.shanoir.ng.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.shanoir.anonymization.anonymization.AnonymizationResult;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dicom.DicomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class
 *
 * @author jlouis
 * @author mkain
 */
public final class ImportUtils {

    private ImportUtils() { }

    private static final Logger LOG = LoggerFactory.getLogger(ImportUtils.class);

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private static final String APPLICATION_ZIP = "application/zip";

    private static final String ZIP_FILE_SUFFIX = ".zip";

    public static final String SUFFIX_DCM = ".dcm";

    private static final String UPLOAD_FILE_SUFFIX = ".upload";

    private static final SecureRandom RANDOM = new SecureRandom();

    /** The Constant KB. */
    private static final int KB = 1024;

    /** The Constant BUFFER_SIZE. */
    private static final int BUFFER_SIZE = 2 * KB;

    /**
     * Convert Iterable to List
     *
     * @param iterable
     * @return a list
     */
    public static <E> List<E> toList(Iterable<E> iterable) {
        if (iterable instanceof List) {
            return (List<E>) iterable;
        }
        ArrayList<E> list = new ArrayList<>();
        if (iterable != null) {
            for (E e : iterable) {
                list.add(e);
            }
        }
        return list;
    }

    public static boolean equalsIgnoreNull(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        if (o2 == null) {
            return o1 == null;
        }
        if (o1 instanceof AbstractEntity && o2 instanceof AbstractEntity) {
            return ((AbstractEntity) o1).getId().equals(((AbstractEntity) o2).getId());
        }
        return o1.equals(o2) || o2.equals(o1);
        // o1.equals(o2) is not equivalent to o2.equals(o1) ! For instance with
        // java.sql.Timestamp and java.util.Date
    }

    /**
     * Check if the given compressed file contains a file whith name fileName.
     *
     * @param fileName
     *            the file name
     * @param file
     *            the file
     *
     * @return true, if check zip contains file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean checkZipContainsFile(final String fileName, final File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().toUpperCase().equals(fileName.toUpperCase())) {
                zipFile.close();
                return true;
            }
        }
        zipFile.close();
        return false;
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();


            String directoryFile;
            String name;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // iterates over entries in the .zip file

                name = entry.getName();
                String filePath = destDirectory + File.separator + name;
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    // create the dir if necessary, file entry can come before directory entry where
                    // is file located
                    directoryFile = getDirectoryPart(name);
                    if (directoryFile != null) {
                        createDirectory(destDir, directoryFile);
                    }
                    extractFile(entry, zipFile, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
            }
        }
    }

    /**
     * Zips a file
     * @param fileToZip file to zip
     * @param fileName new fileName
     * @param zipOut file touget out
     * @throws IOException
     */
    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean first) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/") && !first) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else if (!first) {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, (first ? "" : fileName + "/") + childFile.getName(), zipOut, false);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * Check if sent file is of type .zip.
     *
     * @param file
     */
    public static boolean isZipFile(final MultipartFile file) {
        return file.getOriginalFilename().endsWith(ZIP_FILE_SUFFIX) || file.getContentType().equals(APPLICATION_ZIP)
                || file.getContentType().equals(APPLICATION_OCTET_STREAM);
    }

    public static File getUserImportDir(String importDir) {
        final Long userId = KeycloakUtil.getTokenUserId();
        final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
        final File userImportDir = new File(userImportDirFilePath);
        if (!userImportDir.exists()) {
            userImportDir.mkdirs(); // create if not yet existing
        } // else is wanted case, user has already its import directory
        return userImportDir;
    }

    private static void createDirectory(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists()) {
            d.mkdirs();
        }
    }

    private static String getDirectoryPart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipEntry zipIn, ZipFile zipFile, String filePath) throws IOException {
        try (InputStream in = zipFile.getInputStream(zipIn);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = in.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static File initImportJob(final ImportJobBase importJob, final String importDir) throws IOException {
        importJob.setUserId(KeycloakUtil.getTokenUserId());
        importJob.setUsername(KeycloakUtil.getTokenUserName());
        long n = createRandomLong();
        importJob.setId(Long.toString(n));
        File userImportDir = getUserImportDir(importDir);
        File importJobDir = new File(userImportDir.getAbsolutePath(), importJob.getId());
        if (!importJobDir.exists()) {
            importJobDir.mkdirs();
        } else {
            LOG.error("Error in initImportJob: importJobDir/workFolder exists.");
            throw new IOException("Error in initImportJob: importJobDir/workFolder exists.");
        }
        importJob.setWorkFolder(importJobDir.getAbsolutePath());
        return importJobDir;
    }

    public static File initImportJob(final ImportJobBase importJob, final String importDir, final MultipartFile file) throws IOException {
        File importJobDir = initImportJob(importJob, importDir);
        File uploadedFile = new File(importJobDir.getAbsolutePath() + UPLOAD_FILE_SUFFIX);
        file.transferTo(uploadedFile);
        return uploadedFile;
    }

    /**
     * This method creates a random long number.
     *
     * @return long: random number
     */
    private static long createRandomLong() {
        long n = RANDOM.nextLong();
        if (n == Long.MIN_VALUE) {
            n = 0; // corner case
        } else {
            n = Math.abs(n);
        }
        return n;
    }

    public static void moveDirectoryContents(File sourceDir, File targetDir) throws IOException {
        File[] children = sourceDir.listFiles();
        if (children == null) {
            throw new IOException("Not a directory or IO error: " + sourceDir);
        }
        for (File child : children) {
            Path dest = targetDir.toPath().resolve(child.getName());
            try {
                Files.move(child.toPath(), dest, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(child.toPath(), dest); // fallback: cross-filesystem
            }
        }
        Files.delete(sourceDir.toPath());
    }

    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[] {"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    // size of directory in bytes
    public static long getDirectorySize(Path path) {
        long size = 0;
        try (Stream<Path> walk = java.nio.file.Files.walk(path)) {
            size = walk.filter(java.nio.file.Files::isRegularFile).mapToLong(p -> {
                try {
                    return java.nio.file.Files.size(p);
                } catch (IOException e) {
                    LOG.error("Failed to get size of %s%n%s", p, e);
                    return 0L;
                }
            }).sum();
        } catch (IOException e) {
            LOG.error("IO errors %s", e);
        }
        return size;
    }

    /**
     * Create an exam from fiew attributes
     * @return the created exam
     */
    public static ExaminationDTO createExam(Long studyId, Long centerId, Long subjectId, String comment, LocalDate examDate, String subjectName) {
        // Create one examination
        ExaminationDTO examination = new ExaminationDTO();
        IdName study = new IdName();
        study.setId(studyId);
        examination.setStudy(study);

        IdName subj = new IdName();
        subj.setId(subjectId);
        subj.setName(subjectName);
        examination.setSubject(subj);

        IdName center = new IdName();
        center.setId(centerId);
        examination.setCenter(center);
        examination.setComment(comment);
        examination.setExaminationDate(examDate);

        return examination;
    }

    public static Subject createSubject(String name, Long studyId, String studyName, LocalDate birthDate, String sex, Integer imagedObjectCategory) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setStudy(new IdName(studyId, studyName));
        subject.setBirthDate(birthDate);
        subject.setSex(sex);
        subject.setImagedObjectCategory(imagedObjectCategory);
        return subject;
    }


    /**
     * Pseudonymization rewrites StudyInstanceUID, SeriesInstanceUID and
     * SOPInstanceUID directly inside the DICOM files on disk, in place -- but
     * importJob's own Patient/Study/Serie/Instance tree was built from those
     * files BEFORE that rewrite, from the original, vendor-assigned UIDs.
     * Left untouched, the importJob written to import-job.json (sent as-is
     * to ms-import, and later relied on e.g. by UploadServiceJob's
     * post-import metadata check) would reference UIDs that no longer exist
     * in the actual DICOM data. Patch the tree here, right after
     * anonymization and before the state switch to START_IMPORT_JOB, so
     * import-job.json stays consistent with what's really on disk from this
     * point on.
     *
     * SeriesInstanceUID/StudyInstanceUID/SOPInstanceUID are looked up by
     * their OLD value in the shared old->new maps produced by anonymization.
     * @throws FileNotFoundException
     */
    public static void updateImportJobInstancesWithPseudonymizedUIDs(final ImportJobBase importJob, final File importJobFolder,
            final AnonymizationResult anonymizationResult) throws FileNotFoundException {
        if (importJob.getPatient() == null) {
            return;
        }
        int updatedInstances = 0;
        int missingInstances = 0;
        Study study = importJob.getStudy();
        String newStudyUID = anonymizationResult.getStudyInstanceUIDs().get(study.getStudyInstanceUID());
        if (newStudyUID != null) {
            study.setStudyInstanceUID(newStudyUID);
        }
        if (importJob.getSeries() == null) {
            return;
        }
        for (Serie serie : importJob.getSeries()) {
            String newSeriesUID = anonymizationResult.getSeriesInstanceUIDs().get(serie.getSeriesInstanceUID());
            if (newSeriesUID != null) {
                serie.setSeriesInstanceUID(newSeriesUID);
            }
            if (serie.getInstances() == null) {
                continue;
            }
            for (Instance instance : serie.getInstances()) {
                String newSopInstanceUID = anonymizationResult.getSopInstanceUIDs().get(instance.getSopInstanceUID());
                if (newSopInstanceUID != null) {
                    instance.setSopInstanceUID(newSopInstanceUID);
                    updatedInstances++;
                } else {
                    missingInstances++;
                    LOG.warn("{}: no pseudonymized SOPInstanceUID found for instance file {}; "
                            + "importJob keeps its pre-pseudonymization UID for this instance.",
                            importJobFolder.getName(), instance.getSopInstanceUID());
                }
            }
        }
        LOG.info("{}: importJobUIDs updated for {} instance(s){}.",
                importJobFolder.getName(), updatedInstances,
                missingInstances > 0 ? (", " + missingInstances + " instance(s) could not be matched") : "");
    }

    public static File getInstanceFileByReferencedFileID(Instance instance, String folderFileAbsolutePath)
            throws FileNotFoundException {
        String instanceFilePath = DicomUtils.referencedFileIDToPath(folderFileAbsolutePath, instance.getReferencedFileID());
        return getFileFromPath(instanceFilePath);
    }

    public static File getInstanceFileByUIDs(Instance instance, Serie serie, String folderFileAbsolutePath) throws FileNotFoundException {
        StringBuilder instanceFilePathBuilder = new StringBuilder();
        instanceFilePathBuilder.append(folderFileAbsolutePath)
                .append(File.separator)
                .append(serie.getSeriesInstanceUID())
                .append(File.separator)
                .append(instance.getSopInstanceUID())
                .append(SUFFIX_DCM);
        return getFileFromPath(instanceFilePathBuilder.toString());
    }

    private static File getFileFromPath(String instanceFilePath) throws FileNotFoundException {
        File instanceFile = new File(instanceFilePath);
        if (instanceFile.exists()) {
            return instanceFile;
        } else {
            throw new FileNotFoundException(
                    "instanceFilePath: missing file: " + instanceFilePath);
        }
    }

}
