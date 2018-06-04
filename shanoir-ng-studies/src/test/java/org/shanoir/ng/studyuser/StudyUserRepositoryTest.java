package org.shanoir.ng.studyuser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@DataJpaTest
@ActiveProfiles("test")
public class StudyUserRepositoryTest {

	private static final Long STUDY_TEST_1_ID = 1L;
	private static final Long USER_TEST_1_ID = 2L;

	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;

	@Autowired
	private StudyUserRepository studyUserRepository;

	@Test
	public void findByStudyIdAndUserIdTest() throws Exception {
		StudyUser studyUser = studyUserRepository.findByStudyIdAndUserId(STUDY_TEST_1_ID, USER_TEST_1_ID);
		assertNotNull(studyUser);
		assertThat(studyUser.getStudyId()).isEqualTo(STUDY_TEST_1_ID);
		assertThat(studyUser.getUserId()).isEqualTo(USER_TEST_1_ID);
	}

}
