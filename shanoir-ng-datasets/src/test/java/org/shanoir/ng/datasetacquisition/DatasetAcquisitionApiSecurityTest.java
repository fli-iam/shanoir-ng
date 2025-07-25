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

package org.shanoir.ng.datasetacquisition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.shanoir.ng.datasetacquisition.controler.DatasetAcquisitionApi;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import org.shanoir.ng.study.rights.UserRights;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DatasetAcquisitionApiSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	
	@Autowired
	private DatasetAcquisitionApi api;
	
	@MockBean
	private StudyUserRightsRepository rightsRepository;
	
	private BindingResult mockBindingResult;
	
	@MockBean
	StudyRightsService rightsService;
	
	@MockBean
	private ExaminationRepository examinationRepository;
	
	@MockBean
	private StudyRepository studyRepository;
	
	@MockBean
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	
	@BeforeEach
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockDsAcq(1L), "datasetAcquisition");
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
		given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
		given(rightsRepository.findByUserIdAndStudyId(Mockito.anyLong(), Mockito.anyLong())).willReturn( new StudyUser());

		StudyUser su1 = new StudyUser();
		su1.setStudyId(1L);
		su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL));
		su1.setCenterIds(Arrays.asList(new Long[]{1L}));
		given(rightsService.getUserRights()).willReturn(new UserRights(Arrays.asList(su1)));
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessDenied(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findByStudyCard, 1L);
		assertAccessDenied(api::findDatasetAcquisitions, PageRequest.of(0, 10));
		assertAccessDenied(api::createNewDatasetAcquisition, new ImportJob());
		assertAccessDenied(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessDenied(api::deleteDatasetAcquisition, 1L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		setCenterRightsContext();		
		testAll("ROLE_USER");
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		setCenterRightsContext();
		testAll("ROLE_EXPERT");
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertAccessAuthorized(api::findDatasetAcquisitions, PageRequest.of(0, 10));
		//assertEquals(3, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());

		assertAccessAuthorized(api::createNewDatasetAcquisition, new ImportJob());
		assertAccessAuthorized(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessAuthorized(api::deleteDatasetAcquisition, 4L);
	}
	
	private void testAll(String role) throws ShanoirException, RestServiceException {
		// createNewDatasetAcquisition(ImportJob)
		ImportJob importJob = new ImportJob(); 
		importJob.setExaminationId(1L);
		importJob.setStudyId(1L);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, importJob);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(api::createNewDatasetAcquisition, importJob);
		
		importJob.setExaminationId(3L);
		importJob.setStudyId(1L);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, importJob);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		assertAccessDenied(api::createNewDatasetAcquisition, importJob);
		
		importJob.setExaminationId(2L);
		importJob.setStudyId(2L);
		given(rightsService.hasRightOnStudy(3L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, importJob);
		
		importJob.setExaminationId(4L);
		importJob.setStudyId(4L);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, importJob);
		
		// createNewEegDatasetAcquisition(EegImportJob)
		EegImportJob eegImportJob = new EegImportJob(); 
		eegImportJob.setExaminationId(1L);
		eegImportJob.setStudyId(1L);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, eegImportJob);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(api::createNewDatasetAcquisition, eegImportJob);
		
		eegImportJob.setExaminationId(3L);
		eegImportJob.setStudyId(1L);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, eegImportJob);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		assertAccessDenied(api::createNewDatasetAcquisition, eegImportJob);
		
		eegImportJob.setExaminationId(2L);
		eegImportJob.setStudyId(2L);
		given(rightsService.hasRightOnStudy(3L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, eegImportJob);
		
		eegImportJob.setExaminationId(4L);
		eegImportJob.setStudyId(4L);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(api::createNewDatasetAcquisition, eegImportJob);	
		
		// findByStudyCard(Long)
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertAccessAuthorized(api::findByStudyCard, 2L);
		assertThat(api.findByStudyCard(2L).getBody()).isNullOrEmpty();
		assertAccessAuthorized(api::findByStudyCard, 3L);
		assertThat(api.findByStudyCard(3L).getBody()).isNullOrEmpty();
		assertAccessAuthorized(api::findByStudyCard, 4L);
		assertThat(api.findByStudyCard(4L).getBody()).isNullOrEmpty();
		
		// deleteDatasetAcquisition(Long)
		given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
		if ("ROLE_USER".equals(role)) {
			assertAccessDenied(api::deleteDatasetAcquisition, 1L);
		} else if ("ROLE_EXPERT".equals(role)) {
			assertAccessAuthorized(api::deleteDatasetAcquisition, 1L);		
		}
		given(rightsService.hasRightOnStudy(2L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessDenied(api::deleteDatasetAcquisition, 2L);
		given(rightsService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessDenied(api::deleteDatasetAcquisition, 3L);
		given(rightsService.hasRightOnStudy(4L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessDenied(api::deleteDatasetAcquisition, 4L);
		given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(4L, "CAN_ADMINISTRATE")).willReturn(false);
		
		// findDatasetAcquisitionById(Long)
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findDatasetAcquisitionById, 2L);
		assertAccessDenied(api::findDatasetAcquisitionById, 3L);
		assertAccessDenied(api::findDatasetAcquisitionById, 4L);
		
		// findDatasetAcquisitionByExaminationId(Long)
		assertAccessAuthorized(api::findDatasetAcquisitionByExaminationId, 1L);
		assertAccessDenied(api::findDatasetAcquisitionByExaminationId, 2L);
		assertAccessDenied(api::findDatasetAcquisitionByExaminationId, 3L);
		assertAccessDenied(api::findDatasetAcquisitionByExaminationId, 4L);
		
		// findDatasetAcquisitions(Pageable)
		assertThat(api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody()).hasSize(1);

		given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
		// updateDatasetAcquisition(Long, DatasetAcquisitionDTO, BindingResult)
		if ("ROLE_USER".equals(role)) {
			assertAccessDenied(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(1L), mockBindingResult);			
		} else if ("ROLE_EXPERT".equals(role)) {
			assertAccessAuthorized(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(1L), mockBindingResult);		
		}
		assertAccessDenied(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(2L), mockBindingResult);
		assertAccessDenied(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(3L), mockBindingResult);
		assertAccessDenied(api::updateDatasetAcquisition, 1L, mockDsAcqDTO(4L), mockBindingResult);
	}


	private DatasetAcquisition mockDsAcq(Long id) {
		DatasetAcquisition dsA = ModelsUtil.createDatasetAcq();
		dsA.setId(id);
		return dsA;
	}
	
	private DatasetAcquisition mockDsAcq(Long id, Long examId, Long centerId, Long studyId) {
		DatasetAcquisition dsA = ModelsUtil.createDatasetAcq();
		dsA.setId(id);
		dsA.setExamination(mockExam(examId, centerId, studyId));
		return dsA;
	}
	
	private Examination mockExam(Long id, Long centerId, Long studyId) {
		Examination exam = mockExam(id);
		exam.setCenterId(centerId);
		exam.setStudy(mockStudy(studyId));
		return exam;
	}
	
	private Examination mockExam(Long id) {
		Examination exam = ModelsUtil.createExamination();
		exam.setId(id);
		exam.setInstrumentBasedAssessmentList(new ArrayList<>());
		return exam;
	}
	
	private DatasetAcquisitionDTO mockDsAcqDTO(Long id) {
		DatasetAcquisitionDTO dto = new DatasetAcquisitionDTO();
		dto.setId(id);
		dto.setAcquisitionEquipmentId(1L);
		dto.setExamination(new ExaminationDTO());
		dto.getExamination().setStudyId(1L);
		dto.getExamination().setCenterId(1L);
		dto.setRank(1);
		dto.setSoftwareRelease("v1.0.0");
		dto.setSortingIndex(1);
		dto.setStudyCard(ModelsUtil.createStudyCard());
		dto.setStudyCardTimestamp(10000L);
		dto.setType("Mr");
		return dto;
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
	
	private void setCenterRightsContext() {
		/**
		 * -> study 1 [CAN_SEE_ALL]
		 *     -> subject 1 
		 *         -> center 1 [HAS_RIGHTS]
		 *             -> exam 1
		 *                 -> ds acq 1 (equipment 1 - studycard 1)
		 *         -> center 3 [no_rights]
		 *             -> exam 3
		 *                 -> ds acq 3 (equipment 3 - studycard 3)
		 * -> study 2 [CAN_SEE_ALL]
		 *     -> subject 2
		 *         -> center 2 [no_rights]
		 *             -> exam 2
		 *                 -> ds acq 2 (equipment 2 - studycard 2)
		 * -> study 4 [no_rights]
		 *     -> subject 4
		 *         -> center 4
		 *             -> exam 4
		 *                 -> ds acq 4 (equipment 4 - studycard 4)
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
		
		// study 1
		Study study1 = mockStudy(1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study1));
		// study 2
		Study study2 = mockStudy(2L);
		given(studyRepository.findById(2L)).willReturn(Optional.of(study2));
		// study 4
		Study study4 = mockStudy(4L);
		given(studyRepository.findById(2L)).willReturn(Optional.of(study4));
		
		DatasetAcquisition dsAcq1 = mockDsAcq(1L, 1L, 1L, 1L);
		given(datasetAcquisitionRepository.findById(1L)).willReturn(Optional.of(dsAcq1));
		given(datasetAcquisitionRepository.findByStudyCardId(1L)).willReturn(Utils.toList(dsAcq1));
		DatasetAcquisition dsAcq3 = mockDsAcq(3L, 3L, 3L, 1L);
		given(datasetAcquisitionRepository.findById(3L)).willReturn(Optional.of(dsAcq3));
		given(datasetAcquisitionRepository.findByStudyCardId(3L)).willReturn(Utils.toList(dsAcq3));
		DatasetAcquisition dsAcq2 = mockDsAcq(2L, 2L, 2L, 2L);
		given(datasetAcquisitionRepository.findById(2L)).willReturn(Optional.of(dsAcq2));
		given(datasetAcquisitionRepository.findByStudyCardId(2L)).willReturn(Utils.toList(dsAcq2));
		DatasetAcquisition dsAcq4 = mockDsAcq(4L, 4L, 4L, 4L);
		given(datasetAcquisitionRepository.findById(4L)).willReturn(Optional.of(dsAcq4));
		given(datasetAcquisitionRepository.findByStudyCardId(4L)).willReturn(Utils.toList(dsAcq4));
		
		given(datasetAcquisitionRepository.findPageByStudyCenterOrStudyIdIn(Mockito.<Pair<Long, Long>>anyList(), Mockito.<Long>anySet(), Mockito.any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(new DatasetAcquisition[]{}), PageRequest.of(0, 10), 0));
		List<Pair<Long, Long>> studyCenterIds = new ArrayList<>();
		studyCenterIds.add(Pair.of(1L, 1L));
		given(datasetAcquisitionRepository.findPageByStudyCenterOrStudyIdIn(studyCenterIds, Sets.<Long>newSet(new Long[]{}), PageRequest.of(0, 10)))
			.willReturn(new PageImpl<>(Arrays.asList(new DatasetAcquisition[]{dsAcq1}), PageRequest.of(0, 10), 1));
		given(rightsRepository.findDistinctStudyIdByUserId(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId()))
			.willReturn(Arrays.asList(new Long[]{1L, 2L}));
		StudyUser su1 = new StudyUser();
		su1.setStudyId(1L);
		su1.setCenterIds(Arrays.asList(new Long[]{1L}));
		given(rightsRepository.findByUserIdAndRight(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new StudyUser[]{su1}));
		
	}

}
