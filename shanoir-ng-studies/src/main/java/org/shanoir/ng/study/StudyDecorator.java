package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.subject.SubjectStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Decorator for study.
 * 
 * @author msimon
 *
 */
public abstract class StudyDecorator implements StudyMapper {

	@Autowired
	@Qualifier("delegate")
	private StudyMapper delegate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.ng.study.StudyMapper#studiesToStudyDTOs(java.util.List)
	 */
	@Override
	public List<StudyDTO> studiesToStudyDTOs(List<Study> studies) {
		final List<StudyDTO> studyDTOs = new ArrayList<>();
		for (Study study : studies) {
			studyDTOs.add(studyToStudyDTO(study));
		}
		return studyDTOs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.shanoir.ng.study.StudyMapper#studyToStudyDTO(org.shanoir.ng.study.
	 * Study)
	 */
	@Override
	public StudyDTO studyToStudyDTO(Study study) {
		final StudyDTO studyDTO = delegate.studyToStudyDTO(study);
		if (study.getSubjectStudyList() != null && !study.getSubjectStudyList().isEmpty()) {
			studyDTO.setSubjectNames(new ArrayList<>());
			for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				studyDTO.getSubjectNames().add(subjectStudy.getSubject().getName());
			}
		}
		return studyDTO;
	}

}
