package org.shanoir.ng.dataset;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Dataset mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DatasetMapperTest {

	private static final DateFormat shortDateFormatEN = DateFormat.getDateInstance(DateFormat.SHORT,
			new Locale("FR", "fr"));
	private static final String DATE_STR = "01/01/18";

	private static final Long DATASET_ID = 1L;
	private static final String DATASET_NAME = "test";
	private static final String DATASET_GENERATED_NAME = "1 " + DATE_STR + " MR";

	@Autowired
	private DatasetMapper datasetMapper;

	@Test
	public void datasetsToIdNameDTOsTest() throws ParseException {
		final List<IdNameDTO> datasetDTOs = datasetMapper.datasetsToIdNameDTOs(Arrays.asList(createDataset()));
		Assert.assertNotNull(datasetDTOs);
		Assert.assertTrue(datasetDTOs.size() == 1);
		Assert.assertTrue(DATASET_ID.equals(datasetDTOs.get(0).getId()));
	}

	@Test
	public void datasetToDatasetDTODTOTest() throws ParseException {
		final DatasetDTO datasetDTO = datasetMapper.datasetToDatasetDTO(createDataset());
		Assert.assertNotNull(datasetDTO);
		Assert.assertTrue(DATASET_ID.equals(datasetDTO.getId()));
		Assert.assertTrue(DATASET_NAME.equals(datasetDTO.getOriginMetadata().getName()));
	}

	@Test
	public void datasetToIdNameDTOTest() throws ParseException {
		final IdNameDTO datasetDTO = datasetMapper.datasetToIdNameDTO(createDataset());
		Assert.assertNotNull(datasetDTO);
		Assert.assertTrue(DATASET_ID.equals(datasetDTO.getId()));
		Assert.assertTrue(DATASET_NAME.equals(datasetDTO.getName()));
	}

	@Test
	public void datasetWithoutNameToIdNameDTOTest() throws ParseException {
		final Dataset dataset = createDataset();
		dataset.getOriginMetadata().setName(null);
		final IdNameDTO datasetDTO = datasetMapper.datasetToIdNameDTO(dataset);
		Assert.assertNotNull(datasetDTO);
		Assert.assertTrue(DATASET_ID.equals(datasetDTO.getId()));
		Assert.assertTrue(DATASET_GENERATED_NAME.equals(datasetDTO.getName()));
	}

	private Dataset createDataset() throws ParseException {
		final Dataset dataset = new MrDataset();
		dataset.setCreationDate(shortDateFormatEN.parse(DATE_STR));
		dataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.MR_DATASET);
		dataset.setId(DATASET_ID);
		dataset.getOriginMetadata().setName(DATASET_NAME);
		return dataset;
	}

}
