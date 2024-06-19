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
	public StudyTag studyTagDTOToStudyTag(StudyTagDTO studyTagDTO) { return delegate.studyTagDTOToStudyTag(studyTagDTO); }

	@Override
	public List<StudyTag> studyTagDTOListToStudyTagList(List<StudyTagDTO> studyTagDTOs) {
		final List<StudyTag> studyTags = new ArrayList<>();
		if (studyTagDTOs != null) {
			for (StudyTagDTO studyTagDTO : studyTagDTOs) {
				studyTags.add(studyTagDTOToStudyTag(studyTagDTO));
			}
		}
		return studyTags;
	}

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
