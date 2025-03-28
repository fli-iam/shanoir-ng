package org.shanoir.ng.subject.dto.mapper;

import java.util.List;
import java.util.function.Function;

import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public class SubjectDecorator implements SubjectMapper {

    @Autowired
    private SubjectMapper delegate;
    
    @Override
    public PageImpl<SubjectDTO> subjectsToSubjectDTOs(Page<Subject> page) {
        Page<SubjectDTO> mappedPage = page.map(new Function<Subject, SubjectDTO>() {
            public SubjectDTO apply(Subject entity) {
                return subjectToSubjectDTONoStudies(entity);
            }
        });
        return new PageImpl<>(mappedPage);
    }

    @Override
    public SubjectDTO subjectToSubjectDTONoStudies(Subject subject) {
        return delegate.subjectToSubjectDTONoStudies(subject);
    }
    
    @Override
    public SubjectDTO subjectToSubjectDTO(Subject subject) {
        return delegate.subjectToSubjectDTO(subject);
    }

    @Override
    public List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects) {
        return delegate.subjectsToSubjectDTOs(subjects);
    }

}
