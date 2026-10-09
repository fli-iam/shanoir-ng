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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapperImpl;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.mapper.StudyMapperImpl;
import org.shanoir.ng.shared.mapper.SubjectMapperImpl;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Examination mapper test.
 *
 * @author msimon
 *
 */
@SpringJUnitConfig(classes = {
    ExaminationMapperImpl.class,
    SubjectMapperImpl.class,
    StudyMapperImpl.class
})
public class ExaminationMapperTest {

    private static final Long EXAMINATION_ID = 1L;

    @Autowired
    private ExaminationMapper examinationMapper;

    @Test
    public void examinationsToExaminationDTOsTest() {
        final List<ExaminationDTO> examinationDTOs = examinationMapper
                .examinationListToExaminationListNullRelationsDTO(Arrays.asList(createExamination()));
        Assertions.assertNotNull(examinationDTOs);
        Assertions.assertTrue(examinationDTOs.size() == 1);
        Assertions.assertTrue(EXAMINATION_ID.equals(examinationDTOs.get(0).getId()));
    }

    @Test
    public void examinationToExaminationDTOTest() {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        final ExaminationDTO examinationDTO = examinationMapper.examinationToExaminationNullRelationsDTO(createExamination());
        Assertions.assertNotNull(examinationDTO);
        Assertions.assertTrue(EXAMINATION_ID.equals(examinationDTO.getId()));
    }

    private Examination createExamination() {
        final Examination examination = new Examination();
        examination.setId(EXAMINATION_ID);
        return examination;
    }

    /**
     * Checks that the "Found"/"Total"/"Page size" counters shown
     * below the examinations table display correct values.
     */
    @Test
    public void examinationListToExaminationListDTOPageWithStudyKeepsPagingMetadataTest() {
        Page<Examination> page = new org.springframework.data.domain.PageImpl<>(
                Arrays.asList(createExamination()), PageRequest.of(1, 5), 42);

        final PageImpl<ExaminationDTO> examinationDTOPage = examinationMapper
                .examinationListToExaminationListDTOPageWithStudy(page);

        Assertions.assertNotNull(examinationDTOPage);
        Assertions.assertEquals(1, examinationDTOPage.getNumberOfElements());
        Assertions.assertEquals(42, examinationDTOPage.getTotalElements());
        Assertions.assertEquals(5, examinationDTOPage.getSize());
        Assertions.assertEquals(1, examinationDTOPage.getNumber());
    }

}
