package org.shanoir.ng.study.security;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.model.security.StudyUserRight;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudySecurityService {
	
	@Autowired
	StudyRepository studyRepository;
	
	@Autowired
	SubjectRepository subjectRepository;
		
	
	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
    	StudyUserRight right = StudyUserRight.valueOf(rightStr);
        Study study = studyRepository.findOne(studyId);
        if (study == null) throw new EntityNotFoundException("Cannot find study with id " + studyId); 
        return hasPrivilege(study, right);
    }
    
    /**
	 * Check that the connected user has the given right for the given study.
	 * ! ATTENTION ! This method is meant to be used with a trusted Study, meaning it should not be used with a 
     * Study object that comes from the user API but most likely from a Study coming from the database.
	 * 
	 * @param study the TRUSTED study
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnTrustedStudy(Study study, String rightStr) {
    	StudyUserRight right = StudyUserRight.valueOf(rightStr);
        return hasPrivilege(study, right);
    }
	
    /**
     * Check that the connected user has the given right in at least one study to which the subject participates.
     * 
     * @param subjectId the subject id
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnSubject(Long subjectId, String rightStr) {
    	Subject subject = subjectRepository.findOne(subjectId);
    	if (subject == null) throw new EntityNotFoundException("Cannot find subject with id " + subjectId);
    	StudyUserRight right = StudyUserRight.valueOf(rightStr);
    	for (SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
    		if (hasPrivilege(subjectStudy.getStudy(), right)) return true;
    	}
    	return false;
    }
    
    /**
     * Check that the connected user has the given right in at least one study to which the subject participates.
     * ! ATTENTION ! This method is meant to be used with a trusted Subject, meaning it should not be used with a 
     * Subject object that comes from the user API but most likely from a Subject coming from the database.
     * 
     * @param subject the TRUSTED subject 
     * @param rightStr
     * @return true or false
     */
    public boolean hasRightOnTrustedSubject(Subject subject, String rightStr) {
    	StudyUserRight right = StudyUserRight.valueOf(rightStr);
    	if (subject.getSubjectStudyList() != null) {
    		for (SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
    			if (hasPrivilege(subjectStudy.getStudy(), right)) return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Check that the connected user has the given right for all the studies linked inside the given list.
     * 
     * @param subjectStudyList the list of subject-study relationship objects
     * @param rightStr the right
     * @return true or false
     */
    public boolean checkRightOnSubjectStudyList(Iterable<SubjectStudy> subjectStudyList, String rightStr) {
    	if (subjectStudyList == null) return false;
    	StudyUserRight right = StudyUserRight.valueOf(rightStr);
    	List<Long> ids = new ArrayList<>();
    	System.out.println("1. " + subjectStudyList);
    	for (SubjectStudy subjectStudy : subjectStudyList) {
    		ids.add(subjectStudy.getStudy().getId());
    	}
    	int nbStudies = 0;
    	System.out.println("2. " + ids);
    	for (Study study : studyRepository.findAll(ids)) {
    		nbStudies ++;
    		if (!hasPrivilege(study, right)) return false;
    	}
    	if (nbStudies != ids.size()) return false;
    	return true;
    }
    
    /**
     * Check that the connected user has this right on this study.
     * 
     * @param study
     * @param neededRight
     * @return true or false
     */
    private boolean hasPrivilege(Study study, StudyUserRight neededRight) {
    	if (study == null) throw new IllegalArgumentException("study cannot be null");
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
		Long userId = KeycloakUtil.getTokenUserId();
		if (study.getStudyUserList() == null) return false;
		StudyUser studyUser = study.getStudyUserList().stream()
			.filter(su -> userId.equals(su.getUserId()))
			.findAny().orElse(null);
		if (studyUser == null) return false;
		return neededRight.equals(studyUser.getStudyUserRight());
    }
    
}