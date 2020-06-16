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

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.solr.core.query.result.SolrResultPage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonComponent
public class SolrPageSerializer extends StdSerializer<SolrResultPage> {
	
	private static final long serialVersionUID = 1L;

	public SolrPageSerializer() {
		super(SolrResultPage.class);
	}

	@Override
	public void serialize(SolrResultPage value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField("number", value.getNumber());
		gen.writeNumberField("numberOfElements", value.getNumberOfElements());
		gen.writeNumberField("totalElements", value.getTotalElements());
		gen.writeNumberField("totalPages", value.getTotalPages());
		gen.writeNumberField("size", value.getSize());
		gen.writeObjectField("facetResultPages", value.getFacetResultPages());
		gen.writeFieldName("content");
		provider.defaultSerializeValue(value.getContent(), gen);
		gen.writeEndObject();
	}

}