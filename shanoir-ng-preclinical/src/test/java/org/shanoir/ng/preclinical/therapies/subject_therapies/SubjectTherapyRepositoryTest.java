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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectRepository;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
import org.shanoir.ng.utils.TherapyModelUtil;
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
 * Tests for repository 'subject therapy'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class SubjectTherapyRepositoryTest {

	private static final Long STHERAPY_TEST_CHIMIO_ID = 1L;
	private static final String SUBJECT_TEST_SPECIE_DATA = "rat";
	private static final String THERAPY_TEST_CHIMIO_DATA = "Chimiotherapy";
	private static final Double THERAPY_DOSE_TEST_1_DATA = 2.0;

	@Autowired
	private SubjectTherapyRepository repository;

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
		Iterable<SubjectTherapy> stherapiesDb = repository.findAll();
		assertThat(stherapiesDb).isNotNull();
		int nbTemplates = 0;
		Iterator<SubjectTherapy> stherapiesIt = stherapiesDb.iterator();
		while (stherapiesIt.hasNext()) {
			stherapiesIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	public void findBySubjectTest() throws Exception {
		List<SubjectTherapy> stherapyDb = repository.findByAnimalSubject(AnimalSubjectModelUtil.createAnimalSubject());
		assertNotNull(stherapyDb);
		assertThat(stherapyDb.size()).isEqualTo(2);
		assertThat(stherapyDb.get(0).getId()).isEqualTo(STHERAPY_TEST_CHIMIO_ID);
	}

	@Test
	public void findByTherapyTest() throws Exception {
		List<SubjectTherapy> stherapyDb = repository.findByTherapy(TherapyModelUtil.createTherapyChimio());
		assertNotNull(stherapyDb);
		assertThat(stherapyDb.size()).isEqualTo(2);
		assertThat(stherapyDb.get(0).getId()).isEqualTo(STHERAPY_TEST_CHIMIO_ID);
		assertThat(stherapyDb.get(0).getTherapy().getName()).isEqualTo(THERAPY_TEST_CHIMIO_DATA);
		assertThat(stherapyDb.get(0).getDose()).isEqualTo(THERAPY_DOSE_TEST_1_DATA);
	}

	@Test
	public void findOneTest() throws Exception {
		SubjectTherapy stherapyDb = repository.findById(STHERAPY_TEST_CHIMIO_ID).orElse(null);
		assertThat(stherapyDb.getTherapy().getName()).isEqualTo(THERAPY_TEST_CHIMIO_DATA);
		assertThat(stherapyDb.getDose()).isEqualTo(THERAPY_DOSE_TEST_1_DATA);
	}

}
