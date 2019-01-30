/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.examination;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionMapper;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

/**
 * Mapper for examinations.
 * 
 * @author yyao
 *
 */
@Mapper(componentModel = "spring", uses = { DatasetAcquisitionMapper.class })
@DecoratedWith(ExaminationDecorator.class)
public interface ExaminationMapper {

	/**
	 * Map list of @Examination to list of @ExaminationDTO.
	 * 
	 * @param examinations
	 *            list of examinations.
	 * @return list of examinations DTO.
	 */
	PageImpl<ExaminationDTO> examinationsToExaminationDTOs(Page<Examination> examinations);

	/**
	 * Map list of @Examination to not pageable list of @ExaminationDTO.
	 * 
	 * @param examinations
	 *            list of examinations.
	 * @return list of examinations DTO.
	 */

	List<ExaminationDTO> examinationsToExaminationDTOs(List<Examination> examinations);
	
    
	/**
	 * Map list of @Examination to list of @SubjectExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return list of subject examination DTO.
	 */
	List<SubjectExaminationDTO> examinationsToSubjectExaminationDTOs(List<Examination> examinations);

	@Mappings({ @Mapping(target = "centerName", ignore = true), @Mapping(target = "studyName", ignore = true),
			@Mapping(target = "subject", ignore = true),@Mapping(source="subjectId", target = "subject.id") })
	/**
	 * Map a @Examination to a @ExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return examination DTO.
	 */
	ExaminationDTO examinationToExaminationDTO(Examination examination);

	/**
	 * Map a @ExaminationDTO to a @Examination.
	 * 
	 * @param examinationDTO
	 *
	 * @return examination.
	 */
	@Mapping(source="subject.id", target = "subjectId")
	Examination examinationDTOToExamination(ExaminationDTO examinationDTO);
	
	/**
	 * Map a @Examination to a @SubjectExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return subject examination DTO.
	 */
	SubjectExaminationDTO examinationToSubjectExaminationDTO(Examination examination);

}
