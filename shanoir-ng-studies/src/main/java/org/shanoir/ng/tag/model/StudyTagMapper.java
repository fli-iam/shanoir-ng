package org.shanoir.ng.tag.model;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(StudyTagDecorator.class)
public interface StudyTagMapper {

	/**
	 * Map list of @Tag to list of @TagDTO.
	 *
	 * @param studyTagList
	 *            list of tags
	 * @return list of DTO.
	 */
	List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTagList);

	/**
	 * Map a @Tag to a @TagDTO.
	 * 
	 * @param studyTag
	 * @return DTO.
	 */
	StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag);
}
