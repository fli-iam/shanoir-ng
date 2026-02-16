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

package org.shanoir.ng.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.controler.DatasetApi;
import org.shanoir.ng.dataset.dto.DatasetForRights;
import org.shanoir.ng.dataset.dto.DatasetLight;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.CreateStatisticsService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
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
import org.shanoir.ng.study.rights.UserRights;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

/**
 * DatasetAPI security test.
 *
 * @author jlouis
 *
 */

@SpringBootTest
@ActiveProfiles("test")
public class DatasetApiSecurityTest {

    private static final long LOGGED_USER_ID = 2L;
    private static final String LOGGED_USER_USERNAME = "logged";
    private BindingResult mockBindingResult;

    @Autowired
    private DatasetApi api;

    @MockBean
    private StudyRightsService rightsService;

    @MockBean
    private CreateStatisticsService createStatisticsService;

    @MockBean
    private StudyUserRightsRepository rightsRepository;

    private MockHttpServletResponse response;

    @MockBean
    private DatasetAcquisitionRepository datasetAcquisitionRepository;

    @MockBean
    private ExaminationRepository examinationRepository;

    @MockBean
    private StudyRepository studyRepository;

    @MockBean
    private DatasetRepository datasetRepository;

    @MockBean
    private ShanoirEventService shanoirEventService;

    @MockBean
    private SolrService solrService;

    @MockBean
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @BeforeEach
    public void setup() {
        mockBindingResult = new BeanPropertyBindingResult(mockDataset(1L), "dataset");
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
        assertAccessDenied(api::deleteDataset, 1L);
        assertAccessDenied(api::findDatasetById, 1L);
        assertAccessDenied(api::findDatasets, PageRequest.of(0, 10));
        assertAccessDenied(api::downloadDatasetById, 1L, 1L, "dcm", new MockHttpServletResponse());
        assertAccessDenied(api::updateDataset, 1L, mockDataset(1L), mockBindingResult);
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
        assertAccessAuthorized(api::deleteDataset, 1L);
        assertAccessAuthorized(api::findDatasetById, 1L);
        assertAccessAuthorized(api::findDatasets, PageRequest.of(0, 10));
        assertAccessAuthorized(api::downloadDatasetById, 1L, 1L, "dcm", new MockHttpServletResponse());
        assertAccessAuthorized(api::updateDataset, 1L, mockDataset(1L), mockBindingResult);
    }

    private void testAll(String role) throws ShanoirException, RestServiceException {
        //deleteDataset(Long)
        given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
        if ("ROLE_USER".equals(role)) {
            assertAccessDenied(api::deleteDataset, 1L);
        } else if ("ROLE_EXPERT".equals(role)) {
            assertAccessAuthorized(api::deleteDataset, 1L);
        }

        //deleteDatasets(List<Long>)
        given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
        StudyUser su1 = new StudyUser();
        su1.setStudyId(1L);
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_ADMINISTRATE));
        su1.setCenterIds(Arrays.asList(new Long[]{1L}));
        given(rightsService.getUserRights()).willReturn(new UserRights(Arrays.asList(su1)));
        if ("ROLE_USER".equals(role)) {
            assertAccessDenied(api::deleteDatasets, Utils.toList(1L, 2L, 3L, 4L));
            assertAccessDenied(api::deleteDatasets, Utils.toList(1L, 3L));
        } else if ("ROLE_EXPERT".equals(role)) {
            assertAccessAuthorized(api::deleteDatasets, Utils.toList(1L));
        }

        //findDatasetById(Long)
        given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(false);
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL));
        assertAccessAuthorized(api::findDatasetById, 1L);
        assertAccessDenied(api::findDatasetById, 2L);
        assertAccessDenied(api::findDatasetById, 3L);
        assertAccessDenied(api::findDatasetById, 4L);

        //updateDataset(Long, Dataset, BindingResult)
        given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_SEE_ALL));
        if ("ROLE_USER".equals(role)) {
            assertAccessDenied(api::updateDataset, 1L, mockDataset(1L, 1L, 1L, 1L, 1L), mockBindingResult);
        } else if ("ROLE_EXPERT".equals(role)) {
            assertAccessAuthorized(api::updateDataset, 1L, mockDataset(1L, 1L, 1L, 1L, 1L), mockBindingResult);

            Dataset ds = mockDataset(100L, 1L, 1L, 2L, 1L);
            given(datasetRepository.findById(ds.getId())).willReturn(Optional.of(ds));

            assertAccessDenied(api::updateDataset, 1L, ds, mockBindingResult);
        }
        assertAccessDenied(api::updateDataset, 1L, mockDataset(2L, 2L, 2L, 2L, 2L), mockBindingResult);
        assertAccessDenied(api::updateDataset, 1L, mockDataset(3L, 3L, 3L, 3L, 1L), mockBindingResult);
        assertAccessDenied(api::updateDataset, 1L, mockDataset(4L, 4L, 4L, 4L, 4L), mockBindingResult);

        //findDatasets(Pageable)
        assertThat(api.findDatasets(PageRequest.of(0, 10)).getBody()).hasSize(1);

        //findDatasetByStudyId(Long)
        assertAccessAuthorized(api::findDatasetByStudyId, 1L);
        assertThat(api.findDatasetByStudyId(1L).getBody()).isNotEmpty();
        assertAccessAuthorized(api::findDatasetByStudyId, 2L);
        assertThat(api.findDatasetByStudyId(2L).getBody()).isNullOrEmpty();
        assertAccessDenied(api::findDatasetByStudyId, 3L);

        //findDatasetIdsBySubjectIdStudyId(Long, Long)
        assertAccessAuthorized(api::findDatasetIdsBySubjectIdStudyId, 1L, 1L);
        assertThat(api.findDatasetIdsBySubjectIdStudyId(1L, 1L).getBody()).hasSize(1);
        assertThat(api.findDatasetIdsBySubjectIdStudyId(1L, 1L).getBody().get(0)).isEqualTo(1L);
        assertThat(api.findDatasetIdsBySubjectIdStudyId(3L, 1L).getBody()).isNullOrEmpty();
        assertThat(api.findDatasetIdsBySubjectIdStudyId(2L, 2L).getBody()).isNullOrEmpty();
        assertAccessDenied(api::findDatasetIdsBySubjectIdStudyId, 4L, 4L);

        //findDatasetsBySubjectIdStudyId(Long, Long)
        assertAccessAuthorized(api::findDatasetsBySubjectIdStudyId, 1L, 1L);
        assertThat(api.findDatasetsBySubjectIdStudyId(1L, 1L).getBody()).hasSize(1);
        assertThat(api.findDatasetsBySubjectIdStudyId(1L, 1L).getBody().get(0).getId()).isEqualTo(1L);
        assertThat(api.findDatasetsBySubjectIdStudyId(3L, 1L).getBody()).isNullOrEmpty();
        assertThat(api.findDatasetsBySubjectIdStudyId(2L, 2L).getBody()).isNullOrEmpty();
        assertAccessDenied(api::findDatasetsBySubjectIdStudyId, 4L, 4L);

        //downloadDatasetById(Long, Long, String, HttpServletResponse)
        assertAccessAuthorized(api::downloadDatasetById, 1L, 1L, "file", null);
        assertAccessDenied(api::downloadDatasetById, 2L, 1L, "file", null);
        assertAccessDenied(api::downloadDatasetById, 3L, 1L, "file", null);
        assertAccessDenied(api::downloadDatasetById, 4L, 1L, "file", null);

        //createProcessedDataset(ProcessedDatasetImportJob)
        ProcessedDatasetImportJob job = new ProcessedDatasetImportJob();
        job.setStudyId(1L);
        job.setSubjectId(1L);
        given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(false);
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL));
        assertAccessDenied(api::createProcessedDataset, job);
        given(rightsService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
        assertAccessAuthorized(api::createProcessedDataset, job);
        job.setSubjectId(3L);
        assertAccessAuthorized(api::createProcessedDataset, job);

        job.setStudyId(2L);
        job.setSubjectId(2L);
        StudyUser su2 = new StudyUser();
        su2.setStudyId(2L);
        su2.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
        given(rightsService.getUserRights()).willReturn(new UserRights(Arrays.asList(su2)));
        given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(true);
        assertAccessAuthorized(api::createProcessedDataset, job);

        job.setStudyId(4L);
        job.setSubjectId(4L);
        su2.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL));
        given(rightsService.hasRightOnStudy(2L, "CAN_IMPORT")).willReturn(false);
        assertAccessDenied(api::createProcessedDataset, job);

        job.setStudyId(2L);
        job.setSubjectId(1L);
        assertAccessDenied(api::createProcessedDataset, job);

        //massiveDownloadByDatasetIds(List<Long>, String, HttpServletResponse)
        given(rightsService.getUserRights()).willReturn(new UserRights(Arrays.asList(su1)));
        su1.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_DOWNLOAD));
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        assertAccessAuthorized(() -> api.massiveDownloadByDatasetIds(Utils.toList(1L), "file", 1L, null, mockResponse));
        assertAccessDenied(() -> api.massiveDownloadByDatasetIds(Utils.toList(1L, 3L), "file", 1L, null, mockResponse));
        assertAccessDenied(() -> api.massiveDownloadByDatasetIds(Utils.toList(3L), "file", 1L, null, mockResponse));
        assertAccessDenied(() -> api.massiveDownloadByDatasetIds(Utils.toList(1L, 2L), "file", 1L, null, mockResponse));
        assertAccessDenied(() -> api.massiveDownloadByDatasetIds(Utils.toList(2L), "file", 1L, null, mockResponse));
        assertAccessDenied(() -> api.massiveDownloadByDatasetIds(Utils.toList(4L), "file", 1L, null, mockResponse));

        //massiveDownloadByStudyId(Long, String, HttpServletResponse)
        assertAccessAuthorized(api::massiveDownloadByStudyId, 1L, "file", null);
        assertAccessDenied(api::massiveDownloadByStudyId, 2L, "file", null);
        assertAccessDenied(api::massiveDownloadByStudyId, 3L, "file", null);
        assertAccessDenied(api::massiveDownloadByStudyId, 4L, "file", null);

        //massiveDownloadByExaminationId(Long, String, HttpServletResponse)
        assertAccessAuthorized(api::massiveDownloadByExaminationId, 1L, "file", null);
        assertAccessDenied(api::massiveDownloadByExaminationId, 2L, "file", null);
        assertAccessDenied(api::massiveDownloadByExaminationId, 3L, "file", null);
        assertAccessDenied(api::massiveDownloadByExaminationId, 4L, "file", null);

        //downloadStatistics(String, String, String, String)
        assertAccessDenied(api::downloadStatistics, "", "", "", "");
    }

    private MrDataset mockDataset(Long id) {
        MrDataset ds = ModelsUtil.createMrDataset();
        ds.setId(id);
        return ds;
    }

    private MrDataset mockDataset(Long id, Long dsAcqId, Long examId, Long centerId, Long studyId) {
        MrDataset ds = ModelsUtil.createMrDataset();
        ds.setId(id);
        ds.setStudyId(studyId);
        ds.setDatasetAcquisition(mockDsAcq(dsAcqId, examId, centerId, studyId));
        return ds;
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
         *                 -> dataset acq 1
         *                     -> dataset 1
         *         -> center 3 [no_rights]
         *             -> exam 3
         *                 -> dataset acq 3
         *                     -> dataset 3
         * -> study 2 [CAN_SEE_ALL]
         *     -> subject 2
         *         -> center 2 [no_rights]
         *             -> exam 2
         *                 -> dataset acq 2
         *                     -> dataset 2
         * -> study 4 [no_rights]
         *     -> subject 4
         *         -> center 4
         *             -> exam 4
         *                 -> dataset acq 4
         *                     -> dataset 4
         */

        // has right on study 1
        given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
        given(rightsService.hasRightOnStudy(1L, "CAN_DOWNLOAD")).willReturn(true);
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
        //given(examinationRepository.findByPreclinicalAndStudyIdIn(Mockito.anyBoolean(), Mockito.anyList(), Mockito.any(Pageable.class))).willReturn(new PageImpl<>(Utils.toList(exam1)));

        given(examinationRepository.findByStudy_Id(1L)).willReturn(Utils.toList(exam1, exam3));
        given(examinationRepository.findByStudy_Id(2L)).willReturn(Utils.toList(exam2));


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

        // study 1
        Study study1 = mockStudy(1L);
        given(studyRepository.findById(1L)).willReturn(Optional.of(study1));
        // study 2
        Study study2 = mockStudy(2L);
        given(studyRepository.findById(2L)).willReturn(Optional.of(study2));
        // study 4
        Study study4 = mockStudy(4L);
        given(studyRepository.findById(2L)).willReturn(Optional.of(study4));

        // dataset 1
        Dataset dataset1 = mockDataset(1L, 1L, 1L, 1L, 1L);
        given(datasetRepository.findById(1L)).willReturn(Optional.of(dataset1));
        given(datasetRepository.findByDatasetAcquisitionExaminationId(1L)).willReturn(Arrays.asList(new Dataset[]{dataset1}));
        exam1.setDatasetAcquisitions(Utils.toList(dsAcq1));
        dsAcq1.setDatasets(Arrays.asList(new Dataset[]{dataset1}));
        // dataset 2
        Dataset dataset2 = mockDataset(2L, 2L, 2L, 2L, 3L);
        given(datasetRepository.findById(2L)).willReturn(Optional.of(dataset2));
        given(datasetRepository.findByDatasetAcquisitionExaminationId(2L)).willReturn(Arrays.asList(new Dataset[]{dataset2}));
        exam2.setDatasetAcquisitions(Utils.toList(dsAcq2));
        dsAcq2.setDatasets(Arrays.asList(new Dataset[]{dataset2}));
        // dataset 3
        Dataset dataset3 = mockDataset(3L, 3L, 3L, 3L, 1L);
        given(datasetRepository.findById(3L)).willReturn(Optional.of(dataset3));
        given(datasetRepository.findByDatasetAcquisitionExaminationId(3L)).willReturn(Arrays.asList(new Dataset[]{dataset3}));
        exam3.setDatasetAcquisitions(Utils.toList(dsAcq3));
        dsAcq3.setDatasets(Arrays.asList(new Dataset[]{dataset3}));
        // dataset 4
        Dataset dataset4 = mockDataset(4L, 4L, 4L, 4L, 4L);

        given(datasetRepository.findById(4L)).willReturn(Optional.of(dataset4));
        given(datasetRepository.findByDatasetAcquisitionExaminationId(4L)).willReturn(Arrays.asList(new Dataset[]{dataset4}));

        try {
            DatasetLight datasetLight1 = new DatasetLight(1L, "ds1", MrDataset.class, 1L, "study1", 1L, "subject1", LocalDate.now(), false, 1L);
            DatasetLight datasetLight3 = new DatasetLight(3L, "ds3", MrDataset.class, 1L, "study1", 1L, "subject1", LocalDate.now(), false, 1L);
            given(datasetRepository.findAllLightByStudyId(1L)).willReturn(Arrays.asList(new DatasetLight[]{datasetLight1, datasetLight3}));
        } catch (NoSuchMethodException | InstantiationException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            fail("exception raised : ", ex);
        }

        exam4.setDatasetAcquisitions(Utils.toList(dsAcq4));
        dsAcq4.setDatasets(Arrays.asList(new Dataset[]{dataset4}));

        StudyUser su1 = new StudyUser();
        su1.setStudyId(1L);
        su1.setCenterIds(Arrays.asList(new Long[]{1L}));
        given(rightsRepository.findByUserId(LOGGED_USER_ID)).willReturn(Arrays.asList(new StudyUser[]{su1}));
        given(datasetRepository.findAll(Mockito.any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(new Dataset[]{dataset1, dataset3})));
        given(rightsRepository.findDistinctStudyIdByUserId(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(1L, 2L));
        given(datasetRepository.findByDatasetAcquisitionExaminationStudy_IdIn(Arrays.asList(1L, 2L), PageRequest.of(0, 10).getSort())).willReturn(new PageImpl<>((Arrays.asList(new Dataset[]{dataset1, dataset2, dataset3}))));

        given(datasetRepository.findAllById(Utils.toList(1L))).willReturn(Utils.toList(dataset1));
        given(datasetRepository.findAllById(Utils.toList(1L, 3L))).willReturn(Utils.toList(dataset1, dataset3));
        given(datasetRepository.findAllById(Utils.toList(1L, 2L))).willReturn(Utils.toList(dataset1, dataset2));
        given(datasetRepository.findAllById(Utils.toList(2L))).willReturn(Utils.toList(dataset2));
        given(datasetRepository.findAllById(Utils.toList(3L))).willReturn(Utils.toList(dataset3));
        given(datasetRepository.findAllById(Utils.toList(4L))).willReturn(Utils.toList(dataset4));

        // rights
        given(datasetRepository.findDatasetsForRights(Utils.toList(1L))).willReturn(Utils.toList(new DatasetForRights(1L, 1L, 1L, new HashSet())));
        given(datasetRepository.findDatasetsForRights(Utils.toList(2L))).willReturn(Utils.toList(new DatasetForRights(2L, 2L, 3L, new HashSet())));
        given(datasetRepository.findDatasetsForRights(Utils.toList(3L))).willReturn(Utils.toList(new DatasetForRights(3L, 3L, 1L, new HashSet())));
        given(datasetRepository.findDatasetsForRights(Utils.toList(4L))).willReturn(Utils.toList(new DatasetForRights(4L, 4L, 4L, new HashSet())));
        given(datasetRepository.findDatasetsForRights(Utils.toList(1L, 3L))).willReturn(Utils.toList(new DatasetForRights(1L, 1L, 1L, new HashSet()), new DatasetForRights(3L, 3L, 1L, new HashSet())));
        given(datasetRepository.findDatasetsForRights(Utils.toList(1L, 2L))).willReturn(Utils.toList(new DatasetForRights(1L, 1L, 1L, new HashSet()), new DatasetForRights(2L, 2L, 3L, new HashSet())));
    }

}
