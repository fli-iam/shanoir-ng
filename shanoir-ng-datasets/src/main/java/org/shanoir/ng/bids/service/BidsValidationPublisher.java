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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
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
        rabbit.send("", BidsValidationConfiguration.BIDS_VALIDATION_REQUEST_QUEUE, buildRequest(filePath, null));
    }

    public String requestValidationSync(String filePath) throws AmqpException {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath cannot be null");
        }
        String correlationId = awaiter.newCorrelationId();
        CompletableFuture<String> future = awaiter.register(correlationId);
        rabbit.send("", BidsValidationConfiguration.BIDS_VALIDATION_REQUEST_QUEUE, buildRequest(filePath, correlationId));
        String resultJson;
        try {
            resultJson = future.get(30, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AmqpException("Error waiting for BIDS validation result", e);
        }
        return resultJson;
    }

    /**
     * Build the request as raw text: the external validator reads the message body as the plain path,
     * so it must not go through the JSON message converter, which would quote it.
     */
    private Message buildRequest(String filePath, String correlationId) {
        return MessageBuilder.withBody(filePath.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setCorrelationId(correlationId)
                .build();
    }

}
