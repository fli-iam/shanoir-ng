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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import com.github.luben.zstd.ZstdOutputStream;

/**
 * Archive written entry by entry into a stream, as a .zip or a .tar.zst.
 * close() ends the archive and the underlying stream: to abort a download, do not call it,
 * so the client receives an archive that cannot be extracted.
 */
public interface ArchiveWriter extends Closeable {

    enum Format {
        ZIP(".zip", "application/zip"),
        TAR_ZST(".tar.zst", "application/zstd");

        private final String extension;

        private final String contentType;

        Format(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public String getExtension() {
            return extension;
        }

        public String getContentType() {
            return contentType;
        }
    }

    void addEntry(String name, byte[] content) throws IOException;

    /**
     * @param size the content length, or -1 when unknown (a tar entry then buffers the content to learn it)
     */
    void addEntry(String name, InputStream content, long size) throws IOException;

    void flush() throws IOException;

    static ArchiveWriter open(Format format, OutputStream out) throws IOException {
        return format == Format.TAR_ZST ? new TarZst(out) : new Zip(out);
    }

    final class Zip implements ArchiveWriter {

        private final ZipOutputStream zip;

        Zip(OutputStream out) {
            this.zip = new ZipOutputStream(out);
        }

        @Override
        public void addEntry(String name, byte[] content) throws IOException {
            putEntry(name, content.length);
            zip.write(content);
            zip.closeEntry();
        }

        @Override
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

        @Override
        public void flush() throws IOException {
            zip.flush();
        }

        @Override
        public void close() throws IOException {
            zip.close();
        }
    }

    final class TarZst implements ArchiveWriter {

        /** Fast level: 1000 concurrent VIP downloads share the CPU. */
        private static final int ZSTD_LEVEL = 1;

        private final TarArchiveOutputStream tar;

        TarZst(OutputStream out) throws IOException {
            this.tar = new TarArchiveOutputStream(new ZstdOutputStream(out, ZSTD_LEVEL));
            // DICOM paths are longer than the 100 characters of a basic tar header
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        }

        @Override
        public void addEntry(String name, byte[] content) throws IOException {
            TarArchiveEntry entry = new TarArchiveEntry(name);
            entry.setSize(content.length);
            tar.putArchiveEntry(entry);
            tar.write(content);
            tar.closeArchiveEntry();
        }

        @Override
        public void addEntry(String name, InputStream content, long size) throws IOException {
            if (size < 0) {
                // a tar header needs the size before the content
                addEntry(name, content.readAllBytes());
                return;
            }
            TarArchiveEntry entry = new TarArchiveEntry(name);
            entry.setSize(size);
            tar.putArchiveEntry(entry);
            content.transferTo(tar);
            tar.closeArchiveEntry();
        }

        @Override
        public void flush() throws IOException {
            tar.flush();
        }

        @Override
        public void close() throws IOException {
            tar.finish();
            tar.close();
        }
    }

}
