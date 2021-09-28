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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyMapper;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.tag.model.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Subject - study mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) 
@ActiveProfiles("test")
public class SubjectStudyMapperTest {

	private static final Long SUBJECT_ID = 1L;

	@Autowired
	private SubjectStudyMapper subjectStudyMapper;

	@Mock
	private TagMapper tagMapper;

	@Test
	public void subjectStudyListToSubjectStudyDTOListTest() {
		final List<SubjectStudyDTO> subjectStudyDTOs = subjectStudyMapper
				.subjectStudyListToSubjectStudyDTOList(Arrays.asList(createSubjectStudy()));
		Assert.assertNotNull(subjectStudyDTOs);
		Assert.assertTrue(subjectStudyDTOs.size() == 1);
		Assert.assertTrue(subjectStudyDTOs.get(0).getSubject().getId().equals(SUBJECT_ID));
	}

	@Test
	public void subjectStudyToSubjectStudyDTOTest() {
		final SubjectStudyDTO subjectStudyDTO = subjectStudyMapper.subjectStudyToSubjectStudyDTO(createSubjectStudy());
		Assert.assertNotNull(subjectStudyDTO);
		Assert.assertTrue(subjectStudyDTO.getSubject().getId().equals(SUBJECT_ID));
	}

	private SubjectStudy createSubjectStudy() {
		final SubjectStudy center = new SubjectStudy();
		final Subject subject = new Subject();
		final Study study = new Study();
		study.setTags(Collections.emptyList());
		subject.setId(SUBJECT_ID);
		center.setSubject(subject);
		center.setStudy(study);
		center.setTags(Collections.emptyList());
		return center;
	}

}
