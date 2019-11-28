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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
import org.shanoir.ng.utils.PathologyModelUtil;
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
 * Integration tests for subject pathology controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class SubjectPathologyApiControllerTestIT extends KeycloakControllerTestIT {
	
	private static final String REQUEST_PATH_SUBJECT = "/subject";
	private static final String SUBJECT_ID = "/1";
	private static final String REQUEST_PATH_PATHOLOGY = "/pathology";
	private static final String REQUEST_PATH = REQUEST_PATH_SUBJECT + SUBJECT_ID + REQUEST_PATH_PATHOLOGY;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_SUBJECT_BY_PATHO = REQUEST_PATH_SUBJECT+ "/all" + REQUEST_PATH_PATHOLOGY + "/1";
	private static final String REQUEST_PATH_PATHO_BY_SUBJECT = REQUEST_PATH_SUBJECT+ SUBJECT_ID + REQUEST_PATH_PATHOLOGY + "/all";
	
	
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findSubjectPathologyByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectPathologyByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findSubjectPathologyProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectPathologyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findSubjectPathologiesProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectPathologiesWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findSubjectsByPathologyProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_SUBJECT_BY_PATHO, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findSubjectsByPathologyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_SUBJECT_BY_PATHO, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findPathologiesBySubjectProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_PATHO_BY_SUBJECT, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findPathologiesBySubjectWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_PATHO_BY_SUBJECT, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void saveNewSubjectPathologyProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new SubjectPathology(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewSubjectPathologyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<SubjectPathology> entity = new HttpEntity<SubjectPathology>(PathologyModelUtil.createSubjectPathology(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void updateNewSubjectPathologyProtected() {
		final HttpEntity<SubjectPathology> entity = new HttpEntity<SubjectPathology>(PathologyModelUtil.createSubjectPathology());
		
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateNewSubjectPathologyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<SubjectPathology> entity = new HttpEntity<SubjectPathology>(PathologyModelUtil.createSubjectPathology(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

}
