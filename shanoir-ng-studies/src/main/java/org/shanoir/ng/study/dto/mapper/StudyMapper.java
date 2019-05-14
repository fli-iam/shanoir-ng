package org.shanoir.ng.study.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.timepoint.TimepointMapper;

/**
 * Mapper for studies.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { TimepointMapper.class })
@DecoratedWith(StudyDecorator.class)
public interface StudyMapper {

	/**
	 * Map list of @Study to list of @StudyDTO.
	 * 
	 * @param studies
	 *            list of studies.
	 * @return list of studies DTO.
	 */
	List<StudyDTO> studiesToStudyDTOs (List<Study> studies);

	/**
	 * Map a @Study to a @StudyDTO.
	 * 
	 * @param study
	 *            study to map.
	 * @return study DTO.
	 */
	@Mappings({ @Mapping(target = "experimentalGroupsOfSubjects", ignore = true),
			@Mapping(target = "nbExaminations", ignore = true),
			@Mapping(target = "nbSujects", ignore = true), @Mapping(target = "studyCards", ignore = true),
			@Mapping(target = "studyCenterList", ignore = true), @Mapping(target = "subjectStudyList", ignore = true) })
	StudyDTO studyToStudyDTO (Study study);
	
	@Mappings({ @Mapping(target = "studyCenterList", ignore = true) })
	SimpleStudyDTO studyToSimpleStudyDTO (Study study);
	
	List<SimpleStudyDTO> studiesToSimpleStudyDTOs (List<Study> studies);
	
	IdNameDTO studyToIdNameDTO (Study study);
	
	List<IdNameDTO> studiesToIdNameDTOs (List<Study> studies);

}
