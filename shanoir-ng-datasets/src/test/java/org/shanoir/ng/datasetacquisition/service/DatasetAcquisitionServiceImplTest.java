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
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
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
}
