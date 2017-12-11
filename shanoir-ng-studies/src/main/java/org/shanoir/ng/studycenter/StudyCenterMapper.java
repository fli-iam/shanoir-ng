package org.shanoir.ng.studycenter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.center.CenterMapper;

/**
 * Mapper for link between a study and a center.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { CenterMapper.class })
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
	@Mappings({ @Mapping(target = "studyId", source = "study.id") })
	StudyCenterDTO studyCenterToStudyCenterDTO(StudyCenter studyCenter);

}
