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

package org.shanoir.ng.preclinical.pathologies.pathology_models;

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
import org.shanoir.ng.preclinical.pathologies.PathologyRepository;
import org.shanoir.ng.preclinical.pathologies.PathologyServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.PathologyModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Pathology models service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class PathologyModelServiceTest {

	private static final Long MODEL_ID = 1L;
	private static final String UPDATED_MODEL_DATA = "AAAA";
	private static final Long PATHO_ID = 1L;

	@Mock
	private PathologyModelRepository modelsRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private PathologyModelServiceImpl modelsService;
	
	@InjectMocks
	private PathologyServiceImpl pathosService;
	
	@Mock
	private PathologyRepository pathosRepository;
	
	
	@Before
	public void setup() {
		given(modelsRepository.findAll()).willReturn(Arrays.asList(PathologyModelUtil.createPathologyModel()));
		given(modelsRepository.findByPathology(PathologyModelUtil.createPathology())).willReturn(Arrays.asList(PathologyModelUtil.createPathologyModel()));
		given(modelsRepository.findOne(MODEL_ID)).willReturn(PathologyModelUtil.createPathologyModel());
		given(modelsRepository.save(Mockito.any(PathologyModel.class))).willReturn(PathologyModelUtil.createPathologyModel());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		modelsService.deleteById(MODEL_ID);

		Mockito.verify(modelsRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<PathologyModel> models = modelsService.findAll();
		Assert.assertNotNull(models);
		Assert.assertTrue(models.size() == 1);

		Mockito.verify(modelsRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final PathologyModel model = modelsService.findById(MODEL_ID);
		Assert.assertNotNull(model);
		Assert.assertTrue(PathologyModelUtil.MODEL_NAME.equals(model.getName()));

		Mockito.verify(modelsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
	
	@Test
	public void findByPathologyTest() {
		final List<PathologyModel> models = modelsService.findByPathology(PathologyModelUtil.createPathology());
		Assert.assertNotNull(models);
		Assert.assertTrue(models.size() == 1);

		Mockito.verify(modelsRepository, Mockito.times(1)).findByPathology(PathologyModelUtil.createPathology());
	}

	@Test
	public void saveTest() throws ShanoirException {
		modelsService.save(createPathologyModel());

		Mockito.verify(modelsRepository, Mockito.times(1)).save(Mockito.any(PathologyModel.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final PathologyModel updatedPathology = modelsService.update(createPathologyModel());
		Assert.assertNotNull(updatedPathology);
		Assert.assertTrue(UPDATED_MODEL_DATA.equals(updatedPathology.getName()));

		Mockito.verify(modelsRepository, Mockito.times(1)).save(Mockito.any(PathologyModel.class));
	}

/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private PathologyModel createPathologyModel() {
		final PathologyModel model = new PathologyModel();
		model.setId(MODEL_ID);
		model.setName(UPDATED_MODEL_DATA);
		return model;
	}
	
}
