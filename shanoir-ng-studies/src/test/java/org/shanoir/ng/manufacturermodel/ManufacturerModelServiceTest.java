package org.shanoir.ng.manufacturermodel;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Manufacturer model service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ManufacturerModelServiceTest {

	private static final Long MANUFACTURER_MODEL_ID = 1L;
	private static final String UPDATED_MANUFACTURER_MODEL_NAME = "test";

	@Mock
	private ManufacturerModelRepository manufacturerModelRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ManufacturerModelServiceImpl manufacturerModelService;

	@Before
	public void setup() {
		given(manufacturerModelRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createManufacturerModel()));
		given(manufacturerModelRepository.findBy("name", ModelsUtil.MANUFACTURER_MODEL_NAME))
				.willReturn(Arrays.asList(ModelsUtil.createManufacturerModel()));
		given(manufacturerModelRepository.findOne(MANUFACTURER_MODEL_ID)).willReturn(ModelsUtil.createManufacturerModel());
		given(manufacturerModelRepository.save(Mockito.any(ManufacturerModel.class)))
				.willReturn(ModelsUtil.createManufacturerModel());
	}

	@Test
	public void findAllTest() {
		final List<ManufacturerModel> manufacturerModels = manufacturerModelService.findAll();
		Assert.assertNotNull(manufacturerModels);
		Assert.assertTrue(manufacturerModels.size() == 1);

		Mockito.verify(manufacturerModelRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByTest() {
		final List<ManufacturerModel> manufacturerModels = manufacturerModelService.findBy("name",
				ModelsUtil.MANUFACTURER_MODEL_NAME);
		Assert.assertNotNull(manufacturerModels);
		Assert.assertTrue(manufacturerModels.size() == 1);
		Assert.assertTrue(ModelsUtil.MANUFACTURER_MODEL_NAME.equals(manufacturerModels.get(0).getName()));

		Mockito.verify(manufacturerModelRepository, Mockito.times(1)).findBy(Mockito.anyString(), Mockito.anyObject());
	}

	@Test
	public void findByIdTest() {
		final ManufacturerModel manufacturerModel = manufacturerModelService.findById(MANUFACTURER_MODEL_ID);
		Assert.assertNotNull(manufacturerModel);
		Assert.assertTrue(ModelsUtil.MANUFACTURER_MODEL_NAME.equals(manufacturerModel.getName()));

		Mockito.verify(manufacturerModelRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirStudiesException {
		manufacturerModelService.save(createManufacturerModel());

		Mockito.verify(manufacturerModelRepository, Mockito.times(1)).save(Mockito.any(ManufacturerModel.class));
	}

	private ManufacturerModel createManufacturerModel() {
		final ManufacturerModel manufacturerModel = new ManufacturerModel();
		manufacturerModel.setId(MANUFACTURER_MODEL_ID);
		manufacturerModel.setName(UPDATED_MANUFACTURER_MODEL_NAME);
		return manufacturerModel;
	}

}
