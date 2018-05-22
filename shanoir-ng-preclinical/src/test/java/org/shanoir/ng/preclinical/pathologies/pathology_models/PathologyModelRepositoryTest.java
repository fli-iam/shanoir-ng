package org.shanoir.ng.preclinical.pathologies.pathology_models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.utils.PathologyModelUtil;
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
 * Tests for repository 'pathology models'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class PathologyModelRepositoryTest {

	private static final String MODEL_TEST_1_DATA = "U836";
	private static final Long MODEL_TEST_1_ID = 1L;
	private static final String PATHOLOGY_TEST_1_DATA = "Stroke";

	@Autowired
	private PathologyModelRepository repository;

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
		Iterable<PathologyModel> modelsDb = repository.findAll();
		assertThat(modelsDb).isNotNull();
		int nbTemplates = 0;
		Iterator<PathologyModel> modelsIt = modelsDb.iterator();
		while (modelsIt.hasNext()) {
			modelsIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	public void findByPathologyTest() throws Exception {
		List<PathologyModel> modelDb = repository.findByPathology(PathologyModelUtil.createPathology());
		assertNotNull(modelDb);
		assertThat(modelDb.size()).isEqualTo(1);
		assertThat(modelDb.get(0).getId()).isEqualTo(MODEL_TEST_1_ID);
		assertThat(modelDb.get(0).getPathology().getName()).isEqualTo(PATHOLOGY_TEST_1_DATA);
	}

	@Test
	public void findByNameTest() throws Exception {
		Optional<PathologyModel> modelDb = repository.findByName(MODEL_TEST_1_DATA);
		assertTrue(modelDb.isPresent());
		assertThat(modelDb.get().getId()).isEqualTo(MODEL_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		PathologyModel modelDb = repository.findOne(MODEL_TEST_1_ID);
		assertThat(modelDb.getName()).isEqualTo(MODEL_TEST_1_DATA);
	}

}
