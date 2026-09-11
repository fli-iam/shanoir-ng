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

package org.shanoir.ng.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.KeycloakUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Checks that the deletions cascaded from a subject or study deletion (received asynchronously
 * over RabbitMQ) are attributed to the user who actually requested the deletion, instead of
 * falling back to the generic system user - which used to leave every dataset acquisition
 * deletion event of the cascade without any identifiable user in the study's history.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RabbitMQDatasetsServiceTest {

    private static final Long REQUESTING_USER_ID = 42L;
    private static final Long SUBJECT_ID = 10L;
    private static final Long EXAM_ID = 123L;
    private static final Long STUDY_ID = 99L;

    @Mock
    private SubjectService subjectService;

    @Mock
    private ExaminationRepository examinationRepository;

    @Mock
    private ExaminationService examinationService;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private BIDSService bidsService;

    @Mock
    private ShanoirEventService eventService;

    @Mock
    private StudyCardRepository studyCardRepository;

    @Mock
    private QualityCardRepository qualityCardRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RabbitMQDatasetsService rabbitMQDatasetsService;

    @Test
    void deleteSubjectForwardsReceivedEventAndAttributesCascadeToRequestingUser() throws Exception {
        ShanoirEvent event = new ShanoirEvent(
                ShanoirEventType.DELETE_SUBJECT_EVENT,
                SUBJECT_ID.toString(),
                REQUESTING_USER_ID,
                "Deleting subject...",
                ShanoirEvent.IN_PROGRESS,
                0f,
                STUDY_ID);

        when(objectMapper.readValue(anyString(), eq(ShanoirEvent.class))).thenReturn(event);
        when(subjectService.findById(SUBJECT_ID)).thenReturn(Optional.empty());

        Study study = new Study();
        study.setId(STUDY_ID);
        Examination exam = new Examination();
        exam.setId(EXAM_ID);
        exam.setStudy(study);
        when(examinationRepository.findBySubjectId(SUBJECT_ID)).thenReturn(new ArrayList<>(List.of(exam)));
        when(studyRepository.findAllById(any())).thenReturn(Collections.emptyList());

        // Captures the user id seen through KeycloakUtil (the security context set up
        // by deleteSubject()) at the very moment the examination cascade runs, exactly like
        // DatasetAcquisitionServiceImpl.deleteByIdCascade()
        AtomicReference<Long> userIdSeenDuringCascade = new AtomicReference<>();
        doAnswer(invocation -> {
            userIdSeenDuringCascade.set(KeycloakUtil.getTokenUserId());
            return null;
        }).when(examinationService).deleteById(eq(EXAM_ID), eq(event));

        rabbitMQDatasetsService.deleteSubject("{\"irrelevant\":\"payload, objectMapper.readValue() is mocked\"}");

        // The event received from ms-studies must be forwarded as-is, not dropped as null, so the
        // cascaded examination deletion can still publish its own trail of events.
        verify(examinationService).deleteById(EXAM_ID, event);
        // And every event published while processing the cascade must be attributed to the user
        // who actually requested the deletion, not to a generic system user.
        assertEquals(REQUESTING_USER_ID, userIdSeenDuringCascade.get());
    }

    @Test
    void deleteStudyForwardsReceivedEventAndAttributesCascadeToRequestingUser() throws Exception {
        ShanoirEvent event = new ShanoirEvent(
                ShanoirEventType.DELETE_STUDY_EVENT,
                STUDY_ID.toString(),
                REQUESTING_USER_ID,
                "Deleting study...",
                ShanoirEvent.IN_PROGRESS,
                0f,
                STUDY_ID);

        when(objectMapper.readValue(anyString(), eq(ShanoirEvent.class))).thenReturn(event);

        Examination exam = new Examination();
        exam.setId(EXAM_ID);
        when(examinationRepository.findByStudy_Id(STUDY_ID)).thenReturn(new ArrayList<>(List.of(exam)));

        AtomicReference<Long> userIdSeenDuringCascade = new AtomicReference<>();
        doAnswer(invocation -> {
            userIdSeenDuringCascade.set(KeycloakUtil.getTokenUserId());
            return null;
        }).when(examinationService).deleteById(eq(EXAM_ID), eq(event));

        rabbitMQDatasetsService.deleteStudy("{\"irrelevant\":\"payload, objectMapper.readValue() is mocked\"}");

        verify(examinationService).deleteById(EXAM_ID, event);
        assertEquals(REQUESTING_USER_ID, userIdSeenDuringCascade.get());
    }

}
