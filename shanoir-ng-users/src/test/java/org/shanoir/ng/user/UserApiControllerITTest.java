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

//package org.shanoir.ng.user;
//
//import static org.junit.Assert.assertEquals;
//
//import java.security.GeneralSecurityException;
//import java.time.LocalDate;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.role.model.Role;
//import org.shanoir.ng.shared.dto.IdListDTO;
//import org.shanoir.ng.user.model.ExtensionRequestInfo;
//import org.shanoir.ng.user.model.User;
//import org.shanoir.ng.utils.KeycloakControllerTestIT;
//import org.shanoir.ng.utils.ModelsUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// * Integration tests for user controller.
// *
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//public class UserApiControllerITTest extends KeycloakControllerTestIT {
//
//	private static final String REQUEST_PATH = "/users";
//	private static final String REQUEST_PATH_EXTENSION = REQUEST_PATH + "/extension";
//	private static final String REQUEST_PATH_SEARCH = REQUEST_PATH + "/search";
//	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	public void findUserByIdProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findUserByIdWithLogin() throws GeneralSecurityException {
//		final HttpEntity<User> entity = new HttpEntity<User>(null, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity, String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findUsersProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findUsersWithLogin() throws GeneralSecurityException {
//		final HttpEntity<User> entity = new HttpEntity<User>(null, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findUsersWithBadRole() throws GeneralSecurityException {
//		final HttpEntity<User> entity = new HttpEntity<User>(null, getHeadersWithToken(false));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//	}
//
//	@Test
//	public void requestExtensionProtected() {
//		final HttpEntity<ExtensionRequestInfo> entity = new HttpEntity<>(createExtensionRequestInfo());
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_EXTENSION, HttpMethod.PUT, entity, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void requestExtensionWithLogin() throws GeneralSecurityException {
//		final HttpEntity<ExtensionRequestInfo> entity = new HttpEntity<>(createExtensionRequestInfo(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_EXTENSION, HttpMethod.PUT, entity, String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewUserProtected() {
//		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new User(), String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewUserWithLogin() throws GeneralSecurityException {
//		final User user = createUser();
//		user.setEmail("test@te.st");
//		user.setUsername("test");
//		final HttpEntity<User> entity = new HttpEntity<>(user, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//
//		// Get user id
//		String userId = response.getBody().split("\"id\":")[1].split(",")[0];
//
//		// Delete user
//		final ResponseEntity<String> responseDelete = restTemplate.exchange(REQUEST_PATH + "/" + userId,
//				HttpMethod.DELETE, entity, String.class);
//		assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
//	}
//
//	@Test
//	public void searchUsersProtected() {
//		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH_SEARCH, new IdListDTO(),
//				String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void searchUsersWithLogin() throws GeneralSecurityException {
//		final IdListDTO list = new IdListDTO();
//		list.getIdList().add(1L);
//		final HttpEntity<IdListDTO> entity = new HttpEntity<>(list, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_SEARCH, HttpMethod.POST, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewUserProtected() {
//		final HttpEntity<User> entity = new HttpEntity<>(createUser());
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewUserWithLogin() throws GeneralSecurityException {
//		final HttpEntity<User> entity = new HttpEntity<User>(createUser(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//	/*
//	 * Create an extension request for tests.
//	 * 
//	 * @return an extension request.
//	 */
//	private ExtensionRequestInfo createExtensionRequestInfo() {
//		final ExtensionRequestInfo requestInfo = new ExtensionRequestInfo();
//		requestInfo.setExtensionDate(LocalDate.now());
//		requestInfo.setExtensionMotivation("motivation");
//		return requestInfo;
//	}
//
//	/*
//	 * Create a user for tests.
//	 * 
//	 * @return a user.
//	 */
//	private User createUser() {
//		final Role role = ModelsUtil.createGuestRole();
//		return ModelsUtil.createUser(role);
//	}
//
//}
