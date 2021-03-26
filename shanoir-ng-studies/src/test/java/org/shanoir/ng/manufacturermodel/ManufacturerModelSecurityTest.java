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

package org.shanoir.ng.manufacturermodel;

import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ManufacturerModelSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private ManufacturerModel mockNew;
	private ManufacturerModel mockExisting;
	
	@Autowired
	private ManufacturerModelService service;
	
	@Before
	public void setup() {
		mockNew = ModelsUtil.createManufacturerModel();
		mockExisting = ModelsUtil.createManufacturerModel();
		mockExisting.setId(ENTITY_ID);
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findAll);
		assertAccessDenied(service::findIdsAndNames);
		assertAccessDenied(service::findIdsAndNamesForCenter, 12L);
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findIdsAndNames);
		assertAccessAuthorized(service::findIdsAndNamesForCenter, 12L);
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findIdsAndNames);
		assertAccessAuthorized(service::findIdsAndNamesForCenter, 12L);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessDenied(service::create, mockExisting);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::findIdsAndNames);
		assertAccessAuthorized(service::findIdsAndNamesForCenter, 12L);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessDenied(service::create, mockExisting);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
}
