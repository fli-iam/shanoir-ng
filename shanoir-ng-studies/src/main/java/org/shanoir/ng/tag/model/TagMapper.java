package org.shanoir.ng.tag;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(TagDecorator.class)
public interface TagMapper {

	/**
	 * Map list of @Tag to list of @TagDTO.
	 * 
	 * @param tagList
	 *            list of tags
	 * @return list of DTO.
	 */
	List<TagDTO> tagListToTagDTOList(List<Tag> tagList);

	/**
	 * Map a @Tag to a @TagDTO.
	 * 
	 * @param tag
	 * @return DTO.
	 */
	TagDTO tagToTagDTO(Tag tag);
}
