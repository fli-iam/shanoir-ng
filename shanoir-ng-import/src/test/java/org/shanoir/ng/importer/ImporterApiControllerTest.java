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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;


/**
 * Unit tests for importer controller.
 *
 * @author atouboul
 *
 */
@WebMvcTest(controllers = ImporterApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ImporterApiControllerTest {

	private static final String START_EEG_JOB_PATH = "/importer/start_import_eeg_job/";

	private static final String GET_DICOM = "/importer/get_dicom/";
	
	@Autowired
	private MockMvc mvc;

	@MockBean
	private RestTemplate restTemplate;

	@MockBean
	private DicomDirToModelService dicomDirToModel;

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
	
	@MockBean
	private ShanoirEventService shanoirEventService;

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
	public void testStartImportEEGJob() throws Exception {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

		EegImportJob importJob = new EegImportJob();
		EegDataset dataset = new EegDataset();
		importJob.setExaminationId(1L);
		importJob.setDatasets(Collections.singletonList(dataset));
		dataset.setName("Ceci est un nom bien particulier");

		mvc.perform(MockMvcRequestBuilders.post(START_EEG_JOB_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(JacksonUtils.serialize(importJob)));
		
		// Just check that the name is well transmitted and that the call is made
		verify(rabbitTemplate).convertSendAndReceive(Mockito.any(String.class), captor.capture());

		//verify(restTemplate).exchange(Mockito.any(String.class), Mockito.eq(HttpMethod.POST), captor.capture(), Mockito.eq(String.class));
		assertTrue(((String)captor.getValue()).contains(dataset.getName()));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testGetDicomImageNoPath() throws Exception {
		
		mvc.perform(MockMvcRequestBuilders.get(GET_DICOM)
				.param("path", ""))
		.andExpect(status().is(200));
	}

}
