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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectRepository;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
import org.shanoir.ng.utils.PathologyModelUtil;
import org.shanoir.ng.utils.ReferenceModelUtil;
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
 * Tests for repository 'subject pathology'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class SubjectPathologyRepositoryTest {

	private static final Long SPATHO_TEST_1_ID = 1L;
	private static final String PATHOLOGY_TEST_1_DATA = "Stroke";
	private static final String MODEL_TEST_1_DATA = "U836";
	private static final String LOCATION_TEST_1_DATA = "Brain";
	private static final String SUBJECT_TEST_1_DATA = "Rat";

	@Autowired
	private SubjectPathologyRepository repository;

	@Autowired
	private AnimalSubjectRepository subjectRepository;

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
		Iterable<SubjectPathology> spathosDb = repository.findAll();
		assertThat(spathosDb).isNotNull();
		int nbTemplates = 0;
		Iterator<SubjectPathology> spathosIt = spathosDb.iterator();
		while (spathosIt.hasNext()) {
			spathosIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	public void findByLocationTest() throws Exception {
		List<SubjectPathology> spathoDb = repository.findAllByLocation(ReferenceModelUtil.createReferenceLocation());
		assertNotNull(spathoDb);
		assertThat(spathoDb.size()).isEqualTo(3);
		assertThat(spathoDb.get(0).getId()).isEqualTo(SPATHO_TEST_1_ID);
		assertThat(spathoDb.get(0).getLocation().getValue()).isEqualTo(LOCATION_TEST_1_DATA);
	}

	@Test
	public void findBySubjectTest() throws Exception {
		List<SubjectPathology> spathoDb = repository.findByAnimalSubject(AnimalSubjectModelUtil.createAnimalSubject());
		assertNotNull(spathoDb);
		assertThat(spathoDb.size()).isEqualTo(1);
		assertThat(spathoDb.get(0).getId()).isEqualTo(SPATHO_TEST_1_ID);
		assertThat(spathoDb.get(0).getAnimalSubject().getSpecie().getValue()).isEqualTo(SUBJECT_TEST_1_DATA);
	}

	@Test
	public void findByPathologyTest() throws Exception {
		List<SubjectPathology> spathoDb = repository.findAllByPathology(PathologyModelUtil.createPathology());
		assertNotNull(spathoDb);
		assertThat(spathoDb.size()).isEqualTo(1);
		assertThat(spathoDb.get(0).getId()).isEqualTo(SPATHO_TEST_1_ID);
		assertThat(spathoDb.get(0).getPathology().getName()).isEqualTo(PATHOLOGY_TEST_1_DATA);
	}

	@Test
	public void findByPathologyModelTest() throws Exception {
		List<SubjectPathology> spathoDb = repository.findAllByPathologyModel(PathologyModelUtil.createPathologyModel());
		assertNotNull(spathoDb);
		assertThat(spathoDb.size()).isEqualTo(1);
		assertThat(spathoDb.get(0).getId()).isEqualTo(SPATHO_TEST_1_ID);
		assertThat(spathoDb.get(0).getPathologyModel().getName()).isEqualTo(MODEL_TEST_1_DATA);
	}

	@Test
	public void findOneTest() throws Exception {
		SubjectPathology spathoDb = repository.findOne(SPATHO_TEST_1_ID);
		assertThat(spathoDb.getLocation().getValue()).isEqualTo(LOCATION_TEST_1_DATA);
	}

}
