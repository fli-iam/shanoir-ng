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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidsValidationPublisher {

    @Autowired
    private RabbitTemplate rabbit;

    @Autowired
    private BidsValidationAwaiter awaiter;

    public void requestValidationAsync(String filePath) throws AmqpException {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath cannot be null");
        }
        rabbit.convertAndSend("", BidsValidationConfiguration.BIDS_VALIDATION_REQUEST_QUEUE, filePath);
    }

    public String requestValidationSync(String filePath) throws AmqpException {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath cannot be null");
        }
        String correlationId = awaiter.newCorrelationId();
        CompletableFuture<String> future = awaiter.register(correlationId);
        rabbit.convertAndSend("", BidsValidationConfiguration.BIDS_VALIDATION_REQUEST_QUEUE, filePath, m -> {
            m.getMessageProperties().setCorrelationId(correlationId);
            m.getMessageProperties().setContentType("text/plain");
            m.getMessageProperties().setContentEncoding("UTF-8");
            return m;
        });
        String resultJson;
        try {
            resultJson = future.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AmqpException("Error waiting for BIDS validation result", e);
        }
        return resultJson;
    }

}
