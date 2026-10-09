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

package org.shanoir.ng.shared.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.model.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Neurobagel export queries. Test data: studies 1 and 2 are approved, study 3 is a draft,
 * none is flagged for export.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StudyRepositoryTest {

    @Autowired
    private StudyRepository repository;

    @Test
    public void noStudyIsExportedByDefault() {
        assertTrue(repository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc().isEmpty());
        assertFalse(repository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L));
    }

    @Test
    public void onlyFlaggedApprovedStudiesAreExported() {
        flag(1L);
        flag(3L); // draft: never exported

        List<Study> exported = repository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc();
        assertEquals(List.of(1L), exported.stream().map(Study::getId).toList());

        assertTrue(repository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L));
        assertFalse(repository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(2L)); // not flagged
        assertFalse(repository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(3L)); // draft
        assertFalse(repository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(999L)); // unknown
    }

    private void flag(Long studyId) {
        Study study = repository.findById(studyId).orElseThrow();
        study.setNeurobagelExport(true);
        repository.save(study);
    }

}
