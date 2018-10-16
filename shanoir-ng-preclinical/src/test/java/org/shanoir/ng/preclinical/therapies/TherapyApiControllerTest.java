package org.shanoir.ng.preclinical.therapies;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.TherapyModelUtil;
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
 * Unit tests for therapies controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TherapyApiController.class)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(secure = false)
public class TherapyApiControllerTest {

	private static final String REQUEST_PATH = "/therapy";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_TYPE = REQUEST_PATH + "/type/Drug";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private TherapyService therapyServiceMock;

	@MockBean
	private RefsService refsServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(therapyServiceMock).deleteById(1L);
		given(therapyServiceMock.findAll()).willReturn(Arrays.asList(new Therapy()));
		given(therapyServiceMock.findById(1L)).willReturn(new Therapy());
		given(therapyServiceMock.findByTherapyType(TherapyType.DRUG)).willReturn(Arrays.asList(new Therapy()));
		given(therapyServiceMock.save(Mockito.mock(Therapy.class))).willReturn(new Therapy());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findTherapyByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findTherapiesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findTherapiesByTypeTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_TYPE).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(TherapyModelUtil.createTherapyBrain())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(TherapyModelUtil.createTherapyBrain())))
				.andExpect(status().isOk());
	}

}
