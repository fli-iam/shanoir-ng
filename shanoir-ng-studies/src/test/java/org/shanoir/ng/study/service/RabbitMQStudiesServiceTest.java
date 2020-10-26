package org.shanoir.ng.study.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMQStudiesServiceTest {

	@Mock
	private StudyRepository studyRepo;

	@InjectMocks
	private RabbitMQStudiesService rabbitMQStudiesService;
	
	@Test
	public void testGetStudyAdmin() {
		// GIVEN a study with a list of user with import
		Study s = new Study();
		StudyUser su = new StudyUser();
		su.setUserId(1L);
		su.setReceiveNewImportReport(true);
		StudyUser su2 = new StudyUser();
		su2.setUserId(2L);
		su2.setReceiveNewImportReport(false);
		s.setStudyUserList(new ArrayList<StudyUser>());
		s.getStudyUserList().add(su);
		s.getStudyUserList().add(su2);

		Mockito.when(studyRepo.findOne(1L)).thenReturn(s);
		
		// WHEN we retrieve the list of user having import message config
		List<Long> result = this.rabbitMQStudiesService.manageAdminsStudy("1");
		
		// THEN the list of user's id is returned
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue(result.contains(su.getUserId()));
		assertFalse(result.contains(su2.getUserId()));
	}

	@Test
	public void testGetStudyAdminGetFails() {
		// GIVEN no study
		Mockito.when(studyRepo.findOne(1L)).thenThrow(new IllegalArgumentException("oups"));
		
		// WHEN we retrieve the list of user having import message config
		List<Long> result = this.rabbitMQStudiesService.manageAdminsStudy("1");
		
		// THEN empty collection is returned
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testGetStudyAdminNoStudy() {
		// GIVEN a study with a list of user with import
	
		Mockito.when(studyRepo.findOne(1L)).thenReturn(null);
		
		// WHEN we retrieve the list of user having import message config
		List<Long> result = this.rabbitMQStudiesService.manageAdminsStudy("1");
		
		// THEN empty collection is returned
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
}
