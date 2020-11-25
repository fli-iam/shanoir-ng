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

package org.shanoir.ng.preclinical.references;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
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
 * Integration tests for template controller.
 *
 * @author msimon
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ReferencesApiControllerTestIT extends KeycloakControllerTestIT {

	private static final String REQUEST_PATH = "/refs";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findTemplateByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findTemplateByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findReferencesProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findTemplatesWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	/*
	 * @Test public void saveNewTemplateProtected() { final ResponseEntity<String>
	 * response = restTemplate.postForEntity(REQUEST_PATH, new Template(),
	 * String.class); assertEquals(HttpStatus.FOUND, response.getStatusCode()); }
	 * 
	 * @Test public void saveNewTemplateWithLogin() throws ClientProtocolException,
	 * IOException { final HttpEntity<Template> entity = new
	 * HttpEntity<Template>(ModelsUtil.createTemplate(), getHeadersWithToken(true));
	 * 
	 * final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH,
	 * HttpMethod.POST, entity, String.class); assertEquals(HttpStatus.OK,
	 * response.getStatusCode()); }
	 * 
	 * @Test public void updateNewTemplateProtected() { final HttpEntity<Template>
	 * entity = new HttpEntity<Template>(ModelsUtil.createTemplate());
	 * 
	 * final ResponseEntity<String> response =
	 * restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
	 * String.class); assertEquals(HttpStatus.FOUND, response.getStatusCode()); }
	 * 
	 * @Test public void updateNewTemplateWithLogin() throws
	 * ClientProtocolException, IOException { final HttpEntity<Template> entity =
	 * new HttpEntity<Template>(ModelsUtil.createTemplate(),
	 * getHeadersWithToken(true));
	 * 
	 * final ResponseEntity<String> response =
	 * restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
	 * String.class); assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	 * }
	 */
}
