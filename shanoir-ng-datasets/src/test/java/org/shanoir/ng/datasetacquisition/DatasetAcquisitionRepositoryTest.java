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

package org.shanoir.ng.datasetacquisition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.apache.commons.math3.util.Pair;

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
public class DatasetAcquisitionRepositoryTest {
	
	@Autowired
	private DatasetAcquisitionRepository repository;

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
		Iterable<DatasetAcquisition> acquisitionsDb = repository.findAll();
		assertThat(acquisitionsDb).isNotNull();
		List<DatasetAcquisition> list = Utils.toList(acquisitionsDb);
		assertThat(list.size()).isEqualTo(3);
		assertEquals("Mr", list.get(0).getType());
		assertEquals("Pet", list.get(1).getType());
		assertEquals("Ct", list.get(2).getType());
	}
	
	@Test
	public void findByExaminationByStudyCenterOrStudyIdInTest() throws Exception {
		List<Pair<Long, Long>> studyCentersList = new ArrayList<>();
		studyCentersList.add(new Pair<Long, Long>(1L, 1L));
		Set<Long> studyIds = new HashSet<>();
		studyIds.add(3L);
		
		List<Order> orders = new ArrayList<Order>();
		orders.add(new Order(Direction.ASC, "acquisitionEquipmentId"));
		Pageable pageable = PageRequest.of(0, 10, Sort.by(orders));
		
		Page<DatasetAcquisition> pageDB = repository.findByExaminationByStudyCenterOrStudyIdIn(studyCentersList, studyIds, pageable);
		assertEquals(2, pageDB.getNumberOfElements());
	}
}
