package org.shanoir.ng.preclinical.contrast_agents;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgent;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentRepository;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentServiceImpl;
import org.shanoir.ng.preclinical.references.RefsServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.ContrastAgentModelUtil;
import org.shanoir.ng.utils.ReferenceModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * COntrast agents service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ContrastAgentServiceTest {

	private static final Long AGENT_ID = 1L;
	private static final String UPDATED_AGENT_DATA = "SuperGadolinium";

	@Mock
	private ContrastAgentRepository agentsRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ContrastAgentServiceImpl agentsService;
	
	private RefsServiceImpl refsService;
	
		
	
	@Before
	public void setup() {
		given(agentsRepository.findAll()).willReturn(Arrays.asList(ContrastAgentModelUtil.createContrastAgentGado()));
		given(agentsRepository.findOne(AGENT_ID)).willReturn(ContrastAgentModelUtil.createContrastAgentGado());
		given(agentsRepository.save(Mockito.any(ContrastAgent.class))).willReturn(ContrastAgentModelUtil.createContrastAgentGado());
	}

	@Test
	public void deleteByIdTest() throws ShanoirPreclinicalException {
		agentsService.deleteById(AGENT_ID);

		Mockito.verify(agentsRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<ContrastAgent> agents = agentsService.findAll();
		Assert.assertNotNull(agents);
		Assert.assertTrue(agents.size() == 1);

		Mockito.verify(agentsRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final ContrastAgent agent = agentsService.findById(AGENT_ID);
		Assert.assertNotNull(agent);
		Assert.assertTrue(ContrastAgentModelUtil.AGENT_GADO_REFERENCE_NAME.equals(agent.getName().getValue()));

		Mockito.verify(agentsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
	
	@Test
	public void findByNameTest() {
		final ContrastAgent agent = agentsService.findById(AGENT_ID);
		Assert.assertNotNull(agent);
		Assert.assertTrue(ContrastAgentModelUtil.AGENT_GADO_REFERENCE_NAME.equals(agent.getName().getValue()));

		Mockito.verify(agentsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
	
	

	@Test
	public void saveTest() throws ShanoirPreclinicalException {
		agentsService.save(createContrastAgent());

		Mockito.verify(agentsRepository, Mockito.times(1)).save(Mockito.any(ContrastAgent.class));
	}

	@Test
	public void updateTest() throws ShanoirPreclinicalException {
		final ContrastAgent updatedAgent = agentsService.update(createContrastAgent());
		Assert.assertNotNull(updatedAgent);
		Assert.assertTrue(UPDATED_AGENT_DATA.equals(updatedAgent.getManufacturedName()));

		Mockito.verify(agentsRepository, Mockito.times(1)).save(Mockito.any(ContrastAgent.class));
	}

/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirPreclinicalException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private ContrastAgent createContrastAgent() {
		final ContrastAgent agent = new ContrastAgent();
		agent.setId(AGENT_ID);
		agent.setName(ReferenceModelUtil.createReferenceContrastAgentGado());
		agent.setManufacturedName(UPDATED_AGENT_DATA);
		return agent;
	}
	
}
