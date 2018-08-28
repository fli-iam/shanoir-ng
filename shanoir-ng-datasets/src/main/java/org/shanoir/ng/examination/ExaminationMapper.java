package org.shanoir.ng.examination;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionMapper;
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
	 * Map list of @Examination to list of @SubjectExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return list of subject examination DTO.
	 */
	List<SubjectExaminationDTO> examinationsToSubjectExaminationDTOs(List<Examination> examinations);

	@Mappings({ @Mapping(target = "centerName", ignore = true), @Mapping(target = "studyName", ignore = true),
			@Mapping(target = "subject", ignore = true) })
	/**
	 * Map a @Examination to a @ExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return examination DTO.
	 */
	ExaminationDTO examinationToExaminationDTO(Examination examination);

	/**
	 * Map a @Examination to a @SubjectExaminationDTO.
	 * 
	 * @param examination
	 *            examination to map.
	 * @return subject examination DTO.
	 */
	SubjectExaminationDTO examinationToSubjectExaminationDTO(Examination examination);

}
