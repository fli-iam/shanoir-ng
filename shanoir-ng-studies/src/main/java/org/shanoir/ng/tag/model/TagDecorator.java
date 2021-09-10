package org.shanoir.ng.tag;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class TagDecorator implements TagMapper {

	@Autowired
	private TagMapper delegate;

	@Override
	public TagDTO tagToTagDTO(Tag tag) {
		return delegate.tagToTagDTO(tag);
	}
	
	@Override
	public List<TagDTO> tagListToTagDTOList(List<Tag> tagList) {
		final List<TagDTO> tagDTOs = new ArrayList<>();
		if (tagList != null) {
			for (Tag tag : tagList) {
				tagDTOs.add(tagToTagDTO(tag));
			}
		}
		return tagDTOs;
	}
}
