package org.shanoir.ng.study;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.service.StudyServiceImpl;
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

	@Mock
	private StudyUserRepository studyUserRepository;
	
	@Mock
	private StudyUserUpdateBroadcastService studyUserCom;

	@Before
	public void setup() {
		given(studyRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudy()));
		given(studyRepository.findIdsAndNames()).willReturn(Arrays.asList(new IdNameDTO()));
		given(studyRepository.findOne(STUDY_ID)).willReturn(ModelsUtil.createStudy());
		given(studyRepository.save(Mockito.any(Study.class))).willReturn(ModelsUtil.createStudy());
	}

	@Test
	public void deleteByIdTest() throws AccessDeniedException, EntityNotFoundException {
		final Study newStudy = ModelsUtil.createStudy();
		final StudyUser studyUser = new StudyUser();
		studyUser.setUserId(USER_ID);
		studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_ADMINISTRATE));
		newStudy.getStudyUserList().add(studyUser);
		given(studyRepository.findOne(STUDY_ID)).willReturn(newStudy);

		studyService.deleteById(STUDY_ID);

		Mockito.verify(studyRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

//	@Test
//	public void findAllTest() {
//		final List<Study> studies = studyService.findAll();
//		Assert.assertNotNull(studies);
//		Assert.assertTrue(studies.size() == 1);
//
//		Mockito.verify(studyRepository, Mockito.times(1)).findAll();
//	}

	@Test
	public void findByIdTest() throws AccessDeniedException {
		final Study study = studyService.findById(STUDY_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void findByIdWithAccessRightTest() throws AccessDeniedException {
		final Study newStudy = ModelsUtil.createStudy();
		final StudyUser studyUser = new StudyUser();
		studyUser.setUserId(USER_ID);
		studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_DOWNLOAD));
		newStudy.getStudyUserList().add(studyUser);
		given(studyRepository.findOne(STUDY_ID)).willReturn(newStudy);

		final Study study = studyService.findById(STUDY_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() {
		studyService.create(createStudy());
		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	@Test
	public void updateTest() throws AccessDeniedException, EntityNotFoundException {
		final Study updatedStudy = studyService.update(createStudy());
		Assert.assertNotNull(updatedStudy);
		Assert.assertTrue(UPDATED_STUDY_NAME.equals(updatedStudy.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}
	
	@Test
	public void updateStudyUsersTest() throws EntityNotFoundException {
		Study existing = createStudy();
		existing.setStudyUserList(new ArrayList<StudyUser>());
		existing.getStudyUserList().add(createStudyUsers(1L, 1L, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
		existing.getStudyUserList().add(createStudyUsers(2L, 2L, StudyUserRight.CAN_ADMINISTRATE));
		
		Study updated = createStudy();
		updated.setStudyUserList(new ArrayList<StudyUser>());
		updated.getStudyUserList().add(createStudyUsers(1L, 1L, StudyUserRight.CAN_DOWNLOAD));
		updated.getStudyUserList().add(createStudyUsers(null, 3L, StudyUserRight.CAN_SEE_ALL));
		
		given(studyRepository.findOne(STUDY_ID)).willReturn(existing);
		given(studyUserRepository.findOne(1L)).willReturn(existing.getStudyUserList().get(0));
		given(studyUserRepository.findOne(2L)).willReturn(existing.getStudyUserList().get(1));

		studyService.update(updated);
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setName(UPDATED_STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		return study;
	}
	
	private StudyUser createStudyUsers(Long suId, Long userId, StudyUserRight... rights) {
		StudyUser studyUser = new StudyUser();
		studyUser.setId(suId);
		studyUser.setStudyId(STUDY_ID);
		studyUser.setUserId(userId);
		List<StudyUserRight> studyUserRights = new ArrayList<>();
		for (StudyUserRight right : rights) {
			studyUserRights.add(right);
		}
		studyUser.setStudyUserRights(studyUserRights);
		return studyUser;
	}

}
