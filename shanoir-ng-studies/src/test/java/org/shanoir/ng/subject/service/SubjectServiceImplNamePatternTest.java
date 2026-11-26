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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyExtraDetails;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpStatus;

/**
 * Which subject creations check the study's subject name pattern.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubjectServiceImplNamePatternTest {

    private static final Long STUDY_ID = 10L;

    private static final String NOT_MATCHING_NAME = "wrong";

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private ShanoirEventService eventService;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private MockedStatic<KeycloakUtil> keycloakUtil;

    private Study study;

    @BeforeEach
    public void setup() {
        keycloakUtil = Mockito.mockStatic(KeycloakUtil.class);
        keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

        study = new Study();
        study.setId(STUDY_ID);
        study.setExtraDetails(new StudyExtraDetails());
        study.getExtraDetails().setSubjectNamePattern("^STUDY_[0-9]{3}$");
        given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(study));
        given(studyRepository.findIsDraftById(STUDY_ID)).willReturn(Optional.of(false));
        given(subjectRepository.save(any(Subject.class))).willAnswer(invocation -> {
            Subject subject = invocation.getArgument(0);
            subject.setId(100L);
            return subject;
        });
    }

    @AfterEach
    public void tearDown() {
        keycloakUtil.close();
    }

    @Test
    public void createRejectsANameNotMatchingThePattern() {
        ShanoirException exception = assertThrows(ShanoirException.class,
                () -> subjectService.create(subject(NOT_MATCHING_NAME), false));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorCode());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    public void createAcceptsANameMatchingThePattern() throws ShanoirException {
        assertEquals("STUDY_001", subjectService.create(subject("STUDY_001"), false).getName());
    }

    @Test
    public void createWithoutPatternCheckAcceptsAnyName() throws ShanoirException {
        assertEquals(NOT_MATCHING_NAME, subjectService.create(subject(NOT_MATCHING_NAME), false, false).getName());
    }

    @Test
    public void preclinicalSubjectIsNotChecked() throws ShanoirException {
        Subject subject = subject(NOT_MATCHING_NAME);
        subject.setPreclinical(true);

        assertEquals(NOT_MATCHING_NAME, subjectService.create(subject, false).getName());
    }

    @Test
    public void autoIncrementNameIsNotChecked() throws ShanoirException {
        // ShanoirUploader sends no name in auto-increment mode, the server generates it
        Subject created = subjectService.createAutoIncrement(subject(null), 1L, false);

        assertEquals("0010001", created.getName());
    }

    private Subject subject(String name) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setStudy(study);
        return subject;
    }
}
