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

import com.github.luben.zstd.ZstdOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TarWriter implements ArchiveWriter {

    private static final String EXTENSION = ".tar.zst";

    private static final String CONTENT_TYPE = "application/zstd";

    /** Idle time after which a keep-alive tiny byte load is sent, so that the connection is not dropped while waiting for the PACS. */
    private static final long KEEP_ALIVE_PERIOD = TimeUnit.SECONDS.toNanos(20);

    /** Empty zstd skippable frame, which will be used a keep-alive load */
    private static final byte[] SKIPPABLE_FRAME = {0x50, 0x2A, 0x4D, 0x18, 0, 0, 0, 0};

    /** Only triggers keep-alives: each one runs on a virtual thread, so that a slow client does not delay the others. */
    private static final ScheduledExecutorService KEEP_ALIVE_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "archive-keep-alive");
        thread.setDaemon(true);
        return thread;
    });

    /** Guards every write into the zstd stream, from the archive thread and from keep-alives. */
    private final Lock lock = new ReentrantLock();

    private final OutputStream network;

    private final ZstdOutputStream zstd;

    private final TarArchiveOutputStreamShanoir tar;

    private volatile long lastSent = System.nanoTime();

    /** Guarded by lock. */
    private boolean stopped;

    /** Only using Zstandard compression algo atm **/
    public TarWriter(OutputStream out) throws IOException {
        this.network = new FilterOutputStream(out);
        this.zstd = new ZstdOutputStream(network, 1);
        this.tar = new TarArchiveOutputStreamShanoir(new FilterOutputStream(zstd));

        // DICOM paths are longer than the 100 characters of a basic tar header
        tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        scheduleKeepAlive(KEEP_ALIVE_PERIOD);
    }

    public String getExtension() {
        return EXTENSION;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public void addEntry(String name, byte[] content) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(content.length);
        tar.putArchiveEntry(entry);
        tar.write(content);
        tar.closeArchiveEntry();
    }

    public void addEntry(String name, InputStream content, long size) throws IOException {
        if (size < 0) {
            addEntry(name, content.readAllBytes());
            return;
        }
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        tar.putArchiveEntry(entry);
        content.transferTo(tar);
        tar.closeArchiveEntry();
    }

    public void flush() throws IOException {
        tar.flush();
    }

    public void abort() {
        // Nothing for stream closing, since the exception raising will cut the stream by itself
        // That way, the client will detect that the archive is corrupted
        stopKeepAlive();
    }

    public void close() throws IOException {
        stopKeepAlive();
        tar.finish();
        tar.close();
    }

    /// Keep-alive probe parts

    private void scheduleKeepAlive(long delayNanos) {
        KEEP_ALIVE_SCHEDULER.schedule(() -> Thread.ofVirtual().start(this::keepAlive), delayNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Sends a skippable frame if nothing reached the client for KEEP_ALIVE_PERIOD.
     * The current zstd frame is ended first, as a skippable frame cannot be written inside a frame.
     */
    private void keepAlive() {
        if (!lock.tryLock()) {
            // data is being written
            scheduleKeepAlive(KEEP_ALIVE_PERIOD);
            return;
        }
        try {
            if (stopped) {
                return;
            }

            long idle = System.nanoTime() - lastSent;
            if (idle >= KEEP_ALIVE_PERIOD) {
                zstd.setCloseFrameOnFlush(true);
                try {
                    zstd.flush();
                } finally {
                    zstd.setCloseFrameOnFlush(false);
                }
                network.write(SKIPPABLE_FRAME);
                network.flush();
                idle = 0;
            }
            scheduleKeepAlive(KEEP_ALIVE_PERIOD - idle);
        } catch (IOException e) {
            // client gone: the archive thread gets its own error on its next write
            stopped = true;
        } finally {
            lock.unlock();
        }
    }

    /** Waits for a running keep-alive: nothing is written after this returns, as Tomcat recycles the response. */
    private void stopKeepAlive() {
        lock.lock();
        try {
            stopped = true;
        } finally {
            lock.unlock();
        }
    }

    /// Extension to manage the keep-alive scheduler with the lock
    class TarArchiveOutputStreamShanoir extends TarArchiveOutputStream {

        public TarArchiveOutputStreamShanoir(OutputStream os) {
            super(os);
        }

        @Override
        public void write(int b) throws IOException {
            lock.lock();
            try {
                zstd.write(b);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            lock.lock();
            try {
                zstd.write(b, off, len);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void flush() throws IOException {
            lock.lock();
            try {
                zstd.flush();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws IOException {
            lock.lock();
            try {
                zstd.close();
            } finally {
                lock.unlock();
            }
        }
    }

    /// Extension to manage the keep-alive scheduler with the lock
    class FilterOutputStreamShanoir extends FilterOutputStream {

        public FilterOutputStreamShanoir(OutputStream os) {
            super(os);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            lastSent = System.nanoTime();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            lastSent = System.nanoTime();
        }
    }
}
