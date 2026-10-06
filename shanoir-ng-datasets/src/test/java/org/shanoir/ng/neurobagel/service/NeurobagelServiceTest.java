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
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.neurobagel.dto.NeurobagelDatasetDescriptionDTO;
import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.shanoir.ng.neurobagel.repository.NeurobagelImagingRepository;
import org.shanoir.ng.neurobagel.repository.NeurobagelImagingRow;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.tag.model.StudyTag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NeurobagelServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private NeurobagelImagingRepository imagingRepository;

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

    @Test
    void datasetDescriptionOfAnExportedStudy() {
        ReflectionTestUtils.setField(service, "frontServerAddress", "https://shanoir.example.org/shanoir-ng");
        Study study = new Study(1L, "My study", false);
        study.setStudyTags(Set.of(studyTag("mri"), studyTag("adhd")));
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L)).willReturn(true);
        given(studyRepository.findByIdWithStudyTags(1L)).willReturn(Optional.of(study));

        NeurobagelDatasetDescriptionDTO description = service.findDatasetDescription(1L).orElseThrow();

        assertEquals("My study", description.getName());
        assertEquals(List.of("adhd", "mri"), description.getKeywords());
        assertEquals(List.of("https://shanoir.example.org/shanoir-ng/study/details/1"), description.getReferencesAndLinks());
        assertEquals("restricted", description.getAccessType());
        assertEquals("https://shanoir.example.org/shanoir-ng/access-request/study/1", description.getAccessLink());
        assertTrue(description.getAccessInstructions().contains(description.getAccessLink()));
    }

    @Test
    void datasetDescriptionOfAStudyNotExportedIsNotBuilt() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(2L)).willReturn(false);

        assertFalse(service.findDatasetDescription(2L).isPresent());
        verify(studyRepository, never()).findByIdWithStudyTags(anyLong());
    }

    private static StudyTag studyTag(String name) {
        StudyTag tag = new StudyTag();
        tag.setName(name);
        return tag;
    }

    @Test
    void imagingTableKeepsOnlyTheDatasetsWithASuffix() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L)).willReturn(true);
        given(imagingRepository.findImagingRows(1L)).willReturn(List.of(
                row(10L, 100L, 1000L, "MR", 1, 2),       // T1w
                row(10L, 100L, 1001L, "MR", 2, 9),       // bold
                row(10L, 100L, 1002L, "MR", 13, 9),      // field map: left out
                row(11L, 101L, 1003L, "MR", null, null), // not classified: left out
                row(11L, 101L, 1004L, "PET", null, null),
                row(11L, 101L, 1005L, "OTHER", null, null))); // CT: left out

        String tsv = service.findImagingTsv(1L).orElseThrow();

        assertEquals("sub\tses\tsuffix\tpath\n"
                + "sub-10\tses-100\tT1w\t/shanoir/examination/100/dataset/1000\n"
                + "sub-10\tses-100\tbold\t/shanoir/examination/100/dataset/1001\n"
                + "sub-11\tses-101\tpet\t/shanoir/examination/101/dataset/1004\n", tsv);
    }

    @Test
    void imagingTableWithoutAnySuffixHasTheHeaderOnly() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L)).willReturn(true);
        given(imagingRepository.findImagingRows(1L)).willReturn(List.of(row(10L, 100L, 1000L, "MR", null, null)));

        assertEquals(Optional.of("sub\tses\tsuffix\tpath\n"), service.findImagingTsv(1L));
    }

    @Test
    void imagingTableOfAStudyNotExportedIsNotBuilt() {
        given(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(2L)).willReturn(false);

        assertFalse(service.findImagingTsv(2L).isPresent());
        verify(imagingRepository, never()).findImagingRows(anyLong());
    }

    private static NeurobagelImagingRow row(Long subjectId, Long examinationId, Long datasetId, String modality,
            Integer nature, Integer application) {
        return new NeurobagelImagingRow() {
            public Long getSubjectId() {
                return subjectId;
            }

            public Long getExaminationId() {
                return examinationId;
            }

            public Long getDatasetId() {
                return datasetId;
            }

            public String getModality() {
                return modality;
            }

            public Integer getMrDatasetNature() {
                return nature;
            }

            public Integer getMrSequenceApplication() {
                return application;
            }
        };
    }

}
