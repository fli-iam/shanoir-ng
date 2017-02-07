package org.shanoir.ng.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

    @Autowired
    private TestRestTemplate restTemplate;

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
		final ResponseEntity<String> response = restTemplate.getForEntity("/user/1", String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	@Ignore
	public void findUserByIdWithLogin() {
        HttpHeaders headers = new HttpHeaders();
        // Example
		headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJxeGFkOTFtY3lzc3BycmFQWWJvOVoxcFZOV2FEYlg4ZGVMUDVDVUtLd3ZFIn0.eyJqdGkiOiIwMTM5OWQ1NS1mMDE0LTQzYWQtOWI2My01M2RkYjQzMDQ4OTgiLCJleHAiOjE0ODY0NzcwMzUsIm5iZiI6MCwiaWF0IjoxNDg2NDc2NzM1LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZGVtbyIsImF1ZCI6ImFuZ3VsYXIyLXByb2R1Y3QiLCJzdWIiOiI2NjMxYTdiMy1iYjBhLTRiZDUtODM2ZS04MmJhYzU2NjI3YjIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhbmd1bGFyMi1wcm9kdWN0Iiwibm9uY2UiOiJlNzFkYTBhMC1kOWIwLTRlMmQtOWYxOC1iYTQxY2VhMjViOTQiLCJhdXRoX3RpbWUiOjE0ODY0NzYwOTUsInNlc3Npb25fc3RhdGUiOiIwYzJmNjY0Yi0yYjBkLTQyOGYtYjgwMC03ZGZlZjZiZTJlN2QiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiIyNDNhZDdhMC02ZjZmLTQ4ZmEtODZjYS1jOGE1ZTBlZDAzNTYiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDozMDAwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJhZG1pblJvbGUiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50Iiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IiIsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIiwidXNlcklkIjoxLCJlbWFpbCI6ImFkbWluQHNoYW5vaXIuZnIifQ.PzNsMxwnYicNFhwOuCzSaDzCzVRn2num0rFyrS4rQn6827Bu8TDRdgyXHZNfB2swBS28vV7d2NzWsO5fITe1osDBUS2wpBABSgK4bOhXgbRU5I7fqgzeBfLs1NqES1Wbb7uRljYDV3OvQBIWDPSYMIHf1XG8joYFgr2QTR_JIe18-FzQHnC044CqKzkxsqa5uqFGGVkxbTOFYvMbQKYcQCUepo1EVmaSsAYZTemWIkMzrvZ2DT3EVzDGVHhXrU0kPGG2izlvAW04gSxWLee67PpIojIKT1GVyezF-i5cd31Jtys5j3daEDFvpqZIJvYcBHoHNmBBOZ_Nivs2RZzIRQ");

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		final ResponseEntity<String> response = restTemplate.exchange("/user/1", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findUsersProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/user/all", String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	@Ignore
	public void findUsersWithLogin() {
		HttpHeaders headers = null;

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		final ResponseEntity<String> response = restTemplate.exchange("/user/all", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findUsersWithBadRole() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				ModelsUtil.USER_LOGIN_GUEST, ModelsUtil.USER_PASSWORD_GUEST);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity("/user/all", String.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

	@Test
	public void saveNewUserProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity("/user", new User(), String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	@Ignore
	public void saveNewUserWithLogin() {
		HttpHeaders headers = null;

		HttpEntity<User> entity = new HttpEntity<User>(createUser(), headers);
		final ResponseEntity<String> response = restTemplate.exchange("/user", HttpMethod.POST, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
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
