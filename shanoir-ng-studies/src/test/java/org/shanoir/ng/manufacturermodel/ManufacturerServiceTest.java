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

package org.shanoir.ng.manufacturermodel;

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
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
import org.shanoir.ng.manufacturermodel.service.ManufacturerServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
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
		given(manufacturerRepository.findById(MANUFACTURER_ID)).willReturn(Optional.of(ModelsUtil.createManufacturer()));
		given(manufacturerRepository.save(Mockito.any(Manufacturer.class))).willReturn(createManufacturer());
	}

	@Test
	public void findAllTest() {
		final List<Manufacturer> manufacturers = manufacturerService.findAll();
		Assert.assertNotNull(manufacturers);
		Assert.assertTrue(manufacturers.size() == 1);

		Mockito.verify(manufacturerRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Manufacturer manufacturer = manufacturerService.findById(MANUFACTURER_ID).orElseThrow();
		Assert.assertNotNull(manufacturer);
		Assert.assertTrue(ModelsUtil.MANUFACTURER_NAME.equals(manufacturer.getName()));

		Mockito.verify(manufacturerRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	public void saveTest() {
		manufacturerService.create(createManufacturer());

		Mockito.verify(manufacturerRepository, Mockito.times(1)).save(Mockito.any(Manufacturer.class));
	}

	@Test
	public void updateTest() throws EntityNotFoundException {
		final Manufacturer updatedManufacturer = manufacturerService.update(createManufacturer());
		Assert.assertNotNull(updatedManufacturer);
		Assert.assertTrue(UPDATED_MANUFACTURER_NAME.equals(updatedManufacturer.getName()));

		Mockito.verify(manufacturerRepository, Mockito.times(1)).save(Mockito.any(Manufacturer.class));
	}

	private Manufacturer createManufacturer() {
		final Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(MANUFACTURER_ID);
		manufacturer.setName(UPDATED_MANUFACTURER_NAME);
		return manufacturer;
	}

}
