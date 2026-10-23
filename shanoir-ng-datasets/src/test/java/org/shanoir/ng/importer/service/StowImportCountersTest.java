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

package org.shanoir.ng.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetProtocol;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * The two counters a STOW-RS import grows with every accepted instance:
 * the slice count of the acquisition protocol and the size of the expression.
 * They are UPDATE statements, so they are checked against a real (H2) database
 * and read back from it, not from the persistence context.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(StowImportCounters.class)
public class StowImportCountersTest {

    @Autowired
    private StowImportCounters counters;

    @Autowired
    private TestEntityManager em;

    private <T> T reload(Class<T> type, Long id) {
        em.flush();
        em.clear();
        return em.find(type, id);
    }

    @Test
    public void mrSlicesStartAtZeroAndGrowByOnePerInstance() {
        MrProtocol protocol = new MrProtocol();
        protocol.setNumberOfSlices(0);
        Long id = em.persistAndGetId(protocol, Long.class);
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        acquisition.setMrProtocol(protocol);

        assertTrue(counters.addInstanceToAcquisition(acquisition));
        assertTrue(counters.addInstanceToAcquisition(acquisition));
        assertTrue(counters.addInstanceToAcquisition(acquisition));

        assertEquals(3, reload(MrProtocol.class, id).getNumberOfSlices());
    }

    @Test
    public void anUnsetSliceCountIsTreatedAsZero() {
        MrProtocol protocol = new MrProtocol();
        Long id = em.persistAndGetId(protocol, Long.class);
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        acquisition.setMrProtocol(protocol);

        counters.addInstanceToAcquisition(acquisition);

        assertEquals(1, reload(MrProtocol.class, id).getNumberOfSlices());
    }

    @Test
    public void ctAndXaProtocolsAreCountedToo() {
        CtProtocol ctProtocol = new CtProtocol();
        ctProtocol.setNumberOfSlices(0);
        Long ctId = em.persistAndGetId(ctProtocol, Long.class);
        CtDatasetAcquisition ct = new CtDatasetAcquisition();
        ct.setCtProtocol(ctProtocol);
        XaProtocol xaProtocol = new XaProtocol();
        xaProtocol.setNumberOfSlices(0);
        Long xaId = em.persistAndGetId(xaProtocol, Long.class);
        XaDatasetAcquisition xa = new XaDatasetAcquisition();
        xa.setXaProtocol(xaProtocol);

        counters.addInstanceToAcquisition(ct);
        counters.addInstanceToAcquisition(ct);
        counters.addInstanceToAcquisition(xa);

        assertEquals(2, reload(CtProtocol.class, ctId).getNumberOfSlices());
        assertEquals(1, reload(XaProtocol.class, xaId).getNumberOfSlices());
    }

    /**
     * A PET protocol starts from DICOM tag (0054,0081) when the sender sets it;
     * adding one per instance would count every slice on top of that.
     */
    @Test
    public void petSlicesAreLeftAlone() {
        PetProtocol protocol = new PetProtocol();
        protocol.setNumberOfSlices(47);
        // the mandatory fields of a PET protocol
        protocol.setDimensionX(128);
        protocol.setDimensionY(128);
        protocol.setVoxelSizeX(2.0);
        protocol.setVoxelSizeY(2.0);
        protocol.setVoxelSizeZ(2.0);
        Long id = em.persistAndGetId(protocol, Long.class);
        PetDatasetAcquisition acquisition = new PetDatasetAcquisition();
        acquisition.setPetProtocol(protocol);

        assertFalse(counters.addInstanceToAcquisition(acquisition));

        assertEquals(47, reload(PetProtocol.class, id).getNumberOfSlices());
    }

    @Test
    public void anAcquisitionWithoutAProtocolIsSkipped() {
        assertFalse(counters.addInstanceToAcquisition(new MrDatasetAcquisition()));
        assertFalse(counters.addInstanceToAcquisition(null));
    }

    @Test
    public void expressionSizeGrowsFromUnsetByTheBytesOfEachInstance() {
        DatasetExpression expression = new DatasetExpression();
        expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        Long id = em.persistAndGetId(expression, Long.class);
        assertNull(reload(DatasetExpression.class, id).getSize());

        assertTrue(counters.addBytesToExpression(expression, 524_288));
        assertTrue(counters.addBytesToExpression(expression, 1_000));

        assertEquals(525_288L, reload(DatasetExpression.class, id).getSize());
    }

    @Test
    public void nothingIsAddedForAnEmptyInstanceOrAnUnsavedExpression() {
        DatasetExpression saved = new DatasetExpression();
        saved.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        Long id = em.persistAndGetId(saved, Long.class);

        assertFalse(counters.addBytesToExpression(saved, 0));
        assertFalse(counters.addBytesToExpression(saved, -5));
        assertFalse(counters.addBytesToExpression(new DatasetExpression(), 10));
        assertFalse(counters.addBytesToExpression(null, 10));

        assertNull(reload(DatasetExpression.class, id).getSize());
    }

}
