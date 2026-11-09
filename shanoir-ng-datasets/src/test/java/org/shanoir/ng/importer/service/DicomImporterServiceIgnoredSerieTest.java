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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.shared.service.SubjectService;

/**
 * A STOW-RS sender sends objects Shanoir does not manage: they may not make the
 * request fail, nor change what is stored.
 */
@ExtendWith(MockitoExtension.class)
class DicomImporterServiceIgnoredSerieTest {

    private static final Long STUDY_ID = 1L;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private DatasetSecurityService datasetSecurityService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private ExaminationService examinationService;

    @Mock
    private DatasetAcquisitionService acquisitionService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DICOMWebService dicomWebService;

    @InjectMocks
    private DicomImporterService dicomImporterService;

    @Test
    void instanceOfAModalityShanoirDoesNotManageIsSkippedWithoutError() throws Exception {
        // a presentation state or a secondary capture: the ZIP import drops these series too
        Attributes attributes = new Attributes();
        attributes.setString(Tag.DeidentificationMethod, VR.LO, "Karnak");
        attributes.setString(Tag.ClinicalTrialProtocolID, VR.LO, STUDY_ID.toString());
        attributes.setString(Tag.Modality, VR.CS, "PR");
        attributes.setString(Tag.PatientName, VR.PN, "subject01");
        Study study = new Study();
        study.setId(STUDY_ID);
        when(studyRepository.findById(STUDY_ID)).thenReturn(Optional.of(study));
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(true);

        assertTrue(dicomImporterService.importDicom(new Attributes(), attributes));

        verifyNoInteractions(subjectService, examinationService, acquisitionService, datasetService, dicomWebService);
    }

}
