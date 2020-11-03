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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class ExaminationServiceSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	@Autowired
	private ExaminationService service;
	
	@MockBean
	private ExaminationRepository examinationRepository;
	
	@MockBean
	private StudyUserRightsRepository rightsRepository;
	
	@MockBean
	StudyRightsService rightsService;
	
	@Before
	public void setup() {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
		given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findPage, new PageRequest(0, 10), false);
		assertAccessDenied(service::findBySubjectId, 1L);
		assertAccessDenied(service::findBySubjectIdStudyId, 1L, 1L);
		assertAccessDenied(service::save, mockExam());
		assertAccessDenied(service::update, mockExam(1L));
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		testFindOne();
		testFindPage();
		testFindBySubjectId();
		testFindBySubjectIdStudyId();
		testCreate();
		testUpdate(true);
		testUpdate(false);
		testDeleteDenied();
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		testFindOne();
		testFindPage();
		testFindBySubjectId();
		testFindBySubjectIdStudyId();
		testCreate();
		testUpdate(true);
		testUpdate(false);
		testDeleteByExpert();
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findPage, new PageRequest(0, 10), false);
		assertAccessAuthorized(service::findBySubjectId, 1L);
		assertAccessAuthorized(service::findBySubjectIdStudyId, 1L, 1L);
		assertAccessAuthorized(service::save, mockExam());
		assertAccessAuthorized(service::update, mockExam(1L));
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	
	private void testFindOne() throws ShanoirException {
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(false);
		given(examinationRepository.findOne(1L)).willReturn(mockExam(1L));
		assertAccessDenied(service::findById, 1L);
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		given(examinationRepository.findOne(1L)).willReturn(mockExam(1L));
		assertNotNull(service.findById(1L));
	}
	
	
	private void testFindPage() throws ShanoirException {
		List<Examination> exList = new ArrayList<>();
		Examination ex1 = mockExam(1L); ex1.setStudyId(1L); exList.add(ex1);
		Examination ex2 = mockExam(2L); ex2.setStudyId(1L); exList.add(ex2);
		Examination ex3 = mockExam(3L); ex3.setStudyId(1L); exList.add(ex3);
		Examination ex4 = mockExam(4L); ex4.setStudyId(2L); exList.add(ex4);
		Pageable pageable = new PageRequest(0, 10);
		given(examinationRepository.findByPreclinicalAndStudyIdIn(false, Arrays.asList(1L), pageable)).willReturn(new PageImpl<>(exList));
		given(examinationRepository.findAll(pageable)).willReturn(new PageImpl<>(exList));
		given(rightsRepository.findDistinctStudyIdByUserId(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(1L));
		given(examinationRepository.findByStudyIdIn(Arrays.asList(1L), pageable)).willReturn(new PageImpl<>(exList));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L, 2L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		
		assertAccessDenied(service::findPage, pageable, false);
		
		List<Examination> exList2 = new ArrayList<>();
		Examination ex11 = mockExam(1L); ex11.setStudyId(1L); exList2.add(ex11);
		Examination ex12 = mockExam(2L); ex12.setStudyId(1L); exList2.add(ex12);
		Examination ex13 = mockExam(3L); ex13.setStudyId(1L); exList2.add(ex13);
		Examination ex14 = mockExam(4L); ex14.setStudyId(2L); exList2.add(ex14);
		given(examinationRepository.findByPreclinicalAndStudyIdIn(false, Arrays.asList(1L, 2L), pageable)).willReturn(new PageImpl<>(exList2));
		given(examinationRepository.findByPreclinicalAndStudyIdIn(false, Arrays.asList(1L), pageable)).willReturn(new PageImpl<>(exList2));
		given(examinationRepository.findAll(pageable)).willReturn(new PageImpl<>(exList2));
		given(examinationRepository.findByStudyIdIn(Arrays.asList(1L, 2L), pageable)).willReturn(new PageImpl<>(exList2));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L, 2L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L, 2L)));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		
		assertAccessAuthorized(service::findPage, pageable, false);
	}
	
	private void testFindBySubjectId() throws ShanoirException {
		List<Examination> exList = new ArrayList<>();
		Examination ex1 = mockExam(1L); ex1.setStudyId(1L); exList.add(ex1);
		Examination ex2 = mockExam(2L); ex2.setStudyId(1L); exList.add(ex2);
		Examination ex3 = mockExam(3L); ex3.setStudyId(1L); exList.add(ex3);
		Examination ex4 = mockExam(4L); ex4.setStudyId(2L); exList.add(ex4);
		given(examinationRepository.findBySubjectId(1L)).willReturn(exList);
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L, 2L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		
		assertEquals(3, service.findBySubjectId(1L).size());
	}
	
	private void testFindBySubjectIdStudyId() throws ShanoirException {
		List<Examination> exList = new ArrayList<>();
		Examination ex1 = mockExam(1L); ex1.setStudyId(1L); exList.add(ex1);
		Examination ex2 = mockExam(2L); ex2.setStudyId(1L); exList.add(ex2);
		Examination ex3 = mockExam(3L); ex3.setStudyId(1L); exList.add(ex3);
		
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(false);
		given(examinationRepository.findBySubjectIdAndStudyId(1L, 1L)).willReturn(exList);
		assertAccessDenied(service::findBySubjectIdStudyId, 1l, 1L);
		
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(service::findBySubjectIdStudyId, 1L, 1L);
		assertEquals(3, service.findBySubjectIdStudyId(1L, 1L).size());
	}
	
	private void testCreate() throws ShanoirException {
		Examination mrDs = mockExam();
		mrDs.setStudyId(10L);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_SEE_ALL")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_DOWNLOAD")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(service::save, mrDs);
		given(rightsService.hasRightOnStudy(10L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(service::save, mrDs);
	}
	
	private void testDeleteDenied() throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		given(examinationRepository.findOne(Mockito.anyLong())).willReturn(mockExam(1L));
		assertAccessDenied(service::deleteById, 1L);
	}

	private void testUpdate(boolean right) throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(right);
		Examination mrDs = mockExam(1L);
		mrDs.setStudyId(10L);
		given(examinationRepository.findOne(Mockito.anyLong())).willReturn(mrDs);
		if (right) {
			assertAccessAuthorized(service::update, mrDs);
		} else {
			assertAccessDenied(service::update, mrDs);
		}
	}
	
	private void testDeleteByExpert() throws ShanoirException {
		Examination exam = mockExam(1L);
		exam.setStudyId(10L);
		given(examinationRepository.findOne(1L)).willReturn(exam);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(10L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_SEE_ALL")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_DOWNLOAD")).willReturn(true);
		assertAccessDenied(service::deleteById, 1L);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessAuthorized(service::deleteById, 1L);
	}

	
	private Examination mockExam(Long id) {
		Examination exam = ModelsUtil.createExamination();
		exam.setId(id);
		return exam;
	}
	
	private Examination mockExam() {
		return mockExam(null);
	}

}
