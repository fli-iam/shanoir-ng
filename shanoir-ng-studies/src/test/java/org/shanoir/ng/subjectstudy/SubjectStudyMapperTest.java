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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentMapper;
import org.shanoir.ng.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
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

	@MockBean
	private AcquisitionEquipmentMapper acquisitionEquipmentMapperMock;

	@Autowired
	private SubjectStudyMapper subjectStudyMapper;

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
		subject.setId(SUBJECT_ID);
		center.setSubject(subject);
		return center;
	}

}
