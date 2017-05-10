package org.shanoir.ng.studycard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'StudyCard'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class StudyCardRepositoryTest {

	private static final String STUDYCARD_TEST_1_DATA = "StudyCard1";
	private static final Long STUDYCARD_TEST_1_ID = 1L;
	private static final Long STUDY_TEST_1_ID = 1L;
	
	@Autowired
	private StudyCardRepository studyCardRepository;
	
	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
	
	@Test
	public void findAllTest() throws Exception {
		Iterable<StudyCard> studyCardDb = studyCardRepository.findAll();
		assertThat(studyCardDb).isNotNull();
		int nbStudyCard = 0;
		Iterator<StudyCard> studyCardIt = studyCardDb.iterator();
		while (studyCardIt.hasNext()) {
			studyCardIt.next();
			nbStudyCard++;
		}
		assertThat(nbStudyCard).isEqualTo(4);
	}
	
	@Test
	public void findByTest() throws Exception {
		List<StudyCard> studyCardDb = studyCardRepository.findBy("name", STUDYCARD_TEST_1_DATA);
		assertNotNull(studyCardDb);
		assertThat(studyCardDb.size()).isEqualTo(1);
		assertThat(studyCardDb.get(0).getId()).isEqualTo(STUDYCARD_TEST_1_ID);
	}
	
	@Test
	public void findByStudyIdInTest() throws Exception {
		List<StudyCard> studyCards = studyCardRepository.findByStudyIdIn(Arrays.asList(STUDY_TEST_1_ID));
		assertNotNull(studyCards);
		assertTrue(studyCards.size() == 2);
	}
	
	@Test
	public void findOneTest() throws Exception {
		StudyCard studyCardDb = studyCardRepository.findOne(STUDYCARD_TEST_1_ID);
		assertThat(studyCardDb.getName()).isEqualTo(STUDYCARD_TEST_1_DATA);
	}
	
}
