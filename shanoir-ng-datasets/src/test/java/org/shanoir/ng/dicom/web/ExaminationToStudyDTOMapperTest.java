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

package org.shanoir.ng.dicom.web;

import java.text.ParseException;
import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.dicom.web.dto.StudyDTO;
import org.shanoir.ng.dicom.web.dto.mapper.ExaminationToStudyDTOMapper;
import org.shanoir.ng.examination.model.Examination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExaminationToStudyDTOMapperTest {
	
	private static final Long EXAMINATION_ID = 1L;

	private static final String EXAMINATION_COMMENT = "ExaminationComment";
	
	private static final String DATE_STR = "2018-01-01";

	private static final long SUBJECT_ID = 1L;

	@Autowired
	private ExaminationToStudyDTOMapper examinationToStudyDTOMapper;

	@Test
	public void examinationToStudyDTO() throws ParseException {
		Examination examination = createExamination();
		final StudyDTO studyDTO = examinationToStudyDTOMapper.examinationToStudyDTO(examination);
		Assert.assertNotNull(studyDTO);
	}

	private Examination createExamination() {
		final Examination examination = new Examination();
		examination.setId(EXAMINATION_ID);
		examination.setComment(EXAMINATION_COMMENT);
		examination.setExaminationDate(LocalDate.parse(DATE_STR));
		examination.setSubjectId(SUBJECT_ID);
		return examination;
	}

}
