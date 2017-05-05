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
import org.shanoir.ng.shared.exception.ShanoirStudyException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Manufacturer service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ManufacturerServiceTest {

	private static final Long MANUFACTURER_ID = 1L;
	private static final String UPDATED_MANUFACTURER_NAME = "test";

	@Mock
	private ManufacturerRepository manufacturerRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ManufacturerServiceImpl manufacturerService;

	@Before
	public void setup() {
		given(manufacturerRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createManufacturer()));
		given(manufacturerRepository.findBy("name", ModelsUtil.MANUFACTURER_NAME))
				.willReturn(Arrays.asList(ModelsUtil.createManufacturer()));
		given(manufacturerRepository.findOne(MANUFACTURER_ID)).willReturn(ModelsUtil.createManufacturer());
		given(manufacturerRepository.save(Mockito.any(Manufacturer.class))).willReturn(ModelsUtil.createManufacturer());
	}

	@Test
	public void findAllTest() {
		final List<Manufacturer> manufacturers = manufacturerService.findAll();
		Assert.assertNotNull(manufacturers);
		Assert.assertTrue(manufacturers.size() == 1);

		Mockito.verify(manufacturerRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByTest() {
		final List<Manufacturer> manufacturers = manufacturerService.findBy("name", ModelsUtil.MANUFACTURER_NAME);
		Assert.assertNotNull(manufacturers);
		Assert.assertTrue(manufacturers.size() == 1);
		Assert.assertTrue(ModelsUtil.MANUFACTURER_NAME.equals(manufacturers.get(0).getName()));

		Mockito.verify(manufacturerRepository, Mockito.times(1)).findBy(Mockito.anyString(), Mockito.anyObject());
	}

	@Test
	public void findByIdTest() {
		final Manufacturer manufacturer = manufacturerService.findById(MANUFACTURER_ID);
		Assert.assertNotNull(manufacturer);
		Assert.assertTrue(ModelsUtil.MANUFACTURER_NAME.equals(manufacturer.getName()));

		Mockito.verify(manufacturerRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirStudyException {
		manufacturerService.save(createManufacturer());

		Mockito.verify(manufacturerRepository, Mockito.times(1)).save(Mockito.any(Manufacturer.class));
	}

	private Manufacturer createManufacturer() {
		final Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(MANUFACTURER_ID);
		manufacturer.setName(UPDATED_MANUFACTURER_NAME);
		return manufacturer;
	}

}
