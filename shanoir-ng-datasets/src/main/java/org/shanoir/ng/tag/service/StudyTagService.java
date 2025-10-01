package org.shanoir.ng.tag.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.IterableUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.repository.StudyTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyTagService {

    @Autowired
    private StudyTagRepository repository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SolrService solrService;

    public List<StudyTag> findByIds(List<Long> ids) {
        return IterableUtils.toList(repository.findAllById(ids));
    }

    @Transactional
    public void addStudyTagsToDataset(Dataset dataset, List<Long> studyTagIds) throws SolrServerException, IOException {
        Set<StudyTag> datasetTags = new HashSet<>(dataset.getTags());

        for (StudyTag tag : findByIds(studyTagIds)) {
            if (tag.getStudy().getId().equals(dataset.getStudyId())) {
                datasetTags.add(tag);
            }
        }
        dataset.setTags(new ArrayList<>(datasetTags));
        datasetRepository.save(dataset);
        solrService.indexDataset(dataset.getId());
    }

    @Transactional
    public void removeStudyTagsFromDataset(Dataset dataset, List<Long> studyTagIds) throws SolrServerException, IOException {
        Set<StudyTag> datasetTags = new HashSet<>(dataset.getTags());

        for (StudyTag tag : findByIds(studyTagIds)) {
            if (tag.getStudy().getId().equals(dataset.getStudyId())) {
                datasetTags.remove(tag);
            }
        }

        dataset.setTags(new ArrayList<>(datasetTags));
        datasetRepository.save(dataset);
        solrService.indexDataset(dataset.getId());
    }
}
