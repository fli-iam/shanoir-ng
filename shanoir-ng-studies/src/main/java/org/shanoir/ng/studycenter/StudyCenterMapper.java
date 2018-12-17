package org.shanoir.ng.studycenter;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper for link between a study and a center.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
@DecoratedWith(StudyCenterDecorator.class)
public interface StudyCenterMapper {

	/**
	 * Map list of @StudyCenter to list of @StudyCenterDTO.
	 * 
	 * @param studyCenterList
	 *            list of links between a study and a center.
	 * @return list of DTO.
	 */
	List<StudyCenterDTO> studyCenterListToStudyCenterDTOList(List<StudyCenter> studyCenterList);

	/**
	 * Map a @StudyCenter to a @StudyCenterDTO.
	 * 
	 * @param study
	 *            link between a study and a center.
	 * @return DTO.
	 */
	@Mappings({ @Mapping(target = "study.id", source = "study.id"),
		@Mapping(target = "center.id", source = "center.id"),
		@Mapping(target = "center.name", source = "center.name"),
		@Mapping(target = "study.name", source = "study.name")})
	StudyCenterDTO studyCenterToStudyCenterDTO(StudyCenter studyCenter);

}
