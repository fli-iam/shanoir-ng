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

package org.shanoir.ng.shared.common;

import static org.mockito.BDDMockito.given;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.center.CenterRepository;
import org.shanoir.ng.study.StudyRepository;
import org.shanoir.ng.subject.SubjectRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Study service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CommonServiceTest {

	private static final Long CENTER_ID = 1L;
	private static final Long STUDY_ID = 1L;
	private static final Long SUBJECT_ID = 1L;

	@Mock
	private CenterRepository centerRepository;
	
	@Mock
	private StudyRepository studyRepository;

	@Mock
	private SubjectRepository subjectRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private CommonServiceImpl commonService;

	private CommonIdsDTO commonIdDTO;
	
	@Before
	public void setup() {
		given(centerRepository.findOne(CENTER_ID)).willReturn(ModelsUtil.createCenter());
		given(studyRepository.findOne(STUDY_ID)).willReturn(ModelsUtil.createStudy());
		given(subjectRepository.findOne(SUBJECT_ID)).willReturn(ModelsUtil.createSubject());
		
		commonIdDTO = new CommonIdsDTO();
		commonIdDTO.setCenterId(CENTER_ID);
		commonIdDTO.setStudyId(STUDY_ID);
		commonIdDTO.setSubjectId(SUBJECT_ID);
	}

	@Test
	public void findByIdsTest() {
		final CommonIdNamesDTO commonIdNamesDTO = commonService.findByIds(commonIdDTO);
		Assert.assertNotNull(commonIdNamesDTO);
		Assert.assertNotNull(commonIdNamesDTO.getCenter());
		Assert.assertNotNull(commonIdNamesDTO.getStudy());
		Assert.assertNotNull(commonIdNamesDTO.getSubject());

		Mockito.verify(centerRepository, Mockito.times(1)).findOne(CENTER_ID);
		Mockito.verify(studyRepository, Mockito.times(1)).findOne(STUDY_ID);
		Mockito.verify(subjectRepository, Mockito.times(1)).findOne(SUBJECT_ID);
	}

}
