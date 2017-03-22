package org.shanoir.ng.role;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.role.Role;
import org.shanoir.ng.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'role'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class RoleRepositoryTest {

	private static final Long ROLE_TEST_1_ID = 1L;
	private static final String ROLE_TEST_1_NAME = "ROLE_ADMIN";
	
	@Autowired
	private RoleRepository repository;
	
	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
	
	@Test
	public void findOneTest() throws Exception {
		final Role roleDb = repository.findOne(ROLE_TEST_1_ID);
		assertThat(roleDb.getName()).isEqualTo(ROLE_TEST_1_NAME);
	}
	
	@Test
	public void getAllNamesTest() throws Exception {
		final List<String> rolesName = repository.getAllNames();
		assertThat(rolesName.size()).isEqualTo(4);
	}
	
}
