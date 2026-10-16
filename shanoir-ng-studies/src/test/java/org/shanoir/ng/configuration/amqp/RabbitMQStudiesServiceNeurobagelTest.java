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

package org.shanoir.ng.configuration.amqp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Neurobagel participants.tsv listener only answers for exported studies.
 */
@ExtendWith(MockitoExtension.class)
class RabbitMQStudiesServiceNeurobagelTest {

    private static final Long STUDY_ID = 7L;

    private static final String TSV = "participant_id\nsub-1\n";

    @Mock
    private StudyRepository studyRepo;

    @Mock
    private BIDSService bidsService;

    @Spy
    private ObjectMapper mapper = new JsonMapper();

    @InjectMocks
    private RabbitMQStudiesService service;

    private static String request(Long studyId) {
        return studyId == null ? "{}" : "{\"studyId\":" + studyId + ",\"firstExaminationDates\":{\"12\":\"2020-03-15\"}}";
    }

    private static Study study(boolean exported, boolean draft) {
        Study study = new Study();
        study.setId(STUDY_ID);
        study.setNeurobagelExport(exported);
        study.setIsDraft(draft);
        return study;
    }

    @Test
    void exportedStudyGetsItsParticipants() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.of(study(true, false)));
        given(bidsService.participantsTsv(STUDY_ID, Map.of(12L, LocalDate.of(2020, 3, 15)))).willReturn(TSV);
        assertEquals(TSV, service.neurobagelParticipantsTsv(request(STUDY_ID)));
    }

    @Test
    void studyNotFlaggedGetsNothing() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.of(study(false, false)));
        assertNull(service.neurobagelParticipantsTsv(request(STUDY_ID)));
        verify(bidsService, never()).participantsTsv(anyLong(), any());
    }

    @Test
    void draftStudyGetsNothing() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.of(study(true, true)));
        assertNull(service.neurobagelParticipantsTsv(request(STUDY_ID)));
        verify(bidsService, never()).participantsTsv(anyLong(), any());
    }

    @Test
    void unknownStudyGetsNothing() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.empty());
        assertNull(service.neurobagelParticipantsTsv(request(STUDY_ID)));
        verify(bidsService, never()).participantsTsv(anyLong(), any());
    }

    @Test
    void nullStudyIdGetsNothing() {
        assertNull(service.neurobagelParticipantsTsv(request(null)));
        verify(bidsService, never()).participantsTsv(anyLong(), any());
    }

}
