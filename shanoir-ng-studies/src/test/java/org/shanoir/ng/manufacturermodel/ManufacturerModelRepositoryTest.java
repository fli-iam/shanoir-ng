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

package org.shanoir.ng.manufacturermodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'manufacturer model'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class ManufacturerModelRepositoryTest {

	private static final Long MANUFACTURER_MODEL_TEST_1_ID = 1L;
	private static final String MANUFACTURER_MODEL_TEST_1_NAME = "DISCOVERY MR750";
	
	@Autowired
	private ManufacturerModelRepository repository;
	
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
		Iterable<ManufacturerModel> manufacturerModelsDb = repository.findAll();
		assertThat(manufacturerModelsDb).isNotNull();
		int nbManufacturerModels = 0;
		Iterator<ManufacturerModel> manufacturerModelsIt = manufacturerModelsDb.iterator();
		while (manufacturerModelsIt.hasNext()) {
			manufacturerModelsIt.next();
			nbManufacturerModels++;
		}
		assertThat(nbManufacturerModels).isEqualTo(3);
	}
	
	@Test
	public void findOneTest() throws Exception {
		ManufacturerModel manufacturerModelDb = repository.findOne(MANUFACTURER_MODEL_TEST_1_ID);
		assertThat(manufacturerModelDb).isNotNull();
		assertThat(manufacturerModelDb.getName()).isEqualTo(MANUFACTURER_MODEL_TEST_1_NAME);
	}
	
}
