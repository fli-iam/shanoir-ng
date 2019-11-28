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

package org.shanoir.ng.preclinical.anesthetics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticRepository;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticType;
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
 * Tests for repository 'anesthetics'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class AnestheticRepositoryTest {

	private static final String ANESTHETIC_TEST_1_NAME = "Gas Iso. 2% Ket. 25%";
	private static final Long ANESTHETIC_TEST_1_ID = 1L;

	@Autowired
	private AnestheticRepository repository;

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
		Iterable<Anesthetic> anestheticsDb = repository.findAll();
		assertThat(anestheticsDb).isNotNull();
		int nbTemplates = 0;
		Iterator<Anesthetic> anestheticsIt = anestheticsDb.iterator();
		while (anestheticsIt.hasNext()) {
			anestheticsIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	public void findByAnestheticTypeTest() throws Exception {
		List<Anesthetic> anestheticsDb = repository.findAllByAnestheticType(AnestheticType.GAS);
		assertNotNull(anestheticsDb);
		assertThat(anestheticsDb.size()).isEqualTo(2);
		assertThat(anestheticsDb.get(0).getId()).isEqualTo(ANESTHETIC_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		Anesthetic anestheticDb = repository.findOne(ANESTHETIC_TEST_1_ID);
		assertThat(anestheticDb.getName()).isEqualTo(ANESTHETIC_TEST_1_NAME);
		assertThat(anestheticDb.getAnestheticType()).isEqualTo(AnestheticType.GAS);
	}

}
