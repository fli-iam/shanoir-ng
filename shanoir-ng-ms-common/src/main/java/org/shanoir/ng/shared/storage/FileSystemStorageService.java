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

package org.shanoir.ng.shared.storage;

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
@ConditionalOnProperty(name = "storage.type", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStorageService.class);

    @Value("${storage.file-system.studies-data}")
    private String baseDir;

    @Override
    public void store(String directory, String filename,
                      InputStream inputStream, String contentType, long size)
            throws StorageException {
        try {
            Path dirPath = Paths.get(baseDir, directory);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(filename);
            LOG.info("Storing file at: {}", filePath);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Resource load(String directory, String filename) throws StorageException {
        Path filePath = Paths.get(baseDir, directory, filename);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new StorageException("File not found: " + filePath, null);
        }
        return resource;
    }

    @Override
    public String getPublicLocation(String directory, String filename) throws StorageException {
        return Paths.get(baseDir, directory, filename).toUri().toString();
    }

    @Override
    public void delete(String directory, String filename) throws StorageException {
        try {
            Files.deleteIfExists(Paths.get(baseDir, directory, filename));
        } catch (IOException e) {
            throw new StorageException("Failed to delete file " + filename, e);
        }
    }

    @Override
    public void deleteDirectory(String directory) throws StorageException {
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
    public void move(String directory, String sourceFilename, String targetFilename)
            throws StorageException {
        try {
            Path source = Paths.get(baseDir, directory, sourceFilename);
            Path target = Paths.get(baseDir, directory, targetFilename);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Moved file from {} to {}", source, target);
        } catch (IOException e) {
            throw new StorageException("Failed to move " + sourceFilename + " -> " + targetFilename, e);
        }
    }

}
