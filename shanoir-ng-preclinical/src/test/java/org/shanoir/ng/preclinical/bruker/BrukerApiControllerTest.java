package org.shanoir.ng.preclinical.bruker;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit test for Bruker Api Controller
 * 
 * @author mbodin
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = BrukerApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class BrukerApiControllerTest {

	private static final String REQUEST_PATH = "/bruker";
	private static final String REQUEST_PATH_UPLOAD_BRUKER = REQUEST_PATH + "/upload";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	@Before
	public void setup() throws ShanoirPreclinicalException {
	}

	@Test
	@WithMockUser
	public void uploadBrukerFileTest() throws Exception {
		MockMultipartFile firstFile = new MockMultipartFile("files", "2dseq", "text/plain", "some xml".getBytes());
		mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH_UPLOAD_BRUKER).file(firstFile))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void uploadBrukerFileNotValidTest() throws Exception {
		MockMultipartFile firstFile = new MockMultipartFile("files", "filename.txt", "text/plain",
				"some xml".getBytes());
		mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH_UPLOAD_BRUKER).file(firstFile))
				.andExpect(status().isNotAcceptable());
	}
}
