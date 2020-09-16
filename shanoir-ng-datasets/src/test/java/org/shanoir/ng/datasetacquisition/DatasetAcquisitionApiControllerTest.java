package org.shanoir.ng.datasetacquisition;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.shanoir.ng.dataset.modality.EegDatasetDTO;
import org.shanoir.ng.datasetacquisition.controler.DatasetAcquisitionApiController;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DatasetAcquisitionApiController.class)
@AutoConfigureMockMvc(secure = false)
public class DatasetAcquisitionApiControllerTest {

	@MockBean
	private ImporterService importerService;
	
	@MockBean 
	private DatasetAcquisitionService datasetAcquisitionService;
	
	@MockBean
	private DatasetAcquisitionMapper dsAcqMapper;
	
	@Autowired
	private MockMvc mvc;

	private Gson gson;
	
	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testStartImportEEGJob() throws Exception {
		
		ArgumentCaptor<EegImportJob> captor = ArgumentCaptor.forClass(EegImportJob.class);
		
		EegImportJob importJob = new EegImportJob();
		EegDatasetDTO dataset = new EegDatasetDTO();
		importJob.setDatasets(Collections.singletonList(dataset));

		dataset.setName("Ceci est un nom bien particulier");
		importJob.setWorkFolder("other_particular_name");
		
		mvc.perform(MockMvcRequestBuilders.post("/datasetacquisition_eeg/")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(importJob))).andExpect(status().isOk());
		
		// Check calls
		verify(importerService).createEegDataset(captor.capture());
		assertEquals(((EegImportJob)captor.getValue()).getDatasets().get(0).getName(), dataset.getName());
		
		verify(importerService).cleanTempFiles(eq(importJob.getWorkFolder()));
	}
}
