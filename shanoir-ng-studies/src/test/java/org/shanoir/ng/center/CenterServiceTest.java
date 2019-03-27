package org.shanoir.ng.center;

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
import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.center.service.CenterServiceImpl;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Center service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CenterServiceTest {

	private static final Long CENTER_ID = 1L;
	private static final String UPDATED_CENTER_NAME = "test";

	@Mock
	private CenterMapper centerMapper;

	@Mock
	private CenterRepository centerRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private CenterServiceImpl centerService;

	@Before
	public void setup() {
		given(centerMapper.centerToCenterDTO(Mockito.any(Center.class))).willReturn(new CenterDTO());
		
		given(centerRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createCenter()));
		given(centerRepository.findIdsAndNames()).willReturn(Arrays.asList(new IdNameDTO()));
		given(centerRepository.findOne(CENTER_ID)).willReturn(ModelsUtil.createCenter());
		given(centerRepository.save(Mockito.any(Center.class))).willReturn(createCenter());
	}

	@Test(expected=EntityNotFoundException.class)
	public void deleteByBadIdTest() throws EntityNotFoundException {
		centerService.deleteById(2L);
	}
	
	@Test
	public void deleteByIdTest() throws EntityNotFoundException {
		centerService.deleteById(CENTER_ID);

		Mockito.verify(centerRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void deleteByIdWithAcquisitionEquipmentTest() throws EntityNotFoundException {
		final Center center = ModelsUtil.createCenter();
		center.getAcquisitionEquipments().add(ModelsUtil.createAcquisitionEquipment());
		given(centerRepository.findOne(CENTER_ID)).willReturn(center);
		centerService.deleteById(CENTER_ID);
	}

	@Test
	public void deleteByIdWithStudyTest() throws EntityNotFoundException {
		final Center center = ModelsUtil.createCenter();
		center.getStudyCenterList().add(new StudyCenter());
		given(centerRepository.findOne(CENTER_ID)).willReturn(center);
		centerService.deleteById(CENTER_ID);
	}

	@Test
	public void findAllTest() {
		final List<Center> centers = centerService.findAll();
		Assert.assertNotNull(centers);
		Assert.assertTrue(centers.size() == 1);

		Mockito.verify(centerRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Center center = centerService.findById(CENTER_ID);
		Assert.assertNotNull(center);
		Assert.assertTrue(ModelsUtil.CENTER_NAME.equals(center.getName()));

		Mockito.verify(centerRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void findIdsAndNamesTest() {
		final List<IdNameDTO> centers = centerService.findIdsAndNames();
		Assert.assertNotNull(centers);
		Assert.assertTrue(centers.size() == 1);

		Mockito.verify(centerRepository, Mockito.times(1)).findIdsAndNames();
	}

	@Test
	public void saveTest() {
		centerService.create(createCenter());

		Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
	}

	@Test
	public void updateTest() throws EntityNotFoundException {
		final Center updatedCenter = centerService.update(createCenter());
		Assert.assertNotNull(updatedCenter);
		Assert.assertTrue(UPDATED_CENTER_NAME.equals(updatedCenter.getName()));

		Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
	}

	private Center createCenter() {
		final Center center = new Center();
		center.setId(CENTER_ID);
		center.setName(UPDATED_CENTER_NAME);
		return center;
	}

}
