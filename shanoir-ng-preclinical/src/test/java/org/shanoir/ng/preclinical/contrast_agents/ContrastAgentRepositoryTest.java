package org.shanoir.ng.preclinical.contrast_agents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgent;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentRepository;
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
 * Tests for repository 'contrast agents'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class ContrastAgentRepositoryTest {

	private static final String AGENT_TEST_1_NAME = "Gadolinium";
	private static final Long AGENT_TEST_1_ID = 1L;
	private static final String AGENT_TEST_1_MANUFACTURED_NAME = "Gadolinium";

	@Autowired
	private ContrastAgentRepository repository;

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
		Iterable<ContrastAgent> agentsDb = repository.findAll();
		assertThat(agentsDb).isNotNull();
		int nbTemplates = 0;
		Iterator<ContrastAgent> agentsIt = agentsDb.iterator();
		while (agentsIt.hasNext()) {
			agentsIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(1);
	}

	@Test
	public void findByNameTest() throws Exception {
		Optional<ContrastAgent> agentDb = repository.findByName(ReferenceModelUtil.createReferenceContrastAgentGado());
		assertTrue(agentDb.isPresent());
		assertThat(agentDb.get().getId()).isEqualTo(AGENT_TEST_1_ID);
		assertThat(agentDb.get().getManufacturedName()).isEqualTo(AGENT_TEST_1_MANUFACTURED_NAME);
	}

	@Test
	public void findOneTest() throws Exception {
		ContrastAgent agentDb = repository.findOne(AGENT_TEST_1_ID);
		assertThat(agentDb.getName().getValue()).isEqualTo(AGENT_TEST_1_NAME);
		assertThat(agentDb.getManufacturedName()).isEqualTo(AGENT_TEST_1_MANUFACTURED_NAME);
	}

}
