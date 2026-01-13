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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfiguration {

    /**
     * Allows to configure the Jackson object mapper.
     *
     * @param objectMapper
     *            an instance of {@link ObjectMapper}.
     */
    @Autowired
    public void configureJacksonObjectMapper(final ObjectMapper objectMapper) {objectMapper
                .registerModule(preparePageModule())
                .registerModule(new JavaTimeModule());
    }

    /**
     * Allows to configure a {@link ValueSerializer} for pagination.
     *
     * @return an instance of {@link JacksonModule}.
     */
    private JacksonModule preparePageModule() {
        return new SimpleModule().setSerializerModifier(new MyClassSerializerModifier());
    }

    public class MyClassSerializer extends ValueSerializer<Page> {
        private final ValueSerializer<Object> defaultSerializer;

        public MyClassSerializer(ValueSerializer<Object> defaultSerializer) {
            this.defaultSerializer = defaultSerializer;
        }

        @Override
        public void serialize(@SuppressWarnings("rawtypes") final Page page, final JsonGenerator jsonGenerator,
                final SerializationContext serializers) throws IOException {
            defaultSerializer.serialize(page, jsonGenerator, serializers);
        }
    }

    public class MyClassSerializerModifier extends BeanSerializerModifier {
        @Override
        public ValueSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, ValueSerializer<?> serializer) {
            if (beanDesc.getBeanClass() == Page.class) {
                return new MyClassSerializer((ValueSerializer<Object>) serializer);
            }
            return serializer;
        }
    }

}
