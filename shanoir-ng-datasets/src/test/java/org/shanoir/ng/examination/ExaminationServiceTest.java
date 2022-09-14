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

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationServiceImpl;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Examination service test.
 * 
 * @author ifakhfakh
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExaminationServiceTest {

	private static final Long EXAMINATION_ID = 1L;
	private static final String UPDATED_EXAMINATION_COMMENT = "examination 2";

	@Mock
	private ExaminationRepository examinationRepository;

	@Mock
	private MicroserviceRequestsService microservicesRequestsService;

	@InjectMocks
	private ExaminationServiceImpl examinationService;
	
	@MockBean
	private StudyRightsService rightsService;

	@Mock
	private ShanoirEventService eventService;

	@Before
	public void setup() throws ShanoirException {
		// given(examinationRepository.findByStudyIdIn(Mockito.anyListOf(Long.class), Mockito.any(Pageable.class)))
		// 		.willReturn(Arrays.asList(ModelsUtil.createExamination()));
		given(examinationRepository.findById(EXAMINATION_ID)).willReturn(Optional.of(ModelsUtil.createExamination()));
		given(examinationRepository.save(Mockito.any(Examination.class))).willReturn(ModelsUtil.createExamination());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void deleteByIdTest() throws ShanoirException {
		examinationService.deleteById(EXAMINATION_ID);

		Mockito.verify(examinationRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void findByIdTest() throws ShanoirException {
		final Examination examination = examinationService.findById(EXAMINATION_ID);
		Assert.assertNotNull(examination);
		Assert.assertTrue(ModelsUtil.EXAMINATION_NOTE.equals(examination.getNote()));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void saveTest() throws ShanoirException {
		examinationService.save(createExamination());

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void updateTest() throws ShanoirException {
		final Examination updatedExamination = examinationService.update(createExamination());
		Assert.assertNotNull(updatedExamination);
		Assert.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void updateAsAdminTest() throws ShanoirException {
		// We update the subject -> admin -> SUCCESS
		Examination updatedExam = createExamination();
		updatedExam.setSubject(null);
		final Examination updatedExamination = examinationService.update(updatedExam);

		Assert.assertNotNull(updatedExamination);
		Assert.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));
		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	private Examination createExamination() {
		Examination oldExam  = ModelsUtil.createExamination();

		final Examination examination = new Examination();
		examination.setId(EXAMINATION_ID);
		examination.setComment(UPDATED_EXAMINATION_COMMENT);
		examination.setCenterId(oldExam.getCenterId());
		examination.setStudyId(oldExam.getStudyId());
		examination.setSubject(oldExam.getSubject());

		return examination;
	}

}
