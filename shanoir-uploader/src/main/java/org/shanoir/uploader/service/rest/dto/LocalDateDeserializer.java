package org.shanoir.uploader.service.rest.dto;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

	private static final long serialVersionUID = 848760673134109758L;

	protected LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
	    if (jp.isExpectedStartArrayToken()) {
	        jp.nextToken();
	        int year = jp.getIntValue(); 
	        jp.nextToken();
	        int month = jp.getIntValue();
	        jp.nextToken();
	        int day = jp.getIntValue();
	        if (jp.nextToken() != JsonToken.END_ARRAY) {
	            throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after LocalDate ints");
	        }
	        return LocalDate.of(year, month, day);
	    }
	    throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "non array LocalDate");
	}

}