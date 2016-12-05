package org.shanoir.ng.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'user'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private UserRepository repository;
	
	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
	
	private User user;
	
	@Before
	public void setUp() {
		final Role role = entityManager.persist(ModelsUtil.createRole());
		user = entityManager.persist(ModelsUtil.createUser(role));
	}
	
	@Test
	public void findAllTest() throws Exception {
		Iterable<User> usersDb = repository.findAll();
		assertThat(usersDb).isNotNull();
		int nbUsers = 0;
		Iterator<User> usersIt = usersDb.iterator();
		while (usersIt.hasNext()) {
			usersIt.next();
			nbUsers++;
		}
		assertThat(nbUsers).isEqualTo(1);
	}
	
	@Test
	public void findByEmailTest() throws Exception {
		User userDb = repository.findByEmail(user.getEmail());
		assertThat(userDb.getUsername()).isEqualTo(user.getUsername());
	}
	
	@Test
	public void findByUsernameTest() throws Exception {
		User userDb = repository.findByUsername(user.getUsername());
		assertThat(userDb.getId()).isEqualTo(user.getId());
	}
	
	@Test
	public void findOneTest() throws Exception {
		User userDb = repository.findOne(user.getId());
		assertThat(userDb.getUsername()).isEqualTo(user.getUsername());
	}
	
}
