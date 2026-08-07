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

package org.shanoir.ng.subject.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.configuration.amqp.RabbitMQSubjectService;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for RabbitMQSubjectService class.
 * @author fli
 *
 */
@SpringBootTest
@ActiveProfiles("test")
public class RabbitMQSubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectService subjectService;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private RabbitMQSubjectService rabbitMQSubjectService;

    private Long studyId = 1L;

    @Test
    public void testGetSubjetsForStudy() throws JsonProcessingException {
        SimpleSubjectDTO dto = new SimpleSubjectDTO();
        String ident = "subjectIdentifier";
        dto.setIdentifier(ident);
        Mockito.when(mapper.writeValueAsString(Mockito.any())).thenReturn(ident);

        // GIVEN a study ID, retrieve all associated subjects
        String result = rabbitMQSubjectService.getSubjectsForStudy(studyId.toString());
        assertNotNull(result);
        assertTrue(result.contains(ident));
    }

    @Test
    public void testGetSubjetsForStudyFail() throws JsonProcessingException {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
        // GIVEN a study ID, retrieve all associated subjects
            rabbitMQSubjectService.getSubjectsForStudy("non parsable long");
        });
    }
}
