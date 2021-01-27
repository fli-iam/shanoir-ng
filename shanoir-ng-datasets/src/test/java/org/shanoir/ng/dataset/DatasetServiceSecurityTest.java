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
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetService;
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
public class DatasetServiceSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	@Autowired
	private DatasetService service;
	
	@MockBean
	private DatasetRepository datasetRepository;
	
	@MockBean
	private StudyRightsService rightsService;
	
	@MockBean
	private StudyUserRightsRepository rightsRepository;
	
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
		assertAccessDenied(service::findAll);
		assertAccessDenied(service::findPage, new PageRequest(0, 10));
		assertAccessDenied(service::create, mockDataset());
		assertAccessDenied(service::update, mockDataset(1L));
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		testFindOne();
		testFindAll();
		testFindPage();
		testCreate();
		testUpdateDenied();
		testDeleteDenied();
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		testFindOne();
		testFindAll();
		testFindPage();
		testCreate();
		testUpdateByExpert();
		testDeleteByExpert();
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findPage, new PageRequest(0, 10));
		assertAccessAuthorized(service::create, mockDataset());
		assertAccessAuthorized(service::update, mockDataset(1L));
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	
	private void testFindOne() throws ShanoirException {
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(false);
		given(datasetRepository.findOne(1L)).willReturn(mockDataset(1L));
		assertAccessDenied(service::findById, 1L);
		given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		given(datasetRepository.findOne(1L)).willReturn(mockDataset(1L));	
		assertNotNull(service.findById(1L));
	}
	

	private void testFindAll() throws ShanoirException {
		List<Dataset> dsList = new ArrayList<>();
		MrDataset ds1 = mockDataset(1L); ds1.getDatasetAcquisition().getExamination().setStudyId(1L); dsList.add(ds1);
		MrDataset ds2 = mockDataset(2L); ds1.getDatasetAcquisition().getExamination().setStudyId(1L); dsList.add(ds2);
		MrDataset ds3 = mockDataset(3L); ds1.getDatasetAcquisition().getExamination().setStudyId(1L); dsList.add(ds3);
		MrDataset ds4 = mockDataset(4L); ds1.getDatasetAcquisition().getExamination().setStudyId(2L); dsList.add(ds4);
		given(datasetRepository.findAll()).willReturn(dsList);
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L, 2L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		assertEquals(3, service.findAll().size());		
	}
	
	private void testFindPage() throws ShanoirException {
		List<Dataset> dsList = new ArrayList<>();
		MrDataset ds1 = mockDataset(1L); ds1.getDatasetAcquisition().getExamination().setStudyId(1L); dsList.add(ds1);
		MrDataset ds2 = mockDataset(2L); ds1.getDatasetAcquisition().getExamination().setStudyId(1L); dsList.add(ds2);
		MrDataset ds3 = mockDataset(3L); ds1.getDatasetAcquisition().getExamination().setStudyId(1L); dsList.add(ds3);
		MrDataset ds4 = mockDataset(4L); ds1.getDatasetAcquisition().getExamination().setStudyId(2L); dsList.add(ds4);		
		Pageable pageable = new PageRequest(0, 10);
		given(datasetRepository.findAll(pageable)).willReturn(new PageImpl<>(dsList));
		given(rightsRepository.findDistinctStudyIdByUserId(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(1L));
		given(datasetRepository.findByDatasetAcquisitionExaminationStudyIdIn(Arrays.asList(1L), pageable)).willReturn(new PageImpl<>(dsList));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L, 2L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		given(rightsService.hasRightOnStudies(new HashSet<Long>(Arrays.asList(1L)), "CAN_SEE_ALL")).willReturn(new HashSet<Long>(Arrays.asList(1L)));
		
		assertAccessDenied(service::findPage, pageable);
		
		List<Dataset> dsList2 = new ArrayList<>();
		MrDataset ds11 = mockDataset(1L); ds11.getDatasetAcquisition().getExamination().setStudyId(1L); dsList2.add(ds11);
		MrDataset ds21 = mockDataset(2L); ds21.getDatasetAcquisition().getExamination().setStudyId(1L); dsList2.add(ds21);
		MrDataset ds31 = mockDataset(3L); ds31.getDatasetAcquisition().getExamination().setStudyId(1L); dsList2.add(ds31);
		given(datasetRepository.findAll(pageable)).willReturn(new PageImpl<>(dsList2));
		given(datasetRepository.findByDatasetAcquisitionExaminationStudyIdIn(Arrays.asList(1L), pageable)).willReturn(new PageImpl<>(dsList2));
		
		assertAccessAuthorized(service::findPage, pageable);
	}
	
	
	private void testCreate() throws ShanoirException {
		MrDataset mrDs = mockDataset();
		mrDs.getDatasetAcquisition().getExamination().setStudyId(10L);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_SEE_ALL")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_DOWNLOAD")).willReturn(true);
		assertAccessDenied(service::create, mrDs);
		given(rightsService.hasRightOnStudy(10L, "CAN_IMPORT")).willReturn(true);
		assertAccessAuthorized(service::create, mrDs);
	}
	
	
	private void testDeleteDenied() throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		given(datasetRepository.findOne(Mockito.anyLong())).willReturn(mockDataset(1L));
		assertAccessDenied(service::deleteById, 1L);
	}

	private void testUpdateDenied() throws ShanoirException {
		given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		MrDataset mrDs = mockDataset(1L);
		mrDs.getDatasetAcquisition().getExamination().setStudyId(10L);
		given(datasetRepository.findOne(Mockito.anyLong())).willReturn(mrDs);
		assertAccessDenied(service::update, mrDs);
	}
	
	private void testDeleteByExpert() throws ShanoirException {
		MrDataset mrDs = mockDataset(1L);
		mrDs.getDatasetAcquisition().getExamination().setStudyId(10L);
		given(datasetRepository.findOne(1L)).willReturn(mrDs);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(10L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_SEE_ALL")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_DOWNLOAD")).willReturn(true);
		assertAccessDenied(service::deleteById, 1L);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessAuthorized(service::deleteById, 1L);
	}

	private void testUpdateByExpert() throws ShanoirException {
		MrDataset mrDs = mockDataset(1L);
		mrDs.getDatasetAcquisition().getExamination().setStudyId(10L);
		given(datasetRepository.findOne(1L)).willReturn(mrDs);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(10L, "CAN_IMPORT")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_SEE_ALL")).willReturn(true);
		given(rightsService.hasRightOnStudy(10L, "CAN_DOWNLOAD")).willReturn(true);
		given(rightsService.hasRightOnStudy(20L, "CAN_ADMINISTRATE")).willReturn(false);
		given(rightsService.hasRightOnStudy(30L, "CAN_ADMINISTRATE")).willReturn(true);
		
		MrDataset mrDsUpdated = mockDataset(1L);
		mrDsUpdated.getDatasetAcquisition().getExamination().setStudyId(10L);
		mrDsUpdated.setSubjectId(123L);
		assertAccessDenied(service::update, mrDsUpdated);
		given(rightsService.hasRightOnStudy(10L, "CAN_ADMINISTRATE")).willReturn(true);
		assertAccessAuthorized(service::update, mrDsUpdated);
		
		mrDsUpdated.getDatasetAcquisition().getExamination().setStudyId(20L);
		assertAccessDenied(service::update, mrDsUpdated);
		
		mrDsUpdated.getDatasetAcquisition().getExamination().setStudyId(30L);
		assertAccessAuthorized(service::update, mrDsUpdated);
	}

	
	private MrDataset mockDataset(Long id) {
		MrDataset ds = ModelsUtil.createMrDataset();
		ds.setId(id);
		return ds;
	}
	
	private MrDataset mockDataset() {
		return mockDataset(null);
	}
}
