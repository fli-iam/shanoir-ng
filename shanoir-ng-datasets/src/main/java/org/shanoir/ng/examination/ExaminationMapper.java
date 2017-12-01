package org.shanoir.ng.examination;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper for examinations.
 * 
 * @author yyao
 *
 */
@Mapper(componentModel = "spring")
@DecoratedWith(ExaminationDecorator.class)
public interface ExaminationMapper {

	/**
	 * Map list of @Examination to list of @ExaminationDTO.
	 * 
	 * @param examinations
	 *            list of examinations.
	 * @return list of examinations DTO.
	 */
	List<ExaminationDTO> examinationsToExaminationDTOs(List<Examination> examinations);
	
	@Mappings({ @Mapping(target = "centerName", ignore = true), @Mapping(target = "studyName", ignore = true),
		@Mapping(target = "subject", ignore = true) })
	/**
	 * Map a Examination to a @ExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return examination DTO.
	 */
	ExaminationDTO examinationToExaminationDTO(Examination examination);
	
}
