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

package org.shanoir.ng.role;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.role.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for repository 'role'.
 *
 * @author msimon
 *
 */
@DataJpaTest
@ActiveProfiles("test")
public class RoleRepositoryTest {

	private static final Long ROLE_TEST_1_ID = 1L;
	private static final String ROLE_TEST_1_NAME = "ROLE_ADMIN";
	
	@Autowired
	private RoleRepository repository;
	
	@Test
	public void findOneTest() throws Exception {
		final Role roleDb = repository.findById(ROLE_TEST_1_ID).orElse(null);
		assertThat(roleDb.getName()).isEqualTo(ROLE_TEST_1_NAME);
	}
	
	@Test
	public void getAllNamesTest() throws Exception {
		final List<String> rolesName = repository.getAllNames();
		assertThat(rolesName.size()).isEqualTo(3);
	}
	
}
