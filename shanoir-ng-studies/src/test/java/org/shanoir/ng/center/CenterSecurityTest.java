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

package org.shanoir.ng.center;

import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.center.service.CenterService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.shanoir.ng.studycenter.StudyCenter;
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
public class CenterSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Center mockNew;
	private Center mockExisting;
	
	@Autowired
	private CenterService service;
	
	@MockBean
	private CenterRepository repository;
	
	@Before
	public void setup() {
		mockNew = ModelsUtil.createCenter();
		mockExisting = ModelsUtil.createCenter();
		mockExisting.setId(ENTITY_ID);
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		assertAccessDenied(service::findByName, "name");
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findAll);
		assertAccessDenied(service::findIdsAndNames);
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
		assertAccessDenied(service::deleteByIdCheckDependencies, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		assertAccessAuthorized(service::findByName, "name");
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findIdsAndNames);
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
		assertAccessDenied(service::deleteByIdCheckDependencies, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		assertAccessAuthorized(service::findByName, "name");
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findIdsAndNames);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessDenied(service::create, mockExisting);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
		assertAccessAuthorized(service::deleteByIdCheckDependencies, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findByName, "name");
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findIdsAndNames);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessDenied(service::create, mockExisting);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
		assertAccessAuthorized(service::deleteByIdCheckDependencies, ENTITY_ID);
	}
	
	@Test(expected = UndeletableDependenciesException.class)
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testDependenciesCheckAcq() throws EntityNotFoundException, UndeletableDependenciesException {
		final long ID = 666L;
		Center center = ModelsUtil.createCenter();
		center.setId(ID);
		List<AcquisitionEquipment> acqs = new ArrayList<>();
		acqs.add(new AcquisitionEquipment());
		center.setAcquisitionEquipments(acqs);
		given(repository.findById(ID)).willReturn(Optional.of(center));
		service.deleteByIdCheckDependencies(ID);
	}
	
	@Test(expected = UndeletableDependenciesException.class)
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testDependenciesCheckStuCenter() throws EntityNotFoundException, UndeletableDependenciesException {
		final long ID = 69L;
		Center center = ModelsUtil.createCenter();
		center.setId(ID);
		List<StudyCenter> studyCenterList = new ArrayList<>();
		studyCenterList.add(new StudyCenter());
		center.setStudyCenterList(studyCenterList);
		given(repository.findById(ID)).willReturn(Optional.of(center));
		service.deleteByIdCheckDependencies(ID);
	}

}
