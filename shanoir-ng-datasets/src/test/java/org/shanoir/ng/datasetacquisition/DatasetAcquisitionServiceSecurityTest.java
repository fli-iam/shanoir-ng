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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
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
public class DatasetAcquisitionServiceSecurityTest {

    private static final long LOGGED_USER_ID = 2L;
    private static final String LOGGED_USER_USERNAME = "logged";
    private static final long ENTITY_ID = 1L;

    @Autowired
    private DatasetAcquisitionService service;

    @MockBean
    private StudyUserRightsRepository rightsRepository;

    @MockBean
    private StudyRightsService rightsService;

    @MockBean
    private ShanoirEventService shanoirEventService;

    @MockBean
    private SolrService solrService;

    @MockBean
    private DatasetAcquisitionRepository datasetAcquisitionRepository;

    @MockBean
    private ExaminationRepository examinationRepository;

    @MockBean
    private StudyRepository studyRepository;

    @MockBean
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @BeforeEach
    public void setup() {
        StudyUser su1 = new StudyUser();
        su1.setStudyId(1L);
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL));
        su1.setCenterIds(Arrays.asList(new Long[]{1L}));
        given(rightsService.getUserRights()).willReturn(new UserRights(Arrays.asList(su1)));
    }

	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findByStudyCard, 1L);
		assertAccessDenied(service::findPage, PageRequest.of(0, 10));
		
		assertAccessDenied(service::create, mockDsAcq(), true);
		assertAccessDenied(service::update, mockDsAcq(1L));
		assertAccessDenied(service::deleteById, 1L, null);
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
	public void testAsAdmin() throws ShanoirException {
		setCenterRightsContext();
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findByStudyCard, 1L);
		assertAccessAuthorized(service::findPage, PageRequest.of(0, 10));		
		Page<DatasetAcquisition> page = service.findPage(PageRequest.of(0, 10));
		assertNotNull(page);
		assertEquals(4, page.getTotalElements());
		assertAccessAuthorized(service::create, mockDsAcq(), true);
		assertAccessAuthorized(service::update, mockDsAcq(1L));
		assertAccessAuthorized(service::deleteById, 1L, null);
	}

	
	private void testAll(String role) throws ShanoirException, RestServiceException {
		// create(DatasetAcquisition)
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(service::create, mockDsAcq(null, 1L, 1L, 1L), true);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(service::create, mockDsAcq(null, 1L, 1L, 1L), true);
		
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(service::create, mockDsAcq(null, 3L, 3L, 1L), true);
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		assertAccessDenied(service::create, mockDsAcq(null, 3L, 3L, 1L), true);
		
		given(rightsService.hasRightOnStudy(3L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(service::create, mockDsAcq(null, 2L, 2L, 2L), true);
		
		given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
		assertAccessDenied(service::create, mockDsAcq(null, 4L, 4L, 4L), true);
		
		// findByStudyCard(Long)
		assertAccessAuthorized(service::findByStudyCard, 1L);
		assertAccessAuthorized(service::findByStudyCard, 2L);
		assertThat(service.findByStudyCard(2L)).isNullOrEmpty();
		assertAccessAuthorized(service::findByStudyCard, 3L);
		assertThat(service.findByStudyCard(3L)).isNullOrEmpty();
		assertAccessAuthorized(service::findByStudyCard, 4L);
		assertThat(service.findByStudyCard(4L)).isNullOrEmpty();
		
		// findById(Long)
		assertAccessAuthorized(service::findById, 1L);
		assertAccessDenied(service::findById, 2L);
		assertAccessDenied(service::findById, 3L);
		assertAccessDenied(service::findById, 4L);
		
		// findByExamination(Long)
		assertAccessAuthorized(service::findByExamination, 1L);
		assertThat(service.findByExamination(2L).isEmpty());
		assertThat(service.findByExamination(3L).isEmpty());
		assertThat(service.findByExamination(4L).isEmpty());
		
		// findPage(Pageable)
		assertThat(service.findPage(PageRequest.of(0, 10))).hasSize(1);
		
		// update(DatasetAcquisition)
		if ("ROLE_USER".equals(role)) {
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
			assertAccessDenied(service::update, mockDsAcq(1L, 1L, 1L, 1L));			
		} else if ("ROLE_EXPERT".equals(role)) {
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
			assertAccessDenied(service::update, mockDsAcq(1L, 1L, 1L, 1L));
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
			assertAccessAuthorized(service::update, mockDsAcq(1L, 1L, 1L, 1L));
		}
		assertAccessDenied(service::update, mockDsAcq(2L, 2L, 2L, 2L));
		assertAccessDenied(service::update, mockDsAcq(3L, 3L, 3L, 1L));
		assertAccessDenied(service::update, mockDsAcq(4L, 4L, 4L, 4L));
		
		// deleteById(Long)
		given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
		if ("ROLE_USER".equals(role)) {
			assertAccessDenied(service::deleteById, 1L, null);
		} else if ("ROLE_EXPERT".equals(role)) {
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
			assertAccessDenied(service::deleteById, 1L, null);
			given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
			assertAccessAuthorized(service::deleteById, 1L, null);
		}
		given(rightsService.hasRightOnStudy(2L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessDenied(service::deleteById, 2L, null);
		given(rightsService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessDenied(service::deleteById, 3L, null);
		given(rightsService.hasRightOnStudy(4L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessDenied(service::deleteById, 4L, null);
		given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(2L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(4L, "CAN_ADMINISTRATE")).willReturn(false);
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

    private DatasetAcquisition mockDsAcq() {
        return mockDsAcq(null);
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
        given(datasetAcquisitionRepository.findPageByStudyCenterOrStudyIdIn(studyCenterIds, Sets.<Long>newSet(new Long[]{}), PageRequest.of(0, 10))).willReturn(new PageImpl<>(Arrays.asList(new DatasetAcquisition[]{dsAcq1}), PageRequest.of(0, 10), 1));
        given(datasetAcquisitionRepository.findAll(Mockito.any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(new DatasetAcquisition[]{dsAcq1, dsAcq2, dsAcq3, dsAcq4}), PageRequest.of(0, 10), 0));
        given(rightsRepository.findDistinctStudyIdByUserId(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new Long[]{1L, 2L}));
        StudyUser su1 = new StudyUser();
        su1.setStudyId(1L);
        su1.setCenterIds(Arrays.asList(new Long[]{1L}));
        given(rightsRepository.findByUserIdAndRight(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new StudyUser[]{su1}));

    }

}
