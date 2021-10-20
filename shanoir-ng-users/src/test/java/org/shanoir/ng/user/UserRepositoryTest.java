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

package org.shanoir.ng.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
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
	public void findAdminEmailsTest() throws Exception {
		List<String> emails = repository.findAdminEmails();
		assertNotNull(emails);
		assertTrue(!emails.isEmpty());
	}
	
	@Test
	public void findByEmailTest() throws Exception {
		Optional<User> userDb = repository.findByEmail(USER_TEST_1_EMAIL);
		assertTrue(userDb.isPresent());
		assertThat(userDb.get().getUsername()).isEqualTo(USER_TEST_1_USERNAME);
	}
	
	@Test
	public void findOneTest() throws Exception {
		User userDb = repository.findById(USER_TEST_1_ID).orElse(null);
		assertThat(userDb.getUsername()).isEqualTo(USER_TEST_1_USERNAME);
	}
	
	@Test
	public void findByExpirationDateLessThanAndFirstExpirationNotificationSentFalseTest() throws Exception {
		// 15/06/2017
		final LocalDate date = Instant.ofEpochMilli(1497484800000L).atZone(ZoneId.systemDefault()).toLocalDate();
		List<User> usersDb = repository.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(date);
		assertThat(usersDb.size()).isEqualTo(1);
		assertThat(usersDb.get(0).getId()).isEqualTo(5L);
	}
	
	@Test
	public void findByExpirationDateLessThanAndSecondExpirationNotificationSentFalseTest() throws Exception {
		// 01/01/2017
		final LocalDate date = Instant.ofEpochMilli(1483228800000L).atZone(ZoneId.systemDefault()).toLocalDate();
		List<User> usersDb = repository.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(date);
		assertThat(usersDb.size()).isEqualTo(1);
		assertThat(usersDb.get(0).getId()).isEqualTo(4L);
	}
	
	@Test
	public void findByIdInTest() throws Exception {
		List<User> usersDb = repository.findByIdIn(Arrays.asList(USER_TEST_1_ID));
		assertNotNull(usersDb);
		assertThat(usersDb.get(0).getId()).isEqualTo(USER_TEST_1_ID);
	}
	
	@Test
	public void findByUsernameTest() throws Exception {
		Optional<User> userDb = repository.findByUsername(USER_TEST_1_USERNAME);
		assertTrue(userDb.isPresent());
		assertThat(userDb.get().getId()).isEqualTo(USER_TEST_1_ID);
	}

}
