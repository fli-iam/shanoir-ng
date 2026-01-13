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

package org.shanoir.ng.shared.paging;

import java.io.IOException;

import org.springframework.boot.jackson.JacksonComponent;

import tools.jackson.databind.SerializationContext;
import org.springframework.data.domain.PageImpl;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ser.std.StdSerializer;

@JacksonComponent
public class PageSerializer extends StdSerializer<PageImpl> {

    private static final long serialVersionUID = 1L;

    public PageSerializer() {
        super(PageImpl.class);
    }

    @Override
    public void serialize(PageImpl value, JsonGenerator gen, SerializationContext provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberProperty("number", value.getNumber());
        gen.writeNumberProperty("numberOfElements", value.getNumberOfElements());
        gen.writeNumberProperty("totalElements", value.getTotalElements());
        gen.writeNumberProperty("totalPages", value.getTotalPages());
        gen.writeNumberProperty("size", value.getSize());
        gen.writeName("content");
        provider.defaultSerializeValue(value.getContent(), gen);
        gen.writeEndObject();
    }

}
