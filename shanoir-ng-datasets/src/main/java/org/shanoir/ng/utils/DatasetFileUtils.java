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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.dataset.dto.InputDTO;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.DatasetDownloadError;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;

public class DatasetFileUtils {

    private static final String UNDERSCORE = "_";

    public static final String INPUT = "input.json";

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
    public static void getDatasetFilePathURLs(final Dataset dataset, final List<URL> pathURLs, final DatasetExpressionFormat format, DatasetDownloadError downloadResult) {
        List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
        for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
            DatasetExpression datasetExpression = itExpressions.next();
            if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
                List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
                int i = 0;
                for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext(); i++) {
                    DatasetFile datasetFile = itFiles.next();
                    URL url;
                    try {
                        url = new URL(datasetFile.getPath().replaceAll("%20", " "));
                        pathURLs.add(url);
                    } catch (MalformedURLException e) {
                        downloadResult.update("Malformed URI: " + datasetFile.getPath().replaceAll("%20", " "), DatasetDownloadError.PARTIAL_FAILURE);
                    }
                }
            }
        }
    }

    /**
     * Receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
     * Return the list of copied files
     *
     * @param urls
     * @param subjectName         the subjectName
     * @param datasetFilePath
     * @param datasetDownloadName
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public static List<String> copyNiftiFilesForURLs(final List<URL> urls, final ZipOutputStream zipOutputStream, Dataset dataset, String subjectName, boolean keepName, String datasetFilePath, String datasetDownloadName) throws IOException {
        int index = 0;
        List<String> files = new ArrayList<>();
        for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
            URL url =  iterator.next();
            File srcFile = new File(UriUtils.decode(url.getPath(), StandardCharsets.UTF_8.name()));
            String srcPath = srcFile.getAbsolutePath();

            String fileName = getFileName(keepName, srcFile, subjectName, dataset, index, datasetDownloadName);

            if (fileName.contains(File.separator)) {
                fileName = fileName.replaceAll(File.separator, UNDERSCORE);
            }
            // add folder logic if necessary
            if (datasetFilePath != null) {
                fileName = datasetFilePath + File.separator + fileName;
            }
            boolean compressed = srcPath.endsWith(".nii");

            // Gzip file if necessary in order to always return a .nii.gz file
            if (compressed) {
                srcPath = srcPath + ".gz";
                fileName = fileName + ".gz";
                compressGzipFile(srcFile.getAbsolutePath(), srcPath);
            }

            files.add(fileName);

            FileSystemResource fileSystemResource = new FileSystemResource(srcPath);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipEntry.setSize(fileSystemResource.contentLength());
            zipEntry.setTime(System.currentTimeMillis());
            zipOutputStream.putNextEntry(zipEntry);
            StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
            zipOutputStream.closeEntry();

            if (compressed) {
                // If we gzipped the file, delete the newly created file directly after
                FileUtils.deleteQuietly(new File(srcPath));
            }

            index++;
        }
        return files;
    }

    public static void writeManifestForExport(final ZipOutputStream zipOutputStream, Map<Long, List<String>> filesByAcquisitionId) throws IOException {
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

    public static String getFileName(boolean keepName, File srcFile, String subjectName, Dataset dataset, int index, String datasetDownloadName) {
        if (dataset.getDatasetProcessing() != null || dataset.getDatasetAcquisition() == null) {
            return datasetDownloadName;
        }
        if (keepName) {
            return srcFile.getName();
        }

        // Theorical file name:  NomSujet_SeriesDescription_SeriesNumberInProtocol_SeriesNumberInSequence.nii(.gz)
        StringBuilder name = new StringBuilder();

        name.append(subjectName).append(UNDERSCORE);
        if (dataset instanceof EegDataset) {
            name.append(dataset.getName()).append(UNDERSCORE);
        } else {
            if (dataset.getUpdatedMetadata().getComment() != null) {
                name.append(dataset.getUpdatedMetadata().getComment()).append(UNDERSCORE);
            }
            name.append(dataset.getDatasetAcquisition().getSortingIndex()).append(UNDERSCORE);
            if (dataset.getUpdatedMetadata().getName() != null && dataset.getUpdatedMetadata().getName().lastIndexOf(" ") != -1) {
                name.append(dataset.getUpdatedMetadata().getName().substring(dataset.getUpdatedMetadata().getName().lastIndexOf(" ") + 1)).append(UNDERSCORE);
            }
        }
        name.append(dataset.getDatasetAcquisition().getRank()).append(UNDERSCORE)
        .append(index)
        .append(".");
        if (srcFile.getName().endsWith(".nii.gz")) {
            name.append("nii.gz");
        } else {
            name.append(FilenameUtils.getExtension(srcFile.getName()));
        }
        return name.toString();
    }

    public static void compressGzipFile(String source, String gzipDestination) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
                FileOutputStream fos = new FileOutputStream(gzipDestination);
                GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, len);
            }
        }
    }
}
