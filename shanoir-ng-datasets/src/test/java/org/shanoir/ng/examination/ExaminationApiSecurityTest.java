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

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.examination.controler.ExaminationApi;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("test")
public class ExaminationApiSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private BindingResult mockBindingResult;
	
	@Autowired
	private ExaminationApi api;
	
	@MockBean
	StudyRightsService commService;
	
	@Before
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockExam(1L), "examination");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		assertAccessDenied(t -> { try { api.deleteExamination(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
		assertAccessDenied(t -> { try { api.findExaminationById(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
		assertAccessDenied(api::findExaminations, new PageRequest(0, 10));
		assertAccessDenied(api::findExaminationsBySubjectIdStudyId, 1L, 1L);
		assertAccessDenied(api::findExaminationsBySubjectId, 1L);
		assertAccessDenied(api::saveNewExaminationFromShup, new ExaminationDTO(), mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewExamination(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new ExaminationDTO(), mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateExamination(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockExamDTO(1L), mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		// ?
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		// ?
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		assertAccessAuthorized(t -> { try { api.deleteExamination(t); } catch (RestServiceException e) {}}, 1L);
		assertAccessAuthorized(t -> { try { api.findExaminationById(t); } catch (RestServiceException e) {}}, 1L);
		assertAccessAuthorized(api::findExaminations, new PageRequest(0, 10));
		assertAccessAuthorized(api::findExaminationsBySubjectIdStudyId, 1L, 1L);
		assertAccessAuthorized(api::findExaminationsBySubjectId, 1L);
		assertAccessAuthorized(api::saveNewExaminationFromShup, new ExaminationDTO(), mockBindingResult);
		assertAccessAuthorized((t, u) -> { try { api.saveNewExamination(t, u); } catch (RestServiceException e) {}}, new ExaminationDTO(), mockBindingResult);
		assertAccessAuthorized((t, u, v) -> { try { api.updateExamination(t, u, v); } catch (RestServiceException e) {}}, 1L, mockExamDTO(1L), mockBindingResult);
	}

	
	private Examination mockExam(Long id) {
		Examination exam = ModelsUtil.createExamination();
		exam.setId(id);
		return exam;
	}
	
	private Examination mockExam() {
		return mockExam(null);
	}
	
	private ExaminationDTO mockExamDTO(Long id) {
		ExaminationDTO exam = new ExaminationDTO();
		exam.setId(id);
		return exam;
	}

}
