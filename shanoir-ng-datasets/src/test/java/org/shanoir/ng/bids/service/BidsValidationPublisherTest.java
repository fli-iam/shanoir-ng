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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * The external BIDS validator reads the request body as the plain path, so the request
 * must not go through the JSON message converter (which would send it quoted).
 */
@ExtendWith(MockitoExtension.class)
class BidsValidationPublisherTest {

    private static final String FILE_PATH = "/var/bids-data/study-7";

    private static final String RESULT_JSON = "{\"ok\":true}";

    @Mock
    private RabbitTemplate rabbit;

    @Spy
    private BidsValidationAwaiter awaiter;

    @InjectMocks
    private BidsValidationPublisher publisher;

    @Test
    void requestValidationSyncSendsRawPathAsPlainTextAndReturnsResult() {
        AtomicReference<Message> sent = new AtomicReference<>();
        doAnswer(invocation -> {
            Message message = invocation.getArgument(2);
            sent.set(message);
            // simulate the validator answering on the result queue
            awaiter.complete(message.getMessageProperties().getCorrelationId(), RESULT_JSON);
            return null;
        }).when(rabbit).send(eq(""), eq(BidsValidationConfiguration.BIDS_VALIDATION_REQUEST_QUEUE), any(Message.class));

        // the publisher waits for the result: fail fast instead of hanging if the request is not sent through send()
        String result = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> publisher.requestValidationSync(FILE_PATH));

        assertEquals(RESULT_JSON, result);
        MessageProperties properties = sent.get().getMessageProperties();
        assertEquals(FILE_PATH, new String(sent.get().getBody(), StandardCharsets.UTF_8));
        assertEquals(MessageProperties.CONTENT_TYPE_TEXT_PLAIN, properties.getContentType());
        assertEquals(StandardCharsets.UTF_8.name(), properties.getContentEncoding());
        assertNotNull(properties.getCorrelationId());
        verify(rabbit, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void requestValidationSyncRejectsNullPath() {
        assertThrows(IllegalArgumentException.class, () -> publisher.requestValidationSync(null));
        verify(rabbit, never()).send(anyString(), anyString(), any(Message.class));
    }

}
