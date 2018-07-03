package org.shanoir.ng.subjectstudy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Decorator for link between a subject and a study.
 * 
 * @author msimon
 *
 */
public class SubjectStudyDecorator implements SubjectStudyMapper {

	@Autowired
	private SubjectStudyMapper delegate;

	@Override
	public List<SubjectStudyDTO> subjectStudyListToSubjectStudyDTOList(List<SubjectStudy> subjectStudies) {
		final List<SubjectStudyDTO> subjectStudyDTOs = new ArrayList<>();
		for (SubjectStudy subjectStudy : subjectStudies) {
			subjectStudyDTOs.add(subjectStudyToSubjectStudyDTO(subjectStudy));
		}
		return subjectStudyDTOs;
	}

	@Override
	public SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy) {
		final SubjectStudyDTO subjectStudyDTO = delegate.subjectStudyToSubjectStudyDTO(subjectStudy);
		if (!StringUtils.isEmpty(subjectStudy.getSubjectStudyIdentifier())) {
			subjectStudyDTO.setSubjectStudyIdentifier(subjectStudy.getSubjectStudyIdentifier());
		}
		return subjectStudyDTO;
	}

}
