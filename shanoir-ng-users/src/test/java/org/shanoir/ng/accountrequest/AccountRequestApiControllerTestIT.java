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

//package org.shanoir.ng.accountrequest;
//
//import static org.junit.Assert.assertEquals;
//
//import java.security.GeneralSecurityException;
//
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.accountrequest.model.AccountRequestInfo;
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
//@ActiveProfiles("dev")
//public class AccountRequestApiControllerTestIT extends KeycloakControllerTestIT {
//
//	private static final String AR_REQUEST_PATH = "/accountrequest";
//	private static final String USER_REQUEST_PATH = "/user";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	@Ignore
//	public void saveNewUser() throws GeneralSecurityException {
//		final User user = ModelsUtil.createUser(null);
//		user.setEmail("test@te.st");
//		user.setUsername("test");
//		final AccountRequestInfo info = new AccountRequestInfo();
//		info.setContact("contact");
//		info.setFunction("function");
//		info.setInstitution("institution");
//		info.setService("service");
//		info.setStudy("study");
//		info.setWork("work");
//		user.setAccountRequestInfo(info);
//
//		final ResponseEntity<String> response = restTemplate.postForEntity(AR_REQUEST_PATH, user, String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//		
//		// Get user id
//		String userId = response.getBody().split("\"id\":")[1].split(",")[0];
//
//		// Delete user
//		final HttpEntity<User> entity = new HttpEntity<User>(user, getHeadersWithToken(true));
//		final ResponseEntity<String> responseDelete = restTemplate
//				.exchange(USER_REQUEST_PATH + "/" + userId, HttpMethod.DELETE, entity, String.class);
//		assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
//	}
//
//}
