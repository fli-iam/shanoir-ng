package org.shanoir.ng.subjectstudy;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper for link between a subject and a study.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
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
	@Mappings({ @Mapping(target = "subjectId", source = "subject.id") })
	SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy);

}
