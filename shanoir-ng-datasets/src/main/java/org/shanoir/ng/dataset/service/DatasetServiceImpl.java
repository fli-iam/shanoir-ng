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

package org.shanoir.ng.dataset.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Dataset service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class DatasetServiceImpl implements DatasetService {

    @PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private DatasetRepository repository;
	
	@Autowired
	private StudyUserRightsRepository rightsRepository;

	@Autowired
	private ShanoirEventService shanoirEventService;

	@Autowired
	private SolrService solrService;

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException {
		final Dataset datasetDb = repository.findOne(id);
		if (datasetDb == null) {
			throw new EntityNotFoundException(Dataset.class, id);
		}
		repository.delete(id);
		solrService.deleteFromIndex(id);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_DATASET_EVENT, id.toString(), KeycloakUtil.getTokenUserId(null), "", ShanoirEvent.SUCCESS));
	}

	@Override
	public Dataset findById(final Long id) {
		return repository.findOne(id);
	}

	@Override
	public List<Dataset> findByIdIn(List<Long> ids) {
		return Utils.toList(repository.findAll(ids));
	}

	@Override
	public Dataset create(final Dataset dataset) {
		Dataset ds = repository.save(dataset);
		solrService.indexDataset(ds.getId());
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(null), "", ShanoirEvent.SUCCESS));
		return ds;
	}

	@Override
	public Dataset update(final Dataset dataset) throws EntityNotFoundException {
		final Dataset datasetDb = repository.findOne(dataset.getId());
		if (datasetDb == null) {
			throw new EntityNotFoundException(Dataset.class, dataset.getId());
		}
		updateDatasetValues(datasetDb, dataset);
		Dataset ds = repository.save(datasetDb);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(null), "", ShanoirEvent.SUCCESS));
		return ds;
	}

	
	/**
	 * Update some values of dataset to save them in database.
	 * 
	 * @param datasetDb dataset found in database.
	 * @param dataset dataset with new values.
	 * @return database dataset with new values.
	 */
	private Dataset updateDatasetValues(final Dataset datasetDb, final Dataset dataset) {
		datasetDb.setCreationDate(dataset.getCreationDate());
		datasetDb.setId(dataset.getId());
		//datasetDb.setOriginMetadata(dataset.getOriginMetadata());
		//datasetDb.setProcessings(dataset.getProcessings());
		//datasetDb.setReferencedDatasetForSuperimposition(dataset.getReferencedDatasetForSuperimposition());
		//datasetDb.setReferencedDatasetForSuperimpositionChildrenList(dataset.getReferencedDatasetForSuperimpositionChildrenList());
		//datasetDb.setStudyId(dataset.getStudyId());
		datasetDb.setSubjectId(dataset.getSubjectId());
		datasetDb.setUpdatedMetadata(dataset.getUpdatedMetadata());
		if (dataset instanceof MrDataset) {
			MrDataset mrDataset = (MrDataset) dataset;
			((MrDataset) datasetDb).setUpdatedMrMetadata(mrDataset.getUpdatedMrMetadata());
		}
		return datasetDb;
	}

	@Override
	public List<Dataset> findAll() {
		return Utils.toList(repository.findAll());
	}

	@Override
	public Page<Dataset> findPage(final Pageable pageable) {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return repository.findAll(pageable);
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());
			
			return repository.findByDatasetAcquisitionExaminationStudyIdIn(studyIds, pageable);
		}
	}

	@Override
	public List<Dataset> findByStudyId(Long studyId) {
		return Utils.toList(repository.findByDatasetAcquisitionExaminationStudyId(studyId));
	}

	@Override
	public Object queryStatistics(String studyNameInRegExp, String studyNameOutRegExp, String subjectNameInRegExp, String subjectNameOutRegExp) {

        //"login" this is the name of your procedure
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getStatistics"); 

        //Declare the parameters in the same order
        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

        //Pass the parameter values
        query.setParameter(1, studyNameInRegExp);
        query.setParameter(2, studyNameOutRegExp);
        query.setParameter(3, subjectNameInRegExp);
        query.setParameter(4, subjectNameOutRegExp);

        //Execute query
        query.execute();

		//Get output parameters
		Object results = query.getSingleResult();
        return results;
	}

}
