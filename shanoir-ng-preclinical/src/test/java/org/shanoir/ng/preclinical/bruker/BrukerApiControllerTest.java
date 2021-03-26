/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.preclinical.bruker;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

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
	private RestTemplate restTemplate;

	@MockBean
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();
	
	public static String tempFolderPath;
	@BeforeClass
	public static void beforeClass() {
		tempFolderPath = tempFolder.getRoot().getAbsolutePath() + "/tmp/";
	    System.setProperty("preclinical.uploadBrukerFolder", tempFolderPath);
	}

	@Before
	public void setup() throws ShanoirException {
	}

	@Test
	@WithMockUser
	public void uploadBrukerFileTest() throws Exception {
		String r = "test";
		given(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class),
				Matchers.<Class<String>>any())).willReturn(new ResponseEntity<>(r, HttpStatus.OK));
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
