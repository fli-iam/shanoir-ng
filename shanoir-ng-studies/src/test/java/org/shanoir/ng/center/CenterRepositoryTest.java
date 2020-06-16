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

package org.shanoir.ng.center;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'center'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class CenterRepositoryTest {

	private static final String CENTER_TEST_1_NAME = "CHU Rennes";
	private static final Long CENTER_TEST_1_ID = 1L;
	
	@Autowired
	private CenterRepository repository;
	
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
		Iterable<Center> centersDb = repository.findAll();
		assertThat(centersDb).isNotNull();
		int nbCenters = 0;
		Iterator<Center> centersIt = centersDb.iterator();
		while (centersIt.hasNext()) {
			centersIt.next();
			nbCenters++;
		}
		assertThat(nbCenters).isEqualTo(2);
	}
	
	@Test
	public void findByNameTest() throws Exception {
		Center centerDb = repository.findByName(CENTER_TEST_1_NAME);
		assertNotNull(centerDb);
		assertThat(centerDb.getId()).isEqualTo(CENTER_TEST_1_ID);
	}
	
	@Test
	public void findIdsAndNamesTest() throws Exception {
		List<IdName> centersDb = repository.findIdsAndNames();
		assertNotNull(centersDb);
		assertThat(centersDb.size()).isEqualTo(2);
	}

	@Test
	public void findOneTest() throws Exception {
		Center centerDb = repository.findOne(CENTER_TEST_1_ID);
		assertThat(centerDb.getName()).isEqualTo(CENTER_TEST_1_NAME);
	}
	
	@Test
	public void findNamesByStudyIdTest() throws Exception {
		List<IdName> centersDb = repository.findIdsAndNames(1L);
		assertNotNull(centersDb);
		assertThat(centersDb.size()).isEqualTo(2);
	}
	
}
