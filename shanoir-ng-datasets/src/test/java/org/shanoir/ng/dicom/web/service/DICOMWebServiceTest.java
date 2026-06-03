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

package org.shanoir.ng.dicom.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DICOMWebServiceTest {

    private static final String SERVER_URL = "http://pacs/dicomweb/studies";

    private DICOMWebService dicomWebService;

    private CloseableHttpClient httpClient;

    @BeforeEach
    void setUp() {
        dicomWebService = new DICOMWebService();
        httpClient = mock(CloseableHttpClient.class);
        ReflectionTestUtils.setField(dicomWebService, "httpClient", httpClient);
        ReflectionTestUtils.setField(dicomWebService, "serverURL", SERVER_URL);
    }

    @Test
    void findInstanceReturnsNonEmptyDicomPart10() throws Exception {
        byte[] pacsDicom = createMinimalDicomPart10("Anonymous");
        mockPacsInstanceResponse(pacsDicom);

        ResponseEntity<?> response = dicomWebService.findInstance("1.2.3", "4.5.6", "7.8.9", null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        byte[] body = getResponseBody(response);
        assertDicomPart10Preamble(body);
        assertEquals(pacsDicom.length, response.getHeaders().getContentLength());
    }

    @Test
    void findInstanceWithSubjectNameReturnsModifiedDicomPart10() throws Exception {
        byte[] pacsDicom = createMinimalDicomPart10("Anonymous");
        mockPacsInstanceResponse(pacsDicom);

        ResponseEntity<?> response = dicomWebService.findInstance("1.2.3", "4.5.6", "7.8.9", "Study Subject");

        assertNotNull(response);
        byte[] body = getResponseBody(response);
        assertDicomPart10Preamble(body);
        try (DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(body))) {
            Attributes attributes = dis.readDataset();
            assertEquals("Study Subject", attributes.getString(Tag.PatientName));
            assertEquals("Study Subject", attributes.getString(Tag.PatientID));
        }
    }

    private void mockPacsInstanceResponse(byte[] dicomBytes) throws Exception {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        ByteArrayEntity entity = new ByteArrayEntity(dicomBytes, null);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
    }

    private static byte[] getResponseBody(ResponseEntity<?> response) {
        ByteArrayResource resource = (ByteArrayResource) response.getBody();
        assertNotNull(resource);
        return resource.getByteArray();
    }

    private static void assertDicomPart10Preamble(byte[] body) {
        assertTrue(body.length > 132, "Response body must contain DICOM Part 10 preamble and meta header");
        assertEquals('D', body[128]);
        assertEquals('I', body[129]);
        assertEquals('C', body[130]);
        assertEquals('M', body[131]);
    }

    private static byte[] createMinimalDicomPart10(String patientName) throws IOException {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage);
        attributes.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5");
        attributes.setString(Tag.PatientName, VR.PN, patientName);
        attributes.setString(Tag.PatientID, VR.LO, patientName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (DicomOutputStream dicomOutputStream = new DicomOutputStream(outputStream, UID.ExplicitVRLittleEndian)) {
            dicomOutputStream.writeDataset(
                    attributes.createFileMetaInformation(UID.ExplicitVRLittleEndian), attributes);
        }
        return outputStream.toByteArray();
    }
}
