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

package org.shanoir.ng.preclinical.pathologies;

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
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.PathologyModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Pathologies service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class PathologyServiceTest {

	private static final Long PATHOLOGY_ID = 1L;
	private static final String UPDATED_PATHOLOGY_DATA = "Alzheimer";

	@Mock
	private PathologyRepository pathologiesRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private PathologyServiceImpl pathologiesService;

	@Before
	public void setup() {
		given(pathologiesRepository.findAll()).willReturn(Arrays.asList(PathologyModelUtil.createPathology()));
		given(pathologiesRepository.findOne(PATHOLOGY_ID)).willReturn(PathologyModelUtil.createPathology());
		given(pathologiesRepository.save(Mockito.any(Pathology.class))).willReturn(PathologyModelUtil.createPathology());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		pathologiesService.deleteById(PATHOLOGY_ID);

		Mockito.verify(pathologiesRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Pathology> pathologies = pathologiesService.findAll();
		Assert.assertNotNull(pathologies);
		Assert.assertTrue(pathologies.size() == 1);

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Pathology pathology = pathologiesService.findById(PATHOLOGY_ID);
		Assert.assertNotNull(pathology);
		Assert.assertTrue(PathologyModelUtil.PATHOLOGY_NAME.equals(pathology.getName()));

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
	
	

	@Test
	public void saveTest() throws ShanoirException {
		pathologiesService.save(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Pathology updatedPathology = pathologiesService.update(createPathology());
		Assert.assertNotNull(updatedPathology);
		Assert.assertTrue(UPDATED_PATHOLOGY_DATA.equals(updatedPathology.getName()));

		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
	
	/*
	@Test
	public void findByNameTest() {
		final Pathology pathology = pathologiesService.findByName(UPDATED_PATHOLOGY_DATA);
		Assert.assertNotNull(pathology);
		Assert.assertTrue(PathologyModelUtil.PATHOLOGY_NAME.equals(pathology.getName()));

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
*/
/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private Pathology createPathology() {
		final Pathology pathology = new Pathology();
		pathology.setId(PATHOLOGY_ID);
		pathology.setName(UPDATED_PATHOLOGY_DATA);
		return pathology;
	}

}
