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

package org.shanoir.ng.study.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

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
	
	@Mock
	private DataUserAgreementService dataUserAgreementService;

	@Mock
	private StudyMapper studyMapper;
	
	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();
	
	public static String tempFolderPath;

	@BeforeClass
	public static void beforeClass() {
		tempFolderPath = tempFolder.getRoot().getAbsolutePath() + "/tmp/";
	}

	@Before
	public void setup() {
	    ReflectionTestUtils.setField(studyService, "dataDir", this.tempFolderPath);

		given(studyRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudy()));
		given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(ModelsUtil.createStudy()));
		given(studyRepository.save(Mockito.any(Study.class))).willReturn(ModelsUtil.createStudy());
	}

	@Test
	public void deleteBydTest() throws AccessDeniedException, EntityNotFoundException {
		final Study newStudy = ModelsUtil.createStudy();
		final StudyUser studyUser = new StudyUser();
		studyUser.setUserId(USER_ID);
		studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_ADMINISTRATE));
		newStudy.getStudyUserList().add(studyUser);
		given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(newStudy));

		studyService.deleteById(STUDY_ID);

		Mockito.verify(studyRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
	}

	@Test
	public void findByIdTest() throws AccessDeniedException {
		final Study study = studyService.findById(STUDY_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	public void findByIdWithAccessRightTest() throws AccessDeniedException {
		final Study newStudy = ModelsUtil.createStudy();
		final StudyUser studyUser = new StudyUser();
		studyUser.setUserId(USER_ID);
		studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_DOWNLOAD));
		newStudy.getStudyUserList().add(studyUser);
		given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(newStudy));

		final Study study = studyService.findById(STUDY_ID);
		Assert.assertNotNull(study);
		Assert.assertTrue(ModelsUtil.STUDY_NAME.equals(study.getName()));

		Mockito.verify(studyRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws MicroServiceCommunicationException {
		studyService.create(createStudy());
		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void updateTest() throws AccessDeniedException, EntityNotFoundException, MicroServiceCommunicationException, IOException {
		// Also test protocol file path
		File protocol = new File(tempFolderPath + "study-1/old.txt");

		protocol.getParentFile().mkdirs();
		protocol.createNewFile();
		Study dbStudy = ModelsUtil.createStudy();
		dbStudy.setId(1L);
		dbStudy.setProtocolFilePaths(Collections.singletonList("old.txt"));
		Study updatedStudy = createStudy();
		updatedStudy.setId(1L);
		updatedStudy.setProtocolFilePaths(Collections.singletonList("new.txt"));

		given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(dbStudy));

		final Study returnedStudy = studyService.update(updatedStudy);
		Assert.assertNotNull(returnedStudy);
		Assert.assertTrue(UPDATED_STUDY_NAME.equals(returnedStudy.getName()));
		assertNotNull(returnedStudy.getProtocolFilePaths());
		assertEquals(1, returnedStudy.getProtocolFilePaths().size());
		assertEquals("new.txt", returnedStudy.getProtocolFilePaths().get(0));
		// Check that the file was deleted
		assertFalse(protocol.exists());
		Mockito.verify(studyRepository, Mockito.times(1)).save(Mockito.any(Study.class));
	}
	
	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void updateStudyUsersTest() throws EntityNotFoundException, MicroServiceCommunicationException {
		Study existing = createStudy();
		existing.setStudyUserList(new ArrayList<StudyUser>());
		existing.getStudyUserList().add(createStudyUsers(1L, 1L, existing, true, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
		existing.getStudyUserList().add(createStudyUsers(2L, 2L, existing, true, StudyUserRight.CAN_ADMINISTRATE));
		
		Study updated = createStudy();
		updated.setStudyUserList(new ArrayList<StudyUser>());
		updated.getStudyUserList().add(createStudyUsers(1L, 1L, updated, true, StudyUserRight.CAN_DOWNLOAD));
		updated.getStudyUserList().add(createStudyUsers(null, 3L, updated, true, StudyUserRight.CAN_SEE_ALL));
		
		given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(existing));
		given(studyUserRepository.findById(1L)).willReturn(Optional.of(existing.getStudyUserList().get(0)));
		given(studyUserRepository.findById(2L)).willReturn(Optional.of(existing.getStudyUserList().get(1)));
		List<StudyUser> in = new ArrayList<>(); in.add(updated.getStudyUserList().get(1));
		List<StudyUser> out = new ArrayList<>(); out.add(createStudyUsers(4L, 3L, updated, true, StudyUserRight.CAN_SEE_ALL));
		given(studyUserRepository.saveAll(in)).willReturn(out);

		studyService.update(updated);
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void testUpdateStudyUsersNoDUA() {
		// We delete the DUA from the old study
		Study existing = createStudy();
		existing.setDataUserAgreementPaths(Collections.singletonList("test"));
		existing.setStudyUserList(new ArrayList<StudyUser>());
		existing.getStudyUserList().add(createStudyUsers(1L, 1L, existing, true, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
		StudyUser suToBeDeleted = createStudyUsers(2L, 2L, existing, true, StudyUserRight.CAN_ADMINISTRATE);

		existing.getStudyUserList().add(suToBeDeleted);
		
		Study updated = createStudy();
		updated.setStudyUserList(new ArrayList<StudyUser>());
		updated.getStudyUserList().add(createStudyUsers(1L, 1L, updated, false, StudyUserRight.CAN_DOWNLOAD));
		StudyUser suToBeAdded = createStudyUsers(null, 3L, updated, false, StudyUserRight.CAN_SEE_ALL);

		updated.getStudyUserList().add(suToBeAdded);
				
		given(studyUserRepository.saveAll(Mockito.any(List.class))).willReturn(Collections.singletonList(suToBeAdded));
		given(studyUserRepository.findById(2L)).willReturn(Optional.of(suToBeDeleted));

		studyService.updateStudyUsers(existing, updated);
		
		Mockito.verify(dataUserAgreementService).deleteIncompleteDataUserAgreementForUserInStudy(existing, 2L);
		
		for (StudyUser su : updated.getStudyUserList()) {
			// all are now confirmed
			assertTrue(su.isConfirmed());
			if (su.getId() == null) {
				assertEquals(suToBeAdded, su);
			}
			else if (su.getId().equals("1L")) {
				assertTrue(su.getStudyUserRights().contains(StudyUserRight.CAN_DOWNLOAD));
				assertFalse(su.getStudyUserRights().contains(StudyUserRight.CAN_IMPORT));
			}
			else if (su.getId().equals("2L")) {
				//This su should have been deleted
				fail("This study user with id 2 should have been removed");
			}
		}
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void testUpdateStudyUsersAddDUA() {
		// In this method, a new DUA is added
		Study existing = createStudy();
		existing.setStudyUserList(new ArrayList<StudyUser>());
		existing.getStudyUserList().add(createStudyUsers(1L, 1L, existing, true, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT));
		existing.getStudyUserList().add(createStudyUsers(2L, 2L, existing, true, StudyUserRight.CAN_ADMINISTRATE));
		
		Study updated = createStudy();
		updated.setDataUserAgreementPaths(Collections.singletonList("truc"));
		updated.setStudyUserList(new ArrayList<StudyUser>());
		updated.getStudyUserList().add(createStudyUsers(1L, 1L, updated, true, StudyUserRight.CAN_DOWNLOAD));
		StudyUser suToBeAdded = createStudyUsers(null, 3L, updated, true, StudyUserRight.CAN_SEE_ALL);
		updated.getStudyUserList().add(suToBeAdded);
		
		given(studyUserRepository.saveAll(Mockito.any(List.class))).willReturn(Collections.singletonList(suToBeAdded));

		studyService.updateStudyUsers(existing, updated);
		for (StudyUser su : updated.getStudyUserList()) {
			// all are now not confirmed
			assertFalse(su.isConfirmed());
			if (su.getId() == null) {
				assertEquals(suToBeAdded, su);
			}
			else if (su.getId().equals(1L)) {
				assertTrue(su.getStudyUserRights().contains(StudyUserRight.CAN_DOWNLOAD));
				assertFalse(su.getStudyUserRights().contains(StudyUserRight.CAN_IMPORT));
			}
			else if (su.getId().equals(2L)) {
				//This su should have been deleted
				fail("This study user with id 2 should have been removed");
			}
		}
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setName(UPDATED_STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		return study;
	}
	
	private StudyUser createStudyUsers(Long suId, Long userId, Study study, boolean confirmed, StudyUserRight... rights) {
		StudyUser studyUser = new StudyUser();
		studyUser.setId(suId);
		studyUser.setStudy(study);
		studyUser.setUserId(userId);
		studyUser.setConfirmed(confirmed);
		List<StudyUserRight> studyUserRights = new ArrayList<>();
		for (StudyUserRight right : rights) {
			studyUserRights.add(right);
		}
		studyUser.setStudyUserRights(studyUserRights);
		return studyUser;
	}
	
}
