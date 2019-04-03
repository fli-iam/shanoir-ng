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
