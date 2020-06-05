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

package org.shanoir.ng.study;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.bids.utils.BidsDeserializer;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.controler.StudyApiController;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.security.StudyFieldEditionSecurityManager;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.study.service.StudyUniqueConstraintManager;
import org.shanoir.ng.study.service.StudyUserService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for study controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = StudyApiController.class)
@AutoConfigureMockMvc(secure = false)
public class StudyApiControllerTest {

	private static final String REQUEST_PATH = "/studies";
	private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_FOR_MEMBERS = REQUEST_PATH_WITH_ID + "/members";
	private static final String REQUEST_PATH_FOR_MEMBER_WITH_ID = REQUEST_PATH_FOR_MEMBERS + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private StudyMapper studyMapperMock;

	@MockBean
	private StudyService studyServiceMock;

	@MockBean
	private StudyUserService studyUserServiceMock;

	@MockBean
	private StudyFieldEditionSecurityManager fieldEditionSecurityManager;

	@MockBean
	private StudyUniqueConstraintManager uniqueConstraintManager;

	private Study stud;

	@MockBean
	private StudyBIDSService bidsService;
	
	@MockBean
	private BidsDeserializer bidsDeserializer;

	@MockBean
	private ShanoirEventService eventService;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();
	
	public static String tempFolderPath;
	@BeforeClass
	public static void beforeClass() {
		tempFolderPath = tempFolder.getRoot().getAbsolutePath() + "/tmp/";

	    System.setProperty("study-data", tempFolderPath);
	}
	
	@Before
	public void setup() throws AccessDeniedException, EntityNotFoundException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		stud = new Study();
		stud.setId(1L);

		given(studyMapperMock.studiesToStudyDTOs(Mockito.anyListOf(Study.class)))
		.willReturn(Arrays.asList(new StudyDTO()));
		given(studyMapperMock.studyToStudyDTO(Mockito.any(Study.class))).willReturn(new StudyDTO());

		doNothing().when(studyServiceMock).deleteById(1L);
		given(studyServiceMock.findAll()).willReturn(Arrays.asList(new Study()));
		given(studyServiceMock.findById(1L)).willReturn(stud);
		given(studyServiceMock.create(Mockito.mock(Study.class))).willReturn(new Study());
		given(fieldEditionSecurityManager.validate(Mockito.any(Study.class))).willReturn(new FieldErrorMap());
		given(uniqueConstraintManager.validate(Mockito.any(Study.class))).willReturn(new FieldErrorMap());
	}

	// TODO: manage keycloak token
	// @Test
	public void addMember() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_FOR_MEMBERS).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createStudyUser())))
		.andExpect(status().isNoContent());
	}

	// TODO: manage keycloak token
	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteStudyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findStudiesTest() throws Exception {

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	public void findStudiesNamesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_NAMES).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void testDeleteProtocolFile() {
		// GIVEN a protocol file associated to a study
		stud.setProtocolFilePaths(Collections.singletonList("test.pdf"));
		File pFile = new File(tempFolderPath + "study-1/test.pdf");
		pFile.getParentFile().mkdirs();
		try {
			pFile.createNewFile();

			// WHEN the file is deleted
			mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/protocol-file-delete/1").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

			// THEN the file is well deleted
			assertFalse(pFile.exists());
		} catch (IOException e) {
			System.err.println(e);
			fail();
		} catch (Exception e) {
			System.err.println(e);
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDeleteProtocolFileNotExisting() {
		// GIVEN a protocol file not existing associated to a study
		stud.setProtocolFilePaths(Collections.singletonList("test.pdf"));
		File pFile = new File(tempFolderPath + "study-1/test.pdf");
		pFile.getParentFile().mkdirs();
		try {
			// WHEN the file is deleted
			mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/protocol-file-delete/1").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

			// THEN the file is not really deleted because it does not exists
			assertFalse(pFile.exists());
		} catch (IOException e) {
			System.err.println(e);
			fail();
		} catch (Exception e) {
			System.err.println(e);
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDeleteProtocolFileNotLinked() {
		// GIVEN a protocol file NOT associated to a study
		stud.setProtocolFilePaths(Collections.singletonList("test2.pdf"));
		File pFile = new File(tempFolderPath + "study-1/test.pdf");
		pFile.getParentFile().mkdirs();
		try {
			pFile.createNewFile();

			// WHEN the file is deleted
			mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/protocol-file-delete/1").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

			// THEN the file is not deleted
			assertTrue(pFile.exists());
		} catch (IOException e) {
			System.err.println(e);
			fail();
		} catch (Exception e) {
			System.err.println(e);
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDownloadProtocolFile() throws IOException {
		File importZip = new File(tempFolderPath + "test-import-extra-data.zip");
		File saved = new File(tempFolderPath + "study-1/test-import-extra-data.pdf");

		if (saved.exists()) {
			saved.delete();
		}

		try {
			importZip.createNewFile();
			MockMultipartFile file = new MockMultipartFile("file", "test-import-extra-data.pdf", MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream(importZip.getAbsolutePath()));

			// WHEN The file is added to the examination

			mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH + "/protocol-file-upload/1").file(file))
			.andExpect(status().isOk());

			// THEN the file is saved
			assertTrue(saved.exists());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDownloadProtocolFileNotPDF() throws IOException {
		File importZip = new File(tempFolderPath + "test-import-extra-data.zip");
		File saved = new File(tempFolderPath + "study-1/test-import-extra-data.txt");
		if (saved.exists()) {
			saved.delete();
		}

		try {
			importZip.createNewFile();
			MockMultipartFile file = new MockMultipartFile("file", "test-import-extra-data.txt", MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream(importZip.getAbsolutePath()));

			// WHEN The file is added to the study

			mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH + "/protocol-file-upload/1").file(file))
			.andExpect(status().isNotAcceptable());

			// THEN the file is not saved as it's not a PDF
			assertFalse(saved.exists());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDownloadExtraData() throws IOException {
		// GIVEN an study with protocol file
		File todow = new File(tempFolderPath + "study-1/file.pdf");
		todow.getParentFile().mkdirs();

		// WHEN we download extra-data
		try {
			todow.createNewFile();
			FileUtils.write(todow, "test");
			MvcResult result = mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/protocol-file-download/1/file.pdf/"))
					.andExpect(status().isOk())
					.andReturn();

			// THEN the file is downloaded
			assertNotNull(result.getResponse().getContentAsString());
			System.out.println(result.getResponse().getContentAsString());
		} catch (Exception e) {
			System.out.println(e);
			fail();
		}
	}

}
