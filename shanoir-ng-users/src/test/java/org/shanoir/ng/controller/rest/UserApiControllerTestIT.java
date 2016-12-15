package org.shanoir.ng.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.model.User;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.LoginUtil;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserService userService;

    @Before
    public void setup() {
        given(userService.findAll()).willReturn(Arrays.asList(new User()));
        given(userService.findById(1L)).willReturn(new User());
		given(userService.save(Mockito.mock(User.class))).willReturn(new User());
   }

	@Test
	public void findUserByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/user/1", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findUserByIdWithLogin() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				LoginUtil.USER_LOGIN, LoginUtil.USER_PASSWORD);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity("/user/1", String.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

	@Test
	public void findUsersProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/user/all", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findUsersWithLogin() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				LoginUtil.USER_LOGIN, LoginUtil.USER_PASSWORD);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity("/user/all", String.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

	@Test
	public void saveNewUserProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity("/user", new User(), String.class);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void saveNewUserWithLogin() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				LoginUtil.USER_LOGIN, LoginUtil.USER_PASSWORD);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final HttpEntity<User> request = new HttpEntity<User>(createUser(), generateHeaders());

			final ResponseEntity<String> response = restTemplate.postForEntity("/user", request, String.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

	/*
	 * Create a user for tests.
	 * @return a user.
	 */
	private User createUser() {
		final Role role = ModelsUtil.createRole();
		return ModelsUtil.createUser(role);
	}

	/*
	 * Generate headers for CSRF.
	 * @return http headers.
	 */
	private HttpHeaders generateHeaders() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/user/1", String.class);
		final String xsrfCookie = response.getHeaders().getFirst("Set-Cookie");
		final HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", xsrfCookie);
		headers.set("X-XSRF-TOKEN", xsrfCookie.split("=")[1].split(";")[0]);
		return headers;
	}

}
