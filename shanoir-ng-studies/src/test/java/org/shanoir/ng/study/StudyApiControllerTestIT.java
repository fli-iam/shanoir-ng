package org.shanoir.ng.study;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * Integration tests for study controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class StudyApiControllerTestIT extends KeycloakControllerTestIT {

	private static final String REQUEST_PATH = "/study";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findStudyByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findStudyByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findStudysProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findStudiesWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void saveNewStudyProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new Study(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewStudyWithLogin() throws ClientProtocolException, IOException {

		final Study study = createStudy();
		study.setName("test");
		final HttpEntity<Study> entity = new HttpEntity<Study>(study, getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		// Get study id
		String studyId = response.getBody().split("\"id\":")[1].split(",")[0];

		// Delete study
		final ResponseEntity<String> responseDelete = restTemplate.exchange(REQUEST_PATH + "/" + studyId,
				HttpMethod.DELETE, entity, String.class);
		assertEquals(HttpStatus.OK, responseDelete.getStatusCode());
	}

	@Test
	public void updateNewStudyProtected() {
		final HttpEntity<Study> entity = new HttpEntity<Study>(ModelsUtil.createStudy());

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateNewStudyWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<Study> entity = new HttpEntity<Study>(createStudy(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(1L);
		study.setName("test");
		study.setStudyStatus(StudyStatus.FINISHED);
		final StudyCenter studyCenter = new StudyCenter();
		studyCenter.setCenter(ModelsUtil.createCenter());
		studyCenter.setStudy(study);
		study.setStudyCenterList(Arrays.asList(studyCenter));
		return study;
	}

}
