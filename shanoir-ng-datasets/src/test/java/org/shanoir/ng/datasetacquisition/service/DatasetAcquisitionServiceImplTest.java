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

package org.shanoir.ng.datasetacquisition.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpStatus;

/**
 * Unit tests of the removal of dataset acquisitions that hold no dataset anymore.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatasetAcquisitionServiceImplTest {

    private static final Long ACQ_ID = 123L;

    private static final Long STUDY_ID = 42L;

    @Mock
    private DatasetAcquisitionRepository repository;

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private ShanoirEventService shanoirEventService;

    @Mock
    private StorageService storageService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private SolrService solrService;

    @InjectMocks
    private DatasetAcquisitionServiceImpl service;

    private DatasetAcquisition acquisition;

    @BeforeEach
    void setUp() {
        Examination examination = new Examination();
        examination.setId(7L);
        acquisition = new GenericDatasetAcquisition();
        acquisition.setId(ACQ_ID);
        acquisition.setExamination(examination);

        when(repository.findById(ACQ_ID)).thenReturn(Optional.of(acquisition));
        when(datasetRepository.countByDatasetAcquisitionId(ACQ_ID)).thenReturn(0);
        when(repository.findBySourceId(ACQ_ID)).thenReturn(Collections.emptyList());
        when(repository.findByIdWithDatasets(ACQ_ID)).thenReturn(Optional.of(acquisition));
    }

    @Test
    void isEmptyAndRemovableReturnsTrueWhenNothingIsLeftOnTheAcquisition() throws EntityNotFoundException {
        assertTrue(service.isEmptyAndRemovable(ACQ_ID));
    }

    @Test
    void isEmptyAndRemovableReturnsFalseWhenDatasetsRemain() throws EntityNotFoundException {
        when(datasetRepository.countByDatasetAcquisitionId(ACQ_ID)).thenReturn(1);

        assertFalse(service.isEmptyAndRemovable(ACQ_ID));
    }

    @Test
    void isEmptyAndRemovableReturnsFalseWhenExtraDataFilesAreAttached() throws EntityNotFoundException {
        acquisition.setExtraDataFilePathList(List.of("protocol.pdf"));

        assertFalse(service.isEmptyAndRemovable(ACQ_ID));
    }

    @Test
    void isEmptyAndRemovableReturnsFalseWhenAcquisitionIsTheSourceOfACopy() throws EntityNotFoundException {
        when(repository.findBySourceId(ACQ_ID)).thenReturn(List.of(new GenericDatasetAcquisition()));

        assertFalse(service.isEmptyAndRemovable(ACQ_ID));
    }

    @Test
    void isEmptyAndRemovableThrowsWhenAcquisitionDoesNotExist() {
        when(repository.findById(ACQ_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.isEmptyAndRemovable(ACQ_ID));
    }

    @Test
    void findAcquisitionsLeftEmptyByReturnsTheAcquisitionWhenAllItsDatasetsAreDeleted() {
        givenAcquisitionHolding(2, dataset(11L), dataset(12L));

        List<DatasetAcquisition> leftEmpty = service.findAcquisitionsLeftEmptyBy(List.of(11L, 12L));

        assertEquals(1, leftEmpty.size());
        assertEquals(ACQ_ID, leftEmpty.get(0).getId());
    }

    @Test
    void findAcquisitionsLeftEmptyByIgnoresTheAcquisitionWhenADatasetRemains() {
        givenAcquisitionHolding(3, dataset(11L), dataset(12L));

        assertTrue(service.findAcquisitionsLeftEmptyBy(List.of(11L, 12L)).isEmpty());
    }

    @Test
    void findAcquisitionsLeftEmptyByIgnoresTheAcquisitionWhenItHoldsExtraData() {
        acquisition.setExtraDataFilePathList(List.of("protocol.pdf"));
        givenAcquisitionHolding(1, dataset(11L));

        assertTrue(service.findAcquisitionsLeftEmptyBy(List.of(11L)).isEmpty());
    }

    @Test
    void findAcquisitionsLeftEmptyByReturnsNothingWithoutAnyDataset() {
        assertTrue(service.findAcquisitionsLeftEmptyBy(Collections.emptyList()).isEmpty());
    }

    @Test
    void findEmptyAcquisitionsLeavesOutTheOnesThatMustBeKept() {
        DatasetAcquisition holdingExtraData = new GenericDatasetAcquisition();
        holdingExtraData.setId(456L);
        holdingExtraData.setExtraDataFilePathList(List.of("protocol.pdf"));
        when(repository.findEmpty()).thenReturn(List.of(acquisition, holdingExtraData));

        List<DatasetAcquisition> empty = service.findEmptyAcquisitions(null);

        assertEquals(1, empty.size());
        assertEquals(ACQ_ID, empty.get(0).getId());
    }

    @Test
    void findEmptyAcquisitionsScopesToTheGivenStudy() {
        when(repository.findEmptyByStudyId(STUDY_ID)).thenReturn(List.of(acquisition));

        assertEquals(1, service.findEmptyAcquisitions(STUDY_ID).size());
        verify(repository, never()).findEmpty();
    }

    /**
     * The acquisition under test holds datasetsHeld datasets in total, among which the given ones
     * are about to be deleted.
     */
    private void givenAcquisitionHolding(int datasetsHeld, Dataset... deletedDatasets) {
        when(datasetRepository.findAllById(Mockito.anyIterable())).thenReturn(List.of(deletedDatasets));
        when(datasetRepository.countByDatasetAcquisitionId(ACQ_ID)).thenReturn(datasetsHeld);
    }

    private Dataset dataset(Long id) {
        Dataset dataset = new MrDataset();
        dataset.setId(id);
        dataset.setDatasetAcquisition(acquisition);
        return dataset;
    }

    @Test
    void deleteEmptyAcquisitionRemovesItAndPublishesAnEvent() throws Exception {
        Examination examination = Mockito.mock(Examination.class);
        when(examination.getStudyId()).thenReturn(STUDY_ID);
        acquisition.setExamination(examination);

        try (MockedStatic<KeycloakUtil> keycloakUtil = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

            service.deleteEmptyAcquisition(ACQ_ID);
        }

        verify(repository).deleteById(ACQ_ID);
        verify(storageService).deleteDirectoryAcquisitionExtraData(ACQ_ID);

        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(shanoirEventService).publishEvent(captor.capture());
        assertEquals(ShanoirEventType.DELETE_DATASET_ACQUISITION_EVENT, captor.getValue().getEventType());
        assertEquals(ACQ_ID.toString(), captor.getValue().getObjectId());
        assertEquals(STUDY_ID, captor.getValue().getStudyId());
        // without a message and a progress, the job shows up empty and at NaN%
        assertEquals(Float.valueOf(1f), captor.getValue().getProgress());
        assertFalse(captor.getValue().getMessage().isEmpty());
    }

    @Test
    void deleteEmptyAcquisitionRefusesWhenDatasetsRemain() {
        when(datasetRepository.countByDatasetAcquisitionId(ACQ_ID)).thenReturn(2);

        RestServiceException exception = assertThrows(RestServiceException.class,
                () -> service.deleteEmptyAcquisition(ACQ_ID));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorModel().getCode().intValue());
        verify(repository, never()).deleteById(ACQ_ID);
    }

    @Test
    void deleteEmptyAcquisitionRefusesWhenExtraDataFilesAreAttached() {
        acquisition.setExtraDataFilePathList(List.of("protocol.pdf"));

        RestServiceException exception = assertThrows(RestServiceException.class,
                () -> service.deleteEmptyAcquisition(ACQ_ID));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorModel().getCode().intValue());
        verify(repository, never()).deleteById(ACQ_ID);
    }

    @Test
    void deleteEmptyAcquisitionRefusesWhenAcquisitionIsTheSourceOfACopy() {
        when(repository.findBySourceId(ACQ_ID)).thenReturn(List.of(new GenericDatasetAcquisition()));

        assertThrows(RestServiceException.class, () -> service.deleteEmptyAcquisition(ACQ_ID));

        verify(repository, never()).deleteById(ACQ_ID);
    }

    /**
     * The cascade of an examination deletion empties its acquisitions on purpose and deletes them
     * itself. It must not be confused with the automatic removal of the acquisitions emptied by a
     * dataset deletion: none of the guards of that removal applies here.
     */
    @Test
    void deleteByIdCascadeDeletesTheAcquisitionWhateverTheRemovalGuardsSay() throws Exception {
        acquisition.setExtraDataFilePathList(List.of("protocol.pdf"));
        acquisition.setDatasets(List.of(dataset(11L), dataset(12L)));

        try (MockedStatic<KeycloakUtil> keycloakUtil = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

            service.deleteByIdCascade(ACQ_ID, null);
        }

        verify(datasetService).deleteByIdCascade(11L);
        verify(datasetService).deleteByIdCascade(12L);
        verify(repository).deleteById(ACQ_ID);
        // the emptiness of the acquisition is never even questioned on this path
        verify(datasetRepository, never()).countByDatasetAcquisitionId(Mockito.anyLong());

        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(shanoirEventService).publishEvent(captor.capture());
        // without a message and a progress, the job shows up empty and at NaN%
        assertEquals(Float.valueOf(1f), captor.getValue().getProgress());
        assertFalse(captor.getValue().getMessage().isEmpty());
    }

    @Test
    void deleteRemovesTheAcquisitionItselfOnceItsDatasetsAreGone() throws Exception {
        acquisition.setDatasets(List.of(dataset(11L), dataset(12L)));

        // this is the method the asynchronous acquisition deletion runs, its caller returns
        // before it completes and can not delete the acquisition row itself
        service.delete(acquisition, null);

        verify(datasetService).deleteByIdCascade(11L);
        verify(datasetService).deleteByIdCascade(12L);
        verify(repository).deleteById(ACQ_ID);
    }
}
