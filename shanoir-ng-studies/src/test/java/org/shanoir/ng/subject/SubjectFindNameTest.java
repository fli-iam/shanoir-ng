package org.shanoir.ng.subject;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirStudiesApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class SubjectFindNameTest{
	
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
	public void findOfsepLastNameFromRepository() {
		String name=subjectRepository.find("001");
		assertEquals("0010002", name);
	}

	
}
