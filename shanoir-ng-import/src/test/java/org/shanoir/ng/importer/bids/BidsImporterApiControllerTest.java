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

package org.shanoir.ng.importer.bids;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.dto.CommonIdNamesDTO;
import org.shanoir.ng.importer.dto.ExaminationDTO;
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
@WebMvcTest(controllers =  BidsImporterApiController.class)
@AutoConfigureMockMvc(secure = false)
public class BidsImporterApiControllerTest {

	private static final String IMPORT_AS_BIDS = "/bidsImporter/";

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
	private QueryPACSService queryPACSService;

	@MockBean
	private RabbitTemplate rabbitTemplate;
	
	@MockBean
	private DicomDirGeneratorService dicomDirGeneratorService;

	@MockBean
	private ImporterApiController importer;

	@Before
	public void setup() throws ShanoirException, IOException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
	}

	public MockMultipartFile createFile(boolean withParticipants, boolean studyDescription,
			boolean sourceData, boolean importJson, boolean rawData) throws IOException {
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
	    	File dicomFile = new File(sourceDataFile.getAbsolutePath() + "/" + "DICOM");
	    	File subjectFile = new File(dicomFile.getAbsolutePath() + "/sub-name");
	    	File modalityFile = new File(subjectFile.getAbsolutePath() + "/anat");
	    	sourceDataFile.mkdir();
	    	dicomFile.mkdir();
	    	subjectFile.mkdir();
	    	modalityFile.mkdir();
	    	if (importJson) {
		    	File importJsonFile = new File(subjectFile.getAbsolutePath() + "/shanoir-import.json");
		    	importJsonFile.createNewFile();
		    	FileUtils.write(importJsonFile, "{\"studyId\": 1,\"studyCardId\": \"1\",\"patients\": [{\"patientID\":\"BidsCreated\",\"studies\" : [ {\"series\": [{\"images\": [{\"path\":\"pathToDicomImage\"}]}]}]}]}", StandardCharsets.UTF_8);
	    	}
	    }
	    if (rawData) {
	    	File rawDataFile = new File(importDir.getAbsolutePath() + "/" + "rawData");
	    	File subjectFile = new File(rawDataFile.getAbsolutePath() + "/sub-name");
	    	File modalityFile = new File(subjectFile.getAbsolutePath() + "/anat");
	    	rawDataFile.mkdir();
	    	subjectFile.mkdir();
	    	modalityFile.mkdir();
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
		MockMultipartFile file = createFile(false, false, false, false, false);

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
		MockMultipartFile file = createFile(true, false, false, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
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
		MockMultipartFile file = createFile(true, false, false, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
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
		MockMultipartFile file = createFile(true, true, false, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("No subject folder found."));
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingShanoirImportJson() throws Exception {
		// GIVEN a bids folder to import
		// BUT shanoir-import.json file does not exist
		MockMultipartFile file = createFile(true, true, true, false, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("shanoir-import.json file is mandatory in subject / session folder"));
		
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingEquipement() throws Exception {
		// GIVEN a bids folder to import
		// BUT selected study card does not exists
		MockMultipartFile file = createFile(true, true, true, true, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		String  resp = null;
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, Long.valueOf(1)))
		.thenReturn(resp);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("StudyCard with ID " + 1 + " does not exists."));
		
		// THEN the import fails with an appropriate error message
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsStudyCardDisabled() throws Exception {
		// GIVEN a bids folder to import
		// BUT selected study card is disabled
		MockMultipartFile file = createFile(true, true, true, true, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");
	
		String resp = "{\"name\":\"name\", \"id\":1, \"studyId\":1, \"disabled\":true}";
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, Long.valueOf(1)))
		.thenReturn(resp);

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("StudyCard with ID " + 1 + " is currently disabled, please select another one."));
		
		// THEN the import fails with an appropriate error message
	}
	
	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsMissingStudy() throws Exception {
		// GIVEN a bids folder to import
		// BUT selected study does not corresponds
		MockMultipartFile file = createFile(true, true, true, true, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		String  resp = "{\"name\":\"name\", \"id\":1, \"studyId\":5}";
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, Long.valueOf(1)))
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
		MockMultipartFile file = createFile(true, true, true, true, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"OTHERANEME\", \"id\":1}]");

		String resp = "{\"name\":\"name\", \"id\":1, \"studyId\":1}";
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, Long.valueOf(1)))
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
		MockMultipartFile file = createFile(true, true, true, true, false);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
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
	public void testImportAsBidsNiftiMissingShanoirImportJson() throws Exception {
		// GIVEN a bids folder to import from NIFTI
		// BUT shanoir-import.json file does not exist
		MockMultipartFile file = createFile(true, true, false, false, true);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().is5xxServerError())
		.andExpect(jsonPath("$.message").value("shanoir-import.json file is mandatory in subject / session folder"));
		
		// THEN the import fails with an appropriate error message
	}

	//@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testImportAsBidsNiftiCreateExam() throws Exception {
		// GIVEN a bids folder to import from NIFTI
		// BUT shanoir-import.json file does not exist
		MockMultipartFile file = createFile(true, true, false, true, true);

		Mockito.when(rabbitTemplate.convertSendAndReceive(eq(RabbitMQConfiguration.SUBJECTS_QUEUE), Mockito.anyString()))
		.thenReturn("[{\"name\":\"name\", \"id\":1}]");
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(Mockito.eq(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_CENTER_QUEUE)
				, Mockito.anyLong()))
		.thenReturn("{\"name\":\"name\", \"id\":1}");

		String resp = "{\"name\":\"name\", \"id\":1, \"studyId\":1}";
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, Long.valueOf(1)))
		.thenReturn(resp);
		
		Mockito.when(rabbitTemplate.convertSendAndReceive(Mockito.eq(RabbitMQConfiguration.DATASET_SUBJECT_STUDY_QUEUE),
				Mockito.anyString())).thenReturn("name");

		// WHEN we import the folder
		mvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_AS_BIDS).file(file))
		.andExpect(status().isOk());
		
		ArgumentCaptor<ExaminationDTO> examCaptor = ArgumentCaptor.forClass(ExaminationDTO.class);
		ExaminationDTO exam = examCaptor.getValue();
		assertTrue(exam.getCenterId().equals(1L));
		
		// TODO: check problem here
		Mockito.when(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, examCaptor.capture())).thenReturn("{\"id\":1}");
		
		Mockito.verify(rabbitTemplate).convertAndSend(Mockito.eq(RabbitMQConfiguration.IMPORTER_QUEUE_BIDS_DATASET), Mockito.anyString());

		// THEN the import is sucessfull and a DTO is created
	}

	@After
	public void tearDown() throws IOException {
	    File importDir = new File("/tmp/test-import-as-bids");
	    FileUtils.deleteDirectory(importDir);
	    File importFile = new File("/tmp/test-import-as-bids.zip");
	    FileUtils.deleteQuietly(importFile);
	}
	
}
