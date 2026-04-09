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
package org.shanoir.ng.studyexamination;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.dto.StudyExaminationsDTO.StudyExaminationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository for bulk operations on relations between a study and a center.
 */
@Repository
public class StudyExaminationBulkRepositoryImpl implements StudyExaminationBulkRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 1000;

    @Override
    public void insertInBatches(List<StudyExaminationDTO> total, Long studyId) {
        int size = total.size();
        for (int i = 0; i < size; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, size);
            List<StudyExaminationDTO> subBatch = total.subList(i, end);
            insertOneBatch(subBatch, studyId);
        }
    }

    private void insertOneBatch(List<StudyExaminationDTO> batch, Long studyId) {

        StringBuilder sql = new StringBuilder("""
                INSERT INTO study_examination (examination_id, center_id, study_id, subject_id)
                SELECT v.examination_id, v.center_id, v.study_id, v.subject_id
                FROM (
                """);

        List<Object> params = new ArrayList<>();

        for (int i = 0; i < batch.size(); i++) {
            StudyExaminationDTO dto = batch.get(i);

            if (i > 0) {
                sql.append(" UNION ALL ");
            }

            sql.append("SELECT ? AS examination_id, ? AS center_id, ? AS study_id, ? AS subject_id");

            params.add(dto.getExaminationId());
            params.add(dto.getCenterId());
            params.add(studyId);
            params.add(dto.getSubjectId());
        }

        // The joins are needed to ensure that the examination, center and subject exist,
        // and to avoid inserting duplicates, which would cause a constraint violation.
        sql.append("""
                ) v
                JOIN center c ON c.id = v.center_id
                JOIN subject s ON s.id = v.subject_id
                LEFT JOIN study_examination se
                    ON se.study_id = v.study_id
                AND se.examination_id = v.examination_id
                WHERE se.id IS NULL
                """);
        jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
