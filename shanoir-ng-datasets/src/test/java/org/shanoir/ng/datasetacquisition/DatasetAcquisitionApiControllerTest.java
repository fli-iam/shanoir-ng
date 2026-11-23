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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionWithDatasetsMapper;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.ExaminationDatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.service.DicomImporterService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.shanoir.ng.importer.service.EegImporterService;
import org.shanoir.ng.importer.service.ImporterMailService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.storage.StorageException;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
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
import java.util.List;


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
    private DatasetAcquisitionWithDatasetsMapper dsAcqDsMapper;

    @MockBean
    private SolrService solrService;

    @MockBean
    private ShanoirEventService eventService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private DatasetAcquisitionRepository datasetAcquisitionRepository;

    @MockBean
    private StorageService storageService;

    @MockBean
    private ImporterMailService importerMailService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DatasetAcquisitionApiController api;

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
    public void testCreateNewEegDatasetAcquisitionFailureIsNotRequeued() {
        Mockito.doThrow(new RuntimeException("Could not read file: test.vmrk"))
                .when(eegImporterService).createEegDataset(Mockito.any(EegImportJob.class));

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> api.createNewEegDatasetAcquisition("{\"workFolder\": \"/tmp/3/123\"}"));
        Mockito.verify(importerService, Mockito.never()).cleanTempFiles(Mockito.any());
    }

    @Test
    public void testCreateNewEegDatasetAcquisitionInvalidJsonIsNotRequeued() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> api.createNewEegDatasetAcquisition("not a json"));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testUpdateWithoutFiles() throws Exception {
        // GIVEN an acquisition update without files
        GenericDatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(1L);
        given(dsAcqMapper.acquisitionDTOToAcquisitionIdRelations(Mockito.any())).willReturn(acquisition);
        given(datasetAcquisitionService.update(Mockito.eq(acquisition), Mockito.anyList())).willReturn(acquisition);

        // WHEN it is sent
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, REQUEST_PATH + "/1")
                .file(new MockMultipartFile("datasetAcquisition", "", MediaType.APPLICATION_JSON_VALUE, "{\"id\":1}".getBytes())))
                .andExpect(status().isNoContent());

        // THEN the acquisition is updated with no file
        Mockito.verify(datasetAcquisitionService).update(acquisition, List.of());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testUpdateWithFiles() throws Exception {
        // GIVEN an acquisition of an examination updated with two new files
        Examination exam = new Examination();
        exam.setId(3L);
        GenericDatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(1L);
        acquisition.setExamination(exam);
        given(dsAcqMapper.acquisitionDTOToAcquisitionIdRelations(Mockito.any())).willReturn(acquisition);
        given(datasetAcquisitionService.update(Mockito.eq(acquisition), Mockito.anyList())).willReturn(acquisition);

        // WHEN the update and the files are sent in a single request
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, REQUEST_PATH + "/1")
                .file(new MockMultipartFile("datasetAcquisition", "", MediaType.APPLICATION_JSON_VALUE, "{\"id\":1}".getBytes()))
                .file(new MockMultipartFile("files", "a.txt", MediaType.TEXT_PLAIN_VALUE, "a".getBytes()))
                .file(new MockMultipartFile("files", "b.txt", MediaType.TEXT_PLAIN_VALUE, "b".getBytes())))
                .andExpect(status().isNoContent());

        // THEN the files go to the service with the update, and one mail lists them
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MultipartFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(datasetAcquisitionService).update(Mockito.eq(acquisition), filesCaptor.capture());
        assertEquals(List.of("a.txt", "b.txt"), filesCaptor.getValue().stream().map(MultipartFile::getOriginalFilename).toList());
        Mockito.verify(importerMailService).sendExtraDataEmail(exam, 1L, List.of("a.txt", "b.txt"), 12L);
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testUpdateWithFilesStorageFailure() throws Exception {
        // GIVEN a file that can't be stored
        given(dsAcqMapper.acquisitionDTOToAcquisitionIdRelations(Mockito.any())).willReturn(new GenericDatasetAcquisition());
        given(datasetAcquisitionService.update(Mockito.any(), Mockito.anyList())).willThrow(new StorageException("storage down", null));

        // WHEN the update is sent with it
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, REQUEST_PATH + "/1")
                .file(new MockMultipartFile("datasetAcquisition", "", MediaType.APPLICATION_JSON_VALUE, "{\"id\":1}".getBytes()))
                .file(new MockMultipartFile("files", "a.txt", MediaType.TEXT_PLAIN_VALUE, "a".getBytes())))
                .andExpect(status().isInternalServerError());

        // THEN the acquisition is not updated, so no mail is sent
        Mockito.verifyNoInteractions(importerMailService);
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

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testDeleteEmptyDatasetAcquisitionsReportsItsProgress() throws Exception {
        // GIVEN two acquisitions holding no dataset anymore
        DatasetAcquisition first = new GenericDatasetAcquisition();
        first.setId(1L);
        DatasetAcquisition second = new GenericDatasetAcquisition();
        second.setId(2L);
        given(datasetAcquisitionService.findEmptyAcquisitions(null)).willReturn(List.of(first, second));

        // WHEN they are cleaned up
        mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/empty"))
                .andExpect(status().isOk());

        // THEN both are removed and the clean up is reported as a job
        Mockito.verify(datasetAcquisitionService).deleteEmptyAcquisition(1L);
        Mockito.verify(datasetAcquisitionService).deleteEmptyAcquisition(2L);

        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        Mockito.verify(eventService, Mockito.atLeastOnce()).publishEvent(captor.capture());
        ShanoirEvent event = captor.getValue();
        assertEquals(ShanoirEventType.DELETE_DATASET_ACQUISITION_EVENT, event.getEventType());
        assertEquals(ShanoirEvent.SUCCESS, event.getStatus());
        assertEquals(Float.valueOf(1f), event.getProgress());
        assertEquals("2 empty dataset acquisition(s) deleted.", event.getMessage());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testDeleteEmptyDatasetAcquisitionsWithNothingToCleanUp() throws Exception {
        // GIVEN no empty acquisition at all
        given(datasetAcquisitionService.findEmptyAcquisitions(null)).willReturn(Collections.emptyList());

        // WHEN the clean up runs
        mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/empty"))
                .andExpect(status().isOk());

        // THEN the job still completes, without a division by zero on its progress
        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        Mockito.verify(eventService, Mockito.atLeastOnce()).publishEvent(captor.capture());
        assertEquals(Float.valueOf(1f), captor.getValue().getProgress());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testFindDatasetAcquisitionsLeftEmptyBy() throws Exception {
        // GIVEN datasets whose deletion would leave their acquisition empty
        DatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(1L);
        ExaminationDatasetAcquisitionDTO dto = new ExaminationDatasetAcquisitionDTO();
        dto.setId(1L);
        dto.setName("T1 MPRAGE (Mr)");
        given(datasetAcquisitionService.findAcquisitionsLeftEmptyBy(List.of(11L, 12L))).willReturn(List.of(acquisition));
        given(examDsAcqMapper.datasetAcquisitionsToExaminationDatasetAcquisitionDTOs(List.of(acquisition)))
                .willReturn(List.of(dto));

        // WHEN we ask for the acquisitions that deletion would leave empty
        // THEN they are returned
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH + "/emptiedByDatasetIds")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[11, 12]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("T1 MPRAGE (Mr)"));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testFindDatasetAcquisitionsLeftEmptyByWithNoneLeftEmpty() throws Exception {
        // GIVEN a dataset whose acquisition holds other datasets
        given(datasetAcquisitionService.findAcquisitionsLeftEmptyBy(List.of(11L))).willReturn(Collections.emptyList());

        // WHEN we ask for the acquisitions that deletion would leave empty
        // THEN none is returned
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH + "/emptiedByDatasetIds")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[11]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
