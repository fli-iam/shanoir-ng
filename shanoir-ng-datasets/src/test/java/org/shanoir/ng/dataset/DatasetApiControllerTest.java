package org.shanoir.ng.dataset;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for dataset controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DatasetApiController.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles("test")
public class DatasetApiControllerTest {

	private static final String REQUEST_PATH = "/datasets";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private DatasetService datasetServiceMock;

	@MockBean
	private DatasetMapper datasetMapperMock;
	
	@MockBean
	private MrDatasetMapper mrDatasetMapperMock;
	
	@MockBean
	private WADODownloaderService downloader;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(datasetServiceMock).deleteById(1L);
		given(datasetServiceMock.findById(1L)).willReturn(new MrDataset());
		given(datasetServiceMock.create(Mockito.mock(MrDataset.class))).willReturn(new MrDataset());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteDatasetTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findDatasetByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateDatasetTest() throws Exception {
		String json = gson.toJson(ModelsUtil.createMrDataset());
		// Cheat to add dataset type into json
		json = json.substring(0, json.length() - 1) + ",\"type\":\"Mr\"}";
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isNoContent());
	}

}
