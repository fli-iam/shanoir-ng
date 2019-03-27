package org.shanoir.ng.acquisitionequipment;

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
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.acquisitionequipment.service.AcquisitionEquipmentServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.repository.CrudRepository;

/**
 * Acquisition equipment service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class AcquisitionEquipmentServiceTest {

	private static final Long ACQ_EQPT_ID = 1L;
	private static final String UPDATED_ACQ_EQPT_SERIAL_NUMBER = "test";

	@Mock
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private AcquisitionEquipmentServiceImpl acquisitionEquipmentService;

	@Before
	public void setup() {
		given(acquisitionEquipmentRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createAcquisitionEquipment()));
		given(acquisitionEquipmentRepository.findOne(ACQ_EQPT_ID)).willReturn(ModelsUtil.createAcquisitionEquipment());
		given(acquisitionEquipmentRepository.save(Mockito.any(AcquisitionEquipment.class))).willReturn(createAcquisitionEquipment());
	}

	@Test(expected = EntityNotFoundException.class)
	public void deleteByBadIdTest() throws EntityNotFoundException  {
		acquisitionEquipmentService.deleteById(2L);
	}
	
	@Test
	public void deleteByIdTest() throws EntityNotFoundException {
		acquisitionEquipmentService.deleteById(ACQ_EQPT_ID);
		
		Mockito.verify(acquisitionEquipmentRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAll();
		Assert.assertNotNull(equipments);
		Assert.assertTrue(equipments.size() == 1);

		Mockito.verify(acquisitionEquipmentRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final AcquisitionEquipment equipment = acquisitionEquipmentService.findById(ACQ_EQPT_ID);
		Assert.assertNotNull(equipment);
		Assert.assertTrue(ModelsUtil.ACQ_EQPT_SERIAL_NUMBER.equals(equipment.getSerialNumber()));

		Mockito.verify(acquisitionEquipmentRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() {
		acquisitionEquipmentService.create(createAcquisitionEquipment());

		Mockito.verify(acquisitionEquipmentRepository, Mockito.times(1)).save(Mockito.any(AcquisitionEquipment.class));
	}

	@Test
	public void updateTest() throws EntityNotFoundException {
		final AcquisitionEquipment updatedEquipment = acquisitionEquipmentService.update(createAcquisitionEquipment());
		Assert.assertNotNull(updatedEquipment);
		Assert.assertTrue(UPDATED_ACQ_EQPT_SERIAL_NUMBER.equals(updatedEquipment.getSerialNumber()));

		Mockito.verify(acquisitionEquipmentRepository, Mockito.times(1)).save(Mockito.any(AcquisitionEquipment.class));
	}

	private AcquisitionEquipment createAcquisitionEquipment() {
		final AcquisitionEquipment equipment = new AcquisitionEquipment();
		equipment.setId(ACQ_EQPT_ID);
		equipment.setSerialNumber(UPDATED_ACQ_EQPT_SERIAL_NUMBER);
		return equipment;
	}

}
