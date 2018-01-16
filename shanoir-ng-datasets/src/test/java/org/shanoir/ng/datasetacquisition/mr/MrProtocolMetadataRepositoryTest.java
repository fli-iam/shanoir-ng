package org.shanoir.ng.datasetacquisition.mr;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

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
 * Tests for repository 'mrprotocolmetadata'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class MrProtocolMetadataRepositoryTest {

	private static final String MR_PROTOCOL_METADATA_TEST_1_NAME = "MRProtocol1";
	private static final Long MR_PROTOCOL_METADATA_TEST_1_ID = 1L;
	
	@Autowired
	private MrProtocolMetadataRepository repository;
	
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
		Iterable<MrProtocolMetadata> mrProtocolMetadataDb = repository.findAll();
		assertThat(mrProtocolMetadataDb).isNotNull();
		int nbMrProtocolMetadata = 0;
		Iterator<MrProtocolMetadata> mrProtocolMetadataIt = mrProtocolMetadataDb.iterator();
		while (mrProtocolMetadataIt.hasNext()) {
			mrProtocolMetadataIt.next();
			nbMrProtocolMetadata++;
		}
		assertThat(nbMrProtocolMetadata).isEqualTo(1);
	}
	
	@Test
	public void findOneTest() throws Exception {
		MrProtocolMetadata mrProtocolMetadataDb = repository.findOne(MR_PROTOCOL_METADATA_TEST_1_ID);
		assertThat(mrProtocolMetadataDb.getName()).isEqualTo(MR_PROTOCOL_METADATA_TEST_1_NAME);
	}
	
}
