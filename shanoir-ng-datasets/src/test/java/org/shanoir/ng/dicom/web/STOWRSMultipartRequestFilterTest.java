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


package org.shanoir.ng.dicom.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.importer.service.DicomImporterService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The STOW-RS filter imports the instances of one subject one at a time and runs
 * an instance once more when it lost a race for the center or the equipment.
 */
@ExtendWith(MockitoExtension.class)
class STOWRSMultipartRequestFilterTest {

    private static final String BOUNDARY = "TESTBOUNDARY";

    @Mock
    private DicomImporterService dicomImporterService;

    @Mock
    private DicomSEGAndSRImporterService dicomSEGAndSRImporterService;

    @Spy
    private StowImportLock stowImportLock = new StowImportLock();

    @InjectMocks
    private STOWRSMultipartRequestFilter filter;

    @Test
    void importedInstanceIsAccepted() throws Exception {
        when(dicomImporterService.importDicom(any(), any())).thenReturn(true);

        send(instance("CT"));

        verify(dicomImporterService).importDicom(any(), any());
    }

    @Test
    void failedImportFailsTheRequest() throws Exception {
        when(dicomImporterService.importDicom(any(), any())).thenReturn(false);

        assertThrows(IOException.class, () -> send(instance("CT")));
    }

    @Test
    void duplicateKeyOfASharedCenterIsRetriedOnce() throws Exception {
        when(dicomImporterService.importDicom(any(), any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '4' for key 'PRIMARY'"))
                .thenReturn(true);

        send(instance("CT"));

        verify(dicomImporterService, times(2)).importDicom(any(), any());
    }

    @Test
    void duplicateKeyThatPersistsFailsTheRequest() throws Exception {
        when(dicomImporterService.importDicom(any(), any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '4' for key 'PRIMARY'"));

        assertThrows(IOException.class, () -> send(instance("CT")));

        verify(dicomImporterService, times(2)).importDicom(any(), any());
    }

    @Test
    void lockIsReleasedWhenTheImportFails() throws Exception {
        when(dicomImporterService.importDicom(any(), any())).thenReturn(false).thenReturn(true);

        assertThrows(IOException.class, () -> send(instance("CT")));
        // a second request of the same subject is not blocked by the failed one
        send(instance("CT"));

        verify(dicomImporterService, times(2)).importDicom(any(), any());
    }

    private void send(byte[] dicom) throws Exception {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + BOUNDARY + "\r\nContent-Type: application/dicom\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        body.write(dicom);
        body.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.US_ASCII));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/shanoir-ng/datasets/dicomweb/studies");
        request.setContentType("multipart/related; type=\"application/dicom\"; boundary=" + BOUNDARY);
        request.setContent(body.toByteArray());
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    private static byte[] instance(String modality) throws IOException {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.1.1.1");
        dataset.setString(Tag.StudyInstanceUID, VR.UI, "1.2.826.0.1.1");
        dataset.setString(Tag.PatientName, VR.PN, "subject01");
        dataset.setString(Tag.ClinicalTrialProtocolID, VR.LO, "1");
        dataset.setString(Tag.Modality, VR.CS, modality);
        Attributes fileMeta = new Attributes();
        fileMeta.setString(Tag.MediaStorageSOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        fileMeta.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, "1.2.826.0.1.1.1.1");
        fileMeta.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DicomOutputStream dos = new DicomOutputStream(out, UID.ExplicitVRLittleEndian)) {
            dos.writeDataset(fileMeta, dataset);
        }
        return out.toByteArray();
    }

}
