package org.shanoir.ng.tag.model;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(SubjectTagDecorator.class)
public interface SubjectTagMapper {

	/**
	 * Map list of @Tag to list of @TagDTO.
	 *
	 * @param subjectTagList
	 *            list of tags
	 * @return list of DTO.
	 */
	List<SubjectTagDTO> subjectTagListToSubjectTagDTOList(List<SubjectTag> subjectTagList);

	/**
	 * Map a @Tag to a @TagDTO.
	 * 
	 * @param subjectTag
	 * @return DTO.
	 */
	SubjectTagDTO subjectTagToSubjectTagDTO(SubjectTag subjectTag);
}
