package org.shanoir.ng.subject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
 * Tests for repository 'Subject'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class SubjectRepositoryTest {

	private static final String SUBJECT_TEST_1_DATA = "subject1";
	private static final Long SUBJECT_TEST_1_ID = 1L;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
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
		Iterable<Subject> subjectDb = subjectRepository.findAll();
		assertThat(subjectDb).isNotNull();
		int nbSubject = 0;
		Iterator<Subject> subjectIt = subjectDb.iterator();
		while (subjectIt.hasNext()) {
			subjectIt.next();
			nbSubject++;
		}
		assertThat(nbSubject).isEqualTo(4);
	}
	
	@Test
	public void findByTest() throws Exception {
		List<Subject> subjectDb = subjectRepository.findBy("name", SUBJECT_TEST_1_DATA);
		assertNotNull(subjectDb);
		assertThat(subjectDb.size()).isEqualTo(1);
		assertThat(subjectDb.get(0).getId()).isEqualTo(SUBJECT_TEST_1_ID);
	}
	
	@Test
	public void findByDataTest() throws Exception {
		Optional<Subject> subjectDb = subjectRepository.findByName(SUBJECT_TEST_1_DATA);
		assertTrue(subjectDb.isPresent());
		assertThat(subjectDb.get().getId()).isEqualTo(SUBJECT_TEST_1_ID);
	}
	
	@Test
	public void findOneTest() throws Exception {
		Subject subjectDb = subjectRepository.findOne(SUBJECT_TEST_1_ID);
		assertThat(subjectDb.getName()).isEqualTo(SUBJECT_TEST_1_DATA);
	}
	
}
