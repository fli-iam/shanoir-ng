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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.zip.ZipOutputStream;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.dto.CommonIdNamesDTO;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	private static final String IMPORT_AS_BIDS = "/importer/importAsBids/";

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
		    	FileUtils.write(importJsonFile, "{\"studyId\": 1,\"acquisitionEquipmentId\": \"1\",\"patients\": [{\"patientID\":\"BidsCreated\",\"studies\" : [ {\"series\": [{\"images\": [{\"path\":\"pathToDicomImage\"}]}]}]}]}", StandardCharsets.UTF_8);
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
	public void testImportAsBidsNotZipFile() throws Exception {
		// GIVEN no bids folder to import
		MockMultipartFile file = new MockMultipartFile("file", "file.notzip", "application/json", "test".getBytes());

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.message").value("Wrong content type of file upload, .zip required."));

		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingParticipants() throws Exception {
		// GIVEN a bids folder to import with no participants.tsv file
		MockMultipartFile file = createFile(false, false, false, false);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("participants.tsv file is mandatory"));
		
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsParticipantsDeserializerFails() throws Exception {
		// GIVEN a bids folder to import
		MockMultipartFile file = createFile(true, false, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"erreur: fail during parsing\"}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("erreur: fail during parsing"));
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingStudyDescriptionFile() throws Exception {
		// GIVEN a bids folder to import
		// BUT study description file does not exist
		MockMultipartFile file = createFile(true, false, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("studyDescriptionFile file is mandatory"));
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingSourcedataFolder() throws Exception {
		// GIVEN a bids folder to import
		// BUT study description file does not exist
		MockMultipartFile file = createFile(true, true, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("sourcedata folder is mandatory"));
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingShanoirImportJson() throws Exception {
		// GIVEN a bids folder to import
		// BUT shanoir-import.json file does not exist
		MockMultipartFile file = createFile(true, true, true, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("shanoir-import.json file is mandatory in subject folder"));
		
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingEquipement() throws Exception {
		// GIVEN a bids folder to import
		// BUT selected center does not exists
		MockMultipartFile file = createFile(true, true, true, true);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		CommonIdNamesDTO body = new CommonIdNamesDTO();
		ResponseEntity<CommonIdNamesDTO> resp = new ResponseEntity<>(body, HttpStatus.OK);
		
		Mockito.when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(CommonIdNamesDTO.class)))
		.thenReturn(resp);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("Equipement with ID " + 1 + " does not exists."));
		
		// THEN the import fails with an appropriate error message
	}
	
	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingStudy() throws Exception {
		// GIVEN a bids folder to import
		// BUT selected study does not exists
		MockMultipartFile file = createFile(true, true, true, true);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		CommonIdNamesDTO body = new CommonIdNamesDTO();
		body.setEquipement(new IdName(1l, "equip"));
		ResponseEntity<CommonIdNamesDTO> resp = new ResponseEntity<>(body, HttpStatus.OK);
		
		Mockito.when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(CommonIdNamesDTO.class)))
		.thenReturn(resp);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("Study with ID " + 1 + " does not exists."));
		
		// THEN the import fails with an appropriate error message
	}
	
	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingSubject() throws Exception {
		// GIVEN a bids folder to import
		// BUT selected subject does not exists
		MockMultipartFile file = createFile(true, true, true, true);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"OTHERANEME\", \"id\":1}]");

		CommonIdNamesDTO body = new CommonIdNamesDTO();
		body.setEquipement(new IdName(1l, "equip"));
		body.setStudy(new IdName(1l, "study"));
		ResponseEntity<CommonIdNamesDTO> resp = new ResponseEntity<>(body, HttpStatus.OK);
		
		Mockito.when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(CommonIdNamesDTO.class)))
		.thenReturn(resp);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("Subject " + "name" + " could not be created. Please check participants.tsv file."));
		
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsImportFails() throws Exception {
		// GIVEN a bids folder to import
		// BUT the import fails
		MockMultipartFile file = createFile(true, true, true, true);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_EXCHANGE), eq(""), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		CommonIdNamesDTO body = new CommonIdNamesDTO();
		body.setEquipement(new IdName(1l, "equip"));
		body.setStudy(new IdName(1l, "study"));
		ResponseEntity<CommonIdNamesDTO> resp = new ResponseEntity<>(body, HttpStatus.OK);
		
		Mockito.when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(CommonIdNamesDTO.class)))
		.thenReturn(resp);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is(500));
		
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsSuccess() throws Exception {
		// GIVEN a bids folder to import
		// TODO: Hard because we can't mock normal calls

		// WHEN we import the folder
		
		// THEN all the datas are correctly imported
	}
	
	@After
	public void tearDown() throws IOException {
	    File importDir = new File("/tmp/test-import-as-bids");
	    FileUtils.deleteDirectory(importDir);
	    File importFile = new File("/tmp/test-import-as-bids.zip");
	    FileUtils.deleteQuietly(importFile);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
