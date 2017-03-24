package org.shanoir.ng.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.template.Template;
import org.shanoir.ng.template.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'template'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class TemplateRepositoryTest {

	private static final String TEMPLATE_TEST_1_DATA = "Data1";
	private static final Long TEMPLATE_TEST_1_ID = 1L;
	
	@Autowired
	private TemplateRepository repository;
	
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
		Iterable<Template> templatesDb = repository.findAll();
		assertThat(templatesDb).isNotNull();
		int nbTemplates = 0;
		Iterator<Template> templatesIt = templatesDb.iterator();
		while (templatesIt.hasNext()) {
			templatesIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(4);
	}
	
	@Test
	public void findByTest() throws Exception {
		List<Template> templatesDb = repository.findBy("data", TEMPLATE_TEST_1_DATA);
		assertNotNull(templatesDb);
		assertThat(templatesDb.size()).isEqualTo(1);
		assertThat(templatesDb.get(0).getId()).isEqualTo(TEMPLATE_TEST_1_ID);
	}
	
	@Test
	public void findByDataTest() throws Exception {
		Optional<Template> templateDb = repository.findByData(TEMPLATE_TEST_1_DATA);
		assertTrue(templateDb.isPresent());
		assertThat(templateDb.get().getId()).isEqualTo(TEMPLATE_TEST_1_ID);
	}
	
	@Test
	public void findOneTest() throws Exception {
		Template templateDb = repository.findOne(TEMPLATE_TEST_1_ID);
		assertThat(templateDb.getData()).isEqualTo(TEMPLATE_TEST_1_DATA);
	}
	
}
