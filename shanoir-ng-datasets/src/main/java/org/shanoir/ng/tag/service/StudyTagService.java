package org.shanoir.ng.tag.service;

import org.apache.commons.collections4.IterableUtils;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.repository.StudyTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyTagService {

    @Autowired
    private StudyTagRepository repository;

    public List<StudyTag> findByIds(List<Long> ids){
        return IterableUtils.toList(repository.findAllById(ids));
    }

    public boolean existsById(Long id){
        return repository.existsById(id);
    }

}
