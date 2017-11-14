package org.shanoir.ng.subject;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.SubjectStudyDTO;

@Mapper(componentModel= "spring")
public interface SubjectMapper {

    Subject subjectDTOToSubject(SubjectDTO subjectDTO);

    SubjectDTO subjectToSubjectDTO(Subject subject);
   
    List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects);

	@Mappings({
	@Mapping(target = "subjectType",ignore=true)})
	SubjectStudy subjectStudyDTOToSubjectStudy(SubjectStudyDTO subjectStudyDTO);
	
	SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy);
	
	List<SubjectStudyDTO> subjectStudyToSubjectStudyDTO(List<SubjectStudy> subjectStudy);

}
