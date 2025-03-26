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
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.examination.controler.ExaminationApi;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.SubjectDTO;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

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
public class ExaminationApiSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private BindingResult mockBindingResult;
	
	@Autowired
	private ExaminationApi api;
	
	@MockBean
	StudyRightsService rightsService;
	
	@MockBean
	ExaminationRepository examinationRepository;
	
	@MockBean
	StudyRepository studyRepository;
	
	@MockBean
	StudyUserRightsRepository studyUserRightsRepository;

	@MockBean
	private StudyInstanceUIDHandler studyInstanceUIDHandler;

	@BeforeEach
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockExam(1L), "examination");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		assertAccessDenied(api::deleteExamination, 1L);
		assertAccessDenied(api::findExaminationById, 1L);
		assertAccessDenied(api::findExaminations, PageRequest.of(0, 10), "", "");
		assertAccessDenied((subjectId, studyId) -> api.findExaminationsBySubjectIdStudyId(subjectId, studyId), 1L, 1L);
		assertAccessDenied(api::findExaminationsBySubjectId, 1L);
		assertAccessDenied(api::saveNewExamination, new ExaminationDTO(), mockBindingResult);
		assertAccessDenied(api::updateExamination, 1L, mockExaminationDTO(1L), mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		testAll("ROLE_USER");
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		testAll("ROLE_EXPERT");
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		assertAccessAuthorized(api::deleteExamination, 1L);
		assertAccessAuthorized(api::findExaminationById, 1L);
		assertAccessAuthorized(api::findExaminations, PageRequest.of(0, 10), "", "");
		assertAccessAuthorized((subjectId, studyId) -> api.findExaminationsBySubjectIdStudyId(subjectId, studyId), 1L, 1L);
		assertAccessAuthorized(api::findExaminationsBySubjectId, 1L);
		assertAccessAuthorized(api::saveNewExamination, new ExaminationDTO(), mockBindingResult);
		assertAccessAuthorized(api::updateExamination, 1L, mockExaminationDTO(1L), mockBindingResult);
	}
	
	
	private void testAll(String role) throws ShanoirException, RestServiceException {
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
		ExaminationDTO examDTO1 = mockExaminationDTO(1L, 1L, 1L, 1L);
		// exam 2 is in center 2
		Examination exam2 = mockExam(2L, 2L, 2L);
		given(examinationRepository.findById(2L)).willReturn(Optional.of(exam2));
		ExaminationDTO examDTO2 = mockExaminationDTO(2L, 2L, 2L, 2L);
		// exam 3 is in center 3
		Examination exam3 = mockExam(3L, 3L, 1L);
		given(examinationRepository.findById(3L)).willReturn(Optional.of(exam3));
		ExaminationDTO examDTO3 = mockExaminationDTO(3L, 1L, 1L, 3L);
		// exam 4 is in center 4
		Examination exam4 = mockExam(4L, 4L, 4L);
		given(examinationRepository.findById(4L)).willReturn(Optional.of(exam4));
		ExaminationDTO examDTO4 = mockExaminationDTO(4L, 4L, 4L, 4L);
		// exam 1 & 3 are in study 1 > subject 1 (but in different centers)
		given(examinationRepository.findBySubjectIdAndStudy_Id(1L, 1L)).willReturn(Utils.toList(exam1, exam3));
		given(examinationRepository.findBySubjectId(1L)).willReturn(Utils.toList(exam1, exam3));
		// exam 2 is in study 2 > subject 2
		given(examinationRepository.findBySubjectIdAndStudy_Id(2L, 2L)).willReturn(Utils.toList(exam2));
		given(examinationRepository.findBySubjectId(2L)).willReturn(Utils.toList(exam2));
		//exam 4 is in study 4 > subject 4
		given(examinationRepository.findBySubjectIdAndStudy_Id(4L, 4L)).willReturn(Utils.toList(exam4));
		given(examinationRepository.findBySubjectId(4L)).willReturn(Utils.toList(exam4));
		given(examinationRepository.findPageByStudyCenterOrStudyIdIn(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{exam1})));
		given(examinationRepository.findPageByStudyCenterOrStudyIdIn(Mockito.any(), Mockito.any(), Mockito.any())).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{exam1})));
		given(examinationRepository.findPageByStudyCenterOrStudyIdInAndSubjectName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willReturn(new PageImpl<>(Arrays.asList(new Examination[]{exam1})));
		given(examinationRepository.findAllByStudyCenterOrStudyIdIn(Mockito.any(), Mockito.any())).willReturn(Arrays.asList(new Examination[]{exam1}));
		
		// study 1
		Study study1 = mockStudy(1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study1));
		// study 2
		Study study2 = mockStudy(2L);
		given(studyRepository.findById(2L)).willReturn(Optional.of(study2));
		// study 4
		Study study4 = mockStudy(4L);
		given(studyRepository.findById(2L)).willReturn(Optional.of(study4));
		
		// deleteExamination(Long)
		if (role.equals("ROLE_USER")) {			
			assertAccessDenied(api::deleteExamination, 1L);
		} else if (role.equals("ROLE_EXPERT")) {
			assertAccessDenied(api::deleteExamination, 1L);
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
			assertAccessAuthorized(api::deleteExamination, 1L);
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
		}
		assertAccessDenied(api::deleteExamination, 2L);
		assertAccessDenied(api::deleteExamination, 3L);
		assertAccessDenied(api::deleteExamination, 4L);
		
		// findExaminationById(Long)
		assertAccessAuthorized(api::findExaminationById, 1L);
		assertAccessDenied(api::findExaminationById, 2L);
		assertAccessDenied(api::findExaminationById, 3L);
		assertAccessDenied(api::findExaminationById, 4L);
		
		// findExaminations(Pageable)
		assertAccessAuthorized(api::findExaminations, PageRequest.of(0, 10), "", "");
		assertThat(api.findExaminations(PageRequest.of(0, 10), "", "").getBody()).hasSize(1);
		
		// findPreclinicalExaminations(Boolean, Pageable)
		assertAccessAuthorized(api::findPreclinicalExaminations, true, PageRequest.of(0, 10));
		assertAccessAuthorized(api::findPreclinicalExaminations, false, PageRequest.of(0, 10));
		
		// findExaminationsBySubjectIdStudyId(Long, Long)
		assertAccessAuthorized((subjectId, studyId) -> api.findExaminationsBySubjectIdStudyId(subjectId, studyId), 1L, 1L);
		try {
			// either the access is denied or the body is empty, both are fine
			ResponseEntity<List<SubjectExaminationDTO>> examsOfSubject2LStudy2L = api.findExaminationsBySubjectIdStudyId(2L,  2L);
			assertThat(examsOfSubject2LStudy2L.getBody() == null || examsOfSubject2LStudy2L.getBody().isEmpty());
		} catch (AccessDeniedException e) { /* good */ }
		assertAccessDenied((subjectId, studyId) -> api.findExaminationsBySubjectIdStudyId(subjectId, studyId), 4L, 4L);
		// check access denied to exam 3
		List<SubjectExaminationDTO> examList1 = api.findExaminationsBySubjectIdStudyId(1L,  1L).getBody();
		assertThat(examList1.size()).isEqualTo(1);
		assertThat(examList1.get(0).getId()).isEqualTo(1L);
		
		// findExaminationsBySubjectId(Long)
		assertAccessAuthorized(api::findExaminationsBySubjectId, 1L);
		List<ExaminationDTO> examList2 = api.findExaminationsBySubjectId(1L).getBody();
		assertThat(examList2.size()).isEqualTo(1);
		assertThat(examList2.get(0).getId()).isEqualTo(1L);
		assertAccessAuthorized(api::findExaminationsBySubjectId, 2L);
		assertThat(api.findExaminationsBySubjectId(2L).getBody()).isNull();
		assertThat(api.findExaminationsBySubjectId(4L).getBody()).isNull();
		
		// saveNewExamination(ExaminationDTO, BindingResult)
		assertAccessDenied(api::saveNewExamination, mockExaminationDTO(null, 1L, 1L, 1L), mockBindingResult);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(api::saveNewExamination, mockExaminationDTO(null, 1L, 1L, 1L), mockBindingResult);
		assertAccessDenied(api::saveNewExamination, mockExaminationDTO(null, 1L, 1L, 3L), mockBindingResult);
		assertAccessDenied(api::saveNewExamination, mockExaminationDTO(null, 2L, 2L, 2L), mockBindingResult);
		assertAccessDenied(api::saveNewExamination, mockExaminationDTO(null, 4L, 4L, 4L), mockBindingResult);
		assertAccessDenied(api::saveNewExamination, mockExaminationDTO(null, 2L, 1L, 1L), mockBindingResult);
		assertAccessDenied(api::saveNewExamination, mockExaminationDTO(null, 1L, 1L, 4L), mockBindingResult);
		assertAccessAuthorized(api::saveNewExamination, mockExaminationDTO(null, 1L, 2L, 1L), mockBindingResult);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(false);
		
		// updateExamination(Long, ExaminationDTO, BindingResult)
		assertAccessDenied(api::updateExamination, 1L, examDTO1, mockBindingResult);
		assertAccessDenied(api::updateExamination, 2L, examDTO2, mockBindingResult);
		assertAccessDenied(api::updateExamination, 3L, examDTO3, mockBindingResult);
		assertAccessDenied(api::updateExamination, 4L, examDTO4, mockBindingResult);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(api::updateExamination, 1L, examDTO1, mockBindingResult);
		assertAccessDenied(api::updateExamination, 2L, examDTO2, mockBindingResult);
		assertAccessDenied(api::updateExamination, 3L, examDTO3, mockBindingResult);
		examDTO3.setCenterId(1L); // try to move the exam to my center
		assertAccessDenied(api::updateExamination, 3L, examDTO3, mockBindingResult);
		examDTO3.setCenterId(3L); // back to center 3
		examDTO4.setStudyId(1L); // try to move exam to my study
		assertAccessDenied(api::updateExamination, 4L, examDTO4, mockBindingResult);
		examDTO4.setStudyId(4L); //back to study 4
		
		// addExtraData(Long, MultipartFile)
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::addExtraData, 1L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		assertAccessDenied(api::addExtraData, 2L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		assertAccessDenied(api::addExtraData, 3L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		assertAccessDenied(api::addExtraData, 4L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(api::addExtraData, 1L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		assertAccessDenied(api::addExtraData, 2L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		assertAccessDenied(api::addExtraData, 3L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		assertAccessDenied(api::addExtraData, 4L, new MockMultipartFile("data", "filename.zip", "application/zip", "some xml".getBytes()));
		
		// downloadExtraData(Long, String, HttpServletResponse)
		given(rightsService.hasRightOnStudy(1L, "CAN_DOWNLOAD")).willReturn(true);
		given(rightsService.hasRightOnStudy(2L, "CAN_DOWNLOAD")).willReturn(true);
		assertAccessAuthorized(api::downloadExtraData, 1L, "test", new MockHttpServletResponse());
		assertAccessDenied(api::downloadExtraData, 2L, "test", new MockHttpServletResponse());
		assertAccessDenied(api::downloadExtraData, 3L, "test", new MockHttpServletResponse());
		assertAccessDenied(api::downloadExtraData, 4L, "test", new MockHttpServletResponse());
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
	
	private ExaminationDTO mockExaminationDTO(Long id) {
		ExaminationDTO exam = new ExaminationDTO();
		exam.setId(id);
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
	
	private ExaminationDTO mockExaminationDTO(Long studyId, Long subjectId, Long centerId) {
		ExaminationDTO dto = new ExaminationDTO();
		dto.setExaminationDate(LocalDate.now());
		dto.setCenterId(centerId);
		dto.setStudyId(studyId);
		dto.setSubject(new SubjectDTO(subjectId, ""));
		return dto;
	}
	
	private ExaminationDTO mockExaminationDTO(Long id, Long studyId, Long subjectId, Long centerId) {
		ExaminationDTO dto = mockExaminationDTO(studyId, subjectId, centerId);
		dto.setId(id);
		return dto;
	}

}
