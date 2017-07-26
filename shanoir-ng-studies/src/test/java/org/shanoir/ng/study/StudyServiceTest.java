package org.shanoir.ng.study;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Study service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class StudyServiceTest {

	private static final Long STUDY_ID = 1L;
	private static final String UPDATED_STUDY_NAME = "test";

	@Mock
	private StudyRepository studyRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private StudyServiceImpl studyService;

	@Before
	public void setup() {
		given(studyRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudy()));
		given(studyRepository.findOne(STUDY_ID)).willReturn(ModelsUtil.createStudy());
		given(studyRepository.save(Mockito.any(Study.class))).willReturn(ModelsUtil.createStudy());
	}

	@Test
	public void deleteByIdTest() throws ShanoirStudiesException {
		studyService.deleteById(STUDY_ID);

		Mockito.verify(studyRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Study> studies = studyService.findAll();
		Assert.assertNotNull(studies);
		Assert.assertTrue(studies.size() == 1);

		Mockito.verify(studyRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Study study = studyService.findById(STUDY_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirStudiesException {
		studyService.save(createAcquisitionEquipment());

		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	@Test
	public void updateTest() throws ShanoirStudiesException {
		final Study updatedStudy = studyService.update(createAcquisitionEquipment());
		Assert.assertNotNull(updatedStudy);
		Assert.assertTrue(UPDATED_STUDY_NAME.equals(updatedStudy.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	private Study createAcquisitionEquipment() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setName(UPDATED_STUDY_NAME);
		return study;
	}

}
