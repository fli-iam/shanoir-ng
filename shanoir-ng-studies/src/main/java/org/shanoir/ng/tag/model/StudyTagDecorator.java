package org.shanoir.ng.tag.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class StudyTagDecorator implements StudyTagMapper {

	@Autowired
	private StudyTagMapper delegate;

	@Override
	public StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag) { return delegate.studyTagToStudyTagDTO(studyTag); }
	
	@Override
	public List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTagList) {
		final List<StudyTagDTO> studyTagDTOs = new ArrayList<>();
		if (studyTagList != null) {
			for (StudyTag studyTag : studyTagList) {
				studyTagDTOs.add(studyTagToStudyTagDTO(studyTag));
			}
		}
		return studyTagDTOs;
	}
}
