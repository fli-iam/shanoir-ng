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

package org.shanoir.ng.study.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.study.dto.StudyStatisticsDTO;

/**
 * Mapping of the rows returned by the getStudyStatistics procedure.
 */
public class StudyRepositoryImplTest {

    /**
     * Since Hibernate 7, the dates of native query results are java.time types
     * (this used to fail with a ClassCastException).
     */
    @Test
    public void toStudyStatisticsDTOWithJavaTimeDatesTest() {
        Object[] row = createRow(LocalDate.of(2024, 3, 15), LocalDateTime.of(2024, 3, 20, 10, 30));

        StudyStatisticsDTO dto = StudyRepositoryImpl.toStudyStatisticsDTO(row);

        assertEquals(Date.valueOf("2024-03-15"), dto.getExaminationDate());
        assertEquals(Date.valueOf("2024-03-20"), dto.getImportDate());
        assertEquals(1L, dto.getStudyId());
        assertEquals(6L, dto.getDatasetId());
        assertEquals("Mr", dto.getModality());
    }

    @Test
    public void toStudyStatisticsDTOWithSqlDatesAndIntegerIdsTest() {
        Object[] row = createRow(Date.valueOf("2024-03-15"), null);
        row[0] = 1;  // an INT column is read as Integer

        StudyStatisticsDTO dto = StudyRepositoryImpl.toStudyStatisticsDTO(row);

        assertEquals(1L, dto.getStudyId());
        assertEquals(Date.valueOf("2024-03-15"), dto.getExaminationDate());
        assertNull(dto.getImportDate());
    }

    private Object[] createRow(Object examinationDate, Object importDate) {
        return new Object[] {1L, 2L, "center", "C01", 3L, "subject", 4L, "exam", examinationDate,
            5L, importDate, 6L, "dataset", "Mr", "Valid"};
    }

}
