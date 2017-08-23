package org.shanoir.ng.center;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.center.Center;
import org.shanoir.ng.center.CenterRepository;
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
	public void findByTest() throws Exception {
		List<Center> centersDb = repository.findBy("name", CENTER_TEST_1_NAME);
		assertNotNull(centersDb);
		assertThat(centersDb.size()).isEqualTo(1);
		assertThat(centersDb.get(0).getId()).isEqualTo(CENTER_TEST_1_ID);
	}
	
	@Test
	public void findByNameTest() throws Exception {
		Optional<Center> centerDb = repository.findByName(CENTER_TEST_1_NAME);
		assertTrue(centerDb.isPresent());
		assertThat(centerDb.get().getId()).isEqualTo(CENTER_TEST_1_ID);
	}
	
	@Test
	public void findIdsAndNamesTest() throws Exception {
		List<CenterNameDTO> centersDb = repository.findIdsAndNames();
		assertNotNull(centersDb);
		assertThat(centersDb.size()).isEqualTo(2);
	}
	
	@Test
	public void findOneTest() throws Exception {
		Center centerDb = repository.findOne(CENTER_TEST_1_ID);
		assertThat(centerDb.getName()).isEqualTo(CENTER_TEST_1_NAME);
	}
	
}
