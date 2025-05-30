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

package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Examination anesthetics service test.
 * 
 * @author sloury
 * 
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExaminationAnestheticServiceTest {

	private static final Long EXAM_ANESTHETIC_ID = 1L;
	private static final Long EXAMINATION_ID = 1L;
	private static final Long UPDATED_EXAM_ANESTHETIC_ANESTHETIC_ID = 3L;

	@Mock
	private ExaminationAnestheticRepository examAnestheticRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ExaminationAnestheticServiceImpl examAnestheticsService;
	
	@BeforeEach
	public void setup() {
		given(examAnestheticRepository.findAll()).willReturn(Arrays.asList(AnestheticModelUtil.createExaminationAnesthetic()));
		given(examAnestheticRepository.findByExaminationId(1L)).willReturn(Arrays.asList(AnestheticModelUtil.createExaminationAnesthetic()));
		given(examAnestheticRepository.findById(EXAM_ANESTHETIC_ID)).willReturn(Optional.of(AnestheticModelUtil.createExaminationAnesthetic()));
		given(examAnestheticRepository.save(Mockito.any(ExaminationAnesthetic.class))).willReturn(AnestheticModelUtil.createExaminationAnesthetic());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		examAnestheticsService.deleteById(EXAM_ANESTHETIC_ID);

		Mockito.verify(examAnestheticRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<ExaminationAnesthetic> examAnesthetics = examAnestheticsService.findAll();
		Assertions.assertNotNull(examAnesthetics);
		Assertions.assertTrue(examAnesthetics.size() == 1);

		Mockito.verify(examAnestheticRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final ExaminationAnesthetic examAnesthetic = examAnestheticsService.findById(EXAM_ANESTHETIC_ID);
		Assertions.assertNotNull(examAnesthetic);
		Assertions.assertTrue(AnestheticModelUtil.ANESTHETIC_NAME.equals(examAnesthetic.getAnesthetic().getName()));
		
		Mockito.verify(examAnestheticRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}
	
	@Test
	public void findByExaminationIdTest() {
		final List<ExaminationAnesthetic> examAnesthetics = examAnestheticsService.findByExaminationId(1L);
		Assertions.assertNotNull(examAnesthetics);
		Assertions.assertTrue(examAnesthetics.size() == 1);

		Mockito.verify(examAnestheticRepository, Mockito.times(1)).findByExaminationId(1L);
	}

	@Test
	public void saveTest() throws ShanoirException {
		examAnestheticsService.save(createExaminationAnesthetic());

		Mockito.verify(examAnestheticRepository, Mockito.times(1)).save(Mockito.any(ExaminationAnesthetic.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final ExaminationAnesthetic updatedExamAnesthetic = examAnestheticsService.update(createExaminationAnesthetic());
		Assertions.assertNotNull(updatedExamAnesthetic);
		Assertions.assertTrue(UPDATED_EXAM_ANESTHETIC_ANESTHETIC_ID.equals(updatedExamAnesthetic.getAnesthetic().getId()));

		Mockito.verify(examAnestheticRepository, Mockito.times(1)).save(Mockito.any(ExaminationAnesthetic.class));
	}

/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findById(Mockito.anyLong()).orElse(null);
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private ExaminationAnesthetic createExaminationAnesthetic() {
		final ExaminationAnesthetic examAnesthetic = new ExaminationAnesthetic();
		examAnesthetic.setId(EXAM_ANESTHETIC_ID);
		//examAnesthetic.setExaminationId(EXAMINATION_ID);
		examAnesthetic.setAnesthetic(AnestheticModelUtil.createAnestheticInjection());
		return examAnesthetic;
	}
	
}
