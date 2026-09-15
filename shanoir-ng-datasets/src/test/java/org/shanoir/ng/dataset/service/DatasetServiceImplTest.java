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

package org.shanoir.ng.dataset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetfile.DatasetFileRepository;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.springframework.http.HttpStatus;

/**
 * Unit tests of the dataset deletion guards.
 *
 * A dataset that has been copied into another study is the source of its copies, which are mapped
 * with CascadeType.ALL: deleting it would delete the copies along with it. Both the direct deletion
 * and the one cascaded by a parent acquisition must refuse it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatasetServiceImplTest {

    private static final Long DATASET_ID = 11L;

    @Mock
    private DatasetRepository repository;

    @Mock
    private DatasetAsyncService datasetAsyncService;

    @Mock
    private DatasetProcessingService processingService;

    @Mock
    private DatasetPropertyService propertyService;

    @Mock
    private ProcessingResourceRepository processingResourceRepository;

    @Mock
    private DatasetFileRepository datasetFileRepository;

    @InjectMocks
    private DatasetServiceImpl service;

    private Dataset datasetWithCopies() {
        Dataset dataset = new MrDataset();
        dataset.setId(DATASET_ID);
        dataset.setDatasetExpressions(new ArrayList<>());
        Dataset copy = new MrDataset();
        copy.setId(12L);
        copy.setSource(dataset);
        dataset.setCopies(List.of(copy));
        return dataset;
    }

    private Dataset datasetWithoutCopies() {
        Dataset dataset = new MrDataset();
        dataset.setId(DATASET_ID);
        dataset.setDatasetExpressions(new ArrayList<>());
        dataset.setCopies(new ArrayList<>());
        return dataset;
    }

    @Test
    void deleteByIdRefusesToDeleteADatasetThatHasCopies() {
        when(repository.findById(DATASET_ID)).thenReturn(Optional.of(datasetWithCopies()));

        RestServiceException exception = assertThrows(RestServiceException.class,
                () -> service.deleteById(DATASET_ID));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorModel().getCode().intValue());
        verify(repository, never()).delete(any(Dataset.class));
    }

    /**
     * The cascaded deletion goes through the very same guard since deleteByIdCascade() was
     * mutualized into deleteById(id, cascade): a copied dataset must not be dropped because its
     * parent acquisition is being deleted.
     */
    @Test
    void deleteByIdCascadeRefusesToDeleteADatasetThatHasCopies() throws Exception {
        when(repository.findById(DATASET_ID)).thenReturn(Optional.of(datasetWithCopies()));

        RestServiceException exception = assertThrows(RestServiceException.class,
                () -> service.deleteById(DATASET_ID, true));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorModel().getCode().intValue());
        verify(repository, never()).delete(any(Dataset.class));
        verify(datasetAsyncService, never())
                .deleteDatasetFilesFromDiskAndPacsAsync(any(), anyBoolean(), anyLong(), anyBoolean());
    }

    @Test
    void deleteByIdCascadeDeletesADatasetThatHasNoCopy() throws Exception {
        Dataset dataset = datasetWithoutCopies();
        when(repository.findById(DATASET_ID)).thenReturn(Optional.of(dataset));

        service.deleteById(DATASET_ID, true);

        verify(repository).delete(dataset);
        verify(propertyService).deleteByDatasetId(DATASET_ID);
    }

    /**
     * The processings of a deleted dataset are cleaned up along with it, and deleting a processing
     * deletes its output datasets: the cascade flag must reach them, or every processed dataset of
     * a deleted subject publishes a deletion event of its own.
     */
    @Test
    void deleteByIdCascadePropagatesTheCascadeToTheProcessings() throws Exception {
        when(repository.findById(DATASET_ID)).thenReturn(Optional.of(datasetWithoutCopies()));

        service.deleteById(DATASET_ID, true);

        verify(processingService).removeDatasetFromAllProcessingInput(DATASET_ID, true);
    }

    @Test
    void deleteByIdReportsItsOwnDeletionOfTheProcessingOutputs() throws Exception {
        when(repository.findById(DATASET_ID)).thenReturn(Optional.of(datasetWithoutCopies()));

        service.deleteById(DATASET_ID);

        verify(processingService).removeDatasetFromAllProcessingInput(DATASET_ID, false);
    }
}
