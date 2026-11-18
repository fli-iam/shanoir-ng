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

package org.shanoir.ng.neurobagel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;

/**
 * Test data: study 1 has subjects 1, 2, 3; examination 1 (subject 1) holds MR dataset 1 (import
 * nature T1), examination 2 (subject 2) holds PET dataset 2; examination 3 belongs to study 3 and holds
 * CT dataset 3. The datasets have no subject in the test data: setUp assigns them.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NeurobagelImagingRepositoryTest {

    @Autowired
    private NeurobagelImagingRepository repository;

    @Autowired
    private EntityManager entityManager;

    private void sql(String statement) {
        entityManager.createNativeQuery(statement).executeUpdate();
    }

    @BeforeEach
    void setUp() {
        sql("UPDATE dataset SET subject_id = 1 WHERE id = 1");
        sql("UPDATE dataset SET subject_id = 2 WHERE id = 2");
        sql("UPDATE dataset SET subject_id = 3 WHERE id = 3");
    }

    @Test
    void listsTheAcquiredDatasetsOfTheStudy() {
        List<NeurobagelImagingRow> rows = repository.findImagingRows(1L);

        assertEquals(List.of(1L, 2L), rows.stream().map(NeurobagelImagingRow::getDatasetId).toList());

        NeurobagelImagingRow mr = rows.get(0);
        assertEquals(1L, mr.getSubjectId());
        assertEquals(1L, mr.getExaminationId());
        assertEquals("MR", mr.getModality());
        assertEquals(1, mr.getMrDatasetNature()); // import nature, no study card
        assertNull(mr.getMrSequenceApplication());

        NeurobagelImagingRow pet = rows.get(1);
        assertEquals(2L, pet.getSubjectId());
        assertEquals("PET", pet.getModality());
        assertNull(pet.getMrDatasetNature());
    }

    @Test
    void studyCardMetadataWinsOverImportMetadata() {
        sql("INSERT INTO mr_dataset_metadata (id, mr_dataset_nature) VALUES (2, 5)");
        sql("UPDATE mr_dataset SET updated_mr_metadata_id = 2 WHERE id = 1");
        sql("INSERT INTO mr_protocol_metadata (id, name, dtype, mr_sequence_application) VALUES (2, 'card', 2, 8)");
        sql("UPDATE mr_protocol SET updated_metadata_id = 2 WHERE id = 1");

        NeurobagelImagingRow mr = repository.findImagingRows(1L).get(0);

        assertEquals(5, mr.getMrDatasetNature());
        assertEquals(8, mr.getMrSequenceApplication());
    }

    @Test
    void subjectsOutsideTheStudyAreLeftOut() {
        sql("UPDATE subject SET study_id = 3 WHERE id = 2");

        assertEquals(List.of(1L), repository.findImagingRows(1L).stream().map(NeurobagelImagingRow::getDatasetId).toList());
    }

    @Test
    void otherModalitiesAreFlaggedAsOther() {
        // Examination 3 belongs to study 3 but its subject to study 1: left out until the subject moves
        assertTrue(repository.findImagingRows(3L).isEmpty());
        sql("UPDATE subject SET study_id = 3 WHERE id = 3");

        List<NeurobagelImagingRow> rows = repository.findImagingRows(3L);

        assertEquals(1, rows.size());
        assertEquals("OTHER", rows.get(0).getModality()); // CT
    }

    @Test
    void processedDatasetsAreLeftOut() {
        sql("INSERT INTO dataset_metadata (id, cardinality_of_related_subjects, name) VALUES (4, 1, 'processed')");
        sql("INSERT INTO dataset (id, origin_metadata_id, downloadable, subject_id) VALUES (4, 4, 1, 1)");

        assertEquals(List.of(1L, 2L), repository.findImagingRows(1L).stream().map(NeurobagelImagingRow::getDatasetId).toList());
    }

}
