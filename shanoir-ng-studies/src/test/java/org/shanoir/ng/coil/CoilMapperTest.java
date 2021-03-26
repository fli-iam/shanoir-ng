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

package org.shanoir.ng.coil;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.coil.dto.CoilDTO;
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Coil mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CoilMapperTest {

	private static final Long COIL_ID = 1L;
	private static final String COIL_NAME = "test";

	@Autowired
	private CoilMapper coilMapper;

	@Test
	public void coilsToCoilDTOsTest() {
		final List<CoilDTO> coilDTOs = coilMapper.coilsToCoilDTOs(Arrays.asList(createCoil()));
		Assert.assertNotNull(coilDTOs);
		Assert.assertTrue(coilDTOs.size() == 1);
		Assert.assertTrue(coilDTOs.get(0).getId().equals(COIL_ID));
	}

	@Test
	public void coilToCoilDTOTest() {
		final CoilDTO coilDTO = coilMapper.coilToCoilDTO(createCoil());
		Assert.assertNotNull(coilDTO);
		Assert.assertTrue(coilDTO.getId().equals(COIL_ID));
	}

	private Coil createCoil() {
		final Coil coil = new Coil();
		coil.setId(COIL_ID);
		coil.setName(COIL_NAME);
		return coil;
	}

}
