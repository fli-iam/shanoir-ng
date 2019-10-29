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

package org.shanoir.ng.studycard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.studycard.controler.StudyCardApi;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class StudyCardSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private BindingResult mockBindingResult;
	
	@Autowired
	private StudyCardApi api;
	
	@MockBean
	StudyRightsService commService;
	
	@Before
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockStudyCard(1L), "dataset");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessDenied(t -> { try { api.deleteStudyCard(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
		assertAccessDenied(api::findStudyCardByAcqEqId, 1L);
		assertAccessDenied(api::findStudyCardById, 1L);
		assertAccessDenied(api::findStudyCardByStudyId, 1L);
		assertAccessDenied(api::findStudyCards);
		IdList idList = new IdList(); idList.getIdList().add(1L); idList.getIdList().add(2L);
		assertAccessDenied(api::searchStudyCards, idList);
		assertAccessDenied((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockStudyCard(1L), mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewStudyCard(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockStudyCard(), mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		Set<Long> ids = new HashSet<>(); ids.add(1L);
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(ids);
		
		assertAccessAuthorized(api::findStudyCardByAcqEqId, 1L);
		assertEquals(1, api.findStudyCardByAcqEqId(1L).getBody().size());
		
		assertAccessAuthorized(api::findStudyCardById, 1L);
		assertAccessAuthorized(api::findStudyCardByStudyId, 1L);
		assertAccessAuthorized(api::findStudyCards);
		IdList idList = new IdList(); idList.getIdList().add(1L); idList.getIdList().add(2L);
		assertAccessAuthorized(api::searchStudyCards, idList);
		
		assertAccessDenied(t -> { try { api.deleteStudyCard(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
		assertAccessDenied((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockStudyCard(1L), mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewStudyCard(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockStudyCard(), mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		Set<Long> ids = new HashSet<>(); ids.add(1L);
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(ids);
		
		assertAccessAuthorized(api::findStudyCardByAcqEqId, 1L);
		assertEquals(1, api.findStudyCardByAcqEqId(1L).getBody().size());
		
		assertAccessAuthorized(api::findStudyCardById, 1L);
		assertAccessAuthorized(api::findStudyCardByStudyId, 1L);
		assertAccessAuthorized(api::findStudyCards);
		IdList idList = new IdList(); idList.getIdList().add(1L); idList.getIdList().add(2L);
		assertAccessAuthorized(api::searchStudyCards, idList);

		given(commService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
		given(commService.hasRightOnStudy(2L, "CAN_ADMINISTRATE")).willReturn(false);
		given(commService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
			
		StudyCard sc0 = mockStudyCard();
		sc0.setStudyId(3L);
		assertAccessDenied((t, u) -> { try { api.saveNewStudyCard(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, sc0, mockBindingResult);
		sc0.setStudyId(1L);
		assertAccessAuthorized((t, u) -> { try { api.saveNewStudyCard(t, u); } catch (RestServiceException e) { }}, sc0, mockBindingResult);

		assertAccessDenied((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 3L, mockStudyCard(3L), mockBindingResult);
		StudyCard sc = mockStudyCard(1L);
		sc.setStudyId(3L);
		assertAccessDenied((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, sc, mockBindingResult);
		sc.setStudyId(1L);
		assertAccessDenied((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 2L, sc, mockBindingResult);
		assertAccessAuthorized((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { }}, 1L, sc, mockBindingResult);
		
		assertAccessDenied(t -> { try { api.deleteStudyCard(t); } catch (RestServiceException e) { fail(e.toString()); }}, 3L);
		assertAccessAuthorized(t -> { try { api.deleteStudyCard(t); } catch (RestServiceException e) { }}, 1L);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessAuthorized(t -> { try { api.deleteStudyCard(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
		assertAccessAuthorized(api::findStudyCardByAcqEqId, 1L);
		assertAccessAuthorized(api::findStudyCardById, 1L);
		assertAccessAuthorized(api::findStudyCardByStudyId, 1L);
		assertAccessAuthorized(api::findStudyCards);
		IdList idList = new IdList(); idList.getIdList().add(1L); idList.getIdList().add(2L);
		assertAccessAuthorized(api::searchStudyCards, idList);
		assertAccessAuthorized((t, u, v) -> { try { api.updateStudyCard(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockStudyCard(1L), mockBindingResult);
		assertAccessAuthorized((t, u) -> { try { api.saveNewStudyCard(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockStudyCard(), mockBindingResult);
	}
	
	
	private StudyCard mockStudyCard(Long id) {
		StudyCard sc = ModelsUtil.createStudyCard();
		sc.setId(id);
		return sc;
	}
	
	private StudyCard mockStudyCard() {
		return mockStudyCard(null);
	}

}
