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
package org.shanoir.ng.bids.service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Service;

@Service
public class BidsTreeSemaphore {

    private final Map<BidsTreeLockKey, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    public void lockOrThrow(Long studyId, Long userId) throws BidsTreeLockedException {
        BidsTreeLockKey key = new BidsTreeLockKey(userId.toString(), studyId.toString());
        Semaphore semaphore = semaphoreMap.computeIfAbsent(key, k -> new Semaphore(1));
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            throw new BidsTreeLockedException();
        }
    }

    public void unlock(Long studyId, Long userId) {
        BidsTreeLockKey key = new BidsTreeLockKey(userId.toString(), studyId.toString());
        Semaphore semaphore = semaphoreMap.computeIfAbsent(key, k -> new Semaphore(1));
        semaphore.release();
    }

    public boolean isLocked(Long studyId, Long userId) {
        BidsTreeLockKey key = new BidsTreeLockKey(userId.toString(), studyId.toString());
        Semaphore semaphore = semaphoreMap.computeIfAbsent(key, k -> new Semaphore(1));
        return semaphore.availablePermits() == 0;
    }

    /**
     * Wait for unlock up to the specified timeout.
     *
     * @param timeout Timeout duration.
     * @param unit    Timeout unit.
     * @return true if unlocked within the timeout, false otherwise.
     */
    public boolean awaitUnlock(Long studyId, Long userId, long timeout, java.util.concurrent.TimeUnit unit) {
        BidsTreeLockKey key = new BidsTreeLockKey(userId.toString(), studyId.toString());
        Semaphore semaphore = semaphoreMap.computeIfAbsent(key, k -> new Semaphore(1));
        try {
            boolean acquired = semaphore.tryAcquire(timeout, unit);
            if (!acquired) return false;
            semaphore.release();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for BIDS tree unlock", e);
        }
    }
}
