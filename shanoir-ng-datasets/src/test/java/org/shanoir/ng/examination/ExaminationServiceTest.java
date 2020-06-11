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

package org.shanoir.ng.examination;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

/**
 * Examination service test.
 * 
 * @author ifakhfakh
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(KeycloakUtil.class)
public class ExaminationServiceTest {

	private static final Long EXAMINATION_ID = 1L;
	private static final String UPDATED_EXAMINATION_COMMENT = "examination 2";

	@Mock
	private ExaminationRepository examinationRepository;
	
	@Mock
	private KeycloakUtil keycloakUtil;

	@Mock
	private MicroserviceRequestsService microservicesRequestsService;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ExaminationServiceImpl examinationService;
	
	@MockBean
	private StudyRightsService rightsService;

	@Before
	public void setup() throws ShanoirException {
		// given(examinationRepository.findByStudyIdIn(Mockito.anyListOf(Long.class), Mockito.any(Pageable.class)))
		// 		.willReturn(Arrays.asList(ModelsUtil.createExamination()));
		given(examinationRepository.findOne(EXAMINATION_ID)).willReturn(ModelsUtil.createExamination());
		given(examinationRepository.save(Mockito.any(Examination.class))).willReturn(ModelsUtil.createExamination());
		given(examinationRepository.findByStudyIdIn(Mockito.anyListOf(Long.class), Mockito.any(Pageable.class))).willReturn(new PageImpl<Examination>(Arrays.asList(ModelsUtil.createExamination())));

		PowerMockito.mockStatic(KeycloakUtil.class);
		when(KeycloakUtil.getKeycloakHeader()).thenReturn(null);
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		examinationService.deleteById(EXAMINATION_ID);

		Mockito.verify(examinationRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findByIdTest() throws ShanoirException {
		final Examination examination = examinationService.findById(EXAMINATION_ID);
		Assert.assertNotNull(examination);
		Assert.assertTrue(ModelsUtil.EXAMINATION_NOTE.equals(examination.getNote()));
	}

	@Test
	public void saveTest() throws ShanoirException {
		examinationService.save(createExamination());

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Examination updatedExamination = examinationService.update(createExamination());
		Assert.assertNotNull(updatedExamination);
		Assert.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	private Examination createExamination() {
		final Examination examination = new Examination();
		examination.setId(EXAMINATION_ID);
		examination.setComment(UPDATED_EXAMINATION_COMMENT);
		return examination;
	}

}
