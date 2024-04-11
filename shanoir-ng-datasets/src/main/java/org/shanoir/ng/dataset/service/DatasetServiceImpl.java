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

import jakarta.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.VolumeByFormatDTO;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dataset service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private DatasetRepository repository;

	@Autowired
	private StudyUserRightsRepository rightsRepository;

	@Autowired
	private ShanoirEventService shanoirEventService;

	@Autowired
	private SolrService solrService;

	@Autowired
	private DICOMWebService dicomWebService;

	@Autowired
	private DatasetPropertyService propertyService;

	@Value("${dcm4chee-arc.dicom.web}")
	private boolean dicomWeb;
	@Autowired
	private DatasetProcessingService processingService;

	@Override
	@Transactional
	public void deleteById(final Long id) throws ShanoirException, SolrServerException, IOException, RestServiceException {

		List<Dataset> childDatasets = repository.findBySourceId(id);

		if (!CollectionUtils.isEmpty(childDatasets)) {
			throw new RestServiceException(
					new ErrorModel(
							HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"This dataset is linked to another dataset that was copied."
					));
		}

		final Dataset dataset = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(Dataset.class, id));

		processingService.removeDatasetFromAllProcessingInput(id);
		propertyService.deleteByDatasetId(id);
		repository.deleteById(id);

		if (dataset.getSourceId() == null) {
			this.deleteDatasetFromPacs(dataset);
		}
		solrService.deleteFromIndex(id);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_DATASET_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, dataset.getStudyId()));
	}

	@Override
	public void deleteDatasetFromPacs(Dataset dataset) throws ShanoirException {
        if (!dicomWeb) {
            return;
        }
        for (DatasetExpression expression : dataset.getDatasetExpressions()) {

			boolean isDicom = DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat());

			for (DatasetFile file : expression.getDatasetFiles()) {
				if(isDicom && file.isPacs()){
					dicomWebService.deleteDicomFilesFromPacs(file.getPath());
				} else if (!file.isPacs()) {
					try {
						URL url = new URL(file.getPath().replaceAll("%20", " "));
						File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));
						FileUtils.deleteQuietly(srcFile);
					} catch (MalformedURLException e) {
						throw new ShanoirException("Error while deleting dataset file", e);
					}
				}

			}
        }
    }

	@Override
	@Transactional
	public void deleteByIdIn(List<Long> ids) throws ShanoirException, SolrServerException, IOException, RestServiceException {
		for(Long id : ids){
			this.deleteById(id);
		}
    }

	@Override
	public Dataset findById(final Long id) {
		return repository.findById(id).orElse(null);
	}

	@Override
	public int countByStudyId(Long studyId) {
		return repository.countByDatasetAcquisition_Examination_Study_Id(studyId);
	}

	@Override
	public List<Dataset> findByIdIn(List<Long> ids) {
		return Utils.toList(repository.findAllById(ids));
	}

	@Override
	public Dataset create(final Dataset dataset) throws SolrServerException, IOException {
		Dataset ds = repository.save(dataset);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, ds.getStudyId()));
		return ds;
	}

	@Override
	public Dataset update(final Dataset dataset) throws EntityNotFoundException {
		final Dataset datasetDb = repository.findById(dataset.getId()).orElse(null);
		if (datasetDb == null) {
			throw new EntityNotFoundException(Dataset.class, dataset.getId());
		}
		this.updateDatasetValues(datasetDb, dataset);
		Dataset ds = repository.save(datasetDb);
		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, datasetDb.getStudyId()));
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
		datasetDb.setProcessings(dataset.getProcessings());
		datasetDb.setSubjectId(dataset.getSubjectId());
		if (dataset.getUpdatedMetadata().getId().equals(dataset.getOriginMetadata().getId())) {
			// Force creation of a new dataset metadata
			dataset.getUpdatedMetadata().setId(null);
		}
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
		}

		Long userId = KeycloakUtil.getTokenUserId();
		List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());

		// Check if user has restrictions.
		boolean hasRestrictions = false;
		List<StudyUser> studyUsers = Utils.toList(rightsRepository.findByUserId(userId));
		Map<Long, List<Long>> studyUserCenters = new HashMap<>();
		for (StudyUser studyUser : studyUsers) {
			if (! CollectionUtils.isEmpty(studyUser.getCenterIds())) {
				hasRestrictions = true;
				studyUserCenters.put(studyUser.getStudyId(), studyUser.getCenterIds());
			}
		}

		if (!hasRestrictions) {
			return repository.findByDatasetAcquisitionExaminationStudy_IdIn(studyIds, pageable);
		}

		// If yes, get all examinations and filter by centers
		List<Dataset> datasets = Utils.toList(repository.findByDatasetAcquisitionExaminationStudy_IdIn(studyIds, pageable.getSort()));

		if (CollectionUtils.isEmpty(datasets)) {
			return new PageImpl<>(datasets);
		}

		datasets = datasets.stream().filter(ds ->
				studyUserCenters.get(ds.getStudyId()) != null &&
				studyUserCenters.get(ds.getStudyId()).contains(ds.getDatasetAcquisition().getExamination().getCenterId()))
				.collect(Collectors.toList());
		int size = datasets.size();

		datasets = datasets.subList(pageable.getPageSize() * pageable.getPageNumber(), Math.min(datasets.size(), pageable.getPageSize() * (pageable.getPageNumber() + 1)));

		Page<Dataset> page = new PageImpl<>(datasets, pageable, size);
		return page;
	}

	@Override
	public List<Dataset> findByStudyId(Long studyId) {
		return Utils.toList(repository.findByDatasetAcquisition_Examination_Study_Id(studyId));
	}

	@Override
	public List<VolumeByFormatDTO> getVolumeByFormat(Long studyId) {
		List<Object[]> results = repository.findExpressionSizesByStudyIdGroupByFormat(studyId);
		List<VolumeByFormatDTO> sizesByFormat = new ArrayList<>();

		for(Object[] result : results){
			sizesByFormat.add(new VolumeByFormatDTO(DatasetExpressionFormat.getFormat((int) result[0]), (Long) result[1]));
		}

		return sizesByFormat;

	}

	@Override
	public Map<Long, List<VolumeByFormatDTO>> getVolumeByFormatByStudyId(List<Long> studyIds) {
		List<Object[]> results = repository.findExpressionSizesTotalByStudyIdGroupByFormat(studyIds);
		Map<Long, List<VolumeByFormatDTO>> sizesByFormatAndStudy = new HashMap<>();

		for(Long id : studyIds){
			sizesByFormatAndStudy.putIfAbsent(id, new ArrayList<>());
		}

		for(Object[] result : results){
			Long studyId = (Long) result[0];
			sizesByFormatAndStudy.get(studyId).add(new VolumeByFormatDTO(DatasetExpressionFormat.getFormat((int) result[1]), (Long) result[2]));
		}

		return sizesByFormatAndStudy;

	}

	@Override
	public List<Dataset> findByAcquisition(Long acquisitionId) {
		return Utils.toList(repository.findByDatasetAcquisitionId(acquisitionId));
	}
	
	@Override
	public List<Dataset> findByStudycard(Long studycardId) {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return Utils.toList(repository.findBydatasetAcquisitionStudyCardId(studycardId));
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());
			
			return Utils.toList(repository.findByDatasetAcquisitionStudyCardIdAndDatasetAcquisitionExaminationStudy_IdIn(studycardId, studyIds));
		}
	}

	@Override
	public List<Dataset> findByExaminationId(Long examinationId) {
		return Utils.toList(repository.findByDatasetAcquisitionExaminationId(examinationId));
	}

	@Override
	public List<Object[]> queryStatistics(String studyNameInRegExp, String studyNameOutRegExp, String subjectNameInRegExp, String subjectNameOutRegExp) throws Exception {
		return repository.queryStatistics(studyNameInRegExp, studyNameOutRegExp, subjectNameInRegExp, subjectNameOutRegExp);
	}

}
