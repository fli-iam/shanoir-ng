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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.shared.dicom.EchoTime;
import org.shanoir.ng.shared.dicom.SerieToDatasetsSeparator;

/**
 * The separation key rebuilt from a stored dataset must equal the key of an
 * incoming instance of the same series, otherwise STOW opens one dataset per
 * instance.
 */
class DatasetSeparationHelperTest {

    private static final double[] IOP = new double[] {1, 0, 0, 0, 1, 0};

    @Test
    void ctStoredKeyMatchesIncomingKeyEvenWithEchoTags() {
        CtDatasetAcquisition acquisition = new CtDatasetAcquisition();
        Attributes slice = slice();
        slice.setInt(Tag.EchoNumbers, VR.IS, 1);
        slice.setDouble(Tag.EchoTime, VR.DS, 5.0);

        Set<EchoTime> stored = DatasetSeparationHelper.echoTimesForDatasetSeparation(acquisition, new CtDataset());
        Set<EchoTime> incoming = DatasetSeparationHelper.echoTimesForDatasetSeparation(acquisition, slice);

        assertTrue(stored.isEmpty());
        assertTrue(incoming.isEmpty());
        assertSameKey(stored, incoming);
    }

    @Test
    void mrStoredKeyMatchesIncomingKey() {
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        MrDataset dataset = mrDatasetWithEcho(2, 12.34);
        Attributes slice = slice();
        slice.setInt(Tag.EchoNumbers, VR.IS, 2);
        slice.setDouble(Tag.EchoTime, VR.DS, 12.34);

        Set<EchoTime> stored = DatasetSeparationHelper.echoTimesForDatasetSeparation(acquisition, dataset);
        Set<EchoTime> incoming = DatasetSeparationHelper.echoTimesForDatasetSeparation(acquisition, slice);

        assertEquals(1, incoming.size());
        assertSameKey(stored, incoming);
    }

    @Test
    void mrOtherEchoStillOpensAnotherDataset() {
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        MrDataset dataset = mrDatasetWithEcho(1, 5.0);
        Attributes slice = slice();
        slice.setInt(Tag.EchoNumbers, VR.IS, 2);
        slice.setDouble(Tag.EchoTime, VR.DS, 12.34);

        Set<EchoTime> stored = DatasetSeparationHelper.echoTimesForDatasetSeparation(acquisition, dataset);
        Set<EchoTime> incoming = DatasetSeparationHelper.echoTimesForDatasetSeparation(acquisition, slice);

        assertNotEquals(key(stored), key(incoming));
    }

    private static void assertSameKey(Set<EchoTime> stored, Set<EchoTime> incoming) {
        assertEquals(key(stored), key(incoming));
        assertEquals(key(stored).hashCode(), key(incoming).hashCode());
    }

    private static SerieToDatasetsSeparator key(Set<EchoTime> echoTimes) {
        return new SerieToDatasetsSeparator(1, echoTimes, IOP);
    }

    private static MrDataset mrDatasetWithEcho(int echoNumber, double echoTimeValue) {
        org.shanoir.ng.shared.model.EchoTime echoTime = new org.shanoir.ng.shared.model.EchoTime();
        echoTime.setEchoNumber(echoNumber);
        echoTime.setEchoTimeValue(echoTimeValue);
        MrDataset dataset = new MrDataset();
        dataset.setEchoTime(new ArrayList<>(List.of(echoTime)));
        return dataset;
    }

    private static Attributes slice() {
        Attributes attributes = new Attributes();
        attributes.setInt(Tag.AcquisitionNumber, VR.IS, 1);
        attributes.setDouble(Tag.ImageOrientationPatient, VR.DS, IOP);
        return attributes;
    }

}
