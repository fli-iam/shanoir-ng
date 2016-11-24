package org.shanoir.ng.model.hateoas;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom conversion of Link to a json representation
 *
 * @author jlouis
 */
public class LinkSerializer extends JsonSerializer<Link> {

    @Override
    public void serialize(Link link,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
                          throws IOException, JsonProcessingException {


        jsonGenerator.writeObjectField(link.getRel(), link.getHref());
    }
}