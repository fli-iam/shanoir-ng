package org.shanoir.ng.dataset;

import static org.mockito.BDDMockito.given;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Dataset service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

	private static final Long DATASET_ID = 1L;
	private static final String UPDATED_DATASET_NAME = "test";

	@Mock
	private DatasetRepository datasetRepository;

	@InjectMocks
	private DatasetServiceImpl datasetService;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Before
	public void setup() {
		given(datasetRepository.findOne(DATASET_ID)).willReturn(ModelsUtil.createDataset());
		given(datasetRepository.save(Mockito.any(Dataset.class))).willReturn(ModelsUtil.createDataset());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		datasetService.deleteById(DATASET_ID);

		Mockito.verify(datasetRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findByIdTest() {
		final Dataset dataset = datasetService.findById(DATASET_ID);
		Assert.assertNotNull(dataset);
		Assert.assertTrue(ModelsUtil.DATASET_NAME.equals(dataset.getName()));

		Mockito.verify(datasetRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirException {
		datasetService.save(createDataset());

		Mockito.verify(datasetRepository, Mockito.times(1)).save(Mockito.any(Dataset.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Dataset updatedTemplate = datasetService.update(createDataset());
		Assert.assertNotNull(updatedTemplate);
		Assert.assertTrue(UPDATED_DATASET_NAME.equals(updatedTemplate.getName()));

		Mockito.verify(datasetRepository, Mockito.times(1)).save(Mockito.any(Dataset.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		datasetService.updateFromShanoirOld(createDataset());

		Mockito.verify(datasetRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(datasetRepository, Mockito.times(1)).save(Mockito.any(Dataset.class));
	}

	private Dataset createDataset() {
		final Dataset dataset = new Dataset();
		dataset.setId(DATASET_ID);
		dataset.setName(UPDATED_DATASET_NAME);
		return dataset;
	}

}
