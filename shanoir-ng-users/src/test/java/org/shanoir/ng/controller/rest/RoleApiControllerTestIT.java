package org.shanoir.ng.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.service.RoleService;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for role controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class RoleApiControllerTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private RoleService roleService;

    @Before
    public void setup() {
        given(roleService.findAll()).willReturn(Arrays.asList(new Role()));
   }

	@Test
	public void findRolesProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity("/role/all", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findRolesWithLogin() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				ModelsUtil.USER_LOGIN, ModelsUtil.USER_PASSWORD);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity("/role/all", String.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

	@Test
	public void findRolesWithBadRole() {
		final BasicAuthorizationInterceptor basicAuthInterceptor = new BasicAuthorizationInterceptor(
				ModelsUtil.USER_LOGIN_GUEST, ModelsUtil.USER_PASSWORD_GUEST);
		this.restTemplate.getRestTemplate().getInterceptors().add(basicAuthInterceptor);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity("/role/all", String.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		} finally {
			restTemplate.getRestTemplate().getInterceptors().remove(basicAuthInterceptor);
		}
	}

}
