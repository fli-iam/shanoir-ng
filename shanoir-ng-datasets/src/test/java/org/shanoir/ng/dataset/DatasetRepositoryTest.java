package org.shanoir.ng.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'dataset'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class DatasetRepositoryTest {

	private static final String DATASET_TEST_1_NAME = "Dataset1";
	private static final Long DATASET_TEST_1_ID = 1L;
	
	@Autowired
	private MrDatasetRepository repository;
	
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
	public void findByTest() throws Exception {
		List<MrDataset> datasetsDb = repository.findBy("name", DATASET_TEST_1_NAME);
		assertNotNull(datasetsDb);
		assertThat(datasetsDb.size()).isEqualTo(1);
		assertThat(datasetsDb.get(0).getId()).isEqualTo(DATASET_TEST_1_ID);
	}
	
	
	@Test
	public void findOneTest() throws Exception {
		MrDataset datasetDb = repository.findOne(DATASET_TEST_1_ID);
		assertThat(datasetDb.getName()).isEqualTo(DATASET_TEST_1_NAME);
	}
	
}
