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

package org.shanoir.ng.shared.files;

import java.io.IOException;
import java.nio.file.Path;

/**
 * File names sent by the clients (uploaded files, entries of uploaded zip files) are used to build paths
 * on the server: they must not lead outside of the folder they are written to ("../../file", "/etc/file").
 */
public final class SafeFileNames {

    private SafeFileNames() {
    }

    /**
     * Keeps only the name of an uploaded file, without any folder: some browsers send the full path,
     * and a client can send any name.
     *
     * @param originalName the name sent by the client
     * @return the last part of the name, after the last "/" or "\"
     * @throws IllegalArgumentException if there is no name left, or if it is "." or ".."
     */
    public static String fileName(String originalName) {
        if (originalName == null) {
            throw new IllegalArgumentException("The uploaded file has no name");
        }
        String name = originalName.substring(Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\')) + 1);
        if (name.isBlank() || ".".equals(name) || "..".equals(name) || name.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Invalid uploaded file name: " + originalName);
        }
        return name;
    }

    /**
     * Resolves a path sent by the client, such as the name of an entry of an uploaded zip file,
     * that must stay inside the given folder.
     *
     * @param folder the folder the file is written to
     * @param relativePath the path sent by the client, relative to the folder
     * @return the path of the file inside the folder
     * @throws IOException if the path leads outside of the folder
     */
    public static Path resolveInside(Path folder, String relativePath) throws IOException {
        Path normalizedFolder = folder.toAbsolutePath().normalize();
        Path path = normalizedFolder.resolve(relativePath).normalize();
        if (!path.startsWith(normalizedFolder) || path.equals(normalizedFolder)) {
            throw new IOException("Invalid path, outside of its folder: " + relativePath);
        }
        return path;
    }

}
