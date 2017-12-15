package org.shanoir.ng.groupofsubjects;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Mapper for experimental group of subjects.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
public interface ExperimentalGroupOfSubjectsMapper {

	/**
	 * Map list of @ExperimentalGroupOfSubjects to list of @IdNameDTO.
	 * 
	 * @param experimentalGroupsOfSubjects
	 *            list of experimental group of subjects.
	 * @return list of DTO with id and name.
	 */
	List<IdNameDTO> experimentalGroupOfSubjectsToIdNameDTOs(List<ExperimentalGroupOfSubjects> experimentalGroupsOfSubjects);

	/**
	 * Map a @ExperimentalGroupOfSubjects to a @StudyDTO.
	 * 
	 * @param experimentalGroupOfSubjects
	 *            experimental group of subjects to map.
	 * @return DTO with id and name.
	 */
	@Mappings({ @Mapping(target = "name", source = "groupName") })
	IdNameDTO experimentalGroupOfSubjectsToIdNameDTO(ExperimentalGroupOfSubjects experimentalGroupOfSubjects);

}
