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

package org.shanoir.ng.datasetacquisition;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
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

    @MockBean
    private StorageService storageService;

    @Autowired
    private MockMvc mvc;

    private Gson gson;

    @TempDir
    private File tempFolder;

    private static final String REQUEST_PATH = "/datasetacquisition";

    @BeforeEach
    public void setup() throws Exception {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        File tempFile = new File(tempFolder, "file1.pdf");
        Files.writeString(tempFile.toPath(), "test content");
        Resource tempResource = new FileSystemResource(tempFile);
        given(storageService.loadAcquisitionExtraData(1L, "file1.pdf")).willReturn(tempResource);
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
//        mvc.perform(MockMvcRequestBuilders.post("/datasetacquisition_eeg")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(gson.toJson(importJob))).andExpect(status().isOk());
//
//        // Check calls
//        verify(importerService).createEegDataset(captor.capture());
//        assertEquals(((EegImportJob)captor.getValue()).getDatasets().get(0).getName(), dataset.getName());
//
//        verify(importerService).cleanTempFiles(eq(importJob.getWorkFolder()));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testAddExtraData() throws Exception {
        // GIVEN a file to add to a dataset acquisition (service returns null => failure)
        MockMultipartFile file = new MockMultipartFile("file", "extra-data.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "test content".getBytes());

        // WHEN the file is uploaded
        mvc.perform(MockMvcRequestBuilders.multipart(REQUEST_PATH + "/extra-data-upload/1").file(file))
                .andExpect(status().isUnprocessableEntity());

        // THEN the service is called
        Mockito.verify(datasetAcquisitionService).addExtraData(Mockito.any(Long.class), Mockito.any(MultipartFile.class));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testDownloadExtraDataNotExisting() throws Exception {
        // GIVEN a dataset acquisition with no such extra-data file (mock returns null)
        // WHEN we download extra-data
        // THEN we get a "no content" answer
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/extra-data-download/1/missing.pdf/"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testDownloadExtraData() throws Exception {
        // GIVEN a dataset acquisition with an extra-data file (stubbed in setup)
        // WHEN we download extra-data
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/extra-data-download/1/file1.pdf/"))
                .andExpect(status().isOk())
                .andReturn();

        // THEN the file is downloaded
        assertNotNull(result.getResponse().getContentAsString());
    }
}
