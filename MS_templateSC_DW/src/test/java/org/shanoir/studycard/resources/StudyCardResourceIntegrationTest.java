package org.shanoir.studycard.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.shanoir.studycard.StudyCardApplication;
import org.shanoir.studycard.StudyCardConfiguration;
import org.shanoir.studycard.model.StudyCard;

import com.google.common.io.Resources;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;

import io.dropwizard.testing.junit.DropwizardAppRule;

/**
 * @author msimon
 *
 */
public class StudyCardResourceIntegrationTest {

	private static final String STUDY_CARD_URL = "http://localhost:%d/studycard";

	@ClassRule
	public static final DropwizardAppRule<StudyCardConfiguration> RULE = new DropwizardAppRule<>(
			StudyCardApplication.class, resourceFilePath("test-config.yml"));

	protected static Client client;

	private final StudyCard newStudyCard = new StudyCard(0, "test", false, 1L, 1L, 1L, 1L);

	@BeforeClass
	public static void setUp() {
		client = new JerseyClientBuilder().build();
	}

	@AfterClass
	public static void tearDown() {
		JerseyGuiceUtils.reset();
	}

	public static String resourceFilePath(String resourceClassPathLocation) {
		try {
			return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testFindAll() {
		StudyCard sc = client.target(String.format(STUDY_CARD_URL, RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(newStudyCard, MediaType.APPLICATION_JSON), StudyCard.class);

		List<StudyCard> studyCards = client.target(String.format(STUDY_CARD_URL, RULE.getLocalPort())).request()
				.get(new GenericType<List<StudyCard>>() {
				});
		assertThat(studyCards).isNotNull();
		assertThat(studyCards.size()).isGreaterThan(0);
		assertThat(studyCards.get(0).getName()).isEqualTo(newStudyCard.getName());

		client.target(String.format(STUDY_CARD_URL + "/" + sc.getId(), RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON).delete();
	}

	@Test
	public void testFindById() {
		StudyCard sc = client.target(String.format(STUDY_CARD_URL, RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(newStudyCard, MediaType.APPLICATION_JSON), StudyCard.class);

		assertThat(client.target(String.format(STUDY_CARD_URL + "/" + sc.getId(), RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON).get(StudyCard.class).getName()).isEqualTo(newStudyCard.getName());

		client.target(String.format(STUDY_CARD_URL + "/" + sc.getId(), RULE.getLocalPort())).request().delete();
	}

	@Test
	public void testSave() {
		StudyCard sc = client.target(String.format(STUDY_CARD_URL, RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(newStudyCard, MediaType.APPLICATION_JSON), StudyCard.class);

		assertThat(sc.getName()).isEqualTo(newStudyCard.getName());

		client.target(String.format(STUDY_CARD_URL + "/" + sc.getId(), RULE.getLocalPort())).request().delete();
	}

	@Test
	public void testUpdate() {
		StudyCard sc = client.target(String.format(STUDY_CARD_URL, RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(newStudyCard, MediaType.APPLICATION_JSON), StudyCard.class);
		
		final StudyCard updatedStudyCard = new StudyCard(sc.getId(), "test2", false, 1L, 1L, 1L, 1L);

		assertThat(client.target(String.format(STUDY_CARD_URL + "/" + sc.getId(), RULE.getLocalPort())).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(updatedStudyCard, MediaType.APPLICATION_JSON), StudyCard.class).getName())
						.isEqualTo(updatedStudyCard.getName());

		client.target(String.format(STUDY_CARD_URL + "/" + sc.getId(), RULE.getLocalPort())).request().delete();
	}

}
