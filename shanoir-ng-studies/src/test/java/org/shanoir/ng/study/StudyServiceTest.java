/**
$ * Shanoir NG - Import, manage and share neuroimaging data
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
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.service.StudyServiceImpl;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Study service test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
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
	private StudyCenterRepository studyCenterRepository;

	@Mock
	private StudyUserUpdateBroadcastService studyUserCom;

	@Before
	public void setup() {
		given(studyRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudy()));
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
	public void saveTest() throws MicroServiceCommunicationException {
		studyService.create(createStudy());
		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void updateTest() throws AccessDeniedException, EntityNotFoundException, MicroServiceCommunicationException {
		final Study updatedStudy = studyService.update(createStudy());
		Assert.assertNotNull(updatedStudy);
		Assert.assertTrue(UPDATED_STUDY_NAME.equals(updatedStudy.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}
	
	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void updateStudyUsersTest() throws EntityNotFoundException, MicroServiceCommunicationException {
		Study existing = createStudy();
		existing.setStudyUserList(new ArrayList<StudyUser>());
		existing.getStudyUserList().add(createStudyUsers(1L, 1L, existing, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
		existing.getStudyUserList().add(createStudyUsers(2L, 2L, existing, StudyUserRight.CAN_ADMINISTRATE));
		
		Study updated = createStudy();
		updated.setStudyUserList(new ArrayList<StudyUser>());
		updated.getStudyUserList().add(createStudyUsers(1L, 1L, updated, StudyUserRight.CAN_DOWNLOAD));
		updated.getStudyUserList().add(createStudyUsers(null, 3L, updated, StudyUserRight.CAN_SEE_ALL));
		
		given(studyRepository.findOne(STUDY_ID)).willReturn(existing);
		given(studyUserRepository.findOne(1L)).willReturn(existing.getStudyUserList().get(0));
		given(studyUserRepository.findOne(2L)).willReturn(existing.getStudyUserList().get(1));
		List<StudyUser> in = new ArrayList<>(); in.add(updated.getStudyUserList().get(1));
		List<StudyUser> out = new ArrayList<>(); out.add(createStudyUsers(4L, 3L, updated, StudyUserRight.CAN_SEE_ALL));
		given(studyUserRepository.save(in)).willReturn(out);

		studyService.update(updated);
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setName(UPDATED_STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		return study;
	}
	
	private StudyUser createStudyUsers(Long suId, Long userId, Study study, StudyUserRight... rights) {
		StudyUser studyUser = new StudyUser();
		studyUser.setId(suId);
		studyUser.setStudy(study);
		studyUser.setUserId(userId);
		List<StudyUserRight> studyUserRights = new ArrayList<>();
		for (StudyUserRight right : rights) {
			studyUserRights.add(right);
		}
		studyUser.setStudyUserRights(studyUserRights);
		return studyUser;
	}
	
}
