package org.shanoir.ng.tag.model;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(StudyTagDecorator.class)
public interface StudyTagMapper {

	/**
	 * Map list of @StudyTag to list of @StudyTagDTO.
	 *
	 * @param studyTagList
	 *            list of tags
	 * @return list of DTO.
	 */
	List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTagList);

	/**
	 * Map list of @StudyTagDTO to list of @StudyTag.
	 *
	 * @param studyTags
	 * @return
	 */
	List<StudyTag> studyTagDTOListToStudyTagList(List<StudyTagDTO> studyTags);

	/**
	 * Map a @StudyTag to a @StudyTagDTO.
	 * 
	 * @param studyTag
	 * @return DTO.
	 */
	StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag);

	/**
	 * Map a @StudyTag to a @StudyTagDTO.
	 *
	 * @param studyTag
	 * @return DTO.
	 */
	StudyTag studyTagDTOToStudyTag(StudyTagDTO studyTagDTO);
}
