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
import java.util.List;

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

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private BIDSServiceImpl service;

    @TempDir
    private File bidsStorageDir;

    @Test
    void participantsTsvListsTheSubjectsWithoutWritingAnyFile() {
        ReflectionTestUtils.setField(service, "bidsStorageDir", bidsStorageDir.getAbsolutePath());
        Subject subject = new Subject();
        subject.setId(12L);
        subject.setSex(Sex.F);
        given(subjectRepository.findByStudy_Id(STUDY_ID)).willReturn(List.of(subject));

        String tsv = service.participantsTsv(STUDY_ID);

        String[] lines = tsv.split("\n");
        assertEquals(2, lines.length);
        assertEquals("participant_id\tsubject_identifier\tsubject_age\tsubject_sex", lines[0].strip());
        assertEquals("sub-12\t12\tn/a\tF", lines[1].strip());
        assertEquals(0, bidsStorageDir.list().length);
    }

}
