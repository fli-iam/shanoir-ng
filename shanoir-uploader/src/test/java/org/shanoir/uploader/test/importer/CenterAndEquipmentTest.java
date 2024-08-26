package org.shanoir.uploader.test.importer;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.test.AbstractTest;

public class CenterAndEquipmentTest extends AbstractTest {
	
	@Test
	public void createCenterTest() throws Exception {
		Center createdCenter = createCenter();
		Assertions.assertNotNull(createdCenter);
	}
	
	@Test
	public void createEquipmentAndFindBySerialNumber() throws Exception {
		Center createdCenter = createCenter();
		AcquisitionEquipment createdEquipment = createEquipment(createdCenter);
		Assertions.assertNotNull(createdEquipment);
		List<AcquisitionEquipment> equipments = shUpClient.findAcquisitionEquipmentsBySerialNumber(createdEquipment.getSerialNumber());
		Assertions.assertNotNull(equipments);
	}
	
}
