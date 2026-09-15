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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

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
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.KeycloakUtil;

/**
 * Unit tests of the events published while dataset files are removed from disk and pacs.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatasetAsyncServiceImplTest {

    private static final Long DATASET_ID = 11L;

    private final ShanoirEvent parentEvent = new ShanoirEvent(
            ShanoirEventType.DELETE_EXAMINATION_EVENT, "5970", 1L, "Deleting examination...",
            ShanoirEvent.IN_PROGRESS, 0f, 42L);

    @Mock
    private DICOMWebService dicomWebService;

    @Mock
    private ShanoirEventService eventService;

    @InjectMocks
    private DatasetAsyncServiceImpl service;

    private List<DatasetFile> onePacsFile() {
        DatasetFile file = new DatasetFile();
        file.setPacs(true);
        file.setPath("http://localhost/dicom-web/studies/1/series/2/instances/3");
        return List.of(file);
    }

    private ShanoirEvent lastPublishedEvent() {
        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(eventService, atLeastOnce()).publishEvent(captor.capture());
        return captor.getValue();
    }

    @Test
    void cascadedDeletionPublishesNothingWhenItSucceeds() throws Exception {
        try (MockedStatic<KeycloakUtil> keycloak = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloak.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

            service.deleteDatasetFilesFromDiskAndPacsAsync(onePacsFile(), true, DATASET_ID, parentEvent);
        }

        verify(eventService, never()).publishEvent(any(ShanoirEvent.class));
    }

    /**
     * A cascaded deletion stays silent as long as all goes well, but a failure must not be: without
     * an event of its own the parent deletion would look like it completed.
     */
    @Test
    void cascadedDeletionPublishesAnErrorEventWhenItFails() throws Exception {
        doThrow(new ShanoirException("pacs is down")).when(dicomWebService).rejectDatasetFromPacs(any());

        try (MockedStatic<KeycloakUtil> keycloak = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloak.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

            assertThrows(ShanoirException.class,
                    () -> service.deleteDatasetFilesFromDiskAndPacsAsync(onePacsFile(), true, DATASET_ID, parentEvent));
        }

        ShanoirEvent published = lastPublishedEvent();
        assertEquals(ShanoirEvent.ERROR, published.getStatus());
        assertTrue(published.getMessage().contains("pacs is down"));
        assertEquals(parentEvent.getStudyId(), published.getStudyId());
    }

    /**
     * A direct deletion already has an event, that used to be left in progress forever on failure.
     */
    @Test
    void directDeletionFailsItsOwnEventWhenItFails() throws Exception {
        doThrow(new ShanoirException("pacs is down")).when(dicomWebService).rejectDatasetFromPacs(any());

        try (MockedStatic<KeycloakUtil> keycloak = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloak.when(KeycloakUtil::getTokenUserId).thenReturn(1L);

            assertThrows(ShanoirException.class,
                    () -> service.deleteDatasetFilesFromDiskAndPacsAsync(onePacsFile(), true, DATASET_ID, null));
        }

        ShanoirEvent published = lastPublishedEvent();
        assertEquals(ShanoirEvent.ERROR, published.getStatus());
        assertEquals(String.valueOf(DATASET_ID), published.getObjectId());
    }
}
