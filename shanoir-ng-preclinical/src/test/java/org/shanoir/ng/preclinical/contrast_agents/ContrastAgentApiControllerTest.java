package org.shanoir.ng.preclinical.contrast_agents;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgent;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentApiController;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentService;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ContrastAgentModelUtil;
import org.shanoir.ng.utils.ReferenceModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for contrast agents controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ContrastAgentApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class ContrastAgentApiControllerTest {

	private static final String REQUEST_PATH = "/protocol/1/contrastagent";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_WITH_NAME = REQUEST_PATH + "/name/Gadolinium";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ContrastAgentService agentsServiceMock;

	@MockBean
	private RefsService referencesServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(agentsServiceMock).deleteById(1L);
		given(agentsServiceMock.findAll()).willReturn(Arrays.asList(new ContrastAgent()));
		given(referencesServiceMock.findByCategoryTypeAndValue("contrastagent", "name", "Gadolinium"))
				.willReturn(ReferenceModelUtil.createReferenceContrastAgentGado());
		given(agentsServiceMock.findByName(ReferenceModelUtil.createReferenceContrastAgentGado()))
				.willReturn(new ContrastAgent());
		given(agentsServiceMock.findById(1L)).willReturn(new ContrastAgent());
		given(agentsServiceMock.save(Mockito.mock(ContrastAgent.class))).willReturn(new ContrastAgent());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteContrastAgentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findContrastAgentByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findContrastAgentsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findContrastAgentByNameTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_NAME).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewContrastAgentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ContrastAgentModelUtil.createContrastAgentGado()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateContrastAgentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ContrastAgentModelUtil.createContrastAgentGado()))).andExpect(status().isOk());
	}

}
