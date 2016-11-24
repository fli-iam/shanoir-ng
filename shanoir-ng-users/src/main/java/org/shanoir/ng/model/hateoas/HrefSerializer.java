package org.shanoir.ng.model.hateoas;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom conversion of HRef to a json representation
 *
 * @author jlouis
 */
public class HrefSerializer extends JsonSerializer<HRef> {

    @Override
    public void serialize(HRef href,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
                          throws IOException, JsonProcessingException {

    	jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("href", href.getHref());
        jsonGenerator.writeEndObject();
    }
}