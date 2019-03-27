package org.shanoir.ng.subject;

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.tests.assertion.AssertUtils.assertAccessDenied;
import static org.shanoir.ng.utils.tests.assertion.AssertUtils.assertAccessAuthorized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.model.security.StudyUserRight;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.controler.SubjectApi;
import org.shanoir.ng.subject.dto.SubjectFromShupDTO;
import org.shanoir.ng.subject.dto.SubjectStudyCardIdDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.tests.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("test")
public class SubjectApiSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Subject mockNew;
	private Subject mockExisting;
	private BindingResult mockBindingResult;
	
	@Autowired
	private SubjectApi api;
	
	@MockBean
	private SubjectRepository repository;
	
	@MockBean
	private StudyRepository studyRepository;
	
	@Before
	public void setup() {
		mockNew = ModelsUtil.createSubject();
		mockExisting = ModelsUtil.createSubject();
		mockExisting.setId(ENTITY_ID);
		mockBindingResult = BindingResultUtils.getBindingResult(new HashMap<String, String>(), "Subject");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		assertAccessDenied(api::findSubjects);
		assertAccessDenied(api::findSubjectsNames);
		assertAccessDenied(api::findSubjectById, ENTITY_ID);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewOFSEPSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectStudyCardIdDTO(), mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewOFSEPSubjectFromShup(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectFromShupDTO(), mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateSubjectFromShup(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, new SubjectFromShupDTO(), mockBindingResult);
		assertAccessDenied(api::findSubjectsByStudyId, ENTITY_ID);
		assertAccessDenied(api::findSubjectByIdentifier, "identifier");
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		assertAccessDenied(api::findSubjectById, ENTITY_ID);
		assertAccessDenied(api::findSubjectByIdentifier, "identifier");
		assertAccessDenied(api::findSubjects);
		assertAccessDenied(api::findSubjectsNames);
		assertAccessDenied(api::findSubjectsByStudyId, ENTITY_ID);
		
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewOFSEPSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectStudyCardIdDTO(), mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewOFSEPSubjectFromShup(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectFromShupDTO(), mockBindingResult);
		
		assertAccessDenied(api::deleteSubject, ENTITY_ID);
		assertAccessDenied((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);
		assertAccessDenied((t, u, v) -> { try { api.updateSubjectFromShup(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, new SubjectFromShupDTO(), mockBindingResult);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		assertAccessAuthorized(api::deleteSubject, ENTITY_ID);
		assertAccessAuthorized(api::findSubjects);
		assertAccessAuthorized(api::findSubjectsNames);
		assertAccessAuthorized(api::findSubjectById, ENTITY_ID);
		assertAccessAuthorized((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, mockNew, mockBindingResult);
		assertAccessAuthorized((t, u) -> { try { api.saveNewOFSEPSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectStudyCardIdDTO(), mockBindingResult);
		assertAccessAuthorized((t, u) -> { try { api.saveNewOFSEPSubjectFromShup(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectFromShupDTO(), mockBindingResult);
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubject(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, mockExisting, mockBindingResult);
		assertAccessAuthorized((t, u, v) -> { try { api.updateSubjectFromShup(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, ENTITY_ID, new SubjectFromShupDTO(), mockBindingResult);
		assertAccessAuthorized(api::findSubjectsByStudyId, ENTITY_ID);
		assertAccessAuthorized(api::findSubjectByIdentifier, "identifier");
	}
	
	private void testRead() throws ShanoirException {
		final String NAME = "data";
		
		Subject subjectMockNoRights = buildSubjectMock(1L);
		given(repository.findByName(NAME)).willReturn(subjectMockNoRights);
		given(repository.findOne(1L)).willReturn(subjectMockNoRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockNoRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockNoRights);
		given(repository.findFromCenterCode("centerCode")).willReturn(subjectMockNoRights);
		assertAccessDenied(service::findByData, NAME);
		assertAccessDenied(service::findById, 1L);
		assertAccessDenied(service::findByIdentifier, "identifier");
		assertAccessDenied(service::findByIdWithSubjecStudies, 1L);
		assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");
		
		Subject subjectMockWrongRights = buildSubjectMock(1L);
		addStudyToMock(subjectMockWrongRights, 100L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
		given(repository.findByName(NAME)).willReturn(subjectMockWrongRights);
		given(repository.findOne(1L)).willReturn(subjectMockWrongRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockWrongRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockWrongRights);
		given(repository.findFromCenterCode("centerCode")).willReturn(subjectMockWrongRights);
		assertAccessDenied(service::findByData, NAME);
		assertAccessDenied(service::findById, 1L);
		assertAccessDenied(service::findByIdentifier, "identifier");
		assertAccessDenied(service::findByIdWithSubjecStudies, 1L);
		assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");
		
		Subject subjectMockRightRights = buildSubjectMock(1L);
		addStudyToMock(subjectMockRightRights, 100L, StudyUserRight.CAN_SEE_ALL);
		given(repository.findByName(NAME)).willReturn(subjectMockRightRights);
		given(repository.findOne(1L)).willReturn(subjectMockRightRights);
		given(repository.findByIdentifier("identifier")).willReturn(subjectMockRightRights);
		given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockRightRights);
		given(repository.findFromCenterCode("centerCode")).willReturn(subjectMockRightRights);
		assertAccessAuthorized(service::findByData, NAME);
		assertAccessAuthorized(service::findById, 1L);
		assertAccessAuthorized(service::findByIdentifier, "identifier");
		assertAccessAuthorized(service::findByIdWithSubjecStudies, 1L);
		assertAccessAuthorized(service::findSubjectFromCenterCode, "centerCode");
	}

	private void testCreate() throws ShanoirException {
		List<Study> studiesMock;
		
		// Create subject without subject <-> study
		Subject newSubjectMock = buildSubjectMock(null);
		assertAccessDenied(service::create, newSubjectMock);
		assertAccessDenied((t, u) -> { try { api.saveNewSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, newSubjectMock, mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewOFSEPSubject(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectStudyCardIdDTO(), mockBindingResult);
		assertAccessDenied((t, u) -> { try { api.saveNewOFSEPSubjectFromShup(t, u); } catch (RestServiceException e) { fail(e.toString()); }}, new SubjectFromShupDTO(), mockBindingResult);
		
		// Create subject 
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(9L));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 9L }))).willReturn(studiesMock);
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 9L);
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject linked to a study where I can admin, download, see all but not import.
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(10L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 10L }))).willReturn(studiesMock);
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 10L);
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject linked to a study where I can import and also to a study where I can't.
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(11L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
		studiesMock.add(buildStudyMock(12L, StudyUserRight.CAN_IMPORT));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 12L, 11L }))).willReturn(studiesMock);
		given(studyRepository.findAll(Arrays.asList(new Long[] { 11L, 12L }))).willReturn(studiesMock);
		newSubjectMock = buildSubjectMock(null);
		addStudyToMock(newSubjectMock, 11L);
		addStudyToMock(newSubjectMock, 12L);
		System.out.println("0. " + newSubjectMock.getSubjectStudyList());
		assertAccessDenied(service::create, newSubjectMock);
		
		// Create subject linked to a study where I can import
		studiesMock = new ArrayList<>();
		studiesMock.add(buildStudyMock(13L, StudyUserRight.CAN_IMPORT));
		given(studyRepository.findAll(Arrays.asList(new Long[] { 13L }))).willReturn(studiesMock);
		addStudyToMock(newSubjectMock, 13L);
		assertAccessAuthorized(service::create, newSubjectMock);
	}
	
	private Study buildStudyMock(Long id, StudyUserRight... rights) {
		Study study = ModelsUtil.createStudy();
		study.setId(id);
		List<StudyUser> studyUserList = new ArrayList<>();
		for (StudyUserRight right : rights) {
			StudyUser studyUser = new StudyUser();
			studyUser.setUserId(LOGGED_USER_ID);
			studyUser.setStudyId(id);
			studyUser.setStudyUserRight(right);
			studyUserList.add(studyUser);			
		}
		study.setStudyUserList(studyUserList);
		return study;		
	}
	
	private Subject buildSubjectMock(Long id) {
		Subject subject = ModelsUtil.createSubject();
		subject.setId(id);
		return subject;
	}
	
	private void addStudyToMock(Subject mock, Long id, StudyUserRight... rights) {
		Study study = buildStudyMock(id, rights);
		
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setSubject(mock);
		subjectStudy.setStudy(study);
		
		if (study.getSubjectStudyList() == null) study.setSubjectStudyList(new ArrayList<SubjectStudy>());
		if (mock.getSubjectStudyList() == null) mock.setSubjectStudyList(new ArrayList<SubjectStudy>());
		study.getSubjectStudyList().add(subjectStudy);
		mock.getSubjectStudyList().add(subjectStudy);
	}
	
	private SubjectStudyCardIdDTO convert(Subject subject) {
		SubjectStudyCardIdDTO dto = new SubjectStudyCardIdDTO();
		dto.setStudyCardId(1L);
		dto.setSubject(subject);
		return dto;
	}

}
