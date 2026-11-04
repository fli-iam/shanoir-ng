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

import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Read-only query behind the Neurobagel imaging table.
 */
public interface NeurobagelImagingRepository extends Repository<Dataset, Long> {

    /**
     * Acquired datasets of a study (processed datasets have no acquisition and are left out), restricted
     * to the subjects of the study: bagel rejects a subject missing from participants.tsv, and an
     * examination can point to a subject that was moved to another study.
     * Study-card metadata (updated) wins over import metadata (origin).
     */
    @Query(nativeQuery = true, value = """
            SELECT d.subject_id AS subjectId,
                   e.id AS examinationId,
                   d.id AS datasetId,
                   CASE WHEN mr.id IS NOT NULL THEN 'MR'
                        WHEN pet.id IS NOT NULL THEN 'PET'
                        WHEN eeg.id IS NOT NULL THEN 'EEG'
                        WHEN meg.id IS NOT NULL THEN 'MEG'
                        ELSE 'OTHER' END AS modality,
                   md.mr_dataset_nature AS mrDatasetNature,
                   pm.mr_sequence_application AS mrSequenceApplication
            FROM dataset d
            JOIN dataset_acquisition a ON a.id = d.dataset_acquisition_id
            JOIN examination e ON e.id = a.examination_id
            JOIN subject s ON s.id = d.subject_id AND s.study_id = e.study_id
            LEFT JOIN mr_dataset mr ON mr.id = d.id
            LEFT JOIN mr_dataset_metadata md ON md.id = COALESCE(mr.updated_mr_metadata_id, mr.origin_mr_metadata_id)
            LEFT JOIN mr_dataset_acquisition ma ON ma.id = a.id
            LEFT JOIN mr_protocol p ON p.id = ma.mr_protocol_id
            LEFT JOIN mr_protocol_metadata pm ON pm.id = COALESCE(p.updated_metadata_id, p.origin_metadata_id)
            LEFT JOIN pet_dataset pet ON pet.id = d.id
            LEFT JOIN eeg_dataset eeg ON eeg.id = d.id
            LEFT JOIN meg_dataset meg ON meg.id = d.id
            WHERE e.study_id = :studyId
            ORDER BY d.subject_id, e.id, d.id
            """)
    List<NeurobagelImagingRow> findImagingRows(@Param("studyId") Long studyId);

}
