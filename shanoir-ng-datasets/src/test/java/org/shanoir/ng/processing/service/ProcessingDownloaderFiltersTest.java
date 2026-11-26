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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * The filters of a complex massive download come from the client: their types can only be columns of the query, and
 * their values are bound as parameters, never written in the SQL.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessingDownloaderFiltersTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private EntityManager em;

    @InjectMocks
    private ProcessingDownloaderServiceImpl service;

    private Query query;

    @BeforeEach
    void setUp() {
        query = mock(Query.class);
        given(em.createNativeQuery(anyString())).willReturn(query);
        given(query.getResultList()).willReturn(new ArrayList<>(List.of(7L)));
    }

    /** An extraction with one filter per type/value pair. */
    private static JsonNode extraction(Object... typesAndValues) {
        ObjectNode extraction = MAPPER.createObjectNode().put("extraction_identifier", "e1");
        ArrayNode filters = extraction.putArray("filter");
        for (int i = 0; i < typesAndValues.length; i += 2) {
            ObjectNode filter = filters.addObject().put("type", (String) typesAndValues[i]);
            Object value = typesAndValues[i + 1];
            if (value instanceof Boolean b) {
                filter.put("value", b);
            } else {
                filter.put("value", (String) value);
            }
        }
        return extraction;
    }

    private String sql() {
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(em).createNativeQuery(sql.capture());
        return sql.getValue();
    }

    @Test
    void theValuesAreBoundAsParameters() throws Exception {
        List<Long> ids = service.getDatasetIdsFromJsonFilters(extraction(
                "processing.pipeline_identifier", "first,second",
                "dataset.id", "1, 2",
                "examination.examination_date", ">= '2024-01-01'",
                "processing.processing_date", "BETWEEN 2024-01-01 AND '2024-02-01 12:30'",
                "processing.pipeline_identifier", "third"));

        assertEquals(List.of(7L), ids);
        String sql = sql();
        assertTrue(sql.contains("(monitoring.pipeline_identifier REGEXP :filter0 OR monitoring.pipeline_identifier REGEXP :filter5)"), sql);
        assertTrue(sql.contains("(dataset.id IN (:filter1))"), sql);
        assertTrue(sql.contains("(examination.examination_date >= :filter2)"), sql);
        assertTrue(sql.contains("(processing.processing_date BETWEEN :filter3 AND :filter4)"), sql);
        for (String value : List.of("first", "second", "third", "2024")) {
            assertFalse(sql.contains(value), sql);
        }
        verify(query).setParameter("filter0", "first|second");
        verify(query).setParameter("filter1", List.of(1L, 2L));
        verify(query).setParameter("filter2", "2024-01-01");
        verify(query).setParameter("filter3", "2024-01-01");
        verify(query).setParameter("filter4", "2024-02-01 12:30");
        verify(query).setParameter("filter5", "third");
    }

    @Test
    void aBooleanValueIsWrittenAsIsTrueOrFalse() throws Exception {
        service.getDatasetIdsFromJsonFilters(extraction("dataset.downloadable", true, "processing.deleted", false));

        String sql = sql();
        assertTrue(sql.contains("(dataset.downloadable IS TRUE) AND (processing.deleted IS FALSE)"), sql);
    }

    @Test
    void aQuoteInAValueStaysInTheParameter() throws Exception {
        String injection = "x' OR '1'='1";
        service.getDatasetIdsFromJsonFilters(extraction("processing.comment", injection));

        assertFalse(sql().contains("'1'='1"));
        verify(query).setParameter("filter0", injection);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dataset.id) OR (1=1",
        "dataset.id OR 1=1 --",
        "dataset.id;DROP TABLE dataset",
        "users.password",
        "dataset",
        "dataset.id.name",
        "(SELECT 1).id",
    })
    void anyOtherFilterTypeIsRefused(String type) {
        assertThrows(IllegalArgumentException.class,
                () -> service.getDatasetIdsFromJsonFilters(extraction(type, "1", "processing.comment", "x")));
        verify(em, never()).createNativeQuery(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "> '2024-01-01' OR 1=1",
        "> 2024-01-01; DROP TABLE dataset",
        "> SLEEP(5)",
        "2024-01-01",
        "LIKE '2024%'",
        "BETWEEN 2024-01-01 AND NOW()",
    })
    void anyOtherDateFilterIsRefused(String value) {
        assertThrows(IllegalArgumentException.class,
                () -> service.getDatasetIdsFromJsonFilters(extraction("processing.processing_date", value)));
        verify(em, never()).createNativeQuery(anyString());
    }

    @Test
    void theInputsAreReadWithTheIdsBound() throws Exception {
        ObjectNode extraction = (ObjectNode) extraction("processing.comment", "x");
        extraction.put("input", true);
        Query inputQuery = mock(Query.class);
        given(em.createNativeQuery(anyString())).willReturn(query, inputQuery);
        given(inputQuery.getResultList()).willReturn(new ArrayList<>(List.of(8L, 7L)));

        assertEquals(List.of(7L, 8L), service.getDatasetIdsFromJsonFilters(extraction));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(em, org.mockito.Mockito.times(2)).createNativeQuery(sql.capture());
        assertTrue(sql.getAllValues().get(1).endsWith("WHERE dataset.id IN (:ids)"), sql.getAllValues().get(1));
        verify(inputQuery).setParameter("ids", List.of(7L));
    }

}
