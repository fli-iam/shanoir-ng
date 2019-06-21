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

package org.shanoir.ng.manufacturermodel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Manufacturer model mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ManufacturerModelMapperTest {

	private static final String MANUFACTURER_MODEL_NAME = "test";

	@Autowired
	private ManufacturerModelMapper manufacturerModelMapper;

	@Test
	public void centersToCenterDTOsTest() {
		final ManufacturerModelDTO manufacturerModelDTO = manufacturerModelMapper.manufacturerModelToManufacturerModelDTO(createManufacturerModel());
		Assert.assertNotNull(manufacturerModelDTO);
		Assert.assertTrue(manufacturerModelDTO.getName().equals(MANUFACTURER_MODEL_NAME));
	}

	private ManufacturerModel createManufacturerModel() {
		final ManufacturerModel center = new ManufacturerModel();
		center.setName(MANUFACTURER_MODEL_NAME);
		return center;
	}

}
