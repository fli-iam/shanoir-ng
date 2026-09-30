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

package org.shanoir.ng;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.paging.PageSerializer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Guards against a regression where the "Found"/"Total"/"Page size" counters shown below
 * paginated admin tables (Manage examinations, Manage dataset acquisitions, ...) stayed
 * stuck at 0 despite the table itself rendering correctly.
 *
 * <p>Root cause: PageSerializer (registered as a JsonComponent) correctly serializes
 * org.shanoir.ng.shared.paging.PageImpl with a flat shape (content/totalElements/
 * numberOfElements/size/... at top level, which is what the Angular Page model reads).
 * But {@code @EnableSpringDataWebSupport} on ShanoirDatasetApplication defaults to
 * PageSerializationMode.VIA_DTO, which installs a Jackson mixin on the whole Page/PageImpl
 * hierarchy that wraps pagination metadata under a nested {@code "page": {...}} object -
 * silently overriding PageSerializer's own output, since org.shanoir.ng.shared.paging.PageImpl
 * extends Spring's own PageImpl. Only PageSerializationMode.DIRECT keeps PageSerializer's
 * flat shape intact.
 */
class ShanoirDatasetApplicationPageSerializationTest {

    @Test
    void applicationDeclaresDirectPageSerializationMode() {
        org.springframework.data.web.config.EnableSpringDataWebSupport annotation =
                ShanoirDatasetApplication.class.getAnnotation(
                        org.springframework.data.web.config.EnableSpringDataWebSupport.class);

        assertTrue(annotation != null, "ShanoirDatasetApplication must declare @EnableSpringDataWebSupport");
        assertTrue(annotation.pageSerializationMode()
                        == org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.DIRECT,
                "pageSerializationMode must stay DIRECT, or Page responses silently switch to a "
                        + "nested \"page\": {...} shape the frontend does not understand");
    }

    @Test
    void pageStaysFlatEvenWithSpringDataWebSupportRegistered() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // Registered the same way ShanoirDatasetApplication.jacksonPageWithJsonViewModule() does.
        objectMapper.registerModule(new SimpleModule().addSerializer(PageImpl.class, new PageSerializer()));
        // Registered the same way @EnableSpringDataWebSupport does, using the mode declared above.
        org.springframework.data.web.config.EnableSpringDataWebSupport annotation =
                ShanoirDatasetApplication.class.getAnnotation(
                        org.springframework.data.web.config.EnableSpringDataWebSupport.class);
        objectMapper.registerModule(
                new SpringDataJacksonConfiguration.PageModule(new SpringDataWebSettings(annotation.pageSerializationMode())));

        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(page));

        assertTrue(json.has("totalElements"), "totalElements must stay at the top level");
        assertFalse(json.has("page"), "pagination metadata must not be nested under a \"page\" sub-object");
    }
}
