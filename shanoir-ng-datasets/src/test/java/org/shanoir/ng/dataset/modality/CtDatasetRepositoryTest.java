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

/**
 * Tests for repository 'ctdataset'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class CtDatasetRepositoryTest {

	private static final String CT_DATASET_TEST_1_NAME = "CTDataset1";
	private static final Long CT_DATASET_TEST_1_ID = 3L;
	
	@Autowired
	private CtDatasetRepository repository;
	
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
		Iterable<CtDataset> datasetsDb = repository.findAll();
		assertThat(datasetsDb).isNotNull();
		int nbDatasets = 0;
		Iterator<CtDataset> datasetsIt = datasetsDb.iterator();
		while (datasetsIt.hasNext()) {
			datasetsIt.next();
			nbDatasets++;
		}
		assertThat(nbDatasets).isEqualTo(1);
	}
	
	@Test
	public void findOneTest() throws Exception {
		CtDataset datasetDb = repository.findOne(CT_DATASET_TEST_1_ID);
		assertThat(datasetDb.getName()).isEqualTo(CT_DATASET_TEST_1_NAME);
	}
	
}
