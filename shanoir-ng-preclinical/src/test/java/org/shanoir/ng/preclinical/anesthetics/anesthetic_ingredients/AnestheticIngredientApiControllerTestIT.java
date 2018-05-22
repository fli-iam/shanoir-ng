package org.shanoir.ng.preclinical.anesthetics.anesthetic_ingredients;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.utils.AnestheticModelUtil;
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
 * Integration tests for anesthetic ingredient controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AnestheticIngredientApiControllerTestIT extends KeycloakControllerTestIT {
	
	private static final String REQUEST_PATH_ANESTHETIC = "/anesthetic";
	private static final String ANESTHETIC_ID = "/1";
	private static final String REQUEST_PATH_INGREDIENT = "/ingredient";
	private static final String REQUEST_PATH = REQUEST_PATH_ANESTHETIC + ANESTHETIC_ID + REQUEST_PATH_INGREDIENT;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findAnestheticIngredientByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findAnestheticIngredientByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findAnestheticIngredientsProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findAnestheticIngredientsWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void saveNewAnestheticIngredientProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new AnestheticIngredient(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewAnestheticIngredientWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<AnestheticIngredient> entity = new HttpEntity<AnestheticIngredient>(AnestheticModelUtil.createAnestheticIngredient(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void updateNewAnestheticIngredientProtected() {
		final HttpEntity<AnestheticIngredient> entity = new HttpEntity<AnestheticIngredient>(AnestheticModelUtil.createAnestheticIngredient());
		
		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateNewAnestheticIngredientWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<AnestheticIngredient> entity = new HttpEntity<AnestheticIngredient>(AnestheticModelUtil.createAnestheticIngredient(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

}
