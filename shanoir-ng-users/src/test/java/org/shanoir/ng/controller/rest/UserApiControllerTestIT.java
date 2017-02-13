package org.shanoir.ng.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.configuration.security.jwt.token.JwtTokenFactory;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.model.User;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for user controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class UserApiControllerTestIT {

	private static final String REQUEST_PATH = "/user";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenFactory tokenFactory;

    @MockBean
    private UserService userService;

    @Before
    public void setup() throws ShanoirUsersException {
        given(userService.findAll()).willReturn(Arrays.asList(new User()));
        given(userService.findById(1L)).willReturn(new User());
		given(userService.save(Mockito.mock(User.class))).willReturn(new User());
   }

	@Test
	public void findUserByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void findUserByIdWithLogin() {
		HttpHeaders headers = ApiControllerTestUtil.generateHeadersWithTokenForAdmin(tokenFactory);

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findUsersProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_ALL, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void findUsersWithLogin() {
		HttpHeaders headers = ApiControllerTestUtil.generateHeadersWithTokenForAdmin(tokenFactory);

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findUsersWithBadRole() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				ModelsUtil.USER_LOGIN_GUEST, ModelsUtil.USER_PASSWORD_GUEST);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_ALL, String.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

	@Test
	public void saveNewUserProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new User(), String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void saveNewUserWithLogin() {
		HttpHeaders headers = ApiControllerTestUtil.generateHeadersWithTokenForAdmin(tokenFactory);

		HttpEntity<User> entity = new HttpEntity<User>(createUser(), headers);
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void updateNewTemplateProtected() {
		final HttpEntity<User> entity = new HttpEntity<User>(createUser());
		
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void updateNewTemplateWithLogin() throws ClientProtocolException, IOException {
		HttpHeaders headers = ApiControllerTestUtil.generateHeadersWithTokenForAdmin(tokenFactory);

		HttpEntity<User> entity = new HttpEntity<User>(createUser(), headers);
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity, String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	/*
	 * Create a user for tests.
	 * @return a user.
	 */
	private User createUser() {
		final Role role = ModelsUtil.createGuestRole();
		return ModelsUtil.createUser(role);
	}

}
