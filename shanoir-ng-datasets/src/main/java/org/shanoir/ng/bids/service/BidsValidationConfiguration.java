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

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BidsValidationConfiguration {

    public static final String BIDS_VALIDATION_REQUEST_QUEUE = "bids.validate";

    public static final String BIDS_VALIDATION_RESULT_QUEUE = "bids.validated";

    @Bean
    public Queue bidsValidate() {
        return QueueBuilder.durable(BIDS_VALIDATION_REQUEST_QUEUE).build();
    }

    @Bean
    public Queue bidsValidated() {
        return QueueBuilder.durable(BIDS_VALIDATION_RESULT_QUEUE).build();
    }
}
