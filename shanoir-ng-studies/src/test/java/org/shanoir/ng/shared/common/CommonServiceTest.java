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
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.repository.SubjectRepository;
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
