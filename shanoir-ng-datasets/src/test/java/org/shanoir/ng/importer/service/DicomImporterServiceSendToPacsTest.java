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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dicom.web.service.DICOMWebService;

/**
 * sendToPacs reports how many bytes it sent: the import adds that to the size of the
 * dataset expression, because a STOW-RS import never sees the file on disk.
 */
@ExtendWith(MockitoExtension.class)
public class DicomImporterServiceSendToPacsTest {

    @Mock
    private DICOMWebService dicomWebService;

    @InjectMocks
    private DicomImporterService dicomImporterService;

    @Test
    public void sendToPacsReturnsTheSizeOfTheFileItSent() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.9.7133.1");
        dataset.setString(Tag.Modality, VR.CS, "MR");
        dataset.setString(Tag.PatientName, VR.PN, "CT_THX_ABDO_000001");
        Attributes meta = dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian);
        AtomicReference<byte[]> sent = new AtomicReference<>();
        doAnswer(invocation -> {
            sent.set(((InputStream) invocation.getArgument(0)).readAllBytes());
            return null;
        }).when(dicomWebService).sendDicomInputStreamToPacs(any());

        long size = dicomImporterService.sendToPacs(meta, dataset);

        assertTrue(size > 0);
        assertEquals(sent.get().length, size);
    }

}
