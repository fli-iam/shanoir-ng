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

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.controler.DatasetApi;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

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
public class DatasetApiSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private BindingResult mockBindingResult;
	
	@Autowired
	private DatasetApi api;
	
	@MockBean
	StudyRightsService commService;

	MockHttpServletResponse response;
	
	@Before
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockDataset(1L), "dataset");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessDenied(t -> { try { api.deleteDataset(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
		assertAccessDenied(api::findDatasetById, 1L);
		assertAccessDenied(t -> { try { api.findDatasets(t); } catch (RestServiceException e) { fail(e.toString()); }}, PageRequest.of(0, 10));
		assertAccessDenied((t, u) -> { try { api.downloadDatasetById(t, u, response); } catch (IOException | RestServiceException e) { fail(e.toString()); }}, 1L, "dcm");
		assertAccessDenied((t, u, v) -> { try { api.updateDataset(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockDataset(1L), mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		// ?
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		// ?
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		assertAccessAuthorized(t -> { try { api.deleteDataset(t); } catch (RestServiceException e) { }}, 1L);
		assertAccessAuthorized(api::findDatasetById, 1L);
		assertAccessAuthorized(t -> { try { api.findDatasets(t); } catch (RestServiceException e) {  }}, PageRequest.of(0, 10));
		assertAccessAuthorized((t, u) -> { try { api.downloadDatasetById(t, u, response); } catch (IOException | RestServiceException e) { }}, 1L, "dcm");
		assertAccessAuthorized((t, u, v) -> { try { api.updateDataset(t, u, v); } catch (RestServiceException e) { }}, 1L, mockDataset(1L), mockBindingResult);
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
