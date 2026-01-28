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

import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BidsValidationResultListener {

    @Autowired private BidsValidationAwaiter awaiter;

    @RabbitListener(queues = BidsValidationConfiguration.BIDS_VALIDATION_RESULT_QUEUE)
    public void onResult(Message message) throws ShanoirException {
        String correlationId = message.getMessageProperties().getCorrelationId();
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        if (correlationId == null) {
            throw new ShanoirException("Received BIDS validation result message without correlation ID");
        }
        awaiter.complete(correlationId, json);
    }
}
