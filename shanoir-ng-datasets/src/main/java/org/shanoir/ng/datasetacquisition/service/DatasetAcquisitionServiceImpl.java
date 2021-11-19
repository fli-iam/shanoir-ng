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

import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetAcquisitionServiceImpl implements DatasetAcquisitionService {

	
	@Autowired
	private DatasetAcquisitionRepository repository;
	

	@Autowired
	private StudyUserRightsRepository rightsRepository;


	@Autowired
	private ShanoirEventService shanoirEventService;

	@Autowired
	private SolrService solrService;
	
	@Autowired
	private DatasetService datasetService;
	
	@Override
	public List<DatasetAcquisition> findByStudyCard(Long studyCardId) {
		return repository.findByStudyCardId(studyCardId);
	}
	
	@Override
	public List<DatasetAcquisition> findByExamination(Long examinationId) {
		return repository.findByExaminationId(examinationId);
	}

	private DatasetAcquisition updateValues(DatasetAcquisition from, DatasetAcquisition to) {
		to.setAcquisitionEquipmentId(from.getAcquisitionEquipmentId());
		to.setExamination(from.getExamination());
		to.setDatasets(from.getDatasets());
		to.setRank(from.getRank());
		to.setSoftwareRelease(from.getSoftwareRelease());
		to.setSortingIndex(from.getSortingIndex());
		to.setStudyCard(from.getStudyCard());
		return to;
	}

	@Override
	public DatasetAcquisition findById(Long id) {
		return repository.findById(id).orElse(null);
	}

	@Override
	public Page<DatasetAcquisition> findPage(final Pageable pageable) {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return repository.findAll(pageable);
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());
			return repository.findByExaminationStudyIdIn(studyIds, pageable);
		}
	}

	@Override
	public DatasetAcquisition create(DatasetAcquisition entity) {
		DatasetAcquisition savedEntity = repository.save(entity);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_ACQUISITION_EVENT, entity.getId().toString(), KeycloakUtil.getTokenUserId(null), "", ShanoirEvent.SUCCESS));
		
		return savedEntity;
	}

	@Override
	public DatasetAcquisition update(DatasetAcquisition entity) throws EntityNotFoundException {
		final DatasetAcquisition entityDb = repository.findById(entity.getId()).orElse(null);
		if (entityDb == null) {
			throw new EntityNotFoundException(entity.getClass(), entity.getId());
		}
		updateValues(entity, entityDb);
		DatasetAcquisition acq =  repository.save(entityDb);
		
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_DATASET_ACQUISITION_EVENT, entity.getId().toString(), KeycloakUtil.getTokenUserId(null), "", ShanoirEvent.SUCCESS));

		return acq;
	}

	@Override
	@Transactional
	public void deleteById(Long id) throws EntityNotFoundException {
		final DatasetAcquisition entity = repository.findById(id).orElse(null);
		if (entity == null) {
			throw new EntityNotFoundException("Cannot find entity with id = " + id);
		}
		// Remove from solr index
		if (entity.getDatasets() != null) {
			for (Dataset ds : entity.getDatasets()) {
				solrService.deleteFromIndex(ds.getId());
				datasetService.deleteDatasetFromPacs(ds);
			}
		}
		repository.deleteById(id);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_DATASET_ACQUISITION_EVENT, id.toString(), KeycloakUtil.getTokenUserId(null), "", ShanoirEvent.SUCCESS));
	}

}
