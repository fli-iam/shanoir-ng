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

package org.shanoir.ng.examination;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

/**
 * Examination mapper test.
 *
 * @author msimon
 *
 */

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExaminationMapperTest {

    private static final Long EXAMINATION_ID = 1L;

    @Autowired
    private ExaminationMapper examinationMapper;

    @Test
    public void examinationsToSubjectExaminationDTOsTest() {
        final List<SubjectExaminationDTO> examinationDTOs = examinationMapper
                .examinationsToSubjectExaminationDTOs(Arrays.asList(createExamination()));
        Assertions.assertNotNull(examinationDTOs);
        Assertions.assertTrue(examinationDTOs.size() == 1);
        Assertions.assertTrue(EXAMINATION_ID.equals(examinationDTOs.get(0).getId()));
    }

    @Test
    public void examinationToExaminationDTOTest() {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        final ExaminationDTO examinationDTO = examinationMapper.examinationToExaminationDTO(createExamination());
        Assertions.assertNotNull(examinationDTO);
        Assertions.assertTrue(EXAMINATION_ID.equals(examinationDTO.getId()));
    }

    @Test
    public void examinationToSubjectExaminationDTOTest() {
        final SubjectExaminationDTO examinationDTO = examinationMapper
                .examinationToSubjectExaminationDTO(createExamination());
        Assertions.assertNotNull(examinationDTO);
        Assertions.assertTrue(EXAMINATION_ID.equals(examinationDTO.getId()));
    }

    private Examination createExamination() {
        final Examination examination = new Examination();
        examination.setId(EXAMINATION_ID);
        return examination;
    }

}
