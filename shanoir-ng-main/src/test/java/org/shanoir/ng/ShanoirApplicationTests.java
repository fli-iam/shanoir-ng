package org.shanoir.ng;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ShanoirApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

	@Test
	public void homePageLoads() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void userEndpointProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/user", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void resourceEndpointProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/resource", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void loginSucceeds() {
		BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				"user", "password");
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity("/user", String.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

}
