package org.shanoir.ng.subjectstudy.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;

/**
 * Mapper for link between a subject and a study.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
@DecoratedWith(SubjectStudyDecorator.class)
public interface SubjectStudyMapper {

	/**
	 * Map list of @SubjectStudy to list of @SubjectStudyDTO.
	 * 
	 * @param subjectStudies
	 *            list of links between a subject and a study.
	 * @return list of DTO.
	 */
	List<SubjectStudyDTO> subjectStudyListToSubjectStudyDTOList(List<SubjectStudy> subjectStudies);

	/**
	 * Map a @SubjectStudy to a @SubjectStudyDTO.
	 * 
	 * @param study
	 *            link between a subject and a study.
	 * @return DTO.
	 */
	@Mappings({ @Mapping(target = "subject.id", source = "subject.id"),
			@Mapping(target = "subject.name", source = "subject.name"),	
			@Mapping(target = "study.id", source = "study.id"),
			@Mapping(target = "study.name", source = "study.name"),	
			@Mapping(target = "subjectStudyIdentifier", ignore = true) })
	SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy);

}
