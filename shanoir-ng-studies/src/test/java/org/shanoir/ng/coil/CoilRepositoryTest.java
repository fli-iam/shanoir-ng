package org.shanoir.ng.coil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.repository.CoilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'coil'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class CoilRepositoryTest {

	private static final String COIL_TEST_1_NAME = "coil 1";
	private static final Long COIL_TEST_1_ID = 1L;
	
	@Autowired
	private CoilRepository repository;
	
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
		Iterable<Coil> coilsDb = repository.findAll();
		assertThat(coilsDb).isNotNull();
		int nbCoils = 0;
		Iterator<Coil> coilsIt = coilsDb.iterator();
		while (coilsIt.hasNext()) {
			coilsIt.next();
			nbCoils++;
		}
		assertThat(nbCoils).isEqualTo(3);
	}
	
	@Test
	public void findByNameTest() throws Exception {
		Optional<Coil> coilDb = repository.findByName(COIL_TEST_1_NAME);
		assertTrue(coilDb.isPresent());
		assertThat(coilDb.get().getId()).isEqualTo(COIL_TEST_1_ID);
	}
	
	@Test
	public void findOneTest() throws Exception {
		Coil coilDb = repository.findOne(COIL_TEST_1_ID);
		assertThat(coilDb.getName()).isEqualTo(COIL_TEST_1_NAME);
	}
	
}
