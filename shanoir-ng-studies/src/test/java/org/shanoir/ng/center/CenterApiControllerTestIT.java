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

//package org.shanoir.ng.center;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//
//import org.apache.http.client.ClientProtocolException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.center.model.Center;
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
// * Integration tests for center controller.
// *
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev")
//public class CenterApiControllerTestIT extends KeycloakControllerTestIT {
//	
//	private static final String REQUEST_PATH = "/centers";
//	private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
//	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	public void findCenterByIdProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findCenterByIdWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findCentersProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findCentersWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//	
//	@Test
//	public void findCentersWithBadRole() {
//		// test with guest role
//		final HttpEntity<Center> entity = new HttpEntity<Center>(null, getHeadersWithToken(false));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//	}
//
//	@Test
//	public void findCentersNamesProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_NAMES, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findCentersNamesWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_NAMES, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findCentersNamesWithBadRole() {
//		// test with guest role
//		final HttpEntity<Center> entity = new HttpEntity<Center>(null, getHeadersWithToken(false));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_NAMES, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewCenterProtected() {
//		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new Center(), String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewCenterWithLogin() throws ClientProtocolException, IOException {
//		
//		final Center center = ModelsUtil.createCenter();
//		center.setName("tt"); 
//		final HttpEntity<Center> entity = new HttpEntity<Center>(center, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//		
//		// Get center id
//		String centerId = response.getBody().split("\"id\":")[1].split(",")[0];
//
//		// Delete center
//		final ResponseEntity<String> responseDelete = restTemplate
//				.exchange(REQUEST_PATH + "/" + centerId, HttpMethod.DELETE, entity, String.class);
//		assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
//	}
//
//	@Test
//	public void updateNewCenterProtected() {
//		final HttpEntity<Center> entity = new HttpEntity<Center>(ModelsUtil.createCenter());
//		
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewCenterWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<Center> entity = new HttpEntity<Center>(ModelsUtil.createCenter(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//}
