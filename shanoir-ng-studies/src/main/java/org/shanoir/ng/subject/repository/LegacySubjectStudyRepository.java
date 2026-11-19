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

/**
 * Removes rows from the pre-0033 {@code subject_study} junction table.
 *
 * Migration 0024 moved enrollment onto {@code subject.study_id}; migration 0033
 * drops {@code subject_study}. Instances that applied 0024 but not 0033 still
 * have orphan junction rows that block {@code DELETE FROM subject}.
 */
public interface LegacySubjectStudyRepository {

    /**
     * Delete {@code subject_study_tag} and {@code subject_study} rows for
     * {@code subjectId}. No-op when the table is already dropped.
     */
    void deleteLegacyEnrollmentLinks(long subjectId);
}