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

package org.shanoir.ng.solr.model;

import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.data.solr.core.query.result.SolrResultPage;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

@JacksonComponent(type = SolrResultPage.class)
public class SolrPageSerializer extends StdSerializer<SolrResultPage> {

    public SolrPageSerializer() {
        super(SolrResultPage.class);
    }

    @Override
    public void serialize(SolrResultPage value, JsonGenerator gen, SerializationContext provider) {
        gen.writeStartObject();
        gen.writeNumberProperty("number", value.getNumber());
        gen.writeNumberProperty("numberOfElements", value.getNumberOfElements());
        gen.writeNumberProperty("totalElements", value.getTotalElements());
        gen.writeNumberProperty("totalPages", value.getTotalPages());
        gen.writeNumberProperty("size", value.getSize());
        gen.writePOJOProperty("facetResultPages", value.getFacetResultPages());
        gen.writePOJOProperty("content", value.getContent());
        gen.writeEndObject();
    }

}
