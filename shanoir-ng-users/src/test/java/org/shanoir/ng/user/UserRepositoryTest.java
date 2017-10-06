package org.shanoir.ng.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.user.User;
import org.shanoir.ng.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
public class UserRepositoryTest {

	private static final String USER_TEST_1_EMAIL = "admin@shanoir.fr";
	private static final Long USER_TEST_1_ID = 1L;
	private static final String USER_TEST_1_USERNAME = "admin";
	
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
		assertThat(nbUsers).isEqualTo(7);
	}
	
	@Test
	public void findByTest() throws Exception {
		List<User> usersDb = repository.findBy("email", USER_TEST_1_EMAIL);
		assertNotNull(usersDb);
		assertThat(usersDb.size()).isEqualTo(1);
		assertThat(usersDb.get(0).getUsername()).isEqualTo(USER_TEST_1_USERNAME);
	}
	
	@Test
	public void findByEmailTest() throws Exception {
		Optional<User> userDb = repository.findByEmail(USER_TEST_1_EMAIL);
		assertTrue(userDb.isPresent());
		assertThat(userDb.get().getUsername()).isEqualTo(USER_TEST_1_USERNAME);
	}
	
	@Test
	public void findAdminEmailsTest() throws Exception {
		List<String> emails = repository.findAdminEmails();
		assertNotNull(emails);
		assertTrue(!emails.isEmpty());
	}
	
	@Test
	public void findByUsernameTest() throws Exception {
		Optional<User> userDb = repository.findByUsername(USER_TEST_1_USERNAME);
		assertTrue(userDb.isPresent());
		assertThat(userDb.get().getId()).isEqualTo(USER_TEST_1_ID);
	}
	
	@Test
	public void findOneTest() throws Exception {
		User userDb = repository.findOne(USER_TEST_1_ID);
		assertThat(userDb.getUsername()).isEqualTo(USER_TEST_1_USERNAME);
	}
	
	@Test
	public void findByExpirationDateLessThanAndFirstExpirationNotificationSentFalseTest() throws Exception {
		// 15/06/2017
		final Date date = new Date(1497484800000L);
		List<User> usersDb = repository.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(date);
		assertThat(usersDb.size()).isEqualTo(1);
		assertThat(usersDb.get(0).getId()).isEqualTo(5L);
	}
	
	@Test
	public void findByExpirationDateLessThanAndSecondExpirationNotificationSentFalseTest() throws Exception {
		// 01/01/2017
		final Date date = new Date(1483228800000L);
		List<User> usersDb = repository.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(date);
		assertThat(usersDb.size()).isEqualTo(1);
		assertThat(usersDb.get(0).getId()).isEqualTo(4L);
	}
	
}
