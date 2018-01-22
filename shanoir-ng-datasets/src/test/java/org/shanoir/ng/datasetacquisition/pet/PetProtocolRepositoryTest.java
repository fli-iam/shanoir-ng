package org.shanoir.ng.datasetacquisition.pet;

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
 * Tests for repository 'petprotocol'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class PetProtocolRepositoryTest {

	private static final int PET_PROTOCOL_TEST_1_NB_SLICES = 5;
	private static final Long PET_PROTOCOL_TEST_1_ID = 1L;
	
	@Autowired
	private PetProtocolRepository repository;
	
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
		Iterable<PetProtocol> petProtocolsDb = repository.findAll();
		assertThat(petProtocolsDb).isNotNull();
		int nbPetProtocols = 0;
		Iterator<PetProtocol> petProtocolsIt = petProtocolsDb.iterator();
		while (petProtocolsIt.hasNext()) {
			petProtocolsIt.next();
			nbPetProtocols++;
		}
		assertThat(nbPetProtocols).isEqualTo(1);
	}
	
	@Test
	public void findOneTest() throws Exception {
		PetProtocol petProtocolDb = repository.findOne(PET_PROTOCOL_TEST_1_ID);
		assertThat(petProtocolDb.getNumberOfSlices()).isEqualTo(PET_PROTOCOL_TEST_1_NB_SLICES);
	}
	
}
