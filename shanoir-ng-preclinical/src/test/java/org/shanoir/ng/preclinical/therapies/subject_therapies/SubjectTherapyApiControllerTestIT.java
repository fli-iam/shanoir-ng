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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
import org.shanoir.ng.utils.TherapyModelUtil;
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
 * Integration tests for subject therapy controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class SubjectTherapyApiControllerTestIT extends KeycloakControllerTestIT {
	
	private static final String REQUEST_PATH_SUBJECT = "/subject";
	private static final String SUBJECT_ID = "/1";
	private static final String REQUEST_PATH_THERAPY = "/therapy";
	private static final String REQUEST_PATH = REQUEST_PATH_SUBJECT + SUBJECT_ID + REQUEST_PATH_THERAPY;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_SUBJECT_BY_THERAPY = REQUEST_PATH_SUBJECT + "/all" + REQUEST_PATH_THERAPY + "/1";
	private static final String REQUEST_PATH_THERAPY_BY_SUBJECT = REQUEST_PATH_SUBJECT + SUBJECT_ID + REQUEST_PATH_THERAPY + "/all";
	
	
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findSubjectTherapyByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectTherapyByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findSubjectTherapyProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectTherapyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findSubjectTherapiesProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectTherapiesWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findSubjectsByTherapyProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_SUBJECT_BY_THERAPY, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectsByTherapyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_SUBJECT_BY_THERAPY, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findTherapiesBySubjectProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_THERAPY_BY_SUBJECT, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findTherapiesBySubjectWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_THERAPY_BY_SUBJECT, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void saveNewSubjectTherapyProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new SubjectTherapy(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewSubjectTherapyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<SubjectTherapy> entity = new HttpEntity<SubjectTherapy>(TherapyModelUtil.createSubjectTherapy(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void updateNewSubjectTherapyProtected() {
		final HttpEntity<SubjectTherapy> entity = new HttpEntity<SubjectTherapy>(TherapyModelUtil.createSubjectTherapy());
		
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateNewSubjectTherapyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<SubjectTherapy> entity = new HttpEntity<SubjectTherapy>(TherapyModelUtil.createSubjectTherapy(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

}
