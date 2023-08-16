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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.mockito.junit.MockitoJUnitRunner;

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
	private ManufacturerModelRepository repository;
	@Mock
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private ManufacturerModelServiceImpl manufacturerModelService;

	@Before
	public void setup() {
		given(repository.findAll()).willReturn(Arrays.asList(ModelsUtil.createManufacturerModel()));
		given(repository.findById(MANUFACTURER_MODEL_ID))
				.willReturn(Optional.of(ModelsUtil.createManufacturerModel()));
		given(repository.save(Mockito.any(ManufacturerModel.class)))
				.willReturn(createManufacturerModel());
	}

	@Test
	public void findAllTest() {
		final List<ManufacturerModel> manufacturerModels = manufacturerModelService.findAll();
		Assert.assertNotNull(manufacturerModels);
		Assert.assertTrue(manufacturerModels.size() == 1);

		Mockito.verify(repository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final ManufacturerModel manufacturerModel = manufacturerModelService.findById(MANUFACTURER_MODEL_ID).orElseThrow();
		Assert.assertNotNull(manufacturerModel);
		Assert.assertTrue(ModelsUtil.MANUFACTURER_MODEL_NAME.equals(manufacturerModel.getName()));

		Mockito.verify(repository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	public void saveTest() {
		manufacturerModelService.create(createManufacturerModel());

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(ManufacturerModel.class));
	}

	@Test
	public void updateTest() throws EntityNotFoundException {
		final ManufacturerModel updatedManufacturerModel = manufacturerModelService.update(createManufacturerModel());
		Assert.assertNotNull(updatedManufacturerModel);
		Assert.assertTrue(UPDATED_MANUFACTURER_MODEL_NAME.equals(updatedManufacturerModel.getName()));

		Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(ManufacturerModel.class));
	}

	private ManufacturerModel createManufacturerModel() {
		final ManufacturerModel manufacturerModel = new ManufacturerModel();
		manufacturerModel.setId(MANUFACTURER_MODEL_ID);
		manufacturerModel.setName(UPDATED_MANUFACTURER_MODEL_NAME);
		return manufacturerModel;
	}

}
