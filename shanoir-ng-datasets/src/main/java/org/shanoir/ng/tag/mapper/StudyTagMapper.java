package org.shanoir.ng.tag.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.StudyTagDTO;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudyTagMapper {

	List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTags);

	StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag);

	List<StudyTag> StudyTagDTOListToStudyTagList(List<StudyTagDTO> dtos);

	StudyTag StudyTagDTOToStudyTag(StudyTagDTO dto);
}
