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

package org.shanoir.ng.study.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.dto.CopyData;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyExtraDetails;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import jakarta.persistence.EntityManager;
import tools.jackson.databind.ObjectMapper;

/**
 * Subject naming when copying datasets to a study with a subject name pattern.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RelatedDatasetServiceImplTest {

    private static final Long TARGET_STUDY_ID = 10L;
    private static final Long CENTER_ID = 1L;

    @Mock
    private StudyService studyService;

    @Mock
    private CenterRepository centerRepository;

    @Mock
    private SubjectService subjectService;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ShanoirEventService eventService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RelatedDatasetServiceImpl relatedDatasetService;

    private MockedStatic<KeycloakUtil> keycloakUtil;

    private Study targetStudy;

    private final AtomicLong nextSubjectId = new AtomicLong(100);

    @BeforeEach
    public void setup() throws ShanoirException {
        keycloakUtil = Mockito.mockStatic(KeycloakUtil.class);
        keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

        targetStudy = new Study();
        targetStudy.setId(TARGET_STUDY_ID);
        targetStudy.setName("Target");
        targetStudy.setExtraDetails(new StudyExtraDetails());
        targetStudy.setStudyCenterList(new ArrayList<>());
        given(studyService.findById(TARGET_STUDY_ID)).willReturn(targetStudy);
        given(subjectRepository.findByStudyIdAndNameIn(eq(TARGET_STUDY_ID), any())).willReturn(List.of());
        given(subjectRepository.findNamesByStudyIdAndNameStartingWith(eq(TARGET_STUDY_ID), anyString())).willReturn(List.of());
        given(subjectService.create(any(Subject.class), eq(false))).willAnswer(invocation -> {
            Subject subject = invocation.getArgument(0);
            subject.setId(nextSubjectId.getAndIncrement());
            return subject;
        });
    }

    @AfterEach
    public void tearDown() {
        keycloakUtil.close();
    }

    @Test
    public void emptyNamesAreGeneratedWithSuccessiveIdentifiers() throws ShanoirException {
        targetStudy.getExtraDetails().setSubjectNamePattern("^STUDY_[0-9]{3}$");
        // "STUDY_x" and "STUDY_0005" can't come from this pattern and must not shift the numbering
        given(subjectRepository.findNamesByStudyIdAndNameStartingWith(TARGET_STUDY_ID, "STUDY_"))
                .willReturn(List.of("STUDY_002", "STUDY_x", "STUDY_0005"));
        givenSourceSubjects(1L, 2L);

        relatedDatasetService.copyData(copyData(subjectCopy(1L, null), subjectCopy(2L, " ")));

        assertEquals(List.of("STUDY_003", "STUDY_004"), createdSubjectNames());
        verify(eventService, never()).publishErrorEvent(any(), anyString());
    }

    @Test
    public void generatedNameUsesTheCenterIndexOfTheCopiedDatasets() throws ShanoirException {
        targetStudy.getExtraDetails().setSubjectNamePattern("^STUDY_(01|02)_[0-9]{3}$");
        targetStudy.getStudyCenterList().add(studyCenter(CENTER_ID, "02"));
        givenSourceSubjects(1L);

        relatedDatasetService.copyData(copyData(subjectCopy(1L, null)));

        assertEquals(List.of("STUDY_02_001"), createdSubjectNames());
    }

    @Test
    public void centerWithoutIndexInPatternIsRejected() throws ShanoirException {
        targetStudy.getExtraDetails().setSubjectNamePattern("^STUDY_(01|02)_[0-9]{3}$");
        targetStudy.getStudyCenterList().add(studyCenter(CENTER_ID, null));
        givenSourceSubjects(1L);

        ShanoirException exception = assertThrows(ShanoirException.class,
                () -> relatedDatasetService.copyData(copyData(subjectCopy(1L, null))));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorCode());
        verify(subjectService, never()).create(any(Subject.class), Mockito.anyBoolean());
        verifyCopyClosedInError(exception.getMessage());
    }

    @Test
    public void enteredNameIsKeptAsIs() throws ShanoirException {
        targetStudy.getExtraDetails().setSubjectNamePattern("^STUDY_[0-9]{3}$");
        givenSourceSubjects(1L);

        relatedDatasetService.copyData(copyData(subjectCopy(1L, "STUDY_042")));

        assertEquals(List.of("STUDY_042"), createdSubjectNames());
    }

    @Test
    public void enteredNameRejectedBySubjectCreationClosesTheCopyInError() throws ShanoirException {
        targetStudy.getExtraDetails().setSubjectNamePattern("^STUDY_[0-9]{3}$");
        givenSourceSubjects(1L);
        // the pattern check itself is done by SubjectServiceImpl.create
        given(subjectService.create(any(Subject.class), eq(false))).willThrow(new ShanoirException(
                "Subject name \"wrong\" does not match the pattern required by this study.",
                HttpStatus.UNPROCESSABLE_ENTITY.value()));

        ShanoirException exception = assertThrows(ShanoirException.class,
                () -> relatedDatasetService.copyData(copyData(subjectCopy(1L, "wrong"))));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorCode());
        verifyCopyClosedInError(exception.getMessage());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString());
    }

    private void givenSourceSubjects(Long... ids) {
        List<Subject> subjects = new ArrayList<>();
        for (Long id : ids) {
            Subject subject = new Subject();
            subject.setId(id);
            subject.setName("source-" + id);
            subjects.add(subject);
        }
        given(subjectRepository.findWithTagsByIdIn(any())).willReturn(subjects);
    }

    private static CopyData.SubjectCopy subjectCopy(Long id, String newName) {
        CopyData.SubjectCopy subjectCopy = new CopyData.SubjectCopy();
        subjectCopy.setId(id);
        subjectCopy.setNewName(newName);
        subjectCopy.setCenterId(CENTER_ID);
        return subjectCopy;
    }

    private static CopyData copyData(CopyData.SubjectCopy... subjects) {
        CopyData copyData = new CopyData();
        copyData.setTargetStudyId(TARGET_STUDY_ID);
        copyData.setDatasetIds(new ArrayList<>(List.of(1000L)));
        copyData.setCenterIds(List.of(CENTER_ID));
        copyData.setSubjects(List.of(subjects));
        return copyData;
    }

    private static StudyCenter studyCenter(Long centerId, String subjectNamePrefix) {
        Center center = new Center();
        center.setId(centerId);
        StudyCenter studyCenter = new StudyCenter();
        studyCenter.setCenter(center);
        studyCenter.setSubjectNamePrefix(subjectNamePrefix);
        return studyCenter;
    }

    private List<String> createdSubjectNames() throws ShanoirException {
        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectService, Mockito.atLeastOnce()).create(captor.capture(), eq(false));
        return captor.getAllValues().stream().map(Subject::getName).toList();
    }

    private void verifyCopyClosedInError(String cause) {
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(eventService).publishErrorEvent(any(ShanoirEvent.class), message.capture());
        assertTrue(message.getValue().contains(cause), message.getValue());
    }
}
