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

package org.shanoir.ng.subject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.shanoir.ng.subject.controler.SubjectApi;
import org.shanoir.ng.subject.dto.SubjectStudyCardIdDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;

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
public class SubjectApiSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Subject mockNew;
	private Subject mockExisting;
	private BindingResult mockBindingResult;
	
	@Autowired
	private SubjectApi api;
	
	@MockBean
	private SubjectRepository repository;
	
	@MockBean
	private StudyRepository studyRepository;
	
	@MockBean
	private SubjectStudyRepository subjectStudyRepository;
	
	@Before
	public void setup() {
		mockNew = ModelsUtil.createSubject();
		mockExisting = ModelsUtil.createSubject();
		mockExisting.setId(ENTITY_ID);
		mockBindingResult = BindingResultUtils.getBindingResult(new HashMap<String, String>(), "Subject");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		assertAccessDenied(api::findSubjects);
		assertAccessDenied(api::findSubjectsNames);
		assertAccessDenied(api::findSubjectById, ENTITY_ID);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);
		assertAccessDenied(api::findSubjectsByStudyId, ENTITY_ID);
		assertAccessDenied(api::findSubjectByIdentifier, "identifier");
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		testRead();
		testCreate();
		assertAccessDenied((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);

		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		
		Subject mock = buildSubjectMock(ENTITY_ID);
		addStudyToMock(mock, 1L, StudyUserRight.CAN_IMPORT);
		given(repository.findOne(ENTITY_ID)).willReturn(mock);
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		
		addStudyToMock(mock, 2L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findOne(ENTITY_ID)).willReturn(mock);
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		
		mock = buildSubjectMock(ENTITY_ID);
		addStudyToMock(mock, 1L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findOne(ENTITY_ID)).willReturn(mock);
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		testRead();
		testCreate();
		
		assertAccessDenied((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);

		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		
		Subject mock = buildSubjectMock(ENTITY_ID);
		addStudyToMock(mock, 1L, StudyUserRight.CAN_IMPORT);
		given(repository.findOne(ENTITY_ID)).willReturn(mock);
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		
		addStudyToMock(mock, 2L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findOne(ENTITY_ID)).willReturn(mock);
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		
		mock = buildSubjectMock(ENTITY_ID);
		addStudyToMock(mock, 1L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findOne(ENTITY_ID)).willReturn(mock);
		assertAccessAuthorized(api::deleteSubject, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		assertAccessAuthorized(api::deleteSubject, ENTITY_ID);
		assertAccessAuthorized(api::findSubjects);
		assertAccessAuthorized(api::findSubjectsNames);
		assertAccessAuthorized(api::findSubjectById, ENTITY_ID);
		assertAccessAuthorized((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);
		assertAccessAuthorized(api::findSubjectsByStudyId, ENTITY_ID);
		assertAccessAuthorized(api::findSubjectByIdentifier, "identifier");
	}
	
	private void testRead() throws ShanoirException {
		final String NAME = "data";
		
		// No rights
		Subject subjectMockNoRights = buildSubjectMock(1L);
		given(repository.findByName(NAME)).willReturn(subjectMockNoRights);
		given(repository.findOne(1L)).willReturn(subjectMockNoRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockNoRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockNoRights);
		given(repository.findFromCenterCode("centerCode")).willReturn(subjectMockNoRights);
		assertAccessDenied(api::findSubjectById, ENTITY_ID);
		assertAccessDenied(api::findSubjectByIdentifier, "identifier");
		
		given(repository.findAll()).willReturn(Arrays.asList(subjectMockNoRights));
		assertAccessAuthorized(api::findSubjects);
		assertEquals(null, api.findSubjects().getBody());
		assertAccessAuthorized(api::findSubjectsNames);
		//assertNotNull(api.findSubjectsNames().getBody());
		SubjectStudy subjectStudyMock = new SubjectStudy();
		subjectStudyMock.setStudy(buildStudyMock(1L));
		subjectStudyMock.setSubject(subjectMockNoRights);
		given(subjectStudyRepository.findByStudy(subjectStudyMock.getStudy())).willReturn(Arrays.asList(subjectStudyMock));
		assertAccessAuthorized(api::findSubjectsByStudyId, 1L);
		assertEquals(null, api.findSubjectsByStudyId(1L).getBody());
		
		
		// Wrong Rights
		Subject subjectMockWrongRights = buildSubjectMock(1L);
		addStudyToMock(subjectMockWrongRights, 100L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
		given(repository.findByName(NAME)).willReturn(subjectMockWrongRights);
		given(repository.findOne(1L)).willReturn(subjectMockWrongRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockWrongRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockWrongRights);
		given(repository.findFromCenterCode("centerCode")).willReturn(subjectMockWrongRights);
		given(repository.findAll()).willReturn(Arrays.asList(subjectMockWrongRights));
		assertAccessDenied(api::findSubjectById, ENTITY_ID);
		assertAccessDenied(api::findSubjectByIdentifier, "identifier");
		
		given(repository.findAll()).willReturn(Arrays.asList(subjectMockWrongRights));
		assertAccessAuthorized(api::findSubjects);
		assertEquals(null, api.findSubjects().getBody());
		assertAccessAuthorized(api::findSubjectsNames);
		//assertEquals(null, api.findSubjectsNames().getBody());
		subjectStudyMock = new SubjectStudy();
		subjectStudyMock.setStudy(buildStudyMock(1L));
		subjectStudyMock.setSubject(subjectMockWrongRights);
		given(subjectStudyRepository.findByStudy(subjectStudyMock.getStudy())).willReturn(Arrays.asList(subjectStudyMock));
		assertAccessAuthorized(api::findSubjectsByStudyId, 1L);
		assertEquals(null, api.findSubjectsByStudyId(1L).getBody());

		
		// Right rights (!)
		Subject subjectMockRightRights = buildSubjectMock(1L);
		addStudyToMock(subjectMockRightRights, 100L, StudyUserRight.CAN_SEE_ALL);
		given(repository.findByName(NAME)).willReturn(subjectMockRightRights);
		given(repository.findOne(1L)).willReturn(subjectMockRightRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockRightRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockRightRights);
		given(repository.findFromCenterCode("centerCode")).willReturn(subjectMockRightRights);
		assertAccessAuthorized(api::findSubjectById, ENTITY_ID);
		assertAccessAuthorized(api::findSubjectByIdentifier, "identifier");
		
		given(repository.findAll()).willReturn(Arrays.asList(subjectMockRightRights));
		given(repository.findAll(Arrays.asList(1L))).willReturn(Arrays.asList(subjectMockRightRights));
		assertAccessAuthorized(api::findSubjects);
		assertEquals(1, api.findSubjects().getBody().size());
		assertAccessAuthorized(api::findSubjectsNames);
		assertEquals(1, api.findSubjectsNames().getBody().size());
		subjectStudyMock = new SubjectStudy();
		subjectStudyMock.setStudy(buildStudyMock(1L));
		subjectStudyMock.setSubject(subjectMockRightRights);
		given(subjectStudyRepository.findByStudy(subjectStudyMock.getStudy())).willReturn(Arrays.asList(subjectStudyMock));
		given(studyRepository.findOne(1L)).willReturn(subjectStudyMock.getStudy());
		assertAccessAuthorized(api::findSubjectsByStudyId, 1L);
		assertNotNull(api.findSubjectsByStudyId(1L).getBody());
		assertEquals(1, api.findSubjectsByStudyId(1L).getBody().size());
	}

	private void testCreate() throws ShanoirException {
		List<Study> studiesMock;
		
		// Create subject without subject <-> study
		Subject newSubjectMock = buildSubjectMock(null);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, newSubjectMock, mockBindingResult);
		
		// Create subject 
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(9L));
		given(studyRepository.findAll(Arrays.asList(9L))).willReturn(studiesMock);
		given(studyRepository.findOne(9L)).willReturn(buildStudyMock(9L));
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 9L);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, newSubjectMock, mockBindingResult);
		
		// Create subject linked to a study where I can admin, download, see all but not import.
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(10L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		given(studyRepository.findAll(Arrays.asList(10L))).willReturn(studiesMock);
		given(studyRepository.findOne(10L)).willReturn(buildStudyMock(10L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 10L);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, newSubjectMock, mockBindingResult);
		
		// Create subject linked to a study where I can import and also to a study where I can't.
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(11L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		studiesMock.add(buildStudyMock(12L, StudyUserRight.CAN_IMPORT));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 12L, 11L }))).willReturn(studiesMock);
		given(studyRepository.findAll(Arrays.asList(new Long[] { 11L, 12L }))).willReturn(studiesMock);
		given(studyRepository.findOne(11L)).willReturn(buildStudyMock(11L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		given(studyRepository.findOne(12L)).willReturn(buildStudyMock(12L, StudyUserRight.CAN_IMPORT));
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 11L);
		addStudyToMock(newSubjectMock, 12L);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, newSubjectMock, mockBindingResult);
		
		// Create subject linked to a study where I can import
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(13L, StudyUserRight.CAN_IMPORT));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 13L }))).willReturn(studiesMock);
		given(studyRepository.findOne(13L)).willReturn(buildStudyMock(13L, StudyUserRight.CAN_IMPORT));
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 13L);
		assertAccessAuthorized((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, newSubjectMock, mockBindingResult);
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
	
	private Subject buildSubjectMock(Long id) {
		Subject subject = ModelsUtil.createSubject();
		subject.setId(id);
		return subject;
	}
	
	private void addStudyToMock(Subject mock, Long id, StudyUserRight... rights) {
		Study study = buildStudyMock(id, rights);
		
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setSubject(mock);
		subjectStudy.setStudy(study);
		
		if (study.getSubjectStudyList() == null) study.setSubjectStudyList(new ArrayList<SubjectStudy>());
		if (mock.getSubjectStudyList() == null) mock.setSubjectStudyList(new ArrayList<SubjectStudy>());
		study.getSubjectStudyList().add(subjectStudy);
		mock.getSubjectStudyList().add(subjectStudy);
	}
	
	private SubjectStudyCardIdDTO convertToStudyCardIdDto(Subject subject) {
		SubjectStudyCardIdDTO dto = new SubjectStudyCardIdDTO();
		dto.setStudyCardId(1L);
		dto.setSubject(subject);
		return dto;
	}

}
