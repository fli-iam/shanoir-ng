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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;

/**
 * Only an annotation of a dataset has to be linked to it. A SR or SEG that names
 * no source series, e.g. a radiation dose report, must not fail the request.
 */
class DicomSEGAndSRNeedsSourceDatasetTest {

    private final DicomSEGAndSRImporterService service = new DicomSEGAndSRImporterService();

    @Test
    void srNamingItsSourceSeriesNeedsItsDataset() {
        Attributes sr = new Attributes();
        Attributes evidence = new Attributes();
        evidence.newSequence(Tag.ReferencedSeriesSequence, 1).add(new Attributes());
        sr.newSequence(Tag.CurrentRequestedProcedureEvidenceSequence, 1).add(evidence);

        assertTrue(service.needsSourceDataset(sr, true));
        assertTrue(service.needsSourceDataset(sr, false));
    }

    @Test
    void segNamingItsSourceSeriesNeedsItsDataset() {
        Attributes seg = new Attributes();
        seg.newSequence(Tag.ReferencedSeriesSequence, 1).add(new Attributes());

        assertTrue(service.needsSourceDataset(seg, true));
    }

    @Test
    void srWithoutAnyReferenceNeedsNoDataset() {
        Attributes doseReport = new Attributes();
        doseReport.setString(Tag.StudyInstanceUID, VR.UI, "1.2.826.0.1.1");

        assertFalse(service.needsSourceDataset(doseReport, true));
        assertFalse(service.needsSourceDataset(doseReport, false));
    }

    @Test
    void srWhoseEvidenceNamesNoSeriesNeedsNoDataset() {
        Attributes sr = new Attributes();
        Sequence evidence = sr.newSequence(Tag.CurrentRequestedProcedureEvidenceSequence, 1);
        evidence.add(new Attributes());

        assertFalse(service.needsSourceDataset(sr, true));
    }

    @Test
    void everyObjectOfTheViewerIsLinkedEvenWithoutReference() {
        Attributes fromViewer = new Attributes();
        fromViewer.setString(Tag.StudyInstanceUID, VR.UI,
                StudyInstanceUIDAndSubjectNameHandler.PREFIX + "12");

        assertTrue(service.needsSourceDataset(fromViewer, false));
        assertFalse(service.needsSourceDataset(fromViewer, true));
    }

}
