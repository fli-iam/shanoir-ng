<<<<<<< HEAD
=======
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

package org.shanoir.ng.dataset.modality;

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

>>>>>>> upstream/develop
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

//package org.shanoir.ng.dataset.modality;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.Iterator;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
//import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
//
///**
// * Tests for repository 'petdataset'.
// * 
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@DataJpaTest
//@ActiveProfiles("test")
//public class PetDatasetRepositoryTest {
//
//	private static final String PET_DATASET_TEST_1_NAME = "PETDataset1";
//	private static final Long PET_DATASET_TEST_1_ID = 2L;
//	
//	@Autowired
//	private PetDatasetRepository repository;
//	
//	/*
//	 * Mocks used to avoid unsatisfied dependency exceptions.
//	 */
//	@MockBean
//	private AuthenticationManager authenticationManager;
//	@MockBean
//	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
//	@MockBean
//	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
//	
//	@Test
//	public void findAllTest() throws Exception {
//		Iterable<PetDataset> datasetsDb = repository.findAll();
//		assertThat(datasetsDb).isNotNull();
//		int nbDatasets = 0;
//		Iterator<PetDataset> datasetsIt = datasetsDb.iterator();
//		while (datasetsIt.hasNext()) {
//			datasetsIt.next();
//			nbDatasets++;
//		}
//		assertThat(nbDatasets).isEqualTo(1);
//	}
//	
//	@Test
//	public void findOneTest() throws Exception {
//		PetDataset datasetDb = repository.findOne(PET_DATASET_TEST_1_ID);
//		assertThat(datasetDb.getName()).isEqualTo(PET_DATASET_TEST_1_NAME);
//	}
//	
//}
