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

package org.shanoir.ng.tasks;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for AsyncTaskService.
 * Testing if the task is well sent to users MS everytime
 * @author fli
 *
 */

@SpringBootTest
@ActiveProfiles("test")
public class ShanoirEventServiceTest {

    @Autowired
    private ShanoirEventService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @Test
    public void testAddTask() {
        // GIVEN a new task to add
        ShanoirEvent t = new ShanoirEvent();
        t.setId(Long.valueOf(123));
        t.setUserId(Long.valueOf(456));
        t.setMessage("uio");

        // WHEN we add the task
        service.publishEvent(t);

        // THEN the task is sent using RabbitMQ and sent to the front
        ArgumentCaptor<String> argumentCatcher = ArgumentCaptor.forClass(String.class);
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.eq(RabbitMQConfiguration.EVENTS_EXCHANGE), Mockito.eq(t.getEventType()), argumentCatcher.capture());
        String message = argumentCatcher.getValue();
        assertNotNull(message);
        assertTrue(message.contains(t.getId().toString()));
        assertTrue(message.contains(t.getMessage()));
        assertTrue(message.contains("" + t.getUserId()));
    }
}
