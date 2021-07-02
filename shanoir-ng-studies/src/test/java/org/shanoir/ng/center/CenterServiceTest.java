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

package org.shanoir.ng.center;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.shanoir.ng.shared.core.model.IdName;
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
		given(centerRepository.findIdsAndNames()).willReturn(Arrays.asList(new IdName()));
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(ModelsUtil.createCenter()));
		given(centerRepository.save(Mockito.any(Center.class))).willReturn(createCenter());
	}

	@Test(expected=EntityNotFoundException.class)
	public void deleteByBadIdTest() throws EntityNotFoundException {
		centerService.deleteById(2L);
	}
	
	@Test
	public void deleteByIdTest() throws EntityNotFoundException {
		centerService.deleteById(CENTER_ID);

		Mockito.verify(centerRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
	}

	@Test
	public void deleteByIdWithAcquisitionEquipmentTest() throws EntityNotFoundException {
		final Center center = ModelsUtil.createCenter();
		center.getAcquisitionEquipments().add(ModelsUtil.createAcquisitionEquipment());
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		centerService.deleteById(CENTER_ID);
	}

	@Test
	public void deleteByIdWithStudyTest() throws EntityNotFoundException {
		final Center center = ModelsUtil.createCenter();
		center.getStudyCenterList().add(new StudyCenter());
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
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
		final Center center = centerService.findById(CENTER_ID).orElse(null);
		Assert.assertNotNull(center);
		Assert.assertTrue(ModelsUtil.CENTER_NAME.equals(center.getName()));

		Mockito.verify(centerRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	public void findIdsAndNamesTest() {
		final List<IdName> centers = centerService.findIdsAndNames();
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
