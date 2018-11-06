package org.shanoir.ng.preclinical.references;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.shared.exception.ShanoirException;
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
 * Unit tests for references controller.
 *
 * @author sloury
 *
 */

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RefsApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class ReferencesApiControllerTest {

	private static final String REQUEST_PATH = "/refs";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/";
	private static final String REQUEST_PATH_CATEGORIES = REQUEST_PATH + "/categories";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_WITH_CAT_TYPE_VALUE = REQUEST_PATH + "/category/subject/specie/rat";
	private static final String REQUEST_PATH_BY_CAT_VALUE = REQUEST_PATH + "/category/subject";
	private static final String REQUEST_PATH_BY_CAT_AND_TYPE = REQUEST_PATH + "/category/subject/specie";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private RefsService refsServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(refsServiceMock).deleteById(1L);
		given(refsServiceMock.findAll()).willReturn(Arrays.asList(new Reference()));
		given(refsServiceMock.findById(1L)).willReturn(new Reference());
		given(refsServiceMock.findByCategoryTypeAndValue("subject", "specie", "rat")).willReturn(new Reference());
		given(refsServiceMock.save(Mockito.mock(Reference.class))).willReturn(new Reference());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteReferenceTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findReferenceByCategoryTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_BY_CAT_VALUE).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findReferenceByCategoryAndTypeTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_BY_CAT_AND_TYPE).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findReferenceByCategoryTypeAndValueTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_CAT_TYPE_VALUE).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findReferenceTypesByCategoryTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_BY_CAT_VALUE).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findReferencesCategoriesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_CATEGORIES).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findReferencesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewReferenceTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ReferenceModelUtil.createReferenceSpecie()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateReferenceTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ReferenceModelUtil.createReferenceSpecie()))).andExpect(status().isOk());
	}

}
