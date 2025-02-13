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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */

@SpringBootTest
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
	StudyRepository studyRepository;
	
	@MockBean
	StudyRightsService rightsService;

	@MockBean
	private StudyInstanceUIDHandler studyInstanceUIDHandler;
	@BeforeEach
	public void setup() {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
		given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
		given(rightsRepository.findByUserIdAndStudyId(Mockito.anyLong(), Mockito.anyLong())).willReturn( new StudyUser());
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findPage, PageRequest.of(0, 10), false, "", "");
		assertAccessDenied(service::findBySubjectId, 1L);
		assertAccessDenied(service::findBySubjectIdStudyId, 1L, 1L);
		assertAccessDenied(service::save, mockExam(null));
		assertAccessDenied(service::update, mockExam(1L));
		assertAccessDenied(service::deleteById, ENTITY_ID, null);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		setCenterRightsContext();
		
		testUpdate();
		testFindOne();
		testFindPage();
		testFindBySubjectId();
		testFindBySubjectIdStudyId();
		testCreate();
		testDelete("ROLE_USER");
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		setCenterRightsContext();
		
		testUpdate();
		testFindOne();
		testFindPage();
		testFindBySubjectId();
		testFindBySubjectIdStudyId();
		testCreate();
		testDelete("ROLE_EXPERT");
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findPage, PageRequest.of(0, 10), false, "", "");
		assertAccessAuthorized(service::findBySubjectId, 1L);
		assertAccessAuthorized(service::findBySubjectIdStudyId, 1L, 1L);
		assertAccessAuthorized(service::save, mockExam(null));
		assertAccessAuthorized(service::update, mockExam(1L));
		assertAccessAuthorized(service::deleteById, ENTITY_ID, null);
	}
	
	
	private void testFindOne() throws ShanoirException {
		assertAccessAuthorized(service::findById, 1L);
		assertAccessDenied(service::findById, 2L);
		assertAccessDenied(service::findById, 3L);
		assertAccessDenied(service::findById, 4L);
	}
	
	
	private void testFindPage() throws ShanoirException {
		assertAccessAuthorized(service::findPage, PageRequest.of(0, 10), false, "", "");
		assertThat(service.findPage(PageRequest.of(0, 10), false, "", "")).hasSize(1);
	}
	
	private void testFindBySubjectId() throws ShanoirException {
		assertAccessAuthorized(service::findBySubjectId, 1L);
		List<Examination> examList2 = service.findBySubjectId(1L);
		assertThat(examList2.size()).isEqualTo(1);
		assertThat(examList2.get(0).getId()).isEqualTo(1L);
		assertAccessAuthorized(service::findBySubjectId, 2L);
		assertThat(service.findBySubjectId(2L)).isNullOrEmpty();
		assertThat(service.findBySubjectId(4L)).isNullOrEmpty();
	}
	
	private void testFindBySubjectIdStudyId() throws ShanoirException {
		assertAccessAuthorized(service::findBySubjectIdStudyId, 1L, 1L);
		try {
			// either the access is denied or the body is empty, both are fine
			assertThat(service.findBySubjectIdStudyId(2L,  2L)).isNullOrEmpty();
		} catch (AccessDeniedException e) { /* good */ }
		assertAccessDenied(service::findBySubjectIdStudyId, 4L, 4L);
		// check access denied to exam 3
		List<Examination> examList1 = service.findBySubjectIdStudyId(1L,  1L);
		assertThat(examList1.size()).isEqualTo(1);
		assertThat(examList1.get(0).getId()).isEqualTo(1L);
	}
	
	private void testCreate() throws ShanoirException {
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(service::save, mockExamination(null, 1L, 1L, 1L));
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(service::save, mockExamination(null, 1L, 1L, 1L));
		assertAccessDenied(service::save, mockExamination(null, 1L, 1L, 3L));
		assertAccessDenied(service::save, mockExamination(null, 2L, 2L, 2L));
		assertAccessDenied(service::save, mockExamination(null, 4L, 4L, 4L));
		assertAccessDenied(service::save, mockExamination(null, 2L, 1L, 1L));
		assertAccessDenied(service::save, mockExamination(null, 1L, 1L, 4L));
		assertAccessAuthorized(service::save, mockExamination(null, 1L, 2L, 1L));
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(false);
	}
	
	private void testDelete(String role) throws ShanoirException {
		if (role.equals("ROLE_USER")) {			
			assertAccessDenied(service::deleteById, 1L, null);
		} else if (role.equals("ROLE_EXPERT")) {
			assertAccessDenied(service::deleteById, 1L, null);
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
			assertAccessAuthorized(service::deleteById, 1L, null);
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
		}
		assertAccessDenied(service::deleteById, 2L, null);
		assertAccessDenied(service::deleteById, 3L, null);
		assertAccessDenied(service::deleteById, 4L, null);
	}

	private void testUpdate() throws ShanoirException {
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(false);
		Examination exam1 = mockExam(1L, 1L, 1L);
		Examination exam2 = mockExam(2L, 2L, 2L);
		Examination exam3 = mockExam(3L, 3L, 1L);
		Examination exam4 = mockExam(4L, 4L, 4L);
		assertAccessDenied(service::update, exam1);
		assertAccessDenied(service::update, exam2);
		assertAccessDenied(service::update, exam3);
		assertAccessDenied(service::update, exam4);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(service::update, exam1);
		assertAccessDenied(service::update, exam2);
		assertAccessDenied(service::update, exam3);
		exam3.setCenterId(1L); // try to move the exam to my center
		assertAccessDenied(service::update, exam3);
		exam3.setCenterId(3L); // back to center 3
		exam4.setStudy(new Study()); // try to move exam to my study
		exam4.getStudy().setId(1L);
		assertAccessDenied(service::update, exam4);
		exam4.setStudy(new Study()); //back to study 4
		exam4.getStudy().setId(4L);
	}
	
	
	private Examination mockExam(Long id) {
		Examination exam = ModelsUtil.createExamination();
		exam.setId(id);
		return exam;
	}
	
	private Examination mockExam(Long id, Long centerId, Long studyId) {
		Examination exam = mockExam(id);
		exam.setCenterId(centerId);
		exam.setStudy(mockStudy(studyId));
		return exam;
	}
	
	private Study mockStudy(Long id) {
		Study study = new Study();
		study.setId(id);
		study.setName("");
		study.setRelatedDatasets(new ArrayList<>());
		study.setSubjectStudyList(new ArrayList<>());
		study.setTags(new ArrayList<>());
		return study;
	}
	
	private Examination mockExamination(Long studyId, Long subjectId, Long centerId) {
		Examination exam = new Examination();
		exam.setExaminationDate(LocalDate.now());
		exam.setCenterId(centerId);
		exam.setStudy(mockStudy(studyId));
		exam.setSubject(new Subject(subjectId, ""));
		return exam;
	}
	
	private Examination mockExamination(Long id, Long studyId, Long subjectId, Long centerId) {
		Examination exam = mockExamination(studyId, subjectId, centerId);
		exam.setId(id);
		return exam;
	}

	
	private void setCenterRightsContext() {
		/**
		 * -> study 1 [CAN_SEE_ALL]
		 *     -> subject 1 
		 *         -> center 1 [HAS_RIGHTS]
		 *             -> exam 1
		 *         -> center 3 [no_rights]
		 *             -> exam 3
		 * -> study 2 [CAN_SEE_ALL]
		 *     -> subject 2
		 *         -> center 2 [no_rights]
		 *             -> exam 2
		 * -> study 4 [no_rights]
		 *     -> subject 4
		 *         -> center 4
		 *             -> exam 4 
		 */
		
		// has right on study 1
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		// has right on [study 1, center 1] 
		given(rightsService.hasRightOnCenter(1L, 1L)).willReturn(true);
		Set<Long> studyIds1 = new HashSet<Long>(); studyIds1.add(1L);
		given(rightsService.hasRightOnCenter(studyIds1, 1L)).willReturn(true);
		// does not have right on [study 1, center 3] 
		given(rightsService.hasRightOnCenter(1L, 3L)).willReturn(false);
		given(rightsService.hasRightOnCenter(studyIds1, 3L)).willReturn(false);
		
		// has right on study 2
		given(rightsService.hasRightOnStudy(2L, "CAN_SEE_ALL")).willReturn(true);
		// does not have right on [study 2, center 2] 
		given(rightsService.hasRightOnCenter(2L, 2L)).willReturn(false);
		Set<Long> studyIds2 = new HashSet<Long>(); studyIds2.add(2L);
		given(rightsService.hasRightOnCenter(studyIds2, 2L)).willReturn(false);
		
		// does not have right on study 4
		given(rightsService.hasRightOnStudy(4L, "CAN_SEE_ALL")).willReturn(false);
		
		// has rights on studies 1 & 2
		given(rightsService.hasRightOnStudies(Mockito.anySet(), Mockito.anyString())).willReturn(new HashSet<>(Arrays.asList(new Long[]{1L, 2L})));
		
		// exam 1 is in center 1
		Examination exam1 = mockExam(1L, 1L, 1L);
		given(examinationRepository.findById(1L)).willReturn(Optional.of(exam1));
		// exam 2 is in center 2
		Examination exam2 = mockExam(2L, 2L, 2L);
		given(examinationRepository.findById(2L)).willReturn(Optional.of(exam2));
		// exam 3 is in center 3
		Examination exam3 = mockExam(3L, 3L, 1L);
		given(examinationRepository.findById(3L)).willReturn(Optional.of(exam3));
		// exam 4 is in center 4
		Examination exam4 = mockExam(4L, 4L, 4L);
		given(examinationRepository.findById(4L)).willReturn(Optional.of(exam4));
		// exam 1 & 3 are in study 1 > subject 1 (but in different centers)
		given(examinationRepository.findBySubjectIdAndStudy_Id(1L, 1L)).willReturn(Utils.toList(exam1, exam3));
		given(examinationRepository.findBySubjectId(1L)).willReturn(Utils.toList(exam1, exam3));
		// exam 2 is in study 2 > subject 2
		given(examinationRepository.findBySubjectIdAndStudy_Id(2L, 2L)).willReturn(Utils.toList(exam2));
		given(examinationRepository.findBySubjectId(2L)).willReturn(Utils.toList(exam2));
		//exam 4 is in study 4 > subject 4
		given(examinationRepository.findBySubjectIdAndStudy_Id(4L, 4L)).willReturn(Utils.toList(exam4));
		given(examinationRepository.findBySubjectId(4L)).willReturn(Utils.toList(exam4));
		//given(examinationRepository.findByPreclinicalAndStudyIdIn(Mockito.anyBoolean(), Mockito.anyList(), Mockito.any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{exam1})));
		
		given(examinationRepository.findPageByStudyCenterOrStudyIdIn(Mockito.<Pair<Long, Long>>anyList(), Mockito.<Long>anySet(), Mockito.any(Pageable.class), Mockito.anyBoolean())).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{}), PageRequest.of(0, 10), 0));
		List<Pair<Long, Long>> studyCenterIds = new ArrayList<>();
		studyCenterIds.add(Pair.of(1L, 1L));
		given(examinationRepository.findPageByStudyCenterOrStudyIdIn(studyCenterIds, Sets.<Long>newSet(new Long[]{}), PageRequest.of(0, 10), false)).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{exam1}), PageRequest.of(0, 10), 1));
		given(examinationRepository.findAll(Mockito.any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{exam1, exam2, exam3, exam3}), PageRequest.of(0, 10), 0));
		given(rightsRepository.findDistinctStudyIdByUserId(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new Long[]{1L, 2L}));
		StudyUser su1 = new StudyUser();
		su1.setStudyId(1L);
		su1.setCenterIds(Arrays.asList(new Long[]{1L}));
		given(rightsRepository.findByUserIdAndRight(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new StudyUser[]{su1}));
		
		
		// study 1
		Study study1 = mockStudy(1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study1));
		// study 2
		Study study2 = mockStudy(2L);
		given(studyRepository.findById(2L)).willReturn(Optional.of(study2));
		// study 4
		Study study4 = mockStudy(4L);
		given(studyRepository.findById(2L)).willReturn(Optional.of(study4));
	}

}
