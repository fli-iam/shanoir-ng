package org.shanoir.ng.study;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.groupofsubjects.ExperimentalGroupOfSubjectsMapper;

/**
 * Mapper for studies.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { ExperimentalGroupOfSubjectsMapper.class })
@DecoratedWith(StudyDecorator.class)
public interface StudyMapper {

	/**
	 * Map list of @Study to list of @StudyDTO.
	 * 
	 * @param studies
	 *            list of studies.
	 * @return list of studies DTO.
	 */
	List<StudyDTO> studiesToStudyDTOs(List<Study> studies);

	/**
	 * Map a @Study to a @StudyDTO.
	 * 
	 * @param study
	 *            study to map.
	 * @return study DTO.
	 */
	@Mappings({ @Mapping(target = "centers", ignore = true), @Mapping(target = "membersCategories", ignore = true),
			@Mapping(target = "nbSujects", ignore = true), @Mapping(target = "studyCards", ignore = true),
			@Mapping(target = "subjects", ignore = true) })
	StudyDTO studyToStudyDTO(Study study);

}
