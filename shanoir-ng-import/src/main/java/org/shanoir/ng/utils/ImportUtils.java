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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.files.SafeFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    private static final String FILE_POINT = ".";

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
        if (o1 instanceof AbstractEntity entity && o2 instanceof AbstractEntity entity1) {
            return entity.getId().equals(entity1.getId());
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
        Path destDir = Paths.get(destDirectory).toAbsolutePath().normalize();
        Files.createDirectories(destDir);
        try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // the entry names come from the uploaded file: they must stay inside the destination folder ("zip slip")
                Path path;
                try {
                    if (entry.isDirectory() && destDir.resolve(entry.getName()).normalize().equals(destDir)) {
                        continue; // "./" entry: the destination folder itself
                    }
                    path = SafeFileNames.resolveInside(destDir, entry.getName());
                } catch (IOException | InvalidPathException e) {
                    throw new ZipException("Invalid zip entry, outside of the archive folder: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    // a file entry can come before the entry of its folder
                    Files.createDirectories(path.getParent());
                    extractFile(entry, zipFile, path.toString());
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

    /**
     * This method stores an uploaded zip file in a temporary file, creates a new
     * folder with the same name and unzips the content into this folder, and gives
     * back the folder with the content.
     *
     * @param tempFile
     * @param dicomZipFile
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    public static File saveTempFileCreateFolderAndUnzip(final File tempFile, final MultipartFile dicomZipFile,
            final boolean fromDicom) throws IOException, RestServiceException {
        String fileName = tempFile.getName();
        int pos = fileName.lastIndexOf(FILE_POINT);
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        File unzipFolderFile = new File(tempFile.getParentFile().getAbsolutePath() + File.separator + fileName);
        if (!unzipFolderFile.exists()) {
            unzipFolderFile.mkdirs();
        } else {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Error while unzipping file: folder already exists.", null));
        }
        try {
            ImportUtils.unzip(tempFile.getAbsolutePath(), unzipFolderFile.getAbsolutePath());
        } catch (ZipException e) {
            // invalid archive, or an entry leading outside of its folder: nothing of it is kept
            LOG.warn("Invalid zip file {}: {}", dicomZipFile.getOriginalFilename(), e.getMessage());
            FileUtils.deleteQuietly(unzipFolderFile);
            tempFile.delete();
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Invalid zip file: " + e.getMessage(), null));
        }
        tempFile.delete();
        return unzipFolderFile;
    }

    /**
     * This method takes a multipart file and stores it in a configured upload
     * directory in relation with the userId with a random name and the suffix
     * .upload
     *
     * @param file
     * @throws IOException
     */
    public static File saveTempFile(final File userImportDir, final MultipartFile file) throws IOException {
        long n = createRandomLong();
        File uploadFile = new File(userImportDir.getAbsolutePath(), Long.toString(n) + UPLOAD_FILE_SUFFIX);
        file.transferTo(uploadFile);
        return uploadFile;
    }

    /**
     * This method creates a random long number.
     *
     * @return long: random number
     */
    public static long createRandomLong() {
        long n = RANDOM.nextLong();
        if (n == Long.MIN_VALUE) {
            n = 0; // corner case
        } else {
            n = Math.abs(n);
        }
        return n;
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

}
