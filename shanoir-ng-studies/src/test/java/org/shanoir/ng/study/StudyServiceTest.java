package org.shanoir.ng.study;

import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
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
import org.shanoir.ng.shared.dto.IdNameDTO;
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
	private static final Long USER_ID = 1L;

	@Mock
	private StudyRepository studyRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private StudyServiceImpl studyService;

	@Before
	public void setup() {
		given(studyRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudy()));
		given(studyRepository.findIdsAndNames()).willReturn(Arrays.asList(new IdNameDTO()));
		given(studyRepository.findOne(STUDY_ID)).willReturn(ModelsUtil.createStudy());
		given(studyRepository.save(Mockito.any(Study.class))).willReturn(ModelsUtil.createStudy());
	}

	@Test
	public void deleteByIdWithAccessRightTest() throws ShanoirStudiesException {
		final Study newStudy = ModelsUtil.createStudy();
		final StudyUser studyUser = new StudyUser();
		studyUser.setUserId(USER_ID);
		studyUser.setStudyUserType(StudyUserType.RESPONSIBLE);
		newStudy.getStudyUserList().add(studyUser);
		given(studyRepository.findOne(STUDY_ID)).willReturn(newStudy);

		studyService.deleteById(STUDY_ID, USER_ID);

		Mockito.verify(studyRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test(expected = ShanoirStudiesException.class)
	public void deleteByIdWithoutAccessRightTest() throws ShanoirStudiesException {
		studyService.deleteById(STUDY_ID, USER_ID);
	}

	@Test
	public void findAllTest() {
		final List<Study> studies = studyService.findAll();
		Assert.assertNotNull(studies);
		Assert.assertTrue(studies.size() == 1);

		Mockito.verify(studyRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() throws ShanoirStudiesException {
		final Study study = studyService.findById(STUDY_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void findByIdWithAccessRightTest() throws ShanoirStudiesException {
		final Study newStudy = ModelsUtil.createStudy();
		final StudyUser studyUser = new StudyUser();
		studyUser.setUserId(USER_ID);
		studyUser.setStudyUserType(StudyUserType.SEE_DOWNLOAD);
		newStudy.getStudyUserList().add(studyUser);
		given(studyRepository.findOne(STUDY_ID)).willReturn(newStudy);

		final Study study = studyService.findById(STUDY_ID, USER_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(2)).findOne(Mockito.anyLong());
	}

	@Test(expected = ShanoirStudiesException.class)
	public void findByIdWithoutAccessRightTest() throws ShanoirStudiesException {
		studyService.findById(STUDY_ID, USER_ID);
	}

	@Test
	public void findIdsAndNamesTest() {
		final List<IdNameDTO> studies = studyService.findIdsAndNames();
		Assert.assertNotNull(studies);
		Assert.assertTrue(studies.size() == 1);

		Mockito.verify(studyRepository, Mockito.times(1)).findIdsAndNames();
	}

	@Test
	public void saveTest() throws ShanoirStudiesException {
		studyService.save(createStudy());

		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	@Test
	public void updateTest() throws ShanoirStudiesException {
		final Study updatedStudy = studyService.update(createStudy());
		Assert.assertNotNull(updatedStudy);
		Assert.assertTrue(UPDATED_STUDY_NAME.equals(updatedStudy.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setName(UPDATED_STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		return study;
	}

}
