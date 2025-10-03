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

package org.shanoir.ng.tag.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.StudyTagDTO;
import org.shanoir.ng.tag.repository.StudyTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyTagServiceImpl implements StudyTagService {

    @Autowired
    private StudyTagRepository repository;

    public StudyTag create(Study study, StudyTagDTO dto) {
        StudyTag tag = new StudyTag();
        tag.setStudy(study);
        tag.setColor(dto.getColor());
        tag.setName(tag.getName());
        return repository.save(tag);
    }

    @Override
    public void update(StudyTagDTO dto) throws EntityNotFoundException {
        StudyTag tag = repository.findById(dto.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Cannot find study tag with id [" + dto.getId() + "]"));
        tag.setColor(dto.getColor());
        tag.setName(tag.getName());
        repository.save(tag);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }



}
