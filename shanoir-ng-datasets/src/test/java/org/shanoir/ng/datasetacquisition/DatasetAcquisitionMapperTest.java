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
