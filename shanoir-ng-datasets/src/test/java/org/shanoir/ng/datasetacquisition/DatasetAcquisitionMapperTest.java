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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.datasetacquisition.mr.MrDatasetAcquisition;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Dataset acquisition mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DatasetAcquisitionMapperTest {

	private static final Long DATASET_ACQUISITION_ID = 1L;
	private static final String DATASET_ACQUISITION_WIHTOUT_DATASET_NAME = "id=1 ()";
	private static final String DATASET_ACQUISITION_WIHT_DATASET_NAME = ModelsUtil.DATASET_NAME + " ()";

	@Autowired
	private DatasetAcquisitionMapper datasetAcquisitionMapper;

	@Test
	public void datasetAcquisitionsToExaminationDatasetAcquisitionDTOsTest() {
		final List<ExaminationDatasetAcquisitionDTO> datasetAcquisitionDTOs = datasetAcquisitionMapper
				.datasetAcquisitionsToExaminationDatasetAcquisitionDTOs(Arrays.asList(createDatasetAcquisition()));
		Assert.assertNotNull(datasetAcquisitionDTOs);
		Assert.assertTrue(datasetAcquisitionDTOs.size() == 1);
		Assert.assertTrue(DATASET_ACQUISITION_ID.equals(datasetAcquisitionDTOs.get(0).getId()));
		Assert.assertTrue(DATASET_ACQUISITION_WIHTOUT_DATASET_NAME.equals(datasetAcquisitionDTOs.get(0).getName()));
	}

	@Test
	public void datasetAcquisitionToExaminationDatasetAcquisitionDTOTest() {
		final DatasetAcquisition datasetAcquisition = createDatasetAcquisition();
		datasetAcquisition.setDatasets(Arrays.asList(ModelsUtil.createMrDataset()));

		final ExaminationDatasetAcquisitionDTO datasetAcquisitionDTO = datasetAcquisitionMapper
				.datasetAcquisitionToExaminationDatasetAcquisitionDTO(datasetAcquisition);
		Assert.assertNotNull(datasetAcquisitionDTO);
		Assert.assertTrue(DATASET_ACQUISITION_ID.equals(datasetAcquisitionDTO.getId()));
		Assert.assertTrue(DATASET_ACQUISITION_WIHT_DATASET_NAME.equals(datasetAcquisitionDTO.getName()));
	}

	private DatasetAcquisition createDatasetAcquisition() {
		final DatasetAcquisition datasetAcquisition = new MrDatasetAcquisition();
		datasetAcquisition.setId(DATASET_ACQUISITION_ID);
		datasetAcquisition.setDatasets(new ArrayList<>());
		return datasetAcquisition;
	}

}
