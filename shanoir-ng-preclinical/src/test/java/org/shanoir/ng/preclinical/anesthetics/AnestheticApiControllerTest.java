package org.shanoir.ng.preclinical.anesthetics;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticApiController;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticType;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.AnestheticModelUtil;
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
 * Unit tests for anesthetics controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AnestheticApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class AnestheticApiControllerTest {

	private static final String REQUEST_PATH = "/anesthetic";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String ANESTHETIC_TYPE = "Gas";
	private static final String REQUEST_PATH_WITH_TYPE_NAME = REQUEST_PATH + "/type/" + ANESTHETIC_TYPE;
	private static final String REQUEST_PATH_WITH_TYPE_FAKE_NAME = REQUEST_PATH + "/type/Gasoline";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AnestheticService anestheticsServiceMock;

	@Before
	public void setup() throws ShanoirPreclinicalException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(anestheticsServiceMock).deleteById(1L);
		given(anestheticsServiceMock.findAll()).willReturn(Arrays.asList(new Anesthetic()));
		given(anestheticsServiceMock.findAllByAnestheticType(AnestheticType.GAS))
				.willReturn(Arrays.asList(new Anesthetic()));
		given(anestheticsServiceMock.findById(1L)).willReturn(new Anesthetic());
		given(anestheticsServiceMock.save(Mockito.mock(Anesthetic.class))).willReturn(new Anesthetic());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteAnestheticTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findAnestheticByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findAnestheticsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findAnestheticsByTypeTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_TYPE_NAME).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findAnestheticsByTypeFailedTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_TYPE_FAKE_NAME).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser
	public void saveNewAnestheticTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(AnestheticModelUtil.createAnestheticGas()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateAnestheticTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(AnestheticModelUtil.createAnestheticGas()))).andExpect(status().isOk());
	}

}
