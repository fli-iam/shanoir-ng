package org.shanoir.ng.examination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'examination'.
 * 
 * @author ifakhfakh
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class ExaminationRepositoryTest {

	private static final String EXAMINATION_TEST_1_NOTE = "examination1";
	private static final Long EXAMINATION_TEST_1_ID = 1L;
	private static final Long STUDY_TEST_1_ID = 1L;
	private static final Long SUBJECT_TEST_1_ID = 1L;

	@Autowired
	private ExaminationRepository repository;

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
	public void countByStudyIdIn() throws Exception {
		long nbExaminationsDb = repository.countByStudyIdIn(Arrays.asList(STUDY_TEST_1_ID));
		assertThat(nbExaminationsDb).isEqualTo(3);
	}

	@Test
	public void findAllTest() throws Exception {
		Iterable<Examination> examinationsDb = repository.findAll();
		assertThat(examinationsDb).isNotNull();
		int nbTemplates = 0;
		Iterator<Examination> examinationsIt = examinationsDb.iterator();
		while (examinationsIt.hasNext()) {
			examinationsIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	public void findByStudyIdIn() throws Exception {
		List<Examination> examinationsDb = repository.findByStudyIdIn(Arrays.asList(STUDY_TEST_1_ID), null);
		assertThat(examinationsDb.size()).isEqualTo(3);
	}

	@Test
	public void findByStudyIdInPageable() throws Exception {
		Pageable pageable = new PageRequest(0, 2);
		List<Examination> examinationsDb = repository.findByStudyIdIn(Arrays.asList(STUDY_TEST_1_ID), pageable);
		assertThat(examinationsDb.size()).isEqualTo(2);
	}

	@Test
	public void findBySubjectId() throws Exception {
		List<Examination> examinationsDb = repository.findBySubjectId(SUBJECT_TEST_1_ID);
		assertThat(examinationsDb.size()).isEqualTo(1);
		assertThat(examinationsDb.get(0).getId()).isEqualTo(EXAMINATION_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		Examination examinationDb = repository.findOne(EXAMINATION_TEST_1_ID);
		assertThat(examinationDb.getNote()).isEqualTo(EXAMINATION_TEST_1_NOTE);
	}

}
