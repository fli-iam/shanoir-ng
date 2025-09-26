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
    public void delete(Long id){
        repository.deleteById(id);
    }



}
