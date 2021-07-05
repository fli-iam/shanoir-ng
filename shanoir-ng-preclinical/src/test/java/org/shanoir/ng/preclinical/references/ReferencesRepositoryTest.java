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

package org.shanoir.ng.preclinical.references;

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
 * Tests for repository 'template'.
 * 
 * @author sloury
 *
 */

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class ReferencesRepositoryTest {

	/* Results are ordered alphabetically so must use multiple values... */
	private static final String REFERENCE_TEST_CATEGORY_SUBJECT = "subject";
	private static final String REFERENCE_TEST_CATEGORY_ANATOMY = "anatomy";
	private static final String REFERENCE_TEST_TYPE_SPECIE = "specie";
	private static final String REFERENCE_TEST_TYPE_PROVIDER = "provider";
	private static final Long REFERENCE_TEST_1_ID = 1L;
	private static final String REFERENCE_TEST_1_VALUE = "Rat";

	@Autowired
	private RefsRepository repository;

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
		Iterable<Reference> referencesDb = repository.findAll();
		assertThat(referencesDb).isNotNull();
		int nbReferences = 0;
		Iterator<Reference> templatesIt = referencesDb.iterator();
		while (templatesIt.hasNext()) {
			templatesIt.next();
			nbReferences++;
		}
		assertThat(nbReferences).isEqualTo(22);
	}

	@Test
	public void findByCategoryTest() throws Exception {
		List<Reference> refrencesDb = repository.findByCategory(REFERENCE_TEST_CATEGORY_SUBJECT);
		assertNotNull(refrencesDb);
		assertThat(refrencesDb.size()).isEqualTo(10);
		assertThat(refrencesDb.get(0).getId()).isEqualTo(REFERENCE_TEST_1_ID);
	}

	@Test
	public void findByCategoryAndTypeTest() throws Exception {
		List<Reference> refrencesDb = repository.findByCategoryAndType(REFERENCE_TEST_CATEGORY_SUBJECT,
				REFERENCE_TEST_TYPE_SPECIE);
		assertNotNull(refrencesDb);
		assertThat(refrencesDb.size()).isEqualTo(2);
		assertThat(refrencesDb.get(0).getId()).isEqualTo(REFERENCE_TEST_1_ID);
	}

	@Test
	public void findByCategoryTypeAndValueTest() throws Exception {
		Optional<Reference> refrenceDb = repository.findByCategoryTypeAndValue(REFERENCE_TEST_CATEGORY_SUBJECT,
				REFERENCE_TEST_TYPE_SPECIE, REFERENCE_TEST_1_VALUE);
		assertTrue(refrenceDb.isPresent());
		assertThat(refrenceDb.get().getId()).isEqualTo(REFERENCE_TEST_1_ID);
	}

	@Test
	public void findCategoriesTest() throws Exception {
		List<String> categoriesDb = repository.findCategories();
		assertNotNull(categoriesDb);
		assertThat(categoriesDb.size()).isEqualTo(5);
		assertThat(categoriesDb.get(0)).isEqualTo(REFERENCE_TEST_CATEGORY_ANATOMY);
	}

	@Test
	public void findTypesByCategoryTest() throws Exception {
		List<String> typesDb = repository.findTypesByCategory(REFERENCE_TEST_CATEGORY_SUBJECT);
		assertNotNull(typesDb);
		assertThat(typesDb.size()).isEqualTo(5);
		assertThat(typesDb.get(0)).isEqualTo(REFERENCE_TEST_TYPE_PROVIDER);
	}

	@Test
	public void findOneTest() throws Exception {
		Reference referenceDb = repository.findById(REFERENCE_TEST_1_ID).orElse(null);
		assertThat(referenceDb.getCategory()).isEqualTo(REFERENCE_TEST_CATEGORY_SUBJECT);
		assertThat(referenceDb.getReftype()).isEqualTo(REFERENCE_TEST_TYPE_SPECIE);
		assertThat(referenceDb.getValue()).isEqualTo(REFERENCE_TEST_1_VALUE);
	}

}
