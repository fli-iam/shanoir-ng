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
