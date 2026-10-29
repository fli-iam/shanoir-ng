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

package org.shanoir.ng.datasetacquisition;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapperImpl;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapperImpl;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.mapper.StudyMapperImpl;
import org.shanoir.ng.shared.mapper.SubjectMapperImpl;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Checks that the "Found"/"Total"/"Page size" counters shown
 * below the dataset acquisitions table display correct values.
 */
@SpringJUnitConfig(classes = {
    DatasetAcquisitionMapperImpl.class,
    ExaminationMapperImpl.class,
    SubjectMapperImpl.class,
    StudyMapperImpl.class
})
public class DatasetAcquisitionMapperPagingTest {

    @Autowired
    private DatasetAcquisitionMapper datasetAcquisitionMapper;

    @Test
    public void acquisitionPageToAcquisitionWithExaminationDTOPageKeepsPagingMetadataTest() {
        Page<DatasetAcquisition> page = new org.springframework.data.domain.PageImpl<>(
                Arrays.asList(createDatasetAcquisition()), PageRequest.of(1, 5), 42);

        final PageImpl<DatasetAcquisitionDTO> dtoPage = datasetAcquisitionMapper
                .acquisitionPageToAcquisitionWithExaminationDTOPage(page);

        Assertions.assertNotNull(dtoPage);
        Assertions.assertEquals(1, dtoPage.getNumberOfElements());
        Assertions.assertEquals(42, dtoPage.getTotalElements());
        Assertions.assertEquals(5, dtoPage.getSize());
        Assertions.assertEquals(1, dtoPage.getNumber());
    }

    @Test
    public void acquisitionPageToAcquisitionIdRelationsDTOPageKeepsPagingMetadataTest() {
        Page<DatasetAcquisition> page = new org.springframework.data.domain.PageImpl<>(
                Arrays.asList(createDatasetAcquisition()), PageRequest.of(1, 5), 42);

        final PageImpl<DatasetAcquisitionDTO> dtoPage = datasetAcquisitionMapper
                .acquisitionPageToAcquisitionIdRelationsDTOPage(page);

        Assertions.assertNotNull(dtoPage);
        Assertions.assertEquals(1, dtoPage.getNumberOfElements());
        Assertions.assertEquals(42, dtoPage.getTotalElements());
        Assertions.assertEquals(5, dtoPage.getSize());
        Assertions.assertEquals(1, dtoPage.getNumber());
    }

    private DatasetAcquisition createDatasetAcquisition() {
        final DatasetAcquisition datasetAcquisition = new MrDatasetAcquisition();
        datasetAcquisition.setId(1L);
        datasetAcquisition.setDatasets(new ArrayList<>());
        datasetAcquisition.setExamination(new Examination());
        datasetAcquisition.getExamination().setStudy(new Study());
        datasetAcquisition.getExamination().getStudy().setId(1L);
        return datasetAcquisition;
    }

}
