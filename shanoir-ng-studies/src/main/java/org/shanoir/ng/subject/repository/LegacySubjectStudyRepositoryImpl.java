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
package org.shanoir.ng.subject.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LegacySubjectStudyRepositoryImpl implements LegacySubjectStudyRepository {

    private static final Logger LOG = LoggerFactory.getLogger(LegacySubjectStudyRepositoryImpl.class);

    private static final String COUNT_SUBJECT_STUDY_TABLE = """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'subject_study'
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void deleteLegacyEnrollmentLinks(final long subjectId) {
        if (!legacySubjectStudyTablePresent()) {
            return;
        }
        try {
            jdbcTemplate.update("""
                    DELETE sst FROM subject_study_tag sst
                    INNER JOIN subject_study ss ON sst.subject_study_id = ss.id
                    WHERE ss.subject_id = ?
                    """, subjectId);
            int removed = jdbcTemplate.update(
                    "DELETE FROM subject_study WHERE subject_id = ?", subjectId);
            if (removed > 0) {
                LOG.debug("Removed {} legacy subject_study row(s) for subject {}", removed, subjectId);
            }
        } catch (DataAccessException e) {
            LOG.warn(
                    "Could not clear legacy subject_study links for subject {} (apply studies migration 0033): {}",
                    subjectId,
                    e.getMessage());
        }
    }

    private boolean legacySubjectStudyTablePresent() {
        Integer count = jdbcTemplate.queryForObject(COUNT_SUBJECT_STUDY_TABLE, Integer.class);
        return count != null && count > 0;
    }
}