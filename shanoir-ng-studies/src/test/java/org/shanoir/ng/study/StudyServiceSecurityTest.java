package org.shanoir.ng.study;

import static org.shanoir.ng.utils.tests.assertion.AssertUtils.assertAccessDenied;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.utils.tests.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
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
public class StudyServiceSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	
	@Autowired
	private StudyService studyService;
	
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {

//		assertAccessDenied(userService::confirmAccountRequest, mockUser);
//		assertAccessAuthorized(userService::createAccountRequest, mockAccountReqUser);
		assertAccessDenied(studyService::deleteById, 1L);
	
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {

	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {

	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {

	}

}
