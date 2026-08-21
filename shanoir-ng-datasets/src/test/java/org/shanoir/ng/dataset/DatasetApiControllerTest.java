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

package org.shanoir.ng.dataset;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.CreateStatisticsService;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.service.DicomImporterService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.importer.service.ProcessedDatasetImporterService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests of the dataset controller, focused on the removal of the acquisitions that a deletion
 * leaves empty.
 */
@WebMvcTest(controllers = DatasetApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class DatasetApiControllerTest {

    @MockBean
    private DatasetMapper datasetMapper;

    @MockBean
    private MrDatasetMapper mrDatasetMapper;

    @MockBean
    private EegDatasetMapper eegDatasetMapper;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private CreateStatisticsService createStatisticsService;

    @MockBean
    private ExaminationService examinationService;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private ImporterService importerService;

    @MockBean
    private ProcessedDatasetImporterService processedDatasetImporterService;

    @MockBean
    private WADODownloaderService downloader;

    @MockBean
    private ShanoirEventService eventService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private SolrService solrService;

    @MockBean(name = "datasetDownloaderServiceImpl")
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @MockBean
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDAndSubjectNameHandler;

    @MockBean
    private StorageService storageService;

    @MockBean
    private DatasetAcquisitionService datasetAcquisitionService;

    /** Needed by the STOWRSMultipartRequestFilter, which is part of the web slice. */
    @MockBean
    private DicomSEGAndSRImporterService dicomSEGAndSRImporterService;

    @MockBean
    private DicomImporterService dicomImporterService;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockKeycloakUser(id = 1, username = "dummy-admin", authorities = { "ROLE_ADMIN" })
    public void testDeleteDatasetRemovesTheAcquisitionItLeavesEmpty() throws Exception {
        given(datasetService.findById(11L)).willReturn(datasetOfAcquisition(11L, 1L));
        given(datasetAcquisitionService.isEmptyAndRemovable(1L)).willReturn(true);

        mvc.perform(MockMvcRequestBuilders.delete("/datasets/11").param("deleteEmptyAcquisitions", "true"))
                .andExpect(status().isNoContent());

        verify(datasetService).deleteById(11L);
        verify(datasetAcquisitionService).deleteEmptyAcquisition(1L);
    }

    @Test
    @WithMockKeycloakUser(id = 1, username = "dummy-admin", authorities = { "ROLE_ADMIN" })
    public void testDeleteDatasetKeepsTheAcquisitionThatIsNotRemovable() throws Exception {
        given(datasetService.findById(11L)).willReturn(datasetOfAcquisition(11L, 1L));
        given(datasetAcquisitionService.isEmptyAndRemovable(1L)).willReturn(false);

        mvc.perform(MockMvcRequestBuilders.delete("/datasets/11").param("deleteEmptyAcquisitions", "true"))
                .andExpect(status().isNoContent());

        verify(datasetService).deleteById(11L);
        verify(datasetAcquisitionService, never()).deleteEmptyAcquisition(Mockito.anyLong());
    }

    @Test
    @WithMockKeycloakUser(id = 1, username = "dummy-admin", authorities = { "ROLE_ADMIN" })
    public void testDeleteDatasetLeavesTheAcquisitionAloneByDefault() throws Exception {
        given(datasetService.findById(11L)).willReturn(datasetOfAcquisition(11L, 1L));

        mvc.perform(MockMvcRequestBuilders.delete("/datasets/11"))
                .andExpect(status().isNoContent());

        verify(datasetService).deleteById(11L);
        verify(datasetAcquisitionService, never()).isEmptyAndRemovable(Mockito.anyLong());
        verify(datasetAcquisitionService, never()).deleteEmptyAcquisition(Mockito.anyLong());
    }

    @Test
    @WithMockKeycloakUser(id = 1, username = "dummy-admin", authorities = { "ROLE_ADMIN" })
    public void testDeleteDatasetsChecksEachParentAcquisitionOnce() throws Exception {
        given(datasetService.findByIdIn(List.of(11L, 12L, 21L))).willReturn(List.of(
                datasetOfAcquisition(11L, 1L),
                datasetOfAcquisition(12L, 1L),
                datasetOfAcquisition(21L, 2L)));
        given(datasetAcquisitionService.isEmptyAndRemovable(1L)).willReturn(true);
        given(datasetAcquisitionService.isEmptyAndRemovable(2L)).willReturn(false);

        mvc.perform(MockMvcRequestBuilders.delete("/datasets/delete")
                        .param("deleteEmptyAcquisitions", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[11, 12, 21]"))
                .andExpect(status().isNoContent());

        verify(datasetService).deleteByIdIn(List.of(11L, 12L, 21L));
        verify(datasetAcquisitionService).isEmptyAndRemovable(1L);
        verify(datasetAcquisitionService).isEmptyAndRemovable(2L);
        verify(datasetAcquisitionService).deleteEmptyAcquisition(1L);
        verify(datasetAcquisitionService, never()).deleteEmptyAcquisition(2L);
    }

    private Dataset datasetOfAcquisition(Long datasetId, Long acquisitionId) {
        DatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(acquisitionId);
        Dataset dataset = new MrDataset();
        dataset.setId(datasetId);
        dataset.setDatasetAcquisition(acquisition);
        return dataset;
    }
}
