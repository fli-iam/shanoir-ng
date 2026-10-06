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

package org.shanoir.ng.neurobagel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NeurobagelServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NeurobagelService service;

    @Test
    void findExportedStudiesReturnsIdsAndNames() {
        given(studyRepository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc())
                .willReturn(List.of(new Study(1L, "first", false), new Study(4L, "second", false)));

        List<NeurobagelStudyDTO> studies = service.findExportedStudies();

        assertEquals(List.of(1L, 4L), studies.stream().map(NeurobagelStudyDTO::getId).toList());
        assertEquals(List.of("first", "second"), studies.stream().map(NeurobagelStudyDTO::getName).toList());
    }

    @Test
    void exportedStudyIsFound() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L)).willReturn(true);
        given(studyRepository.findById(1L)).willReturn(Optional.of(new Study(1L, "first", false)));

        Optional<NeurobagelStudyDTO> study = service.findExportedStudy(1L);

        assertTrue(study.isPresent());
        assertEquals("first", study.get().getName());
    }

    @Test
    void notExportedStudyIsNotFoundAndNotLoaded() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(2L)).willReturn(false);

        assertFalse(service.findExportedStudy(2L).isPresent());
        verify(studyRepository, never()).findById(anyLong());
    }

    @Test
    void nullStudyIdIsNotExported() {
        assertFalse(service.isExported(null));
    }

    @Test
    void participantsOfAnExportedStudyComeFromStudies() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L)).willReturn(true);
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NEUROBAGEL_PARTICIPANTS_TSV, "1"))
                .willReturn("participant_id\nsub-1\n");

        assertEquals(Optional.of("participant_id\nsub-1\n"), service.findParticipantsTsv(1L));
    }

    @Test
    void participantsOfAStudyNotExportedAreNeverRequested() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(2L)).willReturn(false);

        assertFalse(service.findParticipantsTsv(2L).isPresent());
        verify(rabbitTemplate, never()).convertSendAndReceive(anyString(), any(Object.class));
    }

    @Test
    void noAnswerFromStudiesMeansNoParticipants() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L)).willReturn(true);
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NEUROBAGEL_PARTICIPANTS_TSV, "1")).willReturn(null);

        assertFalse(service.findParticipantsTsv(1L).isPresent());
    }

    @Test
    void participantsDictionaryAnnotatesTheParticipantsColumns() throws Exception {
        JsonNode dictionary = new ObjectMapper().readTree(service.getParticipantsDictionary());

        assertEquals(List.of("participant_id", "subject_identifier", "subject_age", "subject_sex"),
                iterableToList(dictionary.fieldNames()));
        assertEquals("nb:ParticipantID", dictionary.at("/participant_id/Annotations/IsAbout/TermURL").asText());
        assertEquals("nb:Age", dictionary.at("/subject_age/Annotations/IsAbout/TermURL").asText());
        assertEquals("nb:Sex", dictionary.at("/subject_sex/Annotations/IsAbout/TermURL").asText());
        assertEquals(List.of("M", "F", "O"), iterableToList(dictionary.at("/subject_sex/Annotations/Levels").fieldNames()));
    }

    private static List<String> iterableToList(Iterator<String> names) {
        List<String> list = new ArrayList<>();
        names.forEachRemaining(list::add);
        return list;
    }

}
