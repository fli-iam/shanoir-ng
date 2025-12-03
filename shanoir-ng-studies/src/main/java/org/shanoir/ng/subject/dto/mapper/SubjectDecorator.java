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
