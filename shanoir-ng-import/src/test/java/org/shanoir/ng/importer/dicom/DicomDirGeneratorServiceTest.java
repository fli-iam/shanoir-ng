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

package org.shanoir.ng.importer.dicom;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class DicomDirGeneratorServiceTest {

    @Autowired
    private DicomDirGeneratorService dicomDirGeneratorService;

    @BeforeEach
    public void setup() {
        dicomDirGeneratorService = new DicomDirGeneratorService();
    }

    @Test
    public void testGenerateDicomDir() throws NoSuchAlgorithmException, IOException {
//        dicomDirGeneratorService.generateDicomDirFromDirectory(new File("/Users/mkain/Desktop/UCAN-SIM-dataset-5213f9fc-8251-4466-b1a8-194b528bc0af/DICOMDIR"),
//                new File("/Users/mkain/Desktop/UCAN-SIM-dataset-5213f9fc-8251-4466-b1a8-194b528bc0af"));
    }

}
