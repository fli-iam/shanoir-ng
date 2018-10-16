package org.shanoir.ng.preclinical.pathologies.pathology_models;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.PathologyModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for pathology models controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PathologyModelApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class PathologyModelApiControllerTest {

	private static final String REQUEST_PATH = "/pathology/model";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_WITH_PATHOLOGY_ID = "/pathology/1/model/all";
	private static final String REQUEST_PATH_UPLOAD_SPECS = REQUEST_PATH + "/upload/specs/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private PathologyModelService modelServiceMock;
	@MockBean
	private PathologyService pathologyServiceMock;
	@MockBean
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(modelServiceMock).deleteById(1L);
		given(modelServiceMock.findAll()).willReturn(Arrays.asList(new PathologyModel()));
		given(modelServiceMock.findByPathology(new Pathology())).willReturn(Arrays.asList(new PathologyModel()));
		given(modelServiceMock.findById(1L)).willReturn(new PathologyModel());
		given(pathologyServiceMock.findById(1L)).willReturn(new Pathology());
		given(modelServiceMock.save(Mockito.mock(PathologyModel.class))).willReturn(new PathologyModel());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deletePathologyModelTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findPathologyModelByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findPathologyModelsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findPathologyModelsByPathologyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_PATHOLOGY_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewPathologyModelTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(PathologyModelUtil.createPathologyModel()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updatePathologyModelTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(PathologyModelUtil.createPathologyModel()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void uploadSpecificationsTest() throws Exception {
		MockMultipartFile firstFile = new MockMultipartFile("files", "filename.txt", "text/plain",
				"some xml".getBytes());
		mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH_UPLOAD_SPECS).file(firstFile))
				.andExpect(status().isOk());
	}

}
