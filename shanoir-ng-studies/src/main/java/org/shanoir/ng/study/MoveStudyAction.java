package org.shanoir.ng.study;

import java.util.Collections;

import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.study.service.StudyUserService;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * This class is used in administration system to completely move a study and all its data to another shanoir server.
 * 
 * 
 * @author Jean-Côme Douteau
 *
 */
public class MoveStudyAction {
	
	private final String CREATE_STUDY_API = "";
	
	@Autowired
	private StudyService studyService;

	@Autowired
	private StudyUserService studyUserService;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Moves a given study to the given shanoir server
	 * @param studyId the study ID to move
	 * @param shanoirServer the shanoir server on which the study will be created
	 * @return true if the study was successfuly moved, false otherwise
	 */
	public boolean moveStudy(Long studyId, String shanoirServer, Long distantUserId) {
		
		// TODO: use a shanoir event to get advancement and updates

//		Elements to be moved:
//			- Subject
//			-- reset ID
//
//			- Study
//			-- reset ID
//			-- reset SubjectStudy to update with subjects
//			-- Add protocol/ DUA files
//			-- Set StudyUser to only current user
//			-- reset studyExaminations

		Study studyToMove = studyService.findById(studyId);
		
		// 0. Move subjects
		// TODO: keep a map of oldsubject/newSubject for import => nope, check by name using endpoints?
		for (SubjectStudy subjectStudy : studyToMove.getSubjectStudyList()) {
			Subject subject = subjectStudy.getSubject();
			moveSubject(subject);
		}
		
		// 1. Clean the study (users / examinations)
		studyToMove.setExaminationIds(null);
		studyToMove.setExperimentalGroupsOfSubjects(null);
		studyToMove.setSubjectStudyList(null);
		
		// Set only one user => the distant user creating the study
		StudyUser studyUser = null;
		for (StudyUser su : studyToMove.getStudyUserList()) {
			if (su.getUserId().equals(KeycloakUtil.getTokenUserId())) {
				studyUser = su;
				break;
			}
		}
		if (studyUser == null) {
			return false;
		}
		studyUser.setUserId(distantUserId);
		studyUser.setId(null);
		studyToMove.setStudyUserList(Collections.singletonList(studyUser));
		
		// Create the study
		// TODO: distant call to create study
	
		// 2. Protocol file path and DUA
		for (String filePath : studyToMove.getDataUserAgreementPaths()) {
			// TODO: distant call to add files
		}
		for (String filePath : studyToMove.getProtocolFilePaths()) {
			// TODO: distant call to add files
		}
		
		// Once this is done, we update the ShanoirEvent that dataset microservice will catch and will continue the import
		// The problem is the link subject/study that has to be kept for datasets
		

		return true;
	}

	private void moveSubject(Subject subject) {
		subject.setId(null);
		// Clean subjectStudy as it will be updated when creating the subject
		subject.setSubjectStudyList(null);
		// distant call
	}
}
