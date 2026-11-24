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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for repository 'subject'.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository repository;

    @Test
    public void findStudyIdByIdAnswersTheStudyOfTheSubject() throws Exception {
        assertThat(repository.findStudyIdById(3L)).contains(1L);
    }

    @Test
    public void findStudyIdByIdIsEmptyForAMissingSubject() throws Exception {
        assertThat(repository.findStudyIdById(999L)).isEmpty();
    }

    @Test
    public void findStudyIdsByIdsLeavesOutTheMissingSubjects() throws Exception {
        assertThat(repository.findStudyIdsByIds(List.of(1L, 3L, 999L)))
                .containsOnlyKeys(1L, 3L)
                .containsEntry(1L, 1L)
                .containsEntry(3L, 1L);
        assertThat(repository.findStudyIdsByIds(List.of())).isEmpty();
    }

}
