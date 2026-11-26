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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.utils.KeycloakUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * The filters of a complex massive download can match any dataset of the instance: the job only lists the datasets
 * the user can download.
 */
@ExtendWith(MockitoExtension.class)
class ProcessingDownloaderComplexDownloadTest {

    @Mock
    private EntityManager em;

    @Mock
    private DatasetSecurityService datasetSecurityService;

    @Mock
    private ShanoirEventService eventService;

    @InjectMocks
    private ProcessingDownloaderServiceImpl service;

    private static JsonNode request(String json) throws Exception {
        return new ObjectMapper().readTree(json);
    }

    @Test
    void theJobListsOnlyTheDatasetsTheUserCanDownload() throws Exception {
        Query query = mock(Query.class);
        given(em.createNativeQuery(anyString())).willReturn(query);
        given(query.getResultList()).willReturn(new ArrayList<>(List.of(1L, 2L, 3L)));
        given(datasetSecurityService.filterDatasetIds(List.of(1L, 2L, 3L), "CAN_DOWNLOAD")).willReturn(List.of(1L));

        try (MockedStatic<KeycloakUtil> keycloakUtil = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(12L);
            service.complexMassiveDownload(request(
                    "{\"data_to_extract\": [{\"extraction_identifier\": \"e1\", "
                    + "\"filter\": [{\"type\": \"processing.pipeline_identifier\", \"value\": \"pipeline\"}]}]}"));
        }

        ArgumentCaptor<ShanoirEvent> event = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(eventService).publishEvent(event.capture());
        assertTrue(event.getValue().getMessage().endsWith(" : 1"), event.getValue().getMessage());
    }

}
