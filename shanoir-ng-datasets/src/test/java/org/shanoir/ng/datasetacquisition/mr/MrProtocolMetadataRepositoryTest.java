/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
