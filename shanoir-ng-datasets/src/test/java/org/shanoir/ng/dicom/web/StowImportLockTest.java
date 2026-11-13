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


package org.shanoir.ng.dicom.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.Test;

class StowImportLockTest {

    private static final String STUDY_ID = "1";

    private static final String PATIENT = "SUBJECT^ONE";

    private final StowImportLock stowImportLock = new StowImportLock();

    @Test
    void sameSubjectGetsTheSameLock() {
        assertSame(stowImportLock.lockFor(STUDY_ID, PATIENT), stowImportLock.lockFor(new String(STUDY_ID), new String(PATIENT)));
    }

    @Test
    void missingStudyOrPatientDoesNotFail() {
        assertSame(stowImportLock.lockFor(null, null), stowImportLock.lockFor(null, null));
        assertSame(stowImportLock.lockFor(STUDY_ID, null), stowImportLock.lockFor(STUDY_ID, null));
    }

    @Test
    void instancesOfOneSubjectNeverRunTogether() throws Exception {
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxRunning = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                Lock lock = stowImportLock.lockFor(STUDY_ID, PATIENT);
                lock.lock();
                try {
                    maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
                    Thread.sleep(20);
                    running.decrementAndGet();
                } finally {
                    lock.unlock();
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        assertEquals(1, maxRunning.get());
    }

}
