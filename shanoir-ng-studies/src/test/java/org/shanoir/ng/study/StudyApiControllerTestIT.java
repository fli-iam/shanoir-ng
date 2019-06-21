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

//package org.shanoir.ng.study;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//import org.apache.http.client.ClientProtocolException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.center.model.Center;
//import org.shanoir.ng.study.model.Study;
//import org.shanoir.ng.study.model.StudyStatus;
//import org.shanoir.ng.study.rights.StudyUser;
//import org.shanoir.ng.study.model.security.StudyUserRight;
//import org.shanoir.ng.studycenter.StudyCenter;
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
//public class StudyApiControllerTestIT extends KeycloakControllerTestIT {
//
//	private static final String REQUEST_PATH = "/studies";
//	private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
//	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
//	private static final String REQUEST_PATH_FOR_MEMBERS = REQUEST_PATH_WITH_ID + "/members";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	public void addMemberToStudyProtected() {
//		final HttpEntity<StudyUser> entity = new HttpEntity<StudyUser>(ModelsUtil.createStudyUser());
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_MEMBERS, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void addMemberToStudyWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<StudyUser> entity = new HttpEntity<StudyUser>(createStudyUser(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_MEMBERS, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudiesProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudiesWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(false));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudiesNamesProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_NAMES, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudiesNamesWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_NAMES, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudyByIdProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void findStudyByIdWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewStudyProtected() {
//		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new Study(), String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewStudyWithLogin() throws ClientProtocolException, IOException {
//
//		final Study study = createStudy();
//		study.setName("test2");
//		final HttpEntity<Study> entity = new HttpEntity<Study>(study, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//
//		// Get study id
//		String studyId = response.getBody().split("\"id\":")[1].split(",")[0];
//
//		// Delete study
//		final ResponseEntity<String> responseDelete = restTemplate.exchange(REQUEST_PATH + "/" + studyId,
//				HttpMethod.DELETE, entity, String.class);
//		assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
//	}
//
//	@Test
//	public void updateNewStudyProtected() {
//		final HttpEntity<Study> entity = new HttpEntity<Study>(ModelsUtil.createStudy());
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewStudyWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<Study> entity = new HttpEntity<Study>(createStudy(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//	private Study createStudy() {
//		final Study study = new Study();
//		study.setId(1L);
//		study.setName("test");
//		study.setStudyStatus(StudyStatus.FINISHED);
//		final StudyCenter studyCenter = new StudyCenter();
//		final Center center = new Center();
//		center.setId(1L);
//		studyCenter.setCenter(center);
//		study.setStudyCenterList(Arrays.asList(studyCenter));
//		return study;
//	}
//
//	private StudyUser createStudyUser() {
//		final StudyUser studyUser = new StudyUser();
//		studyUser.setStudyId(1L);
//		studyUser.setStudyUserRight(StudyUserRight.CAN_ADMINISTRATE);
//		studyUser.setUserId(1L);
//		return studyUser;
//	}
//
//}
