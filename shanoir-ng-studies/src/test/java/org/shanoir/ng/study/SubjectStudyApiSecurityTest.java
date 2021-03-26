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

package org.shanoir.ng.study;

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.controler.SubjectStudyApi;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class SubjectStudyApiSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	@Autowired
	private SubjectStudyApi api;
	
	@MockBean
	private StudyRepository repository;
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE));
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessDenied((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) { fail(e.toString());	} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUnauthorizedUser() throws ShanoirException, RestServiceException {
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessDenied((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) { fail(e.toString());	} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsAuthorizedUser() throws ShanoirException, RestServiceException {
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_IMPORT));
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID, StudyUserRight.CAN_IMPORT);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) {} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsUnauthorizedExpert() throws ShanoirException, RestServiceException {
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L));
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessDenied((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) { fail(e.toString());	} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsAuthorizedExpert() throws ShanoirException, RestServiceException {
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE));
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID, StudyUserRight.CAN_ADMINISTRATE);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) {} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsUnauthorizedAdmin() throws ShanoirException, RestServiceException {
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) {} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAuthorizedAdmin() throws ShanoirException, RestServiceException {
		SubjectStudy subjectStudy = buildSubjectStudyMock(ENTITY_ID, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL);
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectStudy, "subjectStudy");
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubjectStudy(t, u, v); } catch (RestServiceException e) {} }, ENTITY_ID, subjectStudy, bindingResult);
	}
	
	

	private SubjectStudy buildSubjectStudyMock(Long id, StudyUserRight... rights) {
		Study study = buildStudyMock(1L, rights);

		Subject subject = ModelsUtil.createSubject();
		subject.setId(1L);
		
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setId(id);
		subjectStudy.setStudy(study);
		subjectStudy.setSubject(subject);
		
		return subjectStudy;
	}
	
	private Study buildStudyMock(Long id, StudyUserRight... rights) {
		Study study = ModelsUtil.createStudy();
		study.setId(id);
		List<StudyUser> studyUserList = new ArrayList<>();
		for (StudyUserRight right : rights) {
			StudyUser studyUser = new StudyUser();
			studyUser.setUserId(LOGGED_USER_ID);
			studyUser.setStudy(study);
			studyUser.setStudyUserRights(Arrays.asList(right));
			studyUserList.add(studyUser);
		}
		study.setStudyUserList(studyUserList);
		return study;
	}

}
