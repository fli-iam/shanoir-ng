package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'examination anesthetic'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class ExaminationAnestheticRepositoryTest {

	private static final Long EXAM_ANESTHETIC_TEST_1_ID = 1L;
	private static final Long ANESTHETIC_ID = 1L;

	@Autowired
	private ExaminationAnestheticRepository repository;

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
		Iterable<ExaminationAnesthetic> examAnestheticsDb = repository.findAll();
		assertThat(examAnestheticsDb).isNotNull();
		int nbTemplates = 0;
		Iterator<ExaminationAnesthetic> examAnestheticsIt = examAnestheticsDb.iterator();
		while (examAnestheticsIt.hasNext()) {
			examAnestheticsIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	public void findAllByExaminationTest() throws Exception {
		List<ExaminationAnesthetic> examAnestheticsDb = repository.findByExaminationId(1L);
		assertNotNull(examAnestheticsDb);
		assertThat(examAnestheticsDb.size()).isEqualTo(1);
		assertThat(examAnestheticsDb.get(0).getId()).isEqualTo(EXAM_ANESTHETIC_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		ExaminationAnesthetic examAnestheticDb = repository.findOne(EXAM_ANESTHETIC_TEST_1_ID);
		assertThat(examAnestheticDb.getAnesthetic().getId()).isEqualTo(ANESTHETIC_ID);
		assertThat(examAnestheticDb.getAnesthetic().getName()).isEqualTo(AnestheticModelUtil.ANESTHETIC_NAME);
	}

}
