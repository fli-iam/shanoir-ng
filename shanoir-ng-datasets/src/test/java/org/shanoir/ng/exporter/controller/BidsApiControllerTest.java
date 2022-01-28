package org.shanoir.ng.exporter.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.bids.BidsDeserializer;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Test for BidsApiController class
 * @author fli
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = BidsApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class BidsApiControllerTest {

	private static final String REQUEST_PATH = "/bids";
	
	private static final String REQUEST_PATH_GENERATE = REQUEST_PATH + "/studyId/1/studyName/Name";

	private static final String REQUEST_PATH_EXPORT = REQUEST_PATH + "/exportBIDS/studyId/1";

	@MockBean
	BIDSService bidsService;

	@MockBean
	BidsDeserializer bidsDeserializer;
	
	@MockBean
	StudyRepository studyRepo;

	@Autowired
	private MockMvc mvc;

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testGenerateBIDSByStudyId() throws Exception {
		// GIVEN a study with a bids folder to generate
		
		// WHEN we call the API to generate the folder
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_GENERATE)).andExpect(status().isOk());
		
		// THEN the service is called
		Mockito.verify(bidsService).exportAsBids(1L, "Name");
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testExportBIDSFileNoFile() throws Exception  {
		// GIVEN a study with a bids folder to generate
		
		// WHEN we call the API to generate the folder
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_EXPORT).param("filePath", "inexisting")).andExpect(status().isUnauthorized());
		
		// THEN the service is called
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testExportBIDSFileUnauthorized() throws Exception  {
		// GIVEN a study with a bids folder to generate

		// WHEN we call the API to generate the folder
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_EXPORT).param("filePath", "/var/datasets-data/bids-data/stud-1_NATIVE/truc.pdf")).andExpect(status().isNoContent());

		// THEN the service is called
	}
}
