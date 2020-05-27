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

import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
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
public class SubjectServiceSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Subject mockNew;
	private Subject mockExisting;
	
	@Autowired
	private SubjectService service;
	
	@MockBean
	private SubjectRepository repository;
	
	@MockBean
	private StudyRepository studyRepository;
	
	@Before
	public void setup() {
		mockNew = ModelsUtil.createSubject();
		mockExisting = ModelsUtil.createSubject();
		mockExisting.setId(ENTITY_ID);
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		assertAccessDenied(service::findAll);
		assertAccessDenied(service::findAllSubjectsOfStudy, 1L);
		
		assertAccessDenied(service::findByData, "data");
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findByIdentifier, "identifier");
		assertAccessDenied(service::findByIdWithSubjecStudies, ENTITY_ID);
		assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");
		
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testReadByUser() throws ShanoirException {
		testRead();
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testCreateAsUser() throws ShanoirException {
		testCreate();
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testEditAsUser() throws ShanoirException {
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}
	
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testReadByExpert() throws ShanoirException {
		testRead();
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testCreateAsExpert() throws ShanoirException {
		testCreate();
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testEditAsExpert() throws ShanoirException {
		assertAccessAuthorized(service::update, mockExisting);
		
		Subject subjectMock1 = buildSubjectMock(ENTITY_ID);
		addStudyToMock(subjectMock1, 1L, StudyUserRight.CAN_SEE_ALL);
		given(repository.findOne(ENTITY_ID)).willReturn(subjectMock1);
		assertAccessDenied(service::deleteById, ENTITY_ID);
		
		Subject subjectMock2 = buildSubjectMock(ENTITY_ID);
		addStudyToMock(subjectMock2, 1L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findOne(ENTITY_ID)).willReturn(subjectMock2);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findAllSubjectsOfStudy, 1L);
		assertAccessAuthorized(service::findByData, "data");
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findByIdentifier, "identifier");
		assertAccessAuthorized(service::findByIdWithSubjecStudies, ENTITY_ID);
		assertAccessAuthorized(service::findSubjectFromCenterCode, "centerCode");
		assertAccessAuthorized(service::create, mockNew);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	private void testRead() throws ShanoirException {
		final String NAME = "data";
		
		Subject subjectMockNoRights = buildSubjectMock(1L);
		given(repository.findByName(NAME)).willReturn(subjectMockNoRights);
		given(repository.findOne(1L)).willReturn(subjectMockNoRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockNoRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockNoRights);
		given(repository.findSubjectFromCenterCode("centerCode%")).willReturn(subjectMockNoRights);
		assertAccessDenied(service::findByData, NAME);
		assertAccessDenied(service::findById, 1L);
		assertAccessDenied(service::findByIdentifier, "identifier");
		assertAccessDenied(service::findByIdWithSubjecStudies, 1L);
		assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");
		
		Subject subjectMockWrongRights = buildSubjectMock(1L);
		addStudyToMock(subjectMockWrongRights, 100L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
		given(repository.findByName(NAME)).willReturn(subjectMockWrongRights);
		given(repository.findOne(1L)).willReturn(subjectMockWrongRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockWrongRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockWrongRights);
		given(repository.findSubjectFromCenterCode("centerCode%")).willReturn(subjectMockWrongRights);
		assertAccessDenied(service::findByData, NAME);
		assertAccessDenied(service::findById, 1L);
		assertAccessDenied(service::findByIdentifier, "identifier");
		assertAccessDenied(service::findByIdWithSubjecStudies, 1L);
		assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");
		
		Subject subjectMockRightRights = buildSubjectMock(1L);
		addStudyToMock(subjectMockRightRights, 100L, StudyUserRight.CAN_SEE_ALL);
		given(repository.findByName(NAME)).willReturn(subjectMockRightRights);
		given(repository.findOne(1L)).willReturn(subjectMockRightRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockRightRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockRightRights);
		given(repository.findSubjectFromCenterCode("centerCode%")).willReturn(subjectMockRightRights);
		assertAccessAuthorized(service::findByData, NAME);
		assertAccessAuthorized(service::findById, 1L);
		assertAccessAuthorized(service::findByIdentifier, "identifier");
		assertAccessAuthorized(service::findByIdWithSubjecStudies, 1L);
		assertAccessAuthorized(service::findSubjectFromCenterCode, "centerCode");
	}

	private void testCreate() throws ShanoirException {
		List<Study> studiesMock;
		
		// Create subject without subject <-> study
		Subject newSubjectMock = buildSubjectMock(null);
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(9L));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 9L }))).willReturn(studiesMock);
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 9L);
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject linked to a study where I can admin, download, see all but not import.
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(10L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 10L }))).willReturn(studiesMock);
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 10L);
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject linked to a study where I can import and also to a study where I can't.
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(11L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		studiesMock.add(buildStudyMock(12L, StudyUserRight.CAN_IMPORT));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 12L, 11L }))).willReturn(studiesMock);
		given(studyRepository.findAll(Arrays.asList(new Long[] { 11L, 12L }))).willReturn(studiesMock);
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 11L);
		addStudyToMock(newSubjectMock, 12L);
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject linked to a study where I can import
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(13L, StudyUserRight.CAN_IMPORT));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 13L }))).willReturn(studiesMock);
		addStudyToMock(newSubjectMock, 13L);
		assertAccessAuthorized(service::create, newSubjectMock);
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
		
		if (study.getSubjectStudyList() == null) {
			study.setSubjectStudyList(new ArrayList<SubjectStudy>());
		}
		if (mock.getSubjectStudyList() == null) {
			mock.setSubjectStudyList(new ArrayList<SubjectStudy>());
		}
		study.getSubjectStudyList().add(subjectStudy);
		mock.getSubjectStudyList().add(subjectStudy);
	}

}
