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

package org.shanoir.ng.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "file-system", matchIfMissing = true)
public class FileSystemStorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStorageService.class);

    private static final String FILE = "file://";

    @Value("${storage.file-system.studies-data:UNUSED}")
    private String baseDirStudies;

    @Value("${storage.file-system.datasets-data:UNUSED}")
    private String baseDirDatasets;

    @Value("${storage.file-system.preclinical-data:UNUSED}")
    private String baseDirPreclinical;

    @Override
    public String storeStudyData(Long studyId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (baseDirStudies.equals(UNUSED)) {
            throw new StorageException("Missing studies directory configuration.", null);
        }
        String directory = STUDY + studyId;
        return store(baseDirStudies, directory, fileName, inputStream, contentType, size);
    }

    @Override
    public String storeExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (baseDirDatasets.equals(UNUSED)) {
            throw new StorageException("Missing datasets directory configuration.", null);
        }
        String directory = EXAMINATION + examinationId;
        return store(baseDirDatasets, directory, fileName, inputStream, contentType, size);
    }

    @Override
    public String storeBIDSData(Long studyId, String subjectName, Long examinationId, String fileName,
            String dataTypeBIDS, InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (baseDirDatasets.equals(UNUSED)) {
            throw new StorageException("Missing datasets directory configuration.", null);
        }
        String directory = STUDY + studyId
                + SLASH + SUBJECT + subjectName + SLASH + SESSION + examinationId + SLASH + dataTypeBIDS;
        return store(baseDirDatasets, directory, fileName, inputStream, contentType, size);
    }

    @Override
    public String storeProcessedData(Long subjectId, String timeStamp, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (baseDirDatasets.equals(UNUSED)) {
            throw new StorageException("Missing datasets directory configuration.", null);
        }
        String directory = SLASH + PROCESSED_DATASET
                + SLASH + SUBJECT + subjectId
                + SLASH + timeStamp;
        return store(baseDirDatasets, directory, fileName, inputStream, contentType, size);
    }

    @Override
    public String storePreclinicalExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (baseDirPreclinical.equals(UNUSED)) {
            throw new StorageException("Missing preclinical directory configuration.", null);
        }
        String directory = EXAMINATION + examinationId;
        return store(baseDirPreclinical, directory, fileName, inputStream, contentType, size);
    }

    @Override
    public String storePathologyModelData(Long pathologyModelId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        if (baseDirPreclinical.equals(UNUSED)) {
            throw new StorageException("Missing preclinical directory configuration.", null);
        }
        String directory = PATHOLOGY_MODEL + pathologyModelId;
        return store(baseDirPreclinical, directory, fileName, inputStream, contentType, size);
    }

    private String store(String baseDir, String directory, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        try {
            Path dirPath = Paths.get(baseDir, directory);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName);
            LOG.info("Storing file at: {}", filePath);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return FILE + filePath.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public Resource loadStudyData(Long studyId, String fileName) throws StorageException {
        String directory = STUDY + studyId;
        return load(baseDirStudies, directory, fileName);
    }

    @Override
    public Resource loadExtraData(Long examinationId, String fileName) throws StorageException {
        String directory = EXAMINATION + examinationId;
        return load(baseDirDatasets, directory, fileName);
    }

    @Override
    public Resource loadDatasetsData(String path) throws StorageException {
        return load(baseDirDatasets, path);
    }

    @Override
    public Resource loadPreclinicalExtraData(Long examinationId, String fileName) throws StorageException {
        String directory = EXAMINATION + examinationId;
        return load(baseDirPreclinical, directory, fileName);
    }

    @Override
    public Resource loadPathologyModelData(Long pathologyModelId, String fileName) throws StorageException {
        String directory = PATHOLOGY_MODEL + pathologyModelId;
        return load(baseDirPreclinical, directory, fileName);
    }

    private Resource load(String baseDir, String directory, String fileName) throws StorageException {
        Path filePath = Paths.get(baseDir, directory, fileName);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new StorageException("File not found: " + filePath, null);
        }
        return resource;
    }

    private Resource load(String baseDir, String path) throws StorageException {
        Path filePath = Paths.get(baseDir, path);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new StorageException("File not found: " + filePath, null);
        }
        return resource;
    }

    @Override
    public long getFileSizeExtraData(Long examinationId, String fileName) throws StorageException {
        try {
            Path filePath = Paths.get(baseDirDatasets, EXAMINATION + examinationId, fileName);
            if (!Files.exists(filePath)) {
                return 0L;
            }
            return Files.size(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to get size of file: " + fileName, e);
        }
    }

    @Override
    public String getPublicLocationStudies(String directory, String fileName) throws StorageException {
        return Paths.get(baseDirStudies, directory, fileName).toUri().toString();
    }

    @Override
    public String getPublicLocationDatasets(String directory, String fileName) throws StorageException {
        return Paths.get(baseDirDatasets, directory, fileName).toUri().toString();
    }

    @Override
    public String getPublicLocationPreclinical(String directory, String fileName) throws StorageException {
        return Paths.get(baseDirPreclinical, directory, fileName).toUri().toString();
    }

    @Override
    public void deleteExtraData(Long examinationId, String fileName) throws StorageException {
        delete(baseDirDatasets, EXAMINATION + examinationId, fileName);
    }

    @Override
    public void deletePreclinicalExtraData(Long examinationId, String fileName) throws StorageException {
        delete(baseDirPreclinical, EXAMINATION + examinationId, fileName);
    }

    @Override
    public void deletePathologyModelData(Long pathologyModelId, String fileName) throws StorageException {
        delete(baseDirPreclinical, PATHOLOGY_MODEL + pathologyModelId, fileName);
    }

    @Override
    public void deleteStudyData(Long studyId, String fileName) throws StorageException {
        String directory = STUDY + studyId;
        delete(baseDirStudies, directory, fileName);
    }

    private void delete(String baseDir, String directory, String fileName) throws StorageException {
        try {
            Files.deleteIfExists(Paths.get(baseDir, directory, fileName));
        } catch (IOException e) {
            throw new StorageException("Failed to delete file " + fileName, e);
        }
    }

    @Override
    public void deleteDirectoryDatasets(String directory) throws StorageException {
        deleteDirectory(baseDirDatasets, directory);
    }

    @Override
    public void deleteDirectoryPreclinical(String directory) throws StorageException {
        deleteDirectory(baseDirPreclinical, directory);
    }

    @Override
    public void deleteDirectoryStudyData(Long studyId) throws StorageException {
        String directory = STUDY + studyId;
        deleteDirectory(baseDirStudies, directory);
    }

    @Override
    public void deleteDirectoryExtraData(Long examinationId) throws StorageException {
        deleteDirectoryDatasets(EXAMINATION + examinationId);
    }

    @Override
    public void deleteDirectoryPreclinicalExtraData(Long examinationId) throws StorageException {
        deleteDirectoryPreclinical(EXAMINATION + examinationId);
    }

    private void deleteDirectory(String baseDir, String directory) throws StorageException {
        Path dirPath = Paths.get(baseDir, directory);
        if (!Files.exists(dirPath))
            return;
        try {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException | IOException e) {
            throw new StorageException("Failed to delete directory " + directory, e);
        }
    }

    @Override
    public void moveDatasets(String directory, String sourceFileName, String targetFileName)
            throws StorageException {
        move(baseDirDatasets, directory, sourceFileName, targetFileName);
    }

    @Override
    public void moveStudyData(Long studyId, String sourceFileName, String targetFileName)
            throws StorageException {
        String directory = STUDY + studyId;
        move(baseDirStudies, directory, sourceFileName, targetFileName);
    }

    private void move(String baseDir, String directory, String sourceFileName, String targetFileName)
            throws StorageException {
        try {
            Path source = Paths.get(baseDir, directory, sourceFileName);
            Path target = Paths.get(baseDir, directory, targetFileName);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Moved file from {} to {}", source, target);
        } catch (IOException e) {
            throw new StorageException("Failed to move " + sourceFileName + " -> " + targetFileName, e);
        }
    }

}
