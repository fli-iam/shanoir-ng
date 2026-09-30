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

package org.shanoir.ng.shared.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Guards against a regression where Page serialization silently fell back to Spring's
 * default (pagination metadata nested under a "page" sub-object) instead of the flat
 * legacy shape (content/totalElements/numberOfElements/size at top level) the Angular
 * frontend's Page model expects - because the custom serializer modifier only ever
 * matched the exact Page interface class, which no concrete Page implementation
 * (e.g. PageImpl) can ever satisfy.
 */
class JacksonConfigurationTest {

    @Test
    void pageSerializesWithFlatTopLevelFields() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        new JacksonConfiguration().configureJacksonObjectMapper(objectMapper);

        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(page));

        assertTrue(json.has("totalElements"),
                "totalElements must be serialized at the top level, not nested under \"page\"");
        assertEquals(5, json.get("totalElements").asInt());
        assertEquals(2, json.get("numberOfElements").asInt());
        assertEquals(2, json.get("size").asInt());
        assertFalse(json.has("page"),
                "pagination metadata must stay flat, not nested under a \"page\" sub-object");
    }
}
