//package org.shanoir.ng.dataset;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//
//import org.apache.http.client.ClientProtocolException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.dataset.model.Dataset;
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
// * Integration tests for dataset controller.
// *
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//public class DatasetApiControllerTestIT extends KeycloakControllerTestIT {
//	
//	private static final String REQUEST_PATH = "/datasets";
//	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	public void findDatasetByIdProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findDatasetByIdWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewDatasetProtected() {
//		final HttpEntity<Dataset> entity = new HttpEntity<>(ModelsUtil.createMrDataset());
//		
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewDatasetWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<Dataset> entity = new HttpEntity<>(ModelsUtil.createMrDataset(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//}
