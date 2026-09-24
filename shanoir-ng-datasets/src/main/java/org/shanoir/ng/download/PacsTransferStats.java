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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.LongAdder;

/**
 * Accumulates PACS transfer figures (completed responses, bytes received, time until the last
 * byte) for the download running on the current thread.
 *
 * A download is served entirely by its request thread - WADODownloaderService drains the PACS
 * responses on the calling thread - so a ThreadLocal is enough to scope the figures to one request.
 * Nothing is recorded when no collection has been started on the thread.
 */
public final class PacsTransferStats {

    private static final ThreadLocal<PacsTransferStats> CURRENT = new ThreadLocal<>();

    private final LongAdder responseCount = new LongAdder();

    private final LongAdder totalBytes = new LongAdder();

    private final LongAdder totalResponseNanos = new LongAdder();

    private final LongAdder waitNanos = new LongAdder();

    private final LongAdder zipNanos = new LongAdder();

    private final LongAdder networkNanos = new LongAdder();

    private PacsTransferStats() {
    }

    /** Starts collecting for the current thread and returns the collector. */
    public static PacsTransferStats start() {
        PacsTransferStats stats = new PacsTransferStats();
        CURRENT.set(stats);
        return stats;
    }

    /** Stops collecting for the current thread. Must be called in a finally block. */
    public static void stop() {
        CURRENT.remove();
    }

    public static PacsTransferStats current() {
        return CURRENT.get();
    }

    public static OutputStream withNetworkTiming(OutputStream out) {
        PacsTransferStats stats = CURRENT.get();
        if (stats == null) {
            return out;
        }
        return new FilterOutputStream(out) {
            @Override
            public void write(int b) throws IOException {
                long start = System.nanoTime();
                out.write(b);
                stats.networkNanos.add(System.nanoTime() - start);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                long start = System.nanoTime();
                out.write(b, off, len);
                stats.networkNanos.add(System.nanoTime() - start);
            }

            @Override
            public void flush() throws IOException {
                long start = System.nanoTime();
                out.flush();
                stats.networkNanos.add(System.nanoTime() - start);
            }
        };
    }

    /**
     * Records one fully received PACS response.
     *
     * @param bytes number of bytes received in the response body
     * @param nanos time between sending the request and receiving the last byte
     */
    void record(long bytes, long nanos) {
        responseCount.increment();
        totalBytes.add(bytes);
        totalResponseNanos.add(nanos);
    }

    void recordWait(long nanos) {
        waitNanos.add(nanos);
    }

    void recordZip(long nanos) {
        zipNanos.add(nanos);
    }

    public long getResponseCount() {
        return responseCount.sum();
    }

    public long getTotalBytes() {
        return totalBytes.sum();
    }

    public long getWaitMillis() {
        return waitNanos.sum() / 1_000_000;
    }

    public long getZipMillis() {
        return zipNanos.sum() / 1_000_000;
    }

    public long getNetworkMillis() {
        return networkNanos.sum() / 1_000_000;
    }

    public double getAverageResponseMillis() {
        long count = responseCount.sum();
        return count == 0 ? 0 : totalResponseNanos.sum() / 1_000_000.0 / count;
    }

    /** Bytes received per second of PACS response time. */
    public double getBytesPerSecond() {
        long nanos = totalResponseNanos.sum();
        return nanos == 0 ? 0 : totalBytes.sum() * 1_000_000_000.0 / nanos;
    }

}
