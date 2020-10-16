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

package org.shanoir.ng.importer;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for importer controller.
 *
 * @author atouboul
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImporterApiController.class)
@AutoConfigureMockMvc(secure = false)
public class ImporterApiControllerTest {

	private static final String UPLOAD_EEG_PATH = "/importer/upload_eeg/";

	private static final String START_EEG_JOB_PATH = "/importer/start_import_eeg_job/";

	private static final String GET_DICOM = "/importer/get_dicom/";

	private Gson gson;
	
	@Autowired
	private MockMvc mvc;

	@MockBean
	private RestTemplate restTemplate;

	@MockBean
	private DicomDirToModelService dicomDirToModel;

	@MockBean
	private ImportJobConstructorService importJobConstructorService;

	@MockBean
	private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

	@MockBean
	private ImporterManagerService importerManagerService;

	@MockBean
	private QueryPACSService queryPACSService;

	@MockBean
	private RabbitTemplate rabbitTemplate;
	
	@MockBean
	private DicomDirGeneratorService dicomDirGeneratorService;

	@Autowired
	ImporterApiController controller;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();
	
	public static String tempFolderPath;

	@BeforeClass
	public static void beforeClass() {
		tempFolderPath = tempFolder.getRoot().getAbsolutePath() + "/tmp/";

	    System.setProperty("shanoir.import.directory", tempFolderPath);
	}

	@Before
	public void setup() throws ShanoirException, IOException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
	}

	public MockMultipartFile createFile(boolean withParticipants, boolean studyDescription,
			boolean sourceData, boolean importJson) throws IOException {
	    File importDir = new File("/tmp/test-import-as-bids");
	    importDir.mkdirs();
	    if (withParticipants) {
	    	File participants = new File(importDir.getAbsolutePath() + "/" + "participants.tsv");
	    	participants.createNewFile();
	    }
	    if (studyDescription) {
	    	File studyDesc = new File(importDir.getAbsolutePath() + "/" + "dataset_description.json");
	    	studyDesc.createNewFile();
	    }
	    if (sourceData) {
	    	File sourceDataFile = new File(importDir.getAbsolutePath() + "/" + "sourcedata");
	    	File subjectFile = new File(sourceDataFile.getAbsolutePath() + "/sub-name");
	    	sourceDataFile.mkdir();
	    	subjectFile.mkdir();
	    	if (importJson) {
		    	File importJsonFile = new File(subjectFile.getAbsolutePath() + "/shanoir-import.json");
		    	importJsonFile.createNewFile();
		    	FileUtils.write(importJsonFile, "{\"studyId\": 1,\"studyCardId\": \"1\",\"patients\": [{\"patientID\":\"BidsCreated\",\"studies\" : [ {\"series\": [{\"images\": [{\"path\":\"pathToDicomImage\"}]}]}]}]}", StandardCharsets.UTF_8);
	    	}
	    }
	    File importZip = new File("/tmp/test-import-as-bids.zip");

	    FileOutputStream outStream = new FileOutputStream(importZip.getAbsolutePath());
		ZipOutputStream zipout = new ZipOutputStream(outStream);
		ImportUtils.zipFile(importDir, importDir.getName(), zipout , true);
		zipout.close();
		outStream.close();
		return new MockMultipartFile("file", "test-import-as-bids.zip", "application/zip", new FileInputStream(importZip.getAbsolutePath()));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testUploadEEGZipFile() throws Exception {
		String filePath = "./src/main/resources/tests/eeg/brainvision (copy).zip";
		File f = new File(filePath);

		if (f.exists() && !f.isDirectory()) {
			MockMultipartFile multipartFile = new MockMultipartFile("file", "brainvision (copy).zip", "application/zip",	new FileInputStream(new File(filePath)));

			// Check that we return an EEGImport Job with adapted informations.
			mvc.perform(MockMvcRequestBuilders.fileUpload(UPLOAD_EEG_PATH).file(multipartFile))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("\"name\":\"Fp1\"")))
			.andExpect(content().string(containsString("\"z\":2.5810034")))
			.andExpect(content().string(containsString("/brainvision (copy)/ROBEEG_BACGU020_dlpfc_l_0002.vhdr\"")));
		} else {
			System.out.println("[TEST CASE ERROR] UNABLE TO RETRIEVE FILE FOR TESTCASE ImporterApiControllerTest.uploadFileTest() at location : " + filePath);
			fail();
		}
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testUploadEEGZipFileNotZip() throws Exception {
		// Wrong file
		String filePath = "./src/main/resources/tests/eeg/ROBEEG_BACGU020_dlpfc_l_0002.pos";
		File f = new File(filePath);

		if (f.exists() && !f.isDirectory()) {
			MockMultipartFile multipartFile = new MockMultipartFile("file", "brainvision (copy).zip", "application/zip",	new FileInputStream(new File(filePath)));

			// Check that we return an EEGImport Job with adapted informations.
			mvc.perform(MockMvcRequestBuilders.fileUpload(UPLOAD_EEG_PATH).file(multipartFile))
			.andExpect(status().isUnprocessableEntity());
		} else {
			System.out.println("[TEST CASE ERROR] UNABLE TO RETRIEVE FILE FOR TESTCASE ImporterApiControllerTest.uploadFileTest() at location : " + filePath);
			fail();
		}
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testStartImportEEGJob() throws Exception {
		ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);

		EegImportJob importJob = new EegImportJob();
		EegDataset dataset = new EegDataset();
		importJob.setDatasets(Collections.singletonList(dataset));
		dataset.setName("Ceci est un nom bien particulier");

		mvc.perform(MockMvcRequestBuilders.post(START_EEG_JOB_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(importJob)));
		
		// Just check that the name is well transmitted and that the call is made
		verify(restTemplate).exchange(any(String.class), eq(HttpMethod.POST), captor.capture(), eq(String.class));
		assertEquals(dataset.getName(), ((EegImportJob)captor.getValue().getBody()).getDatasets().get(0).getName());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testGetDicomImageNoPath() throws Exception {
		
		mvc.perform(MockMvcRequestBuilders.get(GET_DICOM)
				.param("path", ""))
		.andExpect(status().is(200));
	}

	@Test
	public void testCleanTempFiles() {
		// GIVEN a temporary folder with many undeleted folders
		List<File> filesDeleted = new ArrayList<>();
	    try {
		    File userDir = new File(tempFolderPath + "/3465468");
		    userDir.mkdirs();
		    filesDeleted.add(userDir);
		    userDir.setLastModified(new DateTime().minusDays(3).getMillis());

		    File downloadFile = new File(tempFolderPath + "/test.download");
		    downloadFile.createNewFile();
		    filesDeleted.add(downloadFile);
		    downloadFile.setLastModified(new DateTime().minusDays(3).getMillis());
		
		    File zipFile = new File(tempFolderPath + "/test.zip");
		    zipFile.createNewFile();
		    filesDeleted.add(zipFile);
		    zipFile.setLastModified(new DateTime().minusDays(3).getMillis());
		    
		    File recentZipFile = new File(tempFolderPath + "/test-recent.zip");
		    recentZipFile.createNewFile();
		    filesDeleted.add(recentZipFile);

		    File brukerFile = new File(tempFolderPath + "/bruker");
		    brukerFile.mkdir();
		    filesDeleted.add(brukerFile);
		    brukerFile.setLastModified(new DateTime().minusDays(3).getMillis());
	
		    File tomcatFile = new File(tempFolderPath + "/tomcat-docbase-2018512");
			tomcatFile.createNewFile();
			filesDeleted.add(tomcatFile);
			tomcatFile.setLastModified(new DateTime().minusDays(3).getMillis());
	
		    File randomFile = new File(tempFolderPath + "/random123");
		    randomFile.createNewFile();
		    filesDeleted.add(randomFile);
		    randomFile.setLastModified(new DateTime().minusDays(3).getMillis());

		    // WHEN we clean the temp folder
		    controller.cleanTempFiles();
		    
		    // THEN the folder is cleaned of certain folders (not all)
		    assertTrue(randomFile.exists());
		    assertTrue(recentZipFile.exists());
		    assertTrue(tomcatFile.exists());

		    assertFalse(userDir.exists());
		    assertFalse(zipFile.exists());
		    assertFalse(downloadFile.exists());
		    assertFalse(brukerFile.exists());

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			// Delete all created files
			for (File fileToDelete : filesDeleted) {
				FileUtils.deleteQuietly(fileToDelete);
			}
		}
	}

}
