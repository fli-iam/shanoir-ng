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

package org.shanoir.ng.subject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'Subject'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class SubjectRepositoryTest {

	private static final String SUBJECT_TEST_1_DATA = "subject1";
	private static final Long SUBJECT_TEST_1_ID = 1L;

	@Autowired
	private SubjectRepository subjectRepository;

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
		Iterable<Subject> subjectDb = subjectRepository.findAll();
		assertThat(subjectDb).isNotNull();
		int nbSubject = 0;
		Iterator<Subject> subjectIt = subjectDb.iterator();
		while (subjectIt.hasNext()) {
			subjectIt.next();
			nbSubject++;
		}
		assertThat(nbSubject).isEqualTo(4);
	}

	@Test
	public void findByTest() throws Exception {
		List<Subject> subjectDb = subjectRepository.findBy("name", SUBJECT_TEST_1_DATA);
		assertNotNull(subjectDb);
		assertThat(subjectDb.size()).isEqualTo(1);
		assertThat(subjectDb.get(0).getId()).isEqualTo(SUBJECT_TEST_1_ID);
	}

	@Test
	public void findByDataTest() throws Exception {
		Subject subjectDb = subjectRepository.findByName(SUBJECT_TEST_1_DATA);
		assertNotNull(subjectDb);
		assertThat(subjectDb.getId()).isEqualTo(SUBJECT_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		Subject subjectDb = subjectRepository.findOne(SUBJECT_TEST_1_ID);
		assertThat(subjectDb.getName()).isEqualTo(SUBJECT_TEST_1_DATA);
	}

}
