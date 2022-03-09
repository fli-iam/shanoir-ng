package org.shanoir.ng.shared.dateTime;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * LocalDateSerializer: custom serializer, written for the deployment
 * of the Spring Boot 2 migrated code. The migration changed the default
 * serializer of the subject birthdate from [1900,1,1] to 1900-01-01,
 * what leads to errors within ShanoirUploader and the current version
 * installed in many hospitals. To progress with the migration and deploy
 * the new version, a "fix" is provided on the server side. Later, when
 * a new version of ShUp is deployed larger, this can be made undone.
 * 
 * @author mkain
 *
 */
public class LocalDateSerializer extends JsonSerializer<LocalDate> {

	@Override
	public void serialize(LocalDate date, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();
        generator.writeNumber(date.getYear());
        generator.writeNumber(date.getMonthValue());
        generator.writeNumber(date.getDayOfMonth());
        generator.writeEndArray();
	}

}
