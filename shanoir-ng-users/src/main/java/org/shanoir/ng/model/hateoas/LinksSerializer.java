package org.shanoir.ng.model.hateoas;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom conversion of Links to a json representation
 *
 * @author jlouis
 */
public class LinksSerializer extends JsonSerializer<Links> {

    @Override
    public void serialize(Links links,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
                          throws IOException, JsonProcessingException {

    	jsonGenerator.writeStartObject();
    	for (Link link : links.getLinks()) {
    		jsonGenerator.writeObjectField(link.getRel(), link);
    	}
        jsonGenerator.writeEndObject();
    }
}