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

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DatasetAcquisitionServiceImpl implements DatasetAcquisitionService {

	
	@Autowired
	private DatasetAcquisitionRepository repository;
	

	@Autowired
	private StudyUserRightsRepository rightsRepository;


	@Override
	public List<DatasetAcquisition> findByStudyCard(Long studyCardId) {
		return repository.findByStudyCardId(studyCardId);
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
		return repository.findOne(id);
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
		return savedEntity;
	}

	@Override
	public DatasetAcquisition update(DatasetAcquisition entity) throws EntityNotFoundException {
		final DatasetAcquisition entityDb = repository.findOne(entity.getId());
		if (entityDb == null) {
			throw new EntityNotFoundException(entity.getClass(), entity.getId());
		}
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	@Override
	public void deleteById(Long id) throws EntityNotFoundException {
		final DatasetAcquisition entity = repository.findOne(id);
		if (entity == null) {
			throw new EntityNotFoundException("Cannot find entity with id = " + id);
		}
		repository.delete(id);
	}

}
