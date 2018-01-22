package org.shanoir.ng.shared.jackson;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * LocalDate deserializer.
 * 
 * @author msimon
 *
 */
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 4250848415804431827L;

	/**
	 * Default constructor.
	 */
	public LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final String dateStr = p.readValueAs(String.class);
		return ZonedDateTime.ofInstant(Instant.parse(dateStr), ZoneId.systemDefault()).toLocalDate();
	}

}
