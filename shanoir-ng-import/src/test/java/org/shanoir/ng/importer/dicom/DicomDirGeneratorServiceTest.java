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
