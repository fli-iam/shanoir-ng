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

package org.shanoir.ng.study;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.bids.utils.BidsDeserializer;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.controler.StudyApi;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
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
public class StudyApiSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Study mockNew;
	private Study mockExisting;
	private BindingResult mockBindingResult;
	
	@Autowired
	private StudyApi api;
	
	@MockBean
	private SubjectRepository subjectRepository;
	
	@MockBean
	private StudyRepository repository;
	
	@MockBean
	private SubjectStudyRepository subjectStudyRepository;
	
	@MockBean
	private StudyBIDSService bidsService;
	
	@MockBean
	private BidsDeserializer bidsDeserializer;

	@Before
	public void setup() {
		mockNew = ModelsUtil.createStudy();
		mockExisting = ModelsUtil.createStudy();
		mockExisting.setId(ENTITY_ID);
		mockBindingResult = new BeanPropertyBindingResult(mockExisting, "study");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		assertAccessDenied(api::deleteStudy, ENTITY_ID);
		assertAccessDenied(api::findStudies);
		assertAccessDenied(t -> { try { api.findStudiesNames(); } catch (RestServiceException e) { fail(e.toString());	} }, null);
		assertAccessDenied(api::findStudyById, ENTITY_ID);
		assertAccessDenied((t, u) -> { try { api.saveNewStudy(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateStudy(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		testRead();
		
		assertAccessDenied((t, u) -> { try { api.saveNewStudy(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateStudy(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL), mockBindingResult);
		
		assertAccessDenied(api::deleteStudy, ENTITY_ID);
		given(repository.findOne(ENTITY_ID)).willReturn(buildStudyMock(ENTITY_ID));
		assertAccessDenied(api::deleteStudy, ENTITY_ID);
		given(repository.findOne(ENTITY_ID)).willReturn(buildStudyMock(ENTITY_ID, StudyUserRight.CAN_SEE_ALL));
		assertAccessDenied(api::deleteStudy, ENTITY_ID);
		given(repository.findOne(ENTITY_ID)).willReturn(buildStudyMock(ENTITY_ID, StudyUserRight.CAN_ADMINISTRATE));
		assertAccessDenied(api::deleteStudy, ENTITY_ID);

	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		testRead();

		assertAccessAuthorized((t, u) -> { try { api.saveNewStudy(t, u); } catch (RestServiceException e) {}}, mockNew, mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateStudy(t, u, v); } catch (RestServiceException e) {}}, 1L, buildStudyMock(1L, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL), mockBindingResult);
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE));
		assertAccessAuthorized((t, u, v) -> { try { api.updateStudy(t, u, v); } catch (RestServiceException e) {}}, 1L, buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD), mockBindingResult);

		given(repository.findOne(ENTITY_ID)).willReturn(buildStudyMock(ENTITY_ID));
		assertAccessDenied(api::deleteStudy, ENTITY_ID);
		given(repository.findOne(ENTITY_ID)).willReturn(buildStudyMock(ENTITY_ID, StudyUserRight.CAN_SEE_ALL));
		assertAccessDenied(api::deleteStudy, ENTITY_ID);
		given(repository.findOne(ENTITY_ID)).willReturn(buildStudyMock(ENTITY_ID, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_SEE_ALL));
		assertAccessAuthorized(api::deleteStudy, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		assertAccessAuthorized(api::deleteStudy, ENTITY_ID);
		assertAccessAuthorized(api::findStudies);
		assertAccessAuthorized(t -> { try { api.findStudiesNames(); } catch (RestServiceException e) {} }, null);
		assertAccessAuthorized(api::findStudyById, ENTITY_ID);
		assertAccessAuthorized((t, u) -> { try { api.saveNewStudy(t, u); } catch (RestServiceException e) {}}, mockNew, mockBindingResult);
		assertAccessAuthorized((t, u, v) -> { try { api.updateStudy(t, u, v); } catch (RestServiceException e) {}}, ENTITY_ID, mockExisting, mockBindingResult);
	}
	
	private void testRead() throws ShanoirException, RestServiceException {
		// No rights
		Study studyMockNoRights = buildStudyMock(1L);
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, null, true)).willReturn(Arrays.asList(studyMockNoRights));
		given(repository.findAll()).willReturn(Arrays.asList(studyMockNoRights));
		given(repository.findOne(1L)).willReturn(studyMockNoRights);
		assertAccessAuthorized(api::findStudies);
		assertEquals(null, api.findStudies().getBody());
		assertAccessAuthorized(t -> { try { api.findStudiesNames(); } catch (RestServiceException e) {} }, null);
		assertEquals(null, api.findStudiesNames().getBody());
		assertAccessDenied(api::findStudyById, 1L);
		
		// Wrong Rights
		Study studyMockWrongRights = buildStudyMock(2L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
		given(repository.findAll()).willReturn(Arrays.asList(studyMockWrongRights));
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId(), true)).willReturn(Arrays.asList(studyMockWrongRights));
		given(repository.findOne(2L)).willReturn(studyMockWrongRights);
		assertAccessAuthorized(api::findStudies);
		assertEquals(null, api.findStudies().getBody());
		assertAccessAuthorized(t -> { try { api.findStudiesNames(); } catch (RestServiceException e) {} }, null);
		assertEquals(null, api.findStudiesNames().getBody());
		assertAccessDenied(api::findStudyById, 2L);
		
		// Right rights
		Study studyMockRightRights = buildStudyMock(3L, StudyUserRight.CAN_SEE_ALL);
		given(repository.findAll()).willReturn(Arrays.asList(studyMockRightRights, studyMockWrongRights, studyMockNoRights));
		given(repository.findAll(Arrays.asList(3L))).willReturn(Arrays.asList(studyMockRightRights));
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId(), true)).willReturn(Arrays.asList(studyMockRightRights, studyMockWrongRights, studyMockNoRights));
		given(repository.findOne(3L)).willReturn(studyMockRightRights);
		assertAccessAuthorized(api::findStudies);
		assertNotNull(api.findStudies().getBody());
		assertEquals(1, api.findStudies().getBody().size());
		assertAccessAuthorized(t -> { try { api.findStudiesNames(); } catch (RestServiceException e) {} }, null);
		assertNotNull(api.findStudiesNames().getBody());
		assertEquals(1, api.findStudiesNames().getBody().size());
		assertAccessAuthorized(api::findStudyById, 3L);
	}

	private Study buildStudyMock(Long id, StudyUserRight... rights) {
		Study study = ModelsUtil.createStudy();
		study.setId(id);
		List<StudyUser> studyUserList = new ArrayList<>();
		for (StudyUserRight right : rights) {
			StudyUser studyUser = new StudyUser();
			studyUser.setUserId(LOGGED_USER_ID);
			studyUser.setUserName(LOGGED_USER_USERNAME);
			studyUser.setStudy(study);
			studyUser.setStudyUserRights(Arrays.asList(right));
			studyUserList.add(studyUser);
		}
		study.setStudyUserList(studyUserList);
		StudyCenter studyCenter = new StudyCenter();
		studyCenter.setCenter(ModelsUtil.createCenter());
		studyCenter.setStudy(study);
		studyCenter.setId(1L);
		study.setStudyCenterList(Arrays.asList(studyCenter));
		return study;
	}

}
