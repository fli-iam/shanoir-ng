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

package org.shanoir.ng.preclinical.anesthetics;

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
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticRepository;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticServiceImpl;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Anesthetics service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class AnestheticServiceTest {

	private static final Long ANESTHETIC_ID = 1L;
	private static final String UPDATED_ANESTHETIC_DATA = "Injection 2%";
	

	@Mock
	private AnestheticRepository anestheticRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private AnestheticServiceImpl anestheticsService;
	
			
	
	@Before
	public void setup() {
		given(anestheticRepository.findAll()).willReturn(Arrays.asList(AnestheticModelUtil.createAnestheticGas()));
		given(anestheticRepository.findAllByAnestheticType(AnestheticType.GAS)).willReturn(Arrays.asList(AnestheticModelUtil.createAnestheticGas()));
		given(anestheticRepository.findOne(ANESTHETIC_ID)).willReturn(AnestheticModelUtil.createAnestheticGas());
		given(anestheticRepository.save(Mockito.any(Anesthetic.class))).willReturn(AnestheticModelUtil.createAnestheticGas());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		anestheticsService.deleteById(ANESTHETIC_ID);

		Mockito.verify(anestheticRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Anesthetic> anesthetics = anestheticsService.findAll();
		Assert.assertNotNull(anesthetics);
		Assert.assertTrue(anesthetics.size() == 1);

		Mockito.verify(anestheticRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Anesthetic anesthetic = anestheticsService.findById(ANESTHETIC_ID);
		Assert.assertNotNull(anesthetic);
		Assert.assertTrue(AnestheticModelUtil.ANESTHETIC_NAME.equals(anesthetic.getName()));

		Mockito.verify(anestheticRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
	
	@Test
	public void findByAnestheticTypeTest() {
		final List<Anesthetic> anesthetics = anestheticsService.findAllByAnestheticType(AnestheticType.GAS);
		Assert.assertNotNull(anesthetics);
		Assert.assertTrue(anesthetics.size() == 1);

		Mockito.verify(anestheticRepository, Mockito.times(1)).findAllByAnestheticType(AnestheticType.GAS);
	}
	
	

	@Test
	public void saveTest() throws ShanoirException {
		anestheticsService.save(createAnesthetic());

		Mockito.verify(anestheticRepository, Mockito.times(1)).save(Mockito.any(Anesthetic.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Anesthetic updatedAnesthetic = anestheticsService.update(createAnesthetic());
		Assert.assertNotNull(updatedAnesthetic);
		Assert.assertTrue(UPDATED_ANESTHETIC_DATA.equals(updatedAnesthetic.getName()));
		Assert.assertTrue(AnestheticType.INJECTION.equals(updatedAnesthetic.getAnestheticType()));

		Mockito.verify(anestheticRepository, Mockito.times(1)).save(Mockito.any(Anesthetic.class));
	}

/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private Anesthetic createAnesthetic() {
		final Anesthetic anesthetic = new Anesthetic();
		anesthetic.setId(ANESTHETIC_ID);
		anesthetic.setName(UPDATED_ANESTHETIC_DATA);
		anesthetic.setAnestheticType(AnestheticType.INJECTION);
		return anesthetic;
	}
	
}
