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

package org.shanoir.ng.shared.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class PreclinicalSecurityServiceTest {

    private static final Long EXAMINATION_ID = 12L;
    private static final Long SUBJECT_ID = 34L;
    private static final Long STUDY_ID = 56L;
    private static final Long CENTER_ID = 78L;
    private static final String STUDY_CENTER_JSON = "{\"studyId\":56,\"centerId\":78}";

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StudyRightsService studyRightsService;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private PreclinicalSecurityService service;

    private MockedStatic<KeycloakUtil> keycloakUtil;

    @BeforeEach
    void mockUser() {
        keycloakUtil = Mockito.mockStatic(KeycloakUtil.class);
        keycloakUtil.when(KeycloakUtil::isAdmin).thenReturn(false);
    }

    @AfterEach
    void closeUserMock() {
        keycloakUtil.close();
    }

    @Test
    void adminHasEveryRightWithoutAskingDatasets() {
        keycloakUtil.when(KeycloakUtil::isAdmin).thenReturn(true);

        assertTrue(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
        assertTrue(service.hasRightOnSubject(SUBJECT_ID, "CAN_IMPORT"));
        assertTrue(service.hasRightOnAtLeastOneStudy("CAN_IMPORT"));
        verify(rabbitTemplate, never()).convertSendAndReceive(anyString(), any(Object.class));
    }

    @Test
    void examinationRightNeedsTheRightOnItsStudyAndCenter() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenReturn(STUDY_CENTER_JSON);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, "CAN_IMPORT")).thenReturn(true);
        when(studyRightsService.hasRightOnCenter(STUDY_ID, CENTER_ID)).thenReturn(true);

        assertTrue(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
    }

    @Test
    void examinationRightIsRefusedWithoutTheRightOnItsStudy() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenReturn(STUDY_CENTER_JSON);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, "CAN_IMPORT")).thenReturn(false);

        assertFalse(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
        // the center check would throw for a user who is not a member of the study
        verify(studyRightsService, never()).hasRightOnCenter(anyLong(), anyLong());
    }

    @Test
    void examinationRightIsRefusedOutsideTheCentersOfTheUser() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenReturn(STUDY_CENTER_JSON);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, "CAN_IMPORT")).thenReturn(true);
        when(studyRightsService.hasRightOnCenter(STUDY_ID, CENTER_ID)).thenReturn(false);

        assertFalse(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
    }

    @Test
    void examinationRightIsRefusedForAnUnknownExamination() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenReturn("");

        assertFalse(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
        assertFalse(service.hasRightOnExamination(null, "CAN_IMPORT"));
        verify(studyRightsService, never()).hasRightOnStudy(anyLong(), anyString());
    }

    @Test
    void examinationRightIsRefusedWhenDatasetsDoesNotAnswer() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenReturn(null);
        assertFalse(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));

        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenThrow(new AmqpTimeoutException("timeout"));
        assertFalse(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
    }

    @Test
    void examinationRightIsRefusedForAnUnreadableAnswer() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, EXAMINATION_ID))
                .thenReturn("not json");

        assertFalse(service.hasRightOnExamination(EXAMINATION_ID, "CAN_IMPORT"));
    }

    @Test
    void subjectRightNeedsTheRightOnItsStudy() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, SUBJECT_ID))
                .thenReturn(STUDY_ID);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, "CAN_SEE_ALL")).thenReturn(true);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, "CAN_IMPORT")).thenReturn(false);

        assertTrue(service.hasRightOnSubject(SUBJECT_ID, "CAN_SEE_ALL"));
        assertFalse(service.hasRightOnSubject(SUBJECT_ID, "CAN_IMPORT"));
    }

    @Test
    void subjectRightAcceptsAnIntegerStudyId() {
        // the json converter may read a small number back as an Integer
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, SUBJECT_ID))
                .thenReturn(56);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, "CAN_SEE_ALL")).thenReturn(true);

        assertTrue(service.hasRightOnSubject(SUBJECT_ID, "CAN_SEE_ALL"));
    }

    @Test
    void subjectRightIsRefusedForAnUnknownSubject() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, SUBJECT_ID))
                .thenReturn(RabbitMQConfiguration.UNKNOWN_STUDY_ID);

        assertFalse(service.hasRightOnSubject(SUBJECT_ID, "CAN_SEE_ALL"));
        assertFalse(service.hasRightOnSubject(null, "CAN_SEE_ALL"));
        verify(studyRightsService, never()).hasRightOnStudy(anyLong(), anyString());
    }

    @Test
    void subjectRightIsRefusedWhenDatasetsDoesNotAnswer() {
        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, SUBJECT_ID))
                .thenReturn(null);
        assertFalse(service.hasRightOnSubject(SUBJECT_ID, "CAN_SEE_ALL"));

        when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, SUBJECT_ID))
                .thenThrow(new AmqpTimeoutException("timeout"));
        assertFalse(service.hasRightOnSubject(SUBJECT_ID, "CAN_SEE_ALL"));
    }

    @Test
    void rightOnAtLeastOneStudyIsCheckedOnTheLocalStudyMembers() {
        when(studyRightsService.hasRightOnAtLeastOneStudy("CAN_IMPORT")).thenReturn(true);
        when(studyRightsService.hasRightOnAtLeastOneStudy("CAN_ADMINISTRATE")).thenReturn(false);

        assertTrue(service.hasRightOnAtLeastOneStudy("CAN_IMPORT"));
        assertFalse(service.hasRightOnAtLeastOneStudy("CAN_ADMINISTRATE"));
        verify(rabbitTemplate, never()).convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE), any(Object.class));
    }

}
