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

package org.shanoir.ng.preclinical.therapies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
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
 * Tests for repository 'therapies'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class TherapyRepositoryTest {

	private static final String THERAPY_TEST_1_DATA = "Brainectomy";
	private static final Long THERAPY_TEST_1_ID = 1L;

	@Autowired
	private TherapyRepository repository;

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
		Iterable<Therapy> therapiesDb = repository.findAll();
		assertThat(therapiesDb).isNotNull();
		int nbTemplates = 0;
		Iterator<Therapy> therapiesIt = therapiesDb.iterator();
		while (therapiesIt.hasNext()) {
			therapiesIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(2);
	}

	@Test
	public void findByNameTest() throws Exception {
		Optional<Therapy> therapyDb = repository.findByName(THERAPY_TEST_1_DATA);
		assertTrue(therapyDb.isPresent());
		assertThat(therapyDb.get().getId()).isEqualTo(THERAPY_TEST_1_ID);
	}

	@Test
	public void findByTherapyTypeTest() throws Exception {
		List<Therapy> therapiesDb = repository.findByTherapyType(TherapyType.SURGERY);
		assertNotNull(therapiesDb);
		assertThat(therapiesDb.size()).isEqualTo(1);
		assertThat(therapiesDb.get(0).getId()).isEqualTo(THERAPY_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		Therapy therapyDb = repository.findById(THERAPY_TEST_1_ID).orElse(null);
		assertThat(therapyDb.getName()).isEqualTo(THERAPY_TEST_1_DATA);
	}

}
