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

package org.shanoir.ng.groupofsubjects;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.core.model.IdName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Subject - study mapper test.
 *
 * @author msimon
 *
 */

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExperimentalGroupOfSubjectsMapperTest {

	private static final Long GROUP_OD_SUBJECTS_ID = 1L;

	@Autowired
	private ExperimentalGroupOfSubjectsMapper experimentalGroupOfSubjectsMapper;

	@Test
	public void experimentalGroupOfSubjectsToIdNameDTOsTest() {
		final List<IdName> subjectStudyDTOs = experimentalGroupOfSubjectsMapper
				.experimentalGroupOfSubjectsToIdNameDTOs(Arrays.asList(createExperimentalGroupOfSubjects()));
		Assertions.assertNotNull(subjectStudyDTOs);
		Assertions.assertTrue(subjectStudyDTOs.size() == 1);
		Assertions.assertTrue(subjectStudyDTOs.get(0).getId().equals(GROUP_OD_SUBJECTS_ID));
	}

	@Test
	public void experimentalGroupOfSubjectsToIdNameDTOTest() {
		final IdName subjectStudyDTO = experimentalGroupOfSubjectsMapper
				.experimentalGroupOfSubjectsToIdNameDTO(createExperimentalGroupOfSubjects());
		Assertions.assertNotNull(subjectStudyDTO);
		Assertions.assertTrue(subjectStudyDTO.getId().equals(GROUP_OD_SUBJECTS_ID));
	}

	private ExperimentalGroupOfSubjects createExperimentalGroupOfSubjects() {
		final ExperimentalGroupOfSubjects groupOfSubjects = new ExperimentalGroupOfSubjects();
		groupOfSubjects.setId(GROUP_OD_SUBJECTS_ID);
		return groupOfSubjects;
	}

}
