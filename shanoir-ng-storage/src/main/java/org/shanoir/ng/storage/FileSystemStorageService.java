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

    @Value("${storage.file-system.studies-data}")
    private String baseDirStudies;

    @Value("${storage.file-system.datasets-data}")
    private String baseDirDatasets;

    @Override
    public String storeExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        String directory = EXAMINATION + examinationId;
        return store(baseDirDatasets, directory, fileName, inputStream, contentType, size);
    }

    @Override
    public String storeStudyFile(Long studyId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException {
        String directory = STUDY + studyId;
        return store(baseDirDatasets, directory, fileName, inputStream, contentType, size);
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
            return filePath.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public Resource loadExtraData(Long examinationId, String fileName) throws StorageException {
        return loadDatasets(EXAMINATION + examinationId, fileName);
    }

    @Override
    public Resource loadDatasets(String directory, String fileName) throws StorageException {
        return load(baseDirDatasets, directory, fileName);
    }

    @Override
    public Resource loadStudyFile(Long studyId, String fileName) throws StorageException {
        String directory = STUDY + studyId;
        return load(baseDirStudies, directory, fileName);
    }

    private Resource load(String baseDir, String directory, String fileName) throws StorageException {
        Path filePath = Paths.get(baseDir, directory, fileName);
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
    public String getPublicLocationDatasets(String directory, String fileName) throws StorageException {
        return Paths.get(baseDirDatasets, directory, fileName).toUri().toString();
    }

    @Override
    public String getPublicLocationStudies(String directory, String fileName) throws StorageException {
        return Paths.get(baseDirStudies, directory, fileName).toUri().toString();
    }

    @Override
    public void deleteDatasets(String directory, String fileName) throws StorageException {
        delete(baseDirDatasets, directory, fileName);
    }

    @Override
    public void deleteExtraData(Long examinationId, String fileName) throws StorageException {
        delete(baseDirDatasets, EXAMINATION + examinationId, fileName);
    }

    @Override
    public void deleteStudyFile(Long studyId, String fileName) throws StorageException {
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
    public void deleteDirectoryStudyFile(Long studyId) throws StorageException {
        String directory = STUDY + studyId;
        deleteDirectory(baseDirStudies, directory);
    }

    @Override
    public void deleteDirectoryExtraData(Long examinationId) throws StorageException {
        deleteDirectoryDatasets(EXAMINATION + examinationId);
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
    public void moveStudyFile(Long studyId, String sourceFileName, String targetFileName)
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
