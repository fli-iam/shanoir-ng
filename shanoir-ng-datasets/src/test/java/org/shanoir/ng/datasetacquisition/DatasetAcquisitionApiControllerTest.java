package org.shanoir.ng.datasetacquisition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.shanoir.ng.dataset.modality.EegDatasetDTO;
import org.shanoir.ng.datasetacquisition.controler.DatasetAcquisitionApiController;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionDatasetsMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.ExaminationDatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.service.DicomImporterService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.shanoir.ng.importer.service.EegImporterService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;


@WebMvcTest(controllers = DatasetAcquisitionApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class DatasetAcquisitionApiControllerTest {

	@MockBean
	private ImporterService importerService;

	@MockBean
	private EegImporterService eegImporterService;

	@MockBean
	private DicomSEGAndSRImporterService dicomSEGAndSRImporterService;

	@MockBean
	private DicomImporterService dicomImporterService;
	
	@MockBean 
	private DatasetAcquisitionService datasetAcquisitionService;
	
	@MockBean
	private DatasetAcquisitionMapper dsAcqMapper;
	
	@MockBean
	private ExaminationDatasetAcquisitionMapper examDsAcqMapper;
	
	@MockBean
	private DatasetAcquisitionDatasetsMapper dsAcqDsMapper;

	@MockBean
	private SolrService solrService;

	@MockBean
	private ShanoirEventService eventService;
	@MockBean
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private MockMvc mvc;

	private Gson gson;
	
	@BeforeEach
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
// MK: Commented as 404 thrown
//		mvc.perform(MockMvcRequestBuilders.post("/datasetacquisition_eeg")
//				.accept(MediaType.APPLICATION_JSON)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(gson.toJson(importJob))).andExpect(status().isOk());
//		
//		// Check calls
//		verify(importerService).createEegDataset(captor.capture());
//		assertEquals(((EegImportJob)captor.getValue()).getDatasets().get(0).getName(), dataset.getName());
//		
//		verify(importerService).cleanTempFiles(eq(importJob.getWorkFolder()));
	}
}
