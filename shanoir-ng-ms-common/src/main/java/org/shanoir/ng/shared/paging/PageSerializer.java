package org.shanoir.ng.shared.paging;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.PageImpl;

@JsonComponent
public class PageSerializer extends StdSerializer<PageImpl> {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(PageSerializer.class);

	public PageSerializer() {
		super(PageImpl.class);
	}

	@Override
	public void serialize(PageImpl value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField("number", value.getNumber());
		gen.writeNumberField("numberOfElements", value.getNumberOfElements());
		gen.writeNumberField("totalElements", value.getTotalElements());
		gen.writeNumberField("totalPages", value.getTotalPages());
		gen.writeNumberField("size", value.getSize());
		gen.writeFieldName("content");
		provider.defaultSerializeValue(value.getContent(), gen);
		gen.writeEndObject();
	}

}