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

package org.shanoir.ng.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrQualityProcedureType;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'dataset'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@DataJpaTest
@ActiveProfiles("test")
public class DatasetRepositoryTest {

	@Autowired
	private DatasetRepository repository;
 
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
	
	
	/**
	 * Test the hierarchy strategy :
	 * A MR Dataset must be saved with its specific fields and those must be retrievable afterwards
	 * 
	 * @throws ShanoirException
	 */
	@Test
	public void heritageConcreteTest() throws ShanoirException {
		MrDataset mr = ModelsUtil.createMrDataset();
		mr.setMrQualityProcedureType(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME);

		MrDataset result = repository.save(mr); // SAVE AS A MR
		assertEquals(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME, result.getMrQualityProcedureType());
		assertNotNull(result.getId());
		Long id = result.getId();
		
		Dataset founded = repository.findOne(id);
		assertTrue(founded instanceof MrDataset);
		MrDataset foundedMr = (MrDataset) founded;
		assertEquals(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME, foundedMr.getMrQualityProcedureType());
	}
	
	/**
	 * Test the hierarchy strategy :
	 * A MR Dataset must be saved with its specific fields and those must be retrievable afterwards
	 * 
	 * @throws ShanoirException
	 */
	@Test
	public void heritageAbstractTest() throws ShanoirException {
		MrDataset mr = ModelsUtil.createMrDataset();
		mr.setMrQualityProcedureType(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME);
		Dataset ds = mr; // <----------
		
		Dataset result = repository.save(ds); // SAVE AS A DATASET
		assertTrue(result instanceof MrDataset);
		assertEquals(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME, ((MrDataset)result).getMrQualityProcedureType());
		assertNotNull(result.getId());
		Long id = result.getId();
		
		Dataset founded = repository.findOne(id);
		assertTrue(founded instanceof MrDataset);
		MrDataset foundedMr = (MrDataset) founded;
		assertEquals(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME, foundedMr.getMrQualityProcedureType());
	}
	
	/**
	 * Test the hierarchy strategy :
	 * A MR Dataset must be saved with its specific fields and those must be retrievable afterwards
	 * 
	 * @throws ShanoirException
	 */
	@Test
	public void heritageListTest() throws ShanoirException {
		MrDataset mr1 = ModelsUtil.createMrDataset();
		mr1.setMrQualityProcedureType(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME);
		Long mr1Id = repository.save(mr1).getId();
		MrDataset mr2 = ModelsUtil.createMrDataset();
		mr2.setMrQualityProcedureType(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_LONG_ECHO_TIME);
		Long mr2Id = repository.save(mr2).getId();
		PetDataset pet1 = ModelsUtil.createPetDataset();
		Long pet1Id = repository.save(pet1).getId();

		List<Dataset> all = Utils.toList(repository.findAll(Arrays.asList(mr1Id, mr2Id, pet1Id)));
		assertEquals(3, all.size());
		
		Dataset foundedMr1 = all.get(0);
		assertEquals(mr1Id, foundedMr1.getId());
		assertTrue(foundedMr1 instanceof MrDataset);
		assertEquals(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME, ((MrDataset)foundedMr1).getMrQualityProcedureType());
		
		Dataset foundedMr2 = all.get(1);
		assertEquals(mr2Id, foundedMr2.getId());
		assertTrue(foundedMr2 instanceof MrDataset);
		assertEquals(MrQualityProcedureType.MAGNETIC_FIELD_QUALITY_DATASET_LONG_ECHO_TIME, ((MrDataset)foundedMr2).getMrQualityProcedureType());
		
		Dataset foundedPet1 = all.get(2);
		assertEquals(pet1Id, foundedPet1.getId());
		assertTrue(foundedPet1 instanceof PetDataset);
		assertEquals("Pet", ((PetDataset)foundedPet1).getType());
	}
	
	@Test
	public void loadingStrategyTest() throws ShanoirException {
		assertNotNull(repository.findOne(1L).getDatasetAcquisition());
		assertNotNull(repository.findAll().iterator().next().getDatasetAcquisition()); 
	}
	
}
