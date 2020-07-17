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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

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
public class DatasetAcquisitionServiceSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	@Autowired
	private DatasetAcquisitionService service;
	
	@MockBean
	StudyRightsService commService;
	
	@MockBean
	private RestTemplate restTemplate;

	
	@Before
	public void setup() {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
		given(commService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		//assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findByStudyCard, 1L);
		assertAccessDenied(service::findAll);
		
		//assertAccessDenied(service::create, mockDsAcq());
		assertAccessDenied(service::update, mockDsAcq(1L));
		assertAccessDenied(service::deleteById, 1L);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(service::findById, ENTITY_ID);
		//assertAccessDenied(service::findById, 3L);
		
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(new HashSet<Long>());
		assertAccessAuthorized(service::findAll);
		assertEquals(0, service.findAll().size());
		assertEquals(0, service.findByStudyCard(1L).size());
		Set<Long> ids = new HashSet<>(); ids.add(1L); ids.add(2L);
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(ids);
		assertEquals(2, service.findAll().size());
		assertEquals(2, service.findByStudyCard(1L).size());
		
		//assertAccessDenied(service::create, mockDsAcq());
		assertAccessDenied(service::update, mockDsAcq(1L));
		assertAccessDenied(service::deleteById, 1L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(service::findById, ENTITY_ID);
		//assertAccessDenied(service::findById, 3L);
		
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(new HashSet<Long>());
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findByStudyCard, 1L);
		assertEquals(0, service.findAll().size());
		assertEquals(0, service.findByStudyCard(1L).size());
		Set<Long> ids = new HashSet<>(); ids.add(1L); ids.add(2L);
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(ids);
		assertEquals(2, service.findAll().size());
		assertEquals(2, service.findByStudyCard(1L).size());
		
		given(commService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		DatasetAcquisition dsAcq = mockDsAcq();
		dsAcq.getExamination().setStudyId(3L);
		//assertAccessDenied(service::create, dsAcq);
		dsAcq.getExamination().setStudyId(1L);
		DatasetAcquisition dsAcqDB = service.create(dsAcq);
		
		given(commService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
		given(commService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
		dsAcqDB.setRank(1000);
		dsAcqDB.getExamination().setStudyId(3L);
		assertAccessDenied(service::update, dsAcqDB);
		assertAccessDenied(service::update, mockDsAcq(3L));
		dsAcqDB.getExamination().setStudyId(1L);
		assertAccessAuthorized(service::update, dsAcqDB);
		
		//assertAccessDenied(service::deleteById, 3L);
		assertAccessAuthorized(service::deleteById, dsAcqDB.getId());
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findByStudyCard, 1L);
		assertAccessAuthorized(service::findAll);
		assertEquals(3, service.findAll().size());
		assertAccessAuthorized(service::create, mockDsAcq());
		assertAccessAuthorized(service::update, mockDsAcq(1L));
		assertAccessAuthorized(service::deleteById, 1L);
	}

	
	private DatasetAcquisition mockDsAcq(Long id) {
		DatasetAcquisition dsA = ModelsUtil.createDatasetAcq();
		dsA.setId(id);
		return dsA;
	}
	
	private DatasetAcquisition mockDsAcq() {
		return mockDsAcq(null);
	}

}
