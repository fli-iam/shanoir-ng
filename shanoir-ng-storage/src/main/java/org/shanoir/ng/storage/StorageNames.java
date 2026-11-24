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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The file names given to the storage often come from the clients (names of uploaded files, names in
 * download URLs): they must not lead outside of the folder (or S3 key prefix) they belong to.
 */
final class StorageNames {

    private StorageNames() {
    }

    /**
     * Checks that a file name is a single name, without any folder.
     *
     * @param fileName the file name
     * @return the file name
     * @throws StorageException if the name is empty, ".", "..", or contains a "/", a "\" or a NUL character
     */
    static String checkFileName(String fileName) throws StorageException {
        if (fileName == null || fileName.isBlank() || ".".equals(fileName) || "..".equals(fileName)
                || fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0 || fileName.indexOf('\0') >= 0) {
            throw new StorageException("Invalid file name: " + fileName, null);
        }
        return fileName;
    }

    /**
     * Checks that a S3 key, or a part of it, has no "." or ".." segment that could be resolved to
     * another prefix.
     *
     * @param key the key, or a part of it
     * @return the key
     * @throws StorageException if the key is null or has a "." or ".." segment
     */
    static String checkKey(String key) throws StorageException {
        if (key == null || key.indexOf('\\') >= 0 || key.indexOf('\0') >= 0) {
            throw new StorageException("Invalid storage key: " + key, null);
        }
        for (String segment : key.split("/")) {
            if (".".equals(segment) || "..".equals(segment)) {
                throw new StorageException("Invalid storage key: " + key, null);
            }
        }
        return key;
    }

    /**
     * Builds a path under a base folder, as Paths.get(baseDir, parts...) does, and checks that it stays
     * inside the base folder.
     *
     * @param baseDir the base folder
     * @param parts the parts of the path under the base folder
     * @return the normalized path
     * @throws StorageException if the path is the base folder itself, leaves it, or is invalid
     */
    static Path pathInside(String baseDir, String... parts) throws StorageException {
        try {
            Path base = Paths.get(baseDir).toAbsolutePath().normalize();
            Path path = Paths.get(baseDir, parts).toAbsolutePath().normalize();
            if (!path.startsWith(base) || path.equals(base)) {
                throw new StorageException("Invalid path, outside of its folder: " + String.join("/", parts), null);
            }
            return path;
        } catch (InvalidPathException e) {
            throw new StorageException("Invalid path: " + String.join("/", parts), e);
        }
    }

}
