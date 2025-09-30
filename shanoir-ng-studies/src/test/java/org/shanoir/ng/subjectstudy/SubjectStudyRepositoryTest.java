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

package org.shanoir.ng.subjectstudy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for repository 'SubjectStudy'.
 *
 * @author msimon
 *
 */

@DataJpaTest
@ActiveProfiles("test")
public class SubjectStudyRepositoryTest {

	private static final Long STUDY_TEST_1_ID = 1L;

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Test
	public void findByStudyTest() throws Exception {
		final Study study = new Study();
		study.setId(STUDY_TEST_1_ID);
		List<SubjectStudy> subjectDb = subjectStudyRepository.findByStudyId(study.getId());
		assertNotNull(subjectDb);
		assertThat(subjectDb.size()).isEqualTo(2);
	}

}
