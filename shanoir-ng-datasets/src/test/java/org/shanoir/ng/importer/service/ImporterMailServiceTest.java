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

package org.shanoir.ng.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.EmailExtraDataAdded;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ImporterMailServiceTest {

    private static final Long STUDY_ID = 7L;

    @Mock
    private StudyUserRightsRepository studyUserRightRepo;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ImporterMailService importerMailService;

    private Examination examination;

    @BeforeEach
    public void setUp() {
        examination = new Examination();
        examination.setId(3L);
        examination.setExaminationDate(LocalDate.of(2026, 10, 22));
        examination.setStudy(new Study(STUDY_ID, "StudyName", false));
        examination.setSubject(new Subject(5L, "SubjectName"));
    }

    @Test
    public void sendExtraDataEmailOnlyToMembersWithImportReport() throws Exception {
        // GIVEN two study members, only one of them asking for import reports
        givenMembers(List.of(studyUser(1L, true), studyUser(2L, false)));

        // WHEN files are added to an acquisition of the examination
        importerMailService.sendExtraDataEmail(examination, 4L, List.of("protocol.pdf"), 9L);

        // THEN one mail is sent to the member asking for import reports
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.eq(RabbitMQConfiguration.EXTRA_DATA_MAIL_QUEUE), captor.capture());
        EmailExtraDataAdded mail = objectMapper.readValue(captor.getValue(), EmailExtraDataAdded.class);
        assertEquals(List.of(1L), mail.getRecipients());
        assertEquals(STUDY_ID, mail.getStudyId());
        assertEquals("StudyName", mail.getStudyName());
        assertEquals("SubjectName", mail.getSubjectName());
        assertEquals("3", mail.getExaminationId());
        assertEquals("2026-10-22", mail.getExamDate());
        assertEquals("4", mail.getDatasetAcquisitionId());
        assertEquals(List.of("protocol.pdf"), mail.getFileNames());
        assertEquals(9L, mail.getUserId());
    }

    @Test
    public void sendExtraDataEmailOnExaminationHasNoAcquisitionId() throws Exception {
        givenMembers(List.of(studyUser(1L, true)));

        importerMailService.sendExtraDataEmail(examination, null, List.of("protocol.pdf"), 9L);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.eq(RabbitMQConfiguration.EXTRA_DATA_MAIL_QUEUE), captor.capture());
        assertNull(objectMapper.readValue(captor.getValue(), EmailExtraDataAdded.class).getDatasetAcquisitionId());
    }

    @Test
    public void sendExtraDataEmailWithoutFilesSendsNothing() {
        importerMailService.sendExtraDataEmail(examination, null, Collections.emptyList(), 9L);

        Mockito.verifyNoInteractions(studyUserRightRepo, rabbitTemplate);
    }

    @Test
    public void sendExtraDataEmailWithoutRecipientsSendsNothing() {
        givenMembers(List.of(studyUser(2L, false)));

        importerMailService.sendExtraDataEmail(examination, null, List.of("protocol.pdf"), 9L);

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    private void givenMembers(List<StudyUser> members) {
        Mockito.when(studyUserRightRepo.findByStudyId(STUDY_ID)).thenReturn(members);
    }

    private StudyUser studyUser(Long userId, boolean receiveNewImportReport) {
        StudyUser studyUser = new StudyUser();
        studyUser.setUserId(userId);
        studyUser.setStudyId(STUDY_ID);
        studyUser.setReceiveNewImportReport(receiveNewImportReport);
        return studyUser;
    }

}
