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

/**
 * Archive written entry by entry into a stream, as a .zip or a .tar.zst.
 * close() ends the archive and the underlying stream: to abort a download, do not call it but abort(),
 * so the client receives an archive that cannot be extracted.
 */
public interface ArchiveWriter extends Closeable {

    String getExtension();

    String getContentType();

    void addEntry(String name, byte[] content) throws IOException;

    /**
     * @param size the content length, or -1 when unknown
     */
    void addEntry(String name, InputStream content, long size) throws IOException;

    void flush() throws IOException;

    /**
     * Stops any background write, without ending the archive: to call instead of close() when aborting a download.
     */
    default void abort() {
        // Nothing for stream closing, since the exception raising will cut the stream by itself
        // That way, the client will detect that the archive is corrupted
    }
}
