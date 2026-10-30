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

package org.shanoir.ng.bids.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.subject.model.Sex;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BIDSServiceImplTest {

    private static final Long STUDY_ID = 7L;

    private static final String HEADER = "participant_id\tsubject_identifier\tsubject_age\tsubject_sex\n";

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private BIDSServiceImpl service;

    @TempDir
    private File bidsStorageDir;

    private static Subject subject(Long id, LocalDate birthDate) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setSex(Sex.F);
        subject.setBirthDate(birthDate);
        return subject;
    }

    @Test
    void participantsTsvListsTheSubjectsWithoutWritingAnyFile() {
        ReflectionTestUtils.setField(service, "bidsStorageDir", bidsStorageDir.getAbsolutePath());
        given(subjectRepository.findByStudy_Id(STUDY_ID)).willReturn(List.of(subject(12L, null)));

        String tsv = service.participantsTsv(STUDY_ID, Map.of());

        assertEquals(HEADER + "sub-12\t12\tn/a\tF\n", tsv);
        assertEquals(0, bidsStorageDir.list().length);
    }

    @Test
    void ageIsComputedAtTheFirstExaminationDate() {
        given(subjectRepository.findByStudy_Id(STUDY_ID)).willReturn(List.of(
                subject(12L, LocalDate.of(1980, 6, 15)),
                subject(13L, LocalDate.of(1990, 1, 1))));

        String tsv = service.participantsTsv(STUDY_ID, Map.of(
                // the day before the 40th birthday
                12L, LocalDate.of(2020, 6, 14),
                13L, LocalDate.of(2015, 1, 1)));

        assertEquals(HEADER + "sub-12\t12\t39\tF\n" + "sub-13\t13\t25\tF\n", tsv);
    }

    @Test
    void ageIsUnknownWithoutExamination() {
        given(subjectRepository.findByStudy_Id(STUDY_ID)).willReturn(List.of(subject(12L, LocalDate.of(1980, 6, 15))));

        assertEquals(HEADER + "sub-12\t12\tn/a\tF\n", service.participantsTsv(STUDY_ID, Map.of()));
        assertEquals(HEADER + "sub-12\t12\tn/a\tF\n", service.participantsTsv(STUDY_ID, null));
    }

    @Test
    void ageIsUnknownWhenTheExaminationIsBeforeTheBirth() {
        given(subjectRepository.findByStudy_Id(STUDY_ID)).willReturn(List.of(subject(12L, LocalDate.of(1980, 6, 15))));

        String tsv = service.participantsTsv(STUDY_ID, Map.of(12L, LocalDate.of(1979, 1, 1)));

        assertEquals(HEADER + "sub-12\t12\tn/a\tF\n", tsv);
    }

}
