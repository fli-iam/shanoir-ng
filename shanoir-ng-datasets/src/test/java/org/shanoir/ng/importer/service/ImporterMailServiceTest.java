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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

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
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.EmailDatasetsCopied;
import org.shanoir.ng.shared.email.EmailExtraDataAdded;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
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

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private ExaminationRepository examinationRepository;

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

    @Test
    public void sendDatasetsCopiedEmailListsTheNewExaminations() throws Exception {
        // GIVEN 3 datasets copied into 2 new examinations, one member asking for import reports
        givenMembers(List.of(studyUser(1L, true), studyUser(2L, false)));
        Mockito.when(studyRepository.findById(STUDY_ID)).thenReturn(Optional.of(examination.getStudy()));
        Examination second = new Examination();
        second.setId(8L);
        second.setSubject(new Subject(6L, "OtherSubject"));
        // the repository returns them in another order than the copy
        Mockito.when(examinationRepository.findAllById(List.of(3L, 8L))).thenReturn(List.of(second, examination));

        // WHEN the copy notification is sent, with a duplicate and a null examination id
        importerMailService.sendDatasetsCopiedEmail(STUDY_ID, 3, Arrays.asList(3L, null, 8L, 3L), 9L);

        // THEN one mail lists both examinations in the copy order
        EmailDatasetsCopied mail = captureMail(RabbitMQConfiguration.DATASETS_COPIED_MAIL_QUEUE, EmailDatasetsCopied.class);
        assertEquals(List.of(1L), mail.getRecipients());
        assertEquals("StudyName", mail.getStudyName());
        assertEquals(3, mail.getCopiedDatasetCount());
        assertEquals(2, mail.getTotalExaminations());
        assertEquals(List.of(3L, 8L), List.copyOf(mail.getExaminations().keySet()));
        assertEquals("SubjectName - 2026-10-22", mail.getExaminations().get(3L));
        assertEquals("OtherSubject", mail.getExaminations().get(8L));
        assertEquals(9L, mail.getUserId());
    }

    @Test
    public void sendDatasetsCopiedEmailTruncatesTheExaminationList() throws Exception {
        givenMembers(List.of(studyUser(1L, true)));
        List<Long> examIds = LongStream.rangeClosed(1, 120).boxed().toList();
        Mockito.when(examinationRepository.findAllById(Mockito.anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return ids.stream().map(id -> {
                Examination exam = new Examination();
                exam.setId(id);
                return exam;
            }).toList();
        });

        importerMailService.sendDatasetsCopiedEmail(STUDY_ID, 120, examIds, 9L);

        EmailDatasetsCopied mail = captureMail(RabbitMQConfiguration.DATASETS_COPIED_MAIL_QUEUE, EmailDatasetsCopied.class);
        assertEquals(ImporterMailService.MAX_COPIED_EXAMINATIONS_IN_MAIL, mail.getExaminations().size());
        assertEquals(120, mail.getTotalExaminations());
        assertTrue(mail.getExaminations().containsKey(1L));
    }

    @Test
    public void sendDatasetsCopiedEmailWithoutCopiedDatasetSendsNothing() {
        importerMailService.sendDatasetsCopiedEmail(STUDY_ID, 0, List.of(3L), 9L);

        Mockito.verifyNoInteractions(studyUserRightRepo, rabbitTemplate, examinationRepository);
    }

    private <T> T captureMail(String queue, Class<T> type) throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.eq(queue), captor.capture());
        return objectMapper.readValue(captor.getValue(), type);
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
