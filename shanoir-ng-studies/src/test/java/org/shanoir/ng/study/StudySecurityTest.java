package org.shanoir.ng.study;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.tests.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.tests.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.model.security.StudyUserRight;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.tests.usermock.WithMockKeycloakUser;
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
public class StudySecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Study mockNew;
	private Study mockExisting;
	
	@Autowired
	private StudyService service;
	
	@MockBean
	private StudyRepository repository;
	
	@Before
	public void setup() {
		mockNew = ModelsUtil.createStudy();
		mockExisting = ModelsUtil.createStudy();
		mockExisting.setId(ENTITY_ID);
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		assertAccessDenied(service::findBy, "id", ENTITY_ID);
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findAll);
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testFindByIdAsUserThatCanSee() throws ShanoirException {
		
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L));
		assertAccessDenied(service::findById, 1L);
		
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT));
		assertAccessDenied(service::findById, 1L);
		
		given(repository.findOne(1L)).willReturn(buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL));
		assertAccessAuthorized(service::findById, 1L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testFindByAsUserThatCanSee() throws ShanoirException {
		assertAccessAuthorized(service::findBy, "id", ENTITY_ID);
		
		given(repository.findBy(Mockito.any(String.class), Mockito.any(Object.class))).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L), buildStudyMock(2L) } ));
		assertEquals(0, service.findBy("any", "any").size());
		
		given(repository.findBy(Mockito.any(String.class), Mockito.any(Object.class))).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT), buildStudyMock(2L) } ));
		assertEquals(0, service.findBy("any", "any").size());
		
		given(repository.findBy(Mockito.any(String.class), Mockito.any(Object.class))).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL), buildStudyMock(2L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT) } ));
		assertEquals(1, service.findBy("any", "any").size());
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testFindAllAsUserThatCanSee() throws ShanoirException {
		assertAccessAuthorized(service::findAll);
		
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightOrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL), buildStudyMock(2L, StudyUserRight.CAN_SEE_ALL) } ));
		assertEquals(2, service.findAll().size());
		
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightOrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId())).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL), buildStudyMock(2L, StudyUserRight.CAN_DOWNLOAD) } ));
		assertEquals(1, service.findAll().size());
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		
		assertAccessAuthorized(service::findBy, "id", ENTITY_ID);
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessAuthorized(service::create, mockExisting);
		
		Study mockOne = buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
		given(repository.findOne(1L)).willReturn(mockOne);
		assertAccessDenied(service::update, mockOne);
		assertAccessDenied(service::deleteById, 1L);
		
		Study mockTwo = buildStudyMock(2L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findOne(2L)).willReturn(mockTwo);
		assertAccessAuthorized(service::update, mockTwo);
		assertAccessAuthorized(service::deleteById, 2L);
		
		Study mockThree = buildStudyMock(3L);
		given(repository.findOne(3L)).willReturn(mockThree);
		assertAccessDenied(service::update, mockThree);
		assertAccessDenied(service::deleteById, 3L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		assertAccessAuthorized(service::findBy, "id", ENTITY_ID);
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessAuthorized(service::create, mockExisting);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	private Study buildStudyMock(Long id, StudyUserRight... rights) {
		Study study = ModelsUtil.createStudy();
		study.setId(id);
		List<StudyUser> studyUserList = new ArrayList<>();
		for (StudyUserRight right : rights) {
			StudyUser studyUser = new StudyUser();
			studyUser.setUserId(LOGGED_USER_ID);
			studyUser.setStudyId(id);
			studyUser.setStudyUserRight(right);
			studyUserList.add(studyUser);			
		}
		study.setStudyUserList(studyUserList);
		return study;		
	}

}
