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

package org.shanoir.ng.center;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentMapper;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Center mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CenterMapperTest {

	private static final Long CENTER_ID = 1L;
	private static final String CENTER_NAME = "test";

	@MockBean
	private AcquisitionEquipmentMapper acquisitionEquipmentMapperMock;

	@Autowired
	private CenterMapper centerMapper;

	@Test
	public void centersToCenterDTOsTest() {
		final List<CenterDTO> centerDTOs = centerMapper.centersToCenterDTOs(Arrays.asList(createCenter()));
		Assert.assertNotNull(centerDTOs);
		Assert.assertTrue(centerDTOs.size() == 1);
		Assert.assertTrue(centerDTOs.get(0).getId().equals(CENTER_ID));
	}

	@Test
	public void centersToIdNameDTOsTest() {
		final List<IdNameDTO> centerDTOs = centerMapper.centersToIdNameDTOs(Arrays.asList(createCenter()));
		Assert.assertNotNull(centerDTOs);
		Assert.assertTrue(centerDTOs.size() == 1);
		Assert.assertTrue(centerDTOs.get(0).getId().equals(CENTER_ID));
	}

	@Test
	public void centerToCenterDTOTest() {
		final CenterDTO centerDTO = centerMapper.centerToCenterDTO(createCenter());
		Assert.assertNotNull(centerDTO);
		Assert.assertTrue(centerDTO.getId().equals(CENTER_ID));
	}

	@Test
	public void centerToIdNameDTOTest() {
		final IdNameDTO centerDTO = centerMapper.centerToIdNameDTO(createCenter());
		Assert.assertNotNull(centerDTO);
		Assert.assertTrue(centerDTO.getId().equals(CENTER_ID));
	}

	private Center createCenter() {
		final Center center = new Center();
		center.setId(CENTER_ID);
		center.setName(CENTER_NAME);
		return center;
	}

}
