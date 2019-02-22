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

package org.shanoir.ng.acquisitionequipment;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Center service test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AcquisitionEquipmentMapperTest {

	private static final Long ACQ_EQPT_ID = 1L;
	private static final String ACQ_EQPT_SERIAL_NUMBER = "test";

	@Autowired
	private AcquisitionEquipmentMapper acquisitionEquipmentMapper;

	@Test
	public void acquisitionEquipmentsToAcquisitionEquipmentDTOsTest() {
		final List<AcquisitionEquipmentDTO> AcquisitionEquipmentDTOs = acquisitionEquipmentMapper
				.acquisitionEquipmentsToAcquisitionEquipmentDTOs(Arrays.asList(createAcquisitionEquipment()));
		Assert.assertNotNull(AcquisitionEquipmentDTOs);
		Assert.assertTrue(AcquisitionEquipmentDTOs.size() == 1);
		Assert.assertTrue(AcquisitionEquipmentDTOs.get(0).getId().equals(ACQ_EQPT_ID));
	}

	@Test
	public void acquisitionEquipmentToAcquisitionEquipmentDTOTest() {
		final AcquisitionEquipmentDTO AcquisitionEquipmentDTO = acquisitionEquipmentMapper
				.acquisitionEquipmentToAcquisitionEquipmentDTO(createAcquisitionEquipment());
		Assert.assertNotNull(AcquisitionEquipmentDTO);
		Assert.assertTrue(AcquisitionEquipmentDTO.getId().equals(ACQ_EQPT_ID));
	}

	private AcquisitionEquipment createAcquisitionEquipment() {
		final AcquisitionEquipment equipment = new AcquisitionEquipment();
		equipment.setId(ACQ_EQPT_ID);
		equipment.setSerialNumber(ACQ_EQPT_SERIAL_NUMBER);
		return equipment;
	}

}
