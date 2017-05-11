package org.shanoir.ng.studycard;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.studycard.StudyCard;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
import org.shanoir.ng.utils.ModelsUtil;
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
 * Integration tests for study card controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class StudyCardApiControllerTestIT extends KeycloakControllerTestIT {
	
	private static final String REQUEST_PATH = "/studycard";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findStudyCardByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findStudyCardByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findTemplatesProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findTemplatesWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void saveNewTemplateProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new StudyCard(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewTemplateWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<StudyCard> entity = new HttpEntity<StudyCard>(ModelsUtil.createStudyCard(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void updateNewTemplateProtected() {
		final HttpEntity<StudyCard> entity = new HttpEntity<StudyCard>(ModelsUtil.createStudyCard());
		
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateNewTemplateWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<StudyCard> entity = new HttpEntity<StudyCard>(ModelsUtil.createStudyCard(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

}
