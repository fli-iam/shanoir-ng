/**
 * Shanoir NG - Import, manage and share neuroimaging data
 */
package org.shanoir.ng.subject.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class LegacySubjectStudyRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private LegacySubjectStudyRepositoryImpl repository;

    @Test
    void skipsWhenSubjectStudyTableAbsent() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
        repository.deleteLegacyEnrollmentLinks(42L);
        verify(jdbcTemplate, never()).update(anyString(), any(Object.class));
    }

    @Test
    void deletesTagsThenRowsWhenTablePresent() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        when(jdbcTemplate.update(contains("subject_study_tag"), eq(42L))).thenReturn(0);
        when(jdbcTemplate.update(eq("DELETE FROM subject_study WHERE subject_id = ?"), eq(42L))).thenReturn(2);
        repository.deleteLegacyEnrollmentLinks(42L);
        verify(jdbcTemplate).update(contains("subject_study_tag"), eq(42L));
        verify(jdbcTemplate).update(eq("DELETE FROM subject_study WHERE subject_id = ?"), eq(42L));
    }
}