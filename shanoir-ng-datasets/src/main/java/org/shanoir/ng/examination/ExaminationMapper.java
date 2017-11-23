package org.shanoir.ng.examination;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * Mapper for examinations.
 * 
 * @author yyao
 *
 */
@Mapper(componentModel = "spring")
public interface ExaminationMapper {

	/**
	 * Map a Examination to a @ExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return examination DTO.
	 */
	ExaminationDTO examinationToExaminationDTO(Examination examination);
	
	/**
	 * Map list of @Examination to list of @ExaminationDTO.
	 * 
	 * @param examinations
	 *            list of examinations.
	 * @return list of examinations DTO.
	 */
	List<ExaminationDTO> examinationsToExaminationDTOs(List<Examination> examinations);

}
