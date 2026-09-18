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

package org.shanoir.ng.processing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.springframework.http.HttpStatus;

/**
 * Unit tests of the deletion of a dataset processing, with a focus on the one whose output
 * datasets have all been deleted already : it still exists, and the rights to delete it can no
 * longer be deduced from its outputs.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatasetProcessingServiceImplTest {

    private static final Long PROCESSING_ID = 9L;

    private static final Long STUDY_ID = 42L;

    private static final String CAN_ADMINISTRATE = "CAN_ADMINISTRATE";

    @Mock
    private DatasetProcessingRepository repository;

    @Mock
    private ProcessingResourceRepository processingResourceRepository;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DatasetSecurityService datasetSecurityService;

    @Mock
    private SolrService solrService;

    @Mock
    private ShanoirEventService eventService;

    @InjectMocks
    private DatasetProcessingServiceImpl service;

    private DatasetProcessing processing;

    @BeforeEach
    void setUp() {
        processing = new DatasetProcessing();
        processing.setId(PROCESSING_ID);
        processing.setStudyId(STUDY_ID);
        processing.setOutputDatasets(new ArrayList<>());

        when(repository.findById(PROCESSING_ID)).thenReturn(Optional.of(processing));
        when(repository.findByIdWithOutputs(PROCESSING_ID)).thenReturn(Optional.of(processing));
        when(repository.findAllByParentId(PROCESSING_ID)).thenReturn(List.of());
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, CAN_ADMINISTRATE)).thenReturn(true);
        when(datasetSecurityService.hasRightOnEveryDataset(anyList(), anyString())).thenReturn(true);
    }

    @Test
    void deleteByIdRemovesAProcessingWhoseOutputDatasetsAreAllGone() throws Exception {
        deleteAsUser();

        verify(processingResourceRepository).deleteByProcessingId(PROCESSING_ID);
        verify(repository).deleteById(PROCESSING_ID);
    }

    @Test
    void deleteByIdReportsTheProcessingDeletionAsASucceededJob() throws Exception {
        deleteAsUser();

        ShanoirEvent event = lastPublishedEvent();
        assertEquals(ShanoirEventType.DELETE_DATASET_PROCESSING_EVENT, event.getEventType());
        assertEquals(PROCESSING_ID.toString(), event.getObjectId());
        assertEquals(STUDY_ID, event.getStudyId());
        assertEquals(ShanoirEvent.SUCCESS, event.getStatus());
        assertEquals(Float.valueOf(1f), event.getProgress());
    }

    @Test
    void deleteByIdDeletesTheOutputDatasetsUnderTheProcessingEvent() throws Exception {
        Dataset output = dataset(1234L);
        processing.setOutputDatasets(new ArrayList<>(List.of(output)));

        deleteAsUser();

        // the datasets are deleted under the processing event, so that they publish none of their own
        ShanoirEvent event = lastPublishedEvent();
        verify(datasetService).deleteById(output.getId(), event);
        verify(solrService).deleteFromIndex(output.getId());
    }

    @Test
    void deleteByIdRefusesAnOutputLessProcessingOnAStudyTheUserDoesNotAdministrate() {
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, CAN_ADMINISTRATE)).thenReturn(false);

        RestServiceException exception = assertThrows(RestServiceException.class, this::deleteAsUser);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.getErrorModel().getCode().intValue());
        verify(repository, never()).deleteById(PROCESSING_ID);
    }

    @Test
    void deleteByIdRefusesWhenAnOutputDatasetIsNotAdministrated() {
        processing.setOutputDatasets(new ArrayList<>(List.of(dataset(1234L))));
        when(datasetSecurityService.hasRightOnEveryDataset(anyList(), anyString())).thenReturn(false);

        assertThrows(RestServiceException.class, this::deleteAsUser);

        verify(repository, never()).deleteById(PROCESSING_ID);
    }

    @Test
    void deleteByIdFailsTheJobWhenTheProcessingDoesNotExist() {
        when(repository.findById(PROCESSING_ID)).thenReturn(Optional.empty());
        when(repository.findByIdWithOutputs(PROCESSING_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, this::deleteAsUser);

        // the job would otherwise stay in progress forever
        assertEquals(ShanoirEvent.ERROR, lastPublishedEvent().getStatus());
    }

    private void deleteAsUser() throws Exception {
        try (MockedStatic<KeycloakUtil> keycloakUtil = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

            service.deleteById(PROCESSING_ID);
        }
    }

    private ShanoirEvent lastPublishedEvent() {
        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(eventService, Mockito.atLeastOnce()).publishEvent(captor.capture());
        return captor.getValue();
    }

    private Dataset dataset(Long id) {
        Dataset dataset = new MrDataset();
        dataset.setId(id);
        return dataset;
    }
}
