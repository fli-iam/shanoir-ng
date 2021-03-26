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

package org.shanoir.ng.preclinical.subjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'subjects'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class AnimalSubjectRepositoryTest {

	private static final String SUBJECT_TEST_1_DATA = "subject1";
	private static final Long SUBJECT_TEST_1_ID = 1L;
	private static final String SUBJECT_TEST_1_SPECIE = "Rat";

	@Autowired
	private AnimalSubjectRepository repository;

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
		Iterable<AnimalSubject> subjectsDb = repository.findAll();
		assertThat(subjectsDb).isNotNull();
		int nbTemplates = 0;
		Iterator<AnimalSubject> subjectsIt = subjectsDb.iterator();
		while (subjectsIt.hasNext()) {
			subjectsIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(4);
	}

	@Test
	public void findBySpecieTest() throws Exception {
		List<AnimalSubject> subjectDb = repository.findByReference(AnimalSubjectModelUtil.createSpecie());
		assertNotNull(subjectDb);
		assertThat(subjectDb.size()).isEqualTo(3);
		assertThat(subjectDb.get(0).getId()).isEqualTo(SUBJECT_TEST_1_ID);
		assertThat(subjectDb.get(0).getSpecie().getValue()).isEqualTo(SUBJECT_TEST_1_SPECIE);
	}

	/*
	 * @Test public void findByDataTest() throws Exception { Optional<Template>
	 * templateDb = repository.findByData(TEMPLATE_TEST_1_DATA);
	 * assertTrue(templateDb.isPresent());
	 * assertThat(templateDb.get().getId()).isEqualTo(TEMPLATE_TEST_1_ID); }
	 */
	@Test
	public void findOneTest() throws Exception {
		AnimalSubject subjectDb = repository.findOne(SUBJECT_TEST_1_ID);
		assertThat(subjectDb.getId()).isEqualTo(SUBJECT_TEST_1_ID);
	}

}
