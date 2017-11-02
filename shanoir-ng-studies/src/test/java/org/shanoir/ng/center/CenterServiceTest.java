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
import org.shanoir.ng.center.Center;
import org.shanoir.ng.center.CenterRepository;
import org.shanoir.ng.center.CenterServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.study.StudyCenter;
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
		given(centerRepository.findIdsAndNames()).willReturn(Arrays.asList(new CenterNameDTO()));
		given(centerRepository.findOne(CENTER_ID)).willReturn(ModelsUtil.createCenter());
		given(centerRepository.save(Mockito.any(Center.class))).willReturn(ModelsUtil.createCenter());
	}

	@Test(expected=ShanoirStudiesException.class)
	public void deleteByBadIdTest() throws ShanoirStudiesException {
		centerService.deleteById(2L);
	}
	
	@Test
	public void deleteByIdTest() throws ShanoirStudiesException {
		centerService.deleteById(CENTER_ID);

		Mockito.verify(centerRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test(expected=ShanoirStudiesException.class)
	public void deleteByIdWithAcquisitionEquipmentTest() throws ShanoirStudiesException {
		final Center center = ModelsUtil.createCenter();
		center.getAcquisitionEquipments().add(ModelsUtil.createAcquisitionEquipment());
		given(centerRepository.findOne(CENTER_ID)).willReturn(center);
		centerService.deleteById(CENTER_ID);
	}

	@Test(expected=ShanoirStudiesException.class)
	public void deleteByIdWithStudyTest() throws ShanoirStudiesException {
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
		final List<CenterNameDTO> centers = centerService.findIdsAndNames();
		Assert.assertNotNull(centers);
		Assert.assertTrue(centers.size() == 1);

		Mockito.verify(centerRepository, Mockito.times(1)).findIdsAndNames();
	}

	@Test
	public void saveTest() throws ShanoirStudiesException {
		centerService.save(createCenter());

		Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
	}

	@Test
	public void updateTest() throws ShanoirStudiesException {
		final Center updatedCenter = centerService.update(createCenter());
		Assert.assertNotNull(updatedCenter);
		Assert.assertTrue(UPDATED_CENTER_NAME.equals(updatedCenter.getName()));

		Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirStudiesException {
		centerService.updateFromShanoirOld(createCenter());

		Mockito.verify(centerRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
	}

	private Center createCenter() {
		final Center center = new Center();
		center.setId(CENTER_ID);
		center.setName(UPDATED_CENTER_NAME);
		return center;
	}

}
