package org.shanoir.ng.importer;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import  org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for study controller.
 *
 * @author atouboul
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(secure = false)
public class ImporterApiControllerTest {

	private static final String REQUEST_PATH = "/importer/upload_dicom/";

	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private ImporterApiController iac;


	@Before
	public void setup() {
		// DO NOTHING FOR NOW
	}

	/**
	 * Test that rest services returns 200 when running (assume that code in rest service is fine)
	 * If file doesn't exists, then return an message in stdout saying file is missing
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void uploadFileTest() throws Exception {
		
		String filePath = "/media/extra/shanoir/sample.zip";
		File f = new File(filePath);
		if(f.exists() && !f.isDirectory()) { 
		
		    MockMultipartFile multipartFile = new MockMultipartFile("file","DCM_IMPORT_SAMPLE.zip","application/zip", new FileInputStream(new File(filePath)));
		
		    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(iac).build();
		    mockMvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH)
		            .file(multipartFile))
		    		.andDo(print())
		            .andExpect(status().isOk());
		
		} else {

			System.out.println("[TEST CASE ERROR] UNABLE TO RETRIEVE FILE FOR TESTCASE ImporterApiControllerTest.uploadFileTest() at location : " + filePath );
		
		}
		
	}

}
