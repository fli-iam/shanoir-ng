package org.shanoir.studycard.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.StudyCardService;
import org.shanoir.studycard.service.impl.StudyCardServiceImpl;

import com.google.common.base.Optional;

import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author msimon
 *
 */
public class StudyCardResourceUnitTest {

	private static final StudyCardService service = mock(StudyCardServiceImpl.class);
	
	private static final String STUDY_CARD_PATH = "/studycard";

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
			.addResource(new StudyCardResource(service)).build();

	private final StudyCard studyCard = new StudyCard(1, "test", false, 1L, 1L, 1L, 1L);

	@Before
	public void setup() {
		when(service.findAll()).thenReturn(Arrays.asList(studyCard));
		when(service.findById(1)).thenReturn(Optional.of(studyCard));
		when(service.save(any(StudyCard.class))).thenReturn(studyCard);
		when(service.update(any(StudyCard.class))).thenReturn(studyCard);
	}

	@After
	public void tearDown() {
		// we have to reset the mock after each test because of the
		// @ClassRule, or use a @Rule as mentioned below.
		reset(service);
	}

	@Test
	public void testFindAll() {
		List<StudyCard> studyCards = resources.client().target(STUDY_CARD_PATH).request()
				.get(new GenericType<List<StudyCard>>() {
				});
		assertThat(studyCards).isNotNull();
		assertThat(studyCards).hasSize(1);
		assertThat(studyCards.get(0)).isEqualToComparingFieldByField(studyCard);
		verify(service).findAll();
	}

	@Test
	public void testFindById() {
		assertThat(resources.client().target(STUDY_CARD_PATH + "/1").request().accept(MediaType.APPLICATION_JSON)
				.get(StudyCard.class)).isEqualToComparingFieldByField(studyCard);
		verify(service).findById(1);
	}

	@Test
	public void testSave() {
		assertThat(resources.client().target(STUDY_CARD_PATH).request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(studyCard, MediaType.APPLICATION_JSON), StudyCard.class))
						.isEqualToComparingFieldByField(studyCard);
		verify(service).save(any(StudyCard.class));
	}

	@Test
	public void testUpdate() {
		assertThat(resources.client().target(STUDY_CARD_PATH + "/1").request().accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(studyCard, MediaType.APPLICATION_JSON), StudyCard.class))
						.isEqualToComparingFieldByField(studyCard);
		verify(service).update(any(StudyCard.class));
	}

}
