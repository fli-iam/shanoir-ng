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

package org.shanoir.ng.preclinical.examination_extradata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.extra_data.ExtraDataBaseRepository;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.utils.ExtraDataModelUtil;
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
 * Tests for repository 'examination extradata'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class ExtraDataRepositoryTest {

	private static final Long EXTRADATA_TEST_1_ID = 1L;
	private static final Long EXAMINATION_ID = 1L;

	@Autowired
	private ExtraDataBaseRepository<ExaminationExtraData> repository;

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
		Iterable<ExaminationExtraData> extradataDb = repository.findAll();
		assertThat(extradataDb).isNotNull();
		int nbTemplates = 0;
		Iterator<ExaminationExtraData> extradataIt = extradataDb.iterator();
		while (extradataIt.hasNext()) {
			extradataIt.next();
			nbTemplates++;
		}
		assertThat(nbTemplates).isEqualTo(3);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findAllByExaminationIdTest() throws Exception {
		List<ExaminationExtraData> extradataDb = repository.findAllByExaminationId(EXAMINATION_ID);
		assertNotNull(extradataDb);
		assertThat(extradataDb.size()).isEqualTo(3);
		assertThat(extradataDb.get(0).getId()).isEqualTo(EXTRADATA_TEST_1_ID);
	}

	@Test
	public void findOneTest() throws Exception {
		ExaminationExtraData extradataDb = repository.findById(EXTRADATA_TEST_1_ID).orElse(null);
		assertThat(extradataDb.getExaminationId()).isEqualTo(EXAMINATION_ID);
		assertThat(extradataDb.getFilename()).isEqualTo(ExtraDataModelUtil.EXTRADATA_FILENAME);
	}

}
