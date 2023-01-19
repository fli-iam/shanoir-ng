package org.shanoir.ng.tag.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class SubjectTagDecorator implements SubjectTagMapper {

	@Autowired
	private SubjectTagMapper delegate;

	@Override
	public SubjectTagDTO subjectTagToSubjectTagDTO(SubjectTag subjectTag) { return delegate.subjectTagToSubjectTagDTO(subjectTag); }
	
	@Override
	public List<SubjectTagDTO> subjectTagListToSubjectTagDTOList(List<SubjectTag> subjectTagList) {
		final List<SubjectTagDTO> subjectTagDTOs = new ArrayList<>();
		if (subjectTagList != null) {
			for (SubjectTag subjectTag : subjectTagList) {
				subjectTagDTOs.add(subjectTagToSubjectTagDTO(subjectTag));
			}
		}
		return subjectTagDTOs;
	}
}
