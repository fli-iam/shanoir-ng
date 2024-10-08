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

package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.timepoint.TimepointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Study mapper test.
 * 
 * @author msimon
 * 
 */

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StudyMapperTest {

	private static final Long STUDY_ID = 1L;
	private static final String STUDY_NAME = "test";

	@Autowired
	private StudyMapper studyMapper;

	@MockBean
	private TimepointMapper timepointMapperMock;

	@Test
	public void studiesToStudyDTOsTest() {
		final List<StudyDTO> studyDTOs = studyMapper.studiesToStudyDTOs(Arrays.asList(createStudy()));
		Assertions.assertNotNull(studyDTOs);
		Assertions.assertTrue(studyDTOs.size() == 1);
		Assertions.assertTrue(studyDTOs.get(0).getId().equals(STUDY_ID));
	}

	@Test
	public void studyToStudyDTOTest() {
		final StudyDTO studyDTO = studyMapper.studyToStudyDTO(createStudy());
		Assertions.assertNotNull(studyDTO);
		Assertions.assertTrue(studyDTO.getId().equals(STUDY_ID));
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setExperimentalGroupsOfSubjects(new ArrayList<>());
		study.setName(STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		study.setStudyUserList(new ArrayList<>());
		study.setSubjectStudyList(new ArrayList<>());
		return study;
	}

}
