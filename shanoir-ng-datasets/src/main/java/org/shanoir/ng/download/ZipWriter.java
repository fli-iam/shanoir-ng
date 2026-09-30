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

package org.shanoir.ng.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriter implements ArchiveWriter {

    private static final String EXTENSION = ".zip";

    private static final String CONTENT_TYPE = "application/zip";

    private final ZipOutputStream zip;

    public ZipWriter(OutputStream out) {
        this.zip = new ZipOutputStream(out);
    }

    public String getExtension() {
        return EXTENSION;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public void addEntry(String name, byte[] content) throws IOException {
        putEntry(name, content.length);
        zip.write(content);
        zip.closeEntry();
    }

    public void addEntry(String name, InputStream content, long size) throws IOException {
        putEntry(name, size);
        content.transferTo(zip);
        zip.closeEntry();
    }

    private void putEntry(String name, long size) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(System.currentTimeMillis());
        if (size >= 0) {
            entry.setSize(size);
        }
        zip.putNextEntry(entry);
    }

    public void flush() throws IOException {
        zip.flush();
    }

    public void close() throws IOException {
        zip.close();
    }
}
