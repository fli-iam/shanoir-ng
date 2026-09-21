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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.KeycloakUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Checks asynchronous deletion cascades received over RabbitMQ (subject / study deletion):
 * user attribution on cascaded events, examination cleanup before subject removal, and error reporting.
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

        event.setMessage("{\"irrelevant\":\"payload, objectMapper.readValue() is mocked\"}");
        rabbitMQDatasetsService.deleteStudy(event);

        verify(examinationService).deleteById(EXAM_ID, event);
        assertEquals(REQUESTING_USER_ID, userIdSeenDuringCascade.get());
    }

    @Test
    void deleteSubjectCompletesSuccessfullyWhenExaminationsAreDeleted() throws Exception {
        ShanoirEvent event = deleteSubjectEvent();
        Subject subject = new Subject(SUBJECT_ID, "phantom");

        when(objectMapper.readValue(anyString(), eq(ShanoirEvent.class))).thenReturn(event);
        when(subjectService.findById(SUBJECT_ID)).thenReturn(Optional.of(subject));
        when(examinationRepository.findBySubjectId(SUBJECT_ID)).thenReturn(new ArrayList<>(List.of(examination(EXAM_ID))));
        when(studyRepository.findAllById(any())).thenReturn(Collections.emptyList());

        rabbitMQDatasetsService.deleteSubject("{\"irrelevant\":\"payload\"}");

        verify(examinationService).deleteById(EXAM_ID, event);
        verify(subjectService).delete(SUBJECT_ID);
        verify(eventService).publishSuccessEvent(eq(event), contains("phantom"));
        verify(eventService, never()).publishErrorEvent(any(), anyString());
    }

    @Test
    void deleteSubjectReportsExaminationErrorAndStillAttemptsSubjectRemoval() throws Exception {
        ShanoirEvent event = deleteSubjectEvent();

        when(objectMapper.readValue(anyString(), eq(ShanoirEvent.class))).thenReturn(event);
        when(subjectService.findById(SUBJECT_ID)).thenReturn(Optional.of(new Subject(SUBJECT_ID, "phantom")));
        when(examinationRepository.findBySubjectId(SUBJECT_ID)).thenReturn(new ArrayList<>(List.of(examination(EXAM_ID))));
        when(studyRepository.findAllById(any())).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("copied examination")).when(examinationService).deleteById(eq(EXAM_ID), eq(event));

        rabbitMQDatasetsService.deleteSubject("{\"irrelevant\":\"payload\"}");

        verify(subjectService).delete(SUBJECT_ID);
        verify(eventService).publishErrorEvent(eq(event), contains("some of its data could not be deleted"));
        assertTrue(event.getReport().contains("Examination [" + EXAM_ID + "] could not be deleted"));
    }

    @Test
    void deleteSubjectReportsSubjectErrorWhenExaminationsAreDeletedButSubjectRemains() throws Exception {
        ShanoirEvent event = deleteSubjectEvent();

        when(objectMapper.readValue(anyString(), eq(ShanoirEvent.class))).thenReturn(event);
        when(subjectService.findById(SUBJECT_ID)).thenReturn(Optional.of(new Subject(SUBJECT_ID, "phantom")));
        when(examinationRepository.findBySubjectId(SUBJECT_ID)).thenReturn(new ArrayList<>(List.of(examination(EXAM_ID))));
        when(studyRepository.findAllById(any())).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("FK constraint examination.subject_id")).when(subjectService).delete(SUBJECT_ID);

        rabbitMQDatasetsService.deleteSubject("{\"irrelevant\":\"payload\"}");

        verify(examinationService).deleteById(EXAM_ID, event);
        verify(eventService).publishErrorEvent(eq(event), contains("some of its data could not be deleted"));
        assertTrue(event.getReport().contains("Subject [" + SUBJECT_ID + "] could not be deleted from ms datasets"));
        verify(eventService, never()).publishSuccessEvent(any(), anyString());
    }

    private ShanoirEvent deleteSubjectEvent() {
        return new ShanoirEvent(
                ShanoirEventType.DELETE_SUBJECT_EVENT,
                SUBJECT_ID.toString(),
                REQUESTING_USER_ID,
                "Deleting subject...",
                ShanoirEvent.IN_PROGRESS,
                0f,
                STUDY_ID);
    }

    private Examination examination(Long id) {
        Study study = new Study();
        study.setId(STUDY_ID);
        Examination exam = new Examination();
        exam.setId(id);
        exam.setStudy(study);
        return exam;
    }

}
