package org.shanoir.ng.examination.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
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

	/**
	 * Map a @Examination to a @ExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return examination DTO.
	 */
	@Mappings({ @Mapping(target = "center", ignore = true), @Mapping(target = "study", ignore = true),
		@Mapping(target = "subject", ignore = true)})
	ExaminationDTO examinationToExaminationDTO(Examination examination);

	/**
	 * Map a @ExaminationDTO to a @Examination.
	 * 
	 * @param examinationDTO
	 *
	 * @return examination.
	 */
	@Mappings({ @Mapping(source="subject.id", target = "subjectId"), 
		@Mapping(source="center.id", target = "centerId"), 
		@Mapping(source="study.id", target = "studyId")
	})
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
