package org.shanoir.ng.shared.jackson;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonUtils {
	
	public static String serialize(Object obj) throws JsonProcessingException {
		SimpleModule module = new SimpleModule();
	    module.addSerializer(LocalDate.class, new LocalDateSerializer());
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.writeValueAsString(obj).toString();
	}
	
}
