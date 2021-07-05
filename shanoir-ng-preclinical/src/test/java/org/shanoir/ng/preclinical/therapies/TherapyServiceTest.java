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

package org.shanoir.ng.preclinical.therapies;

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
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.TherapyModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Therapies service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class TherapyServiceTest {

	private static final Long THERAPY_ID = 1L;
	private static final String UPDATED_THERAPY_DATA = "Chimiotherapy";
	private static final String UPDATED_THERAPY_TYPE_DATA = "Drug";

	@Mock
	private TherapyRepository therapiesRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private TherapyServiceImpl therapiesService;
	
	
	@Before
	public void setup() {
		given(therapiesRepository.findAll()).willReturn(Arrays.asList(TherapyModelUtil.createTherapyBrain()));
		given(therapiesRepository.findByTherapyType(TherapyType.SURGERY)).willReturn(Arrays.asList(TherapyModelUtil.createTherapyBrain()));
		given(therapiesRepository.findByName(TherapyModelUtil.THERAPY_NAME_BRAIN)).willReturn(Optional.of(TherapyModelUtil.createTherapyBrain()));
		given(therapiesRepository.findById(THERAPY_ID).orElse(null)).willReturn(TherapyModelUtil.createTherapyBrain());
		given(therapiesRepository.save(Mockito.any(Therapy.class))).willReturn(TherapyModelUtil.createTherapyBrain());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		therapiesService.deleteById(THERAPY_ID);

		Mockito.verify(therapiesRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Therapy> therapies = therapiesService.findAll();
		Assert.assertNotNull(therapies);
		Assert.assertTrue(therapies.size() == 1);

		Mockito.verify(therapiesRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Therapy therapy = therapiesService.findById(THERAPY_ID);
		Assert.assertNotNull(therapy);
		Assert.assertTrue(TherapyModelUtil.THERAPY_NAME_BRAIN.equals(therapy.getName()));

		Mockito.verify(therapiesRepository, Mockito.times(1)).findById(Mockito.anyLong()).orElse(null);
	}
	
	@Test
	public void findByNameTest() {
		final Therapy therapy = therapiesService.findByName(TherapyModelUtil.THERAPY_NAME_BRAIN);
		Assert.assertNotNull(therapy);
		Assert.assertTrue(THERAPY_ID.equals(therapy.getId()));

		Mockito.verify(therapiesRepository, Mockito.times(1)).findByName(TherapyModelUtil.THERAPY_NAME_BRAIN);
	}
	
	@Test
	public void findByTherapyTypeTest() {
		final List<Therapy> therapies = therapiesService.findByTherapyType(TherapyType.SURGERY);
		Assert.assertNotNull(therapies);
		Assert.assertTrue(therapies.size() == 1);

		Mockito.verify(therapiesRepository, Mockito.times(1)).findByTherapyType(TherapyType.SURGERY);
	}

	@Test
	public void saveTest() throws ShanoirException {
		therapiesService.save(createTherapy());

		Mockito.verify(therapiesRepository, Mockito.times(1)).save(Mockito.any(Therapy.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Therapy updatedTherapy = therapiesService.update(createTherapy());
		Assert.assertNotNull(updatedTherapy);
		Assert.assertTrue(UPDATED_THERAPY_DATA.equals(updatedTherapy.getName()));
		Assert.assertTrue(UPDATED_THERAPY_TYPE_DATA.equals(updatedTherapy.getTherapyType().getValue()));

		Mockito.verify(therapiesRepository, Mockito.times(1)).save(Mockito.any(Therapy.class));
	}

/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findById(Mockito.anyLong()).orElse(null);
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private Therapy createTherapy() {
		final Therapy therapy = new Therapy();
		therapy.setId(THERAPY_ID);
		therapy.setName(UPDATED_THERAPY_DATA);
		therapy.setTherapyType(TherapyType.DRUG);
		return therapy;
	}

}
