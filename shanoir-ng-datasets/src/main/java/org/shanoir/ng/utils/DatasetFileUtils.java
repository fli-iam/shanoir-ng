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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.dataset.dto.InputDTO;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;

public final class DatasetFileUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(DatasetFileUtils.class);

    private DatasetFileUtils() {
    }

    private static final String UNDERSCORE = "_";

    public static final String INPUT = "input.json";

    private static final String S3 = "s3";

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
     * Reads all dataset files depending on the format attached to one dataset.
     * @param dataset
     * @param pathURLs
     * @throws MalformedURLException
     */
    public static void getDatasetFilePathURLs(
            final Dataset dataset,
            final List<URL> pathURLs,
            final DatasetExpressionFormat format,
            DatasetDownloadError downloadResult) {
        dataset.getDatasetExpressions().stream()
                .filter(expr -> format.equals(expr.getDatasetExpressionFormat()))
                .findFirst()
                .ifPresent(expr -> expr.getDatasetFiles()
                        .forEach(file -> processDatasetFile(file, pathURLs, downloadResult)));
    }

    private static void processDatasetFile(
            DatasetFile file,
            List<URL> pathURLs,
            DatasetDownloadError downloadResult) {
        String normalizedPath = file.getPath().replace("%20", " ");
        try {
            pathURLs.add(toURL(normalizedPath));
        } catch (MalformedURLException e) {
            downloadResult.update(
                    "Malformed URL: " + normalizedPath,
                    DatasetDownloadError.PARTIAL_FAILURE);
        }
    }

    private static final URLStreamHandler S3_HANDLER = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection(URL u) {
            throw new UnsupportedOperationException("S3 URLs are path references only");
        }
    };

    private static URL toURL(String path) throws MalformedURLException {
        if (path.startsWith("s3://")) {
            return handleS3(path);
        }
        return URI.create(path).toURL();
    }

    /**
     * // new URL(protocol, host, port, file, handler)
     * // s3:///my/key -> host="", file="/my/key"
     * @param path
     * @return
     * @throws MalformedURLException
     */
    private static URL handleS3(String path) throws MalformedURLException {
        String withoutScheme = path.substring("s3://".length());
        int slashIdx = withoutScheme.indexOf('/');
        String host = slashIdx > 0 ? withoutScheme.substring(0, slashIdx) : "";
        String file = slashIdx >= 0 ? withoutScheme.substring(slashIdx) : "/";
        return new URL(S3, host, -1, file, S3_HANDLER);
    }

    public static Optional<URL> getFirstDatasetFilePathURL(
            final Dataset dataset,
            final DatasetExpressionFormat format) {
        return dataset.getDatasetExpressions().stream()
                .filter(expr -> format.equals(expr.getDatasetExpressionFormat()))
                .findFirst()
                .flatMap(DatasetFileUtils::getFirstValidUrl);
    }

    private static Optional<URL> getFirstValidUrl(DatasetExpression expr) {
        return expr.getDatasetFiles().stream()
                .map(DatasetFile::getPath)
                .map(path -> path.replace("%20", " "))
                .map(DatasetFileUtils::createURL)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private static URL createURL(String path) {
        try {
            if (path.startsWith("s3://")) {
                return handleS3(path);
            }
            return URI.create(path).toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            LOG.warn("Invalid URL: {}", path, e);
            return null;
        }
    }

    /**
     * Receives a list of URLs containing either file:/// or
     * s3:/// urls and copies the files to a folder named workFolder.
     * Return the list of copied files.
     *
     * @param urls
     * @param subjectName         the subjectName
     * @param datasetFilePath
     * @param datasetDownloadNameListPerPath
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public static List<String> copyFilesForDownload(StorageService storageService, final List<URL> urls,
            final ZipOutputStream zipOutputStream, Dataset dataset, String subjectName, boolean keepName,
            String datasetFilePath, Map<String, List<String>> datasetDownloadNameListPerPath)
            throws Exception {

        List<String> filesInZip = new ArrayList<>();
        int index = 0;

        for (URL url : urls) {
            String decodedPath = UriUtils.decode(url.getPath(), StandardCharsets.UTF_8.name());
            Resource resource;
            if (url.getProtocol().equals(S3)) {
                resource = storageService.loadDatasetsData(decodedPath);
            } else {
                resource = new FileSystemResource(new File(decodedPath));
            }
            String srcFileName = StringUtils.getFilename(decodedPath);
            // Generate the target file name
            String fileName = getFileName(keepName, srcFileName, subjectName, dataset, index);
            fileName = fileName.replace(File.separator, UNDERSCORE); // avoid nested folders

            if (!datasetDownloadNameListPerPath.containsKey(datasetFilePath)) {
                datasetDownloadNameListPerPath.put(datasetFilePath, new ArrayList<>(List.of(fileName)));
            } else {
                List<String> nameListForFilePath = datasetDownloadNameListPerPath.get(datasetFilePath);
                String finalFileName = fileName;
                if (nameListForFilePath.contains(finalFileName)) {
                    fileName = fileName.replaceFirst("\\.", "_" + nameListForFilePath.stream()
                            .filter(name -> Objects.equals(name, finalFileName)).count() + ".");
                }
                nameListForFilePath.add(finalFileName);
            }

            // Add folder path if specified
            if (datasetFilePath != null)
                fileName = datasetFilePath + File.separator + fileName;

            // If it's an uncompressed NIfTI file, compress it on-the-fly via a temp file
            boolean compress = decodedPath.endsWith(".nii");
            String zipFileName = compress ? fileName + ".gz" : fileName;

            ZipEntry zipEntry = new ZipEntry(zipFileName);
            zipEntry.setTime(System.currentTimeMillis());

            if (compress) {
                Path tempGz = Files.createTempFile("nii-gz-", ".gz");
                try {
                    try (InputStream resourceStream = resource.getInputStream()) {
                        compressGzipStream(resourceStream, tempGz);
                    }
                    zipEntry.setSize(Files.size(tempGz));
                    zipOutputStream.putNextEntry(zipEntry);
                    try (InputStream gzStream = Files.newInputStream(tempGz)) {
                        StreamUtils.copy(gzStream, zipOutputStream);
                    }
                } finally {
                    Files.deleteIfExists(tempGz);
                }
            } else {
                long contentLength = resource.contentLength();
                if (contentLength >= 0) {
                    zipEntry.setSize(contentLength);
                }
                zipOutputStream.putNextEntry(zipEntry);
                try (InputStream resourceStream = resource.getInputStream()) {
                    StreamUtils.copy(resourceStream, zipOutputStream);
                }
            }

            zipOutputStream.closeEntry();
            filesInZip.add(zipFileName);
            index++;
        }
        return filesInZip;
    }

    public static void writeManifestForExport(final ZipOutputStream zipOutputStream,
            Map<Long, List<String>> filesByAcquisitionId) throws IOException {
        InputDTO input = new InputDTO();
        for (Map.Entry<Long, List<String>> entry : filesByAcquisitionId.entrySet()) {
            InputDTO.InputSerieDTO serie = new InputDTO.InputSerieDTO();
            serie.setId(entry.getKey());
            for (String file : entry.getValue()) {
                serie.getFiles().add(file);
            }
            input.getSeries().add(serie);
        }

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

        ZipEntry zipEntry = new ZipEntry(INPUT);
        zipEntry.setTime(System.currentTimeMillis());
        zipOutputStream.putNextEntry(zipEntry);
        new ObjectMapper(jsonFactory).writeValue(zipOutputStream, input);
        zipOutputStream.closeEntry();
    }

    public static String getFileName(boolean keepName, String srcFileName, String subjectName, Dataset dataset,
            int index) {
        if (keepName) {
            String prefix = srcFileName.split("\\.", 2)[0];
            if (prefix.matches("\\d+") || prefix.matches("\\d+_info")) {
                srcFileName = dataset.getName() + "_" + srcFileName;
            }
            return srcFileName;
        }

        // Theoretical file name:
        // SubjectName_SeriesDescription_SeriesNumberInProtocol_SeriesNumberInSequence.nii(.gz)
        StringBuilder name = new StringBuilder();

        name.append(subjectName).append(UNDERSCORE);
        if (dataset instanceof EegDataset) {
            name.append(dataset.getName()).append(UNDERSCORE);
        } else {
            if (dataset.getUpdatedMetadata().getComment() != null) {
                name.append(dataset.getUpdatedMetadata().getComment()).append(UNDERSCORE);
            }
            name.append(dataset.getDatasetAcquisition().getSortingIndex()).append(UNDERSCORE);
            if (dataset.getUpdatedMetadata().getName() != null
                    && dataset.getUpdatedMetadata().getName().lastIndexOf(" ") != -1) {
                name.append(dataset.getUpdatedMetadata().getName()
                        .substring(dataset.getUpdatedMetadata().getName().lastIndexOf(" ") + 1))
                        .append(UNDERSCORE);
            }
        }
        name.append(dataset.getDatasetAcquisition().getRank()).append(UNDERSCORE)
                .append(index)
                .append(".");

        if (srcFileName.endsWith(".nii.gz")) {
            name.append("nii.gz");
        } else {
            name.append(FilenameUtils.getExtension(srcFileName));
        }

        return name.toString();
    }

    public static void compressGzipStream(InputStream source, Path gzipDestination) throws IOException {
        try (OutputStream fos = Files.newOutputStream(gzipDestination);
                GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            StreamUtils.copy(source, gzipOS);
        }
    }

}
