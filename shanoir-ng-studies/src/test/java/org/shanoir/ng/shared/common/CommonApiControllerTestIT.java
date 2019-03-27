//package org.shanoir.ng.shared.common;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//
//import org.apache.http.client.ClientProtocolException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
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
// * Integration tests for study controller.
// *
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev")
//public class CommonApiControllerTestIT extends KeycloakControllerTestIT {
//
//	private static final String REQUEST_PATH = "/common";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	public void findStudySubjectCenterNamesByIdsProtected() {
//		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH,
//				ModelsUtil.createCommonIdsDTO(), String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudySubjectCenterNamesByIdsWithLogin() throws ClientProtocolException, IOException {
//
//		final HttpEntity<CommonIdsDTO> entity = new HttpEntity<>(ModelsUtil.createCommonIdsDTO(),
//				getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//
//	}
//
//}
