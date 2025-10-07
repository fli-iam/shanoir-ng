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

package org.shanoir.ng.datasetacquisition.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.dicom.web.SeriesInstanceUIDHandler;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.service.SecurityService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;

@Service
public class DatasetAcquisitionServiceImpl implements DatasetAcquisitionService {


    @Autowired
    private DatasetAcquisitionRepository repository;

    @Autowired
    private ExaminationRepository examRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ShanoirEventService shanoirEventService;

    @Autowired
    private SolrService solrService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private SeriesInstanceUIDHandler seriesInstanceUIDHandler;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetAcquisitionServiceImpl.class);
    @Override
    public List<DatasetAcquisition> findByStudyCard(Long studyCardId) {
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return repository.findByStudyCardId(studyCardId);
        } else {
            List<Pair<Long, Long>> studyCenters = new ArrayList<>();
            Set<Long> unrestrictedStudies = new HashSet<Long>();
            securityService.getStudyCentersAndUnrestrictedStudies(studyCenters, unrestrictedStudies);
            return repository.findByStudyCardIdAndStudyCenterOrStudyIdIn(studyCardId, studyCenters, unrestrictedStudies);
        }
    }

    @Override
    public List<DatasetAcquisition> findByDatasetId(Long[] datasetIds) {
        return repository.findDistinctByDatasetsIdIn(datasetIds);
    }

    @Override
    public List<DatasetAcquisition> findByExamination(Long examinationId) {
        Optional<Examination> exam = examRepository.findByIdWithEagerAcquisitions(examinationId);
        if (exam.isPresent()) {
            return exam.get().getDatasetAcquisitions();
        } else {
            return Collections.emptyList();
        }
    }

    private DatasetAcquisition updateValues(DatasetAcquisition from, DatasetAcquisition to) {
        to.setAcquisitionEquipmentId(from.getAcquisitionEquipmentId());
        to.setExamination(from.getExamination());
        to.setDatasets(from.getDatasets());
        to.setRank(from.getRank());
        to.setSoftwareRelease(from.getSoftwareRelease());
        to.setSortingIndex(from.getSortingIndex());
        to.setStudyCard(from.getStudyCard());
        to.setAcquisitionStartTime(from.getAcquisitionStartTime()); // immutable
        return to;
    }

    @Override
    public DatasetAcquisition findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<DatasetAcquisition> findById(List<Long> ids) {
        return Utils.toList(repository.findAllById(ids));
    }

    @Override
    public Page<DatasetAcquisition> findPage(final Pageable pageable) {
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return repository.findAll(pageable);
        } else {
            List<Pair<Long, Long>> studyCenters = new ArrayList<>();
            Set<Long> unrestrictedStudies = new HashSet<Long>();
            securityService.getStudyCentersAndUnrestrictedStudies(studyCenters, unrestrictedStudies);
            return repository.findPageByStudyCenterOrStudyIdIn(studyCenters, unrestrictedStudies, pageable);
        }
    }

    @Override
    public Collection<DatasetAcquisition> createAll(Collection<DatasetAcquisition> acquisitions) {
        Iterable<DatasetAcquisition> result = this.repository.saveAll(acquisitions);
        for (DatasetAcquisition acquisition: result) {
            shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_ACQUISITION_EVENT, acquisition.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, acquisition.getExamination().getStudyId()));
        }
        return StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public DatasetAcquisition create(DatasetAcquisition entity) {
        DatasetAcquisition savedEntity = repository.save(entity);
        shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_ACQUISITION_EVENT, entity.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, entity.getExamination().getStudyId()));
        return savedEntity;
    }

    @Override
    public DatasetAcquisition update(DatasetAcquisition entity) throws EntityNotFoundException {
        final DatasetAcquisition entityDb = repository.findById(entity.getId()).orElse(null);
        if (entityDb == null) {
            throw new EntityNotFoundException(entity.getClass(), entity.getId());
        }
        updateValues(entity, entityDb);
        DatasetAcquisition acq = repository.save(entityDb);

        shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_DATASET_ACQUISITION_EVENT, entity.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, entity.getExamination().getStudyId()));

        return acq;
    }

    @Override
    public Iterable<DatasetAcquisition> update(List<DatasetAcquisition> entities) {
        List<Long> ids = new ArrayList<>();
        for (DatasetAcquisition acq : entities) {
            ids.add(acq.getId());
        }
        final Iterable<DatasetAcquisition> entitiesDb = repository.findAllById(ids);
        // the doc says the order is not guaranteed for findAllById
        Map<Long, DatasetAcquisition> entityMap = new HashMap<Long, DatasetAcquisition>();
        for (DatasetAcquisition entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        for (DatasetAcquisition db : entitiesDb) {
            DatasetAcquisition entity = entityMap.get(db.getId());
            if (entity != null) {
                updateValues(entity, db);
            } else {
                throw new IllegalStateException("method input entities should match entitieDb from the database");
            }
        }
        Iterable<DatasetAcquisition> updatedAcqs = repository.saveAll(entitiesDb);

        for (DatasetAcquisition db : updatedAcqs) {
            shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_DATASET_ACQUISITION_EVENT, db.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, db.getExamination().getStudyId()));
        }
        return updatedAcqs;
    }

    /**
     * Deletes children datasets then delete current acquisition
     * @param entity
     * @param event
     * @throws ShanoirException
     * @throws SolrServerException
     * @throws IOException
     * @throws RestServiceException
     */
    private void delete(DatasetAcquisition entity, ShanoirEvent event) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        // Do not delete entity if it is the source. If getSourceId() is not null, it means it's a copy
        List<DatasetAcquisition> childDsAc = repository.findBySourceId(entity.getId());
        if (!CollectionUtils.isEmpty(childDsAc)) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "This datasetAcquisition is linked to another datasetAcquisition that was copied."
                    ));
        } else {
            List<Dataset> datasets = entity.getDatasets();
            if (datasets != null) {
                List<Long> datasetIds = new ArrayList<>();
                for (Dataset ds : datasets) {
                    if (event != null) {
                        event.setMessage("Delete examination - dataset with id : " + ds.getId());
                        float progressMax = Float.valueOf(event.getEventProperties().get("progressMax"));
                        event.setProgress(event.getProgress() + (1f / progressMax));
                        shanoirEventService.publishEvent(event);
                    }

                    datasetIds.add(ds.getId());
                    datasetService.deleteByIdCascade(ds.getId());
                }
                if (!datasetIds.isEmpty()) solrService.deleteFromIndex(datasetIds);
            }
        }
    }

    /**
     * Call by acquisition-details > delete. Also reject current acquisition from pacs
     * @param id
     * @param event
     * @throws ShanoirException
     * @throws SolrServerException
     * @throws IOException
     * @throws RestServiceException
     */
    @Override
    @Transactional
    public void deleteById(Long id, ShanoirEvent event) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        final DatasetAcquisition acquisition = repository.findById(id).orElse(null);
        if (acquisition == null) {
            throw new EntityNotFoundException("Cannot find entity with id = " + id);
        }
        delete(acquisition, event);

        String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUID(acquisition.getExamination());
        String seriesInstanceUID = seriesInstanceUIDHandler.findSeriesInstanceUID(acquisition);

        if (acquisition.getSource() == null)
            dicomWebService.rejectAcquisitionFromPacs(studyInstanceUID, seriesInstanceUID);

        repository.deleteById(id);
        shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_DATASET_ACQUISITION_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, acquisition.getExamination().getStudyId()));
    }

    /**
     * Called by examination delete. Does not call reject from pacs as examination delete already does it
     * @param id
     * @param event
     * @throws ShanoirException
     * @throws SolrServerException
     * @throws IOException
     * @throws RestServiceException
     */
    public void deleteByIdCascade(Long id, ShanoirEvent event) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        final DatasetAcquisition entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntityNotFoundException("Cannot find entity with id = " + id);
        }
        delete(entity, event);

        repository.deleteById(id);
        shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_DATASET_ACQUISITION_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, entity.getExamination().getStudyId()));
    }

    @Override
    public boolean existsByStudyCardId(Long studyCardId) {
        return repository.existsByStudyCard_Id(studyCardId);
    }

}
