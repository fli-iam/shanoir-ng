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

package org.shanoir.ng.preclinical.pathologies;

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
 * Tests for repository 'pathologies'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class PathologyRepositoryTest {

	private static final String PATHOLOGY_TEST_1_DATA = "Stroke";
	private static final Long PATHOLOGY_TEST_1_ID = 1L;

	@Autowired
	private PathologyRepository repository;

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
		Iterable<Pathology> pathologiesDb = repository.findAll();
		assertThat(pathologiesDb).isNotNull();
		int nbTemplates = 0;
		Iterator<Pathology> pathologiesIt = pathologiesDb.iterator();
		while (pathologiesIt.hasNext()) {
			pathologiesIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(4);
	}

	@Test
	public void findByTest() throws Exception {
		List<Pathology> pathologyDb = repository.findBy("name", PATHOLOGY_TEST_1_DATA);
		assertNotNull(pathologyDb);
		assertThat(pathologyDb.size()).isEqualTo(1);
		assertThat(pathologyDb.get(0).getId()).isEqualTo(PATHOLOGY_TEST_1_ID);
	}

	@Test
	public void findByNameTest() throws Exception {
		Optional<Pathology> pathologyDb = repository.findByName(PATHOLOGY_TEST_1_DATA);
		assertTrue(pathologyDb.isPresent());
		assertThat(pathologyDb.get().getId()).isEqualTo(PATHOLOGY_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		Pathology pathologyDb = repository.findById(PATHOLOGY_TEST_1_ID).orElse(null);
		assertThat(pathologyDb.getName()).isEqualTo(PATHOLOGY_TEST_1_DATA);
	}

}
