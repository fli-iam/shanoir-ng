package org.shanoir.studycard.model;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

/**
 * @author msimon
 *
 */
public class StudyCardTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws Exception {
        final StudyCard studyCard = new StudyCard(1, "test", false, 1L, 1L, 1L, 1L);

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/studyCard.json"), StudyCard.class));

        assertThat(MAPPER.writeValueAsString(studyCard)).isEqualTo(expected);
    }
    
    @Test
    public void deserializesFromJSON() throws Exception {
        final StudyCard studyCard = new StudyCard(1, "test", false, 1L, 1L, 1L, 1L);
        assertThat(MAPPER.readValue(fixture("fixtures/studyCard.json"), StudyCard.class))
                .isEqualToComparingFieldByField(studyCard);
    }
    
}
