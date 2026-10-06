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

package org.shanoir.ng.shared.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;

/**
 * Unit tests of the update of the local study copy received from ms-studies.
 */
@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    private static final Long STUDY_ID = 42L;

    @Mock
    private StudyRepository repository;

    @Mock
    private DatasetRepository dsRepository;

    @InjectMocks
    private StudyServiceImpl studyService;

    @Test
    void updateStudyCopiesNeurobagelExport() {
        Study current = new Study(STUDY_ID, "study", false);
        Study updated = new Study(STUDY_ID, "study", false);

        updated.setNeurobagelExport(true);
        studyService.updateStudy(updated, current);
        assertTrue(current.isNeurobagelExport());

        updated.setNeurobagelExport(false);
        studyService.updateStudy(updated, current);
        assertFalse(current.isNeurobagelExport());

        verify(repository, times(2)).save(current);
    }

}
