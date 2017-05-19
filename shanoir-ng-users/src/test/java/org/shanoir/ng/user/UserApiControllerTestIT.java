package org.shanoir.ng.user;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.role.Role;
import org.shanoir.ng.user.User;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UserApiControllerTestIT extends KeycloakControllerTestIT {

	private static final String REQUEST_PATH = "/user";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findUserByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findUserByIdWithLogin() {
		final HttpEntity<User> entity = new HttpEntity<User>(null, getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findUsersProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findUsersWithLogin() {
		final HttpEntity<User> entity = new HttpEntity<User>(null, getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findUsersWithBadRole() {
		final HttpEntity<User> entity = new HttpEntity<User>(null, getHeadersWithToken(false));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void requestExtensionProtected() {
		final HttpEntity<String> entity = new HttpEntity<String>("motivation");

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH + "/extension", HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void requestExtensionWithLogin() {
		final HttpEntity<String> entity = new HttpEntity<String>("motivation", getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH + "/extension", HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	public void saveNewUserProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new User(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewUserWithLogin() {
		final User user = createUser();
		user.setEmail("test@te.st");
		user.setUsername("test");
		final HttpEntity<User> entity = new HttpEntity<User>(user, getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		// Get user id
		String userId = response.getBody().split("\"id\":")[1].split(",")[0];

		// Delete user
		final ResponseEntity<String> responseDelete = restTemplate
				.exchange(REQUEST_PATH + "/" + userId, HttpMethod.DELETE, entity, String.class);
		assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
	}

	@Test
	public void updateNewUserProtected() {
		final HttpEntity<User> entity = new HttpEntity<User>(createUser());

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateNewUserWithLogin() {
		final HttpEntity<User> entity = new HttpEntity<User>(createUser(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	/*
	 * Create a user for tests.
	 * 
	 * @return a user.
	 */
	private User createUser() {
		final Role role = ModelsUtil.createGuestRole();
		return ModelsUtil.createUser(role);
	}

}
