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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class BidsValidationAwaiter {

    private final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public String newCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public CompletableFuture<String> register(String correlationId) {
        CompletableFuture<String> f = new CompletableFuture<>();
        pending.put(correlationId, f);
        return f;
    }

    public void complete(String correlationId, String json) {
        CompletableFuture<String> f = pending.remove(correlationId);
        if (f != null) {
            f.complete(json);
        }
    }

    public void fail(String correlationId, Throwable t) {
        CompletableFuture<String> f = pending.remove(correlationId);
        if (f != null) {
            f.completeExceptionally(t);
        }
    }
}
