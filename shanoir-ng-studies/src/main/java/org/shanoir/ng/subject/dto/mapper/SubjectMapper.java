package org.shanoir.ng.subject.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyMapper;

/**
 * Mapper for subjects.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { SubjectStudyMapper.class })
public interface SubjectMapper {

	SubjectDTO subjectToSubjectDTO(Subject subject);

	List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects);
	

}
