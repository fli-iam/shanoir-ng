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

package org.shanoir.ng.dicom.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;

/**
 * Unit tests of the virtual UID handling for the OHIF viewer: the
 * disambiguation between real SeriesInstanceUIDs, acquisitionUIDs
 * and datasetUIDs, the series-to-virtual-UID map of an examination
 * and its short-lived cache.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeriesInstanceUIDHandlerTest {

    private static final String SERIES_UID_ACQ = "1.4.9.12.34.1.8527.1111111111111111111111111111111111111111";

    private static final String SERIES_UID_SEG = "1.2.826.0.1.3680043.8.498.33333333333333333333333333333333";

    private static final String SERIES_UID_OUTPUT = "1.4.9.12.34.1.8527.4444444444444444444444444444444444444444";

    @Mock
    private DatasetAcquisitionService acquisitionService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DatasetProcessingRepository datasetProcessingRepository;

    @InjectMocks
    private SeriesInstanceUIDHandler handler;

    @BeforeEach
    void setUp() {
        handler.init();
    }

    @Test
    void isAcquisitionUIDAcceptsOnlyUIDsWithLongSuffix() {
        assertTrue(handler.isAcquisitionUID("1.4.9.12.34.1.8527.100"));
        // real UIDs generated during pseudonymization end with a 40-digit
        // number, that never fits into a Long
        assertFalse(handler.isAcquisitionUID(SERIES_UID_ACQ));
        // a datasetUID is not an acquisitionUID: "0.500" does not parse as Long
        assertFalse(handler.isAcquisitionUID("1.4.9.12.34.1.8527.0.500"));
        assertFalse(handler.isAcquisitionUID("1.2.840.10008.100"));
        assertFalse(handler.isAcquisitionUID(null));
    }

    @Test
    void isDatasetUIDAcceptsOnlyDatasetPrefixWithLongSuffix() {
        assertTrue(handler.isDatasetUID("1.4.9.12.34.1.8527.0.500"));
        assertFalse(handler.isDatasetUID("1.4.9.12.34.1.8527.500"));
        assertFalse(handler.isDatasetUID("1.4.9.12.34.1.8527.0.500.1"));
        assertFalse(handler.isDatasetUID(SERIES_UID_SEG));
        assertFalse(handler.isDatasetUID(null));
    }

    @Test
    void extractIdsFromVirtualUIDs() {
        assertEquals(Long.valueOf(100L), handler.extractAcquisitionId("1.4.9.12.34.1.8527.100"));
        assertEquals(Long.valueOf(500L), handler.extractDatasetId("1.4.9.12.34.1.8527.0.500"));
    }

    @Test
    void resolveSeriesInstanceUIDPassesRealUIDsThrough() {
        assertEquals(SERIES_UID_ACQ, handler.resolveSeriesInstanceUID(SERIES_UID_ACQ));
    }

    @Test
    void resolveSeriesInstanceUIDResolvesAcquisitionAndDatasetUIDs() {
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        acquisition.setId(100L);
        acquisition.setSeriesInstanceUID(SERIES_UID_ACQ);
        when(acquisitionService.findById(100L)).thenReturn(acquisition);
        assertEquals(SERIES_UID_ACQ, handler.resolveSeriesInstanceUID("1.4.9.12.34.1.8527.100"));

        GenericDataset dataset = createDataset(new GenericDataset(), 500L,
                "/studies/1.4.9.12.34.1.8527.42/series/" + SERIES_UID_SEG + "/instances/1.2.3");
        when(datasetService.findById(500L)).thenReturn(dataset);
        assertEquals(SERIES_UID_SEG, handler.resolveSeriesInstanceUID("1.4.9.12.34.1.8527.0.500"));
    }

    @Test
    void findSeriesInstanceUIDOfDatasetExtractsUIDFromWadoRSAndWadoURIPaths() {
        Dataset wadoRs = createDataset(new MrDataset(), 1L,
                "http://dcm4chee-arc:8081/dcm4chee-arc/aets/AS_RECEIVED/rs/studies/1.2.3/series/"
                        + SERIES_UID_ACQ + "/instances/1.2.4");
        assertEquals(SERIES_UID_ACQ, handler.findSeriesInstanceUID(wadoRs));

        Dataset wadoUri = createDataset(new MrDataset(), 2L,
                "http://dcm4chee-arc:8081/wado?requestType=WADO&studyUID=1.2.3&seriesUID="
                        + SERIES_UID_ACQ + "&objectUID=1.2.4&contentType=application/dicom");
        assertEquals(SERIES_UID_ACQ, handler.findSeriesInstanceUID(wadoUri));
    }

    @Test
    void findSeriesInstanceUIDOfAcquisitionPrefersDatabaseValueAndFallsBackToDatasetPath() {
        MrDatasetAcquisition withDbValue = new MrDatasetAcquisition();
        withDbValue.setSeriesInstanceUID(SERIES_UID_ACQ);
        assertEquals(SERIES_UID_ACQ, handler.findSeriesInstanceUID(withDbValue));

        GenericDatasetAcquisition withDatasetPath = new GenericDatasetAcquisition();
        withDatasetPath.setDatasets(List.of(createDataset(new GenericDataset(), 1L,
                "/studies/1.2.3/series/" + SERIES_UID_SEG + "/instances/1.2.4")));
        assertEquals(SERIES_UID_SEG, handler.findSeriesInstanceUID(withDatasetPath));

        MrDatasetAcquisition withoutDatasets = new MrDatasetAcquisition();
        withoutDatasets.setDatasets(List.of());
        assertNull(handler.findSeriesInstanceUID(withoutDatasets));
    }

    @Test
    void findSeriesToVirtualUIDsMapsAcquisitionsDatasetsAndProcessingOutputs() {
        when(acquisitionService.findByExamination(42L)).thenReturn(List.of(createAcquisition()));
        DatasetProcessing processing = new DatasetProcessing();
        processing.setOutputDatasets(List.of(createDataset(new GenericDataset(), 600L,
                "/studies/1.2.3/series/" + SERIES_UID_OUTPUT + "/instances/1.2.6")));
        when(datasetProcessingRepository.findAllByInputDatasets_IdIn(anyList())).thenReturn(List.of(processing));

        Map<String, String> seriesToVirtualUIDs = handler.findSeriesToVirtualUIDs(42L);

        assertEquals(3, seriesToVirtualUIDs.size());
        assertEquals("1.4.9.12.34.1.8527.100", seriesToVirtualUIDs.get(SERIES_UID_ACQ));
        assertEquals("1.4.9.12.34.1.8527.0.500", seriesToVirtualUIDs.get(SERIES_UID_SEG));
        assertEquals("1.4.9.12.34.1.8527.0.600", seriesToVirtualUIDs.get(SERIES_UID_OUTPUT));
    }

    @Test
    void findSeriesToVirtualUIDsCachesTheMapOfAnExamination() {
        when(acquisitionService.findByExamination(42L)).thenReturn(List.of(createAcquisition()));

        Map<String, String> first = handler.findSeriesToVirtualUIDs(42L);
        Map<String, String> second = handler.findSeriesToVirtualUIDs(42L);

        assertEquals(first, second);
        // the second call within the TTL is served from the cache
        verify(acquisitionService, times(1)).findByExamination(42L);

        handler.clearVirtualUIDCaches();
        handler.findSeriesToVirtualUIDs(42L);
        verify(acquisitionService, times(2)).findByExamination(42L);
    }

    private MrDatasetAcquisition createAcquisition() {
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        acquisition.setId(100L);
        acquisition.setSeriesInstanceUID(SERIES_UID_ACQ);
        MrDataset primaryDataset = createDataset(new MrDataset(), 400L,
                "/studies/1.2.3/series/" + SERIES_UID_ACQ + "/instances/1.2.4");
        GenericDataset segDataset = createDataset(new GenericDataset(), 500L,
                "/studies/1.2.3/series/" + SERIES_UID_SEG + "/instances/1.2.5");
        acquisition.setDatasets(List.of(primaryDataset, segDataset));
        return acquisition;
    }

    private <T extends Dataset> T createDataset(T dataset, Long id, String path) {
        DatasetFile file = new DatasetFile();
        file.setPacs(true);
        file.setPath(path);
        DatasetExpression expression = new DatasetExpression();
        expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        expression.setDatasetFiles(List.of(file));
        dataset.setId(id);
        dataset.setDatasetExpressions(List.of(expression));
        return dataset;
    }

}
