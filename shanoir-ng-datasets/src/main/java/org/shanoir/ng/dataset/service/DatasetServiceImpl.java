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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetLight;
import org.shanoir.ng.dataset.dto.VolumeByFormatDTO;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
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
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.transaction.Transactional;

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

	@Value("${dcm4chee-arc.dicom.web}")
	private boolean dicomWeb;

	@Autowired
	private DatasetPropertyService propertyService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DatasetProcessingService processingService;

	@Autowired
	private DatasetExpressionRepository datasetExpressionRepository;

	@Autowired
	private DatasetAsyncService datasetAsyncService;

	@Autowired
	private ProcessingResourceRepository processingResourceRepository;

	private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

	private void delete(Dataset entity) throws ShanoirException, SolrServerException, IOException, RestServiceException {
		Long id = entity.getId();

		// Remove parent processing to avoid errors
		entity.setDatasetProcessing(null);
		processingService.removeDatasetFromAllProcessingInput(id);
		processingResourceRepository.deleteByDatasetId(id);
		propertyService.deleteByDatasetId(id);
		repository.deleteById(id);
	}

	/**
	 * Call by dataset-details. Also reject from pacs
	 * @param id dataset id.
	 * @throws ShanoirException
	 * @throws SolrServerException
	 * @throws IOException
	 * @throws RestServiceException
	 */
	@Override
	@Transactional
	public void deleteById(final Long id) throws ShanoirException, SolrServerException, IOException, RestServiceException {
		final Dataset dataset = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(Dataset.class, id));
		// Do not delete entity if it is the source (or if it has copies). If getSourceId() is not null, it means it's a copy
		if (!CollectionUtils.isEmpty(dataset.getCopies())) {
			throw new RestServiceException(
					new ErrorModel(
							HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"This dataset is linked to another dataset that was copied."
					));
		}
		long startTime = System.currentTimeMillis();
		delete(dataset);
		deleteDatasetFilesFromDiskAndPacs(dataset);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        LOG.info("Dataset deletion time: " + elapsedTime + " milliseconds");
	}

	/**
	 * Called by acquisition delete. Does not reject from pacs as acquisition already does it.
	 * @param id
	 * @throws ShanoirException
	 * @throws SolrServerException
	 * @throws IOException
	 * @throws RestServiceException
	 */
	public void deleteByIdCascade(final Long id) throws ShanoirException, SolrServerException, IOException, RestServiceException {
		final Dataset dataset = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(Dataset.class, id));

		// Do not delete entity if it is the source (or if it has copies). If getSourceId() is not null, it means it's a copy
		if (!CollectionUtils.isEmpty(dataset.getCopies())) {
			throw new RestServiceException(
					new ErrorModel(
							HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"This dataset is linked to another dataset that was copied."
					));
		}

		delete(dataset);
	}

	public void deleteDatasetFilesFromDiskAndPacs(Dataset dataset) throws ShanoirException {
		if (!dicomWeb) {
			return;
		}
		Long id = dataset.getId();
		for (DatasetExpression expression : dataset.getDatasetExpressions()) {
			boolean isDicom = DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat());
			List<DatasetFile> datasetFiles = expression.getDatasetFiles();
			if (dataset.getSource() == null)
				datasetAsyncService.deleteDatasetFilesFromDiskAndPacsAsync(datasetFiles, isDicom, id);
		}
	}

	@Override
	public boolean existsById(Long id) {
		return repository.existsById(id);
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
	public List<DatasetLight> findLightByIdIn(List<Long> ids) {
		return Utils.toList(repository.findAllLightById(ids));
	}

	@Override
	public List<DatasetLight> findLightByStudyId(Long studyId) {
		return Utils.toList(repository.findAllLightByStudyId(studyId));
	}

	@Override
	public Dataset create(final Dataset dataset) throws SolrServerException, IOException {
		Dataset ds = repository.save(dataset);
		Long studyId;
		if (ds.getDatasetAcquisition() != null) {
			studyId = ds.getDatasetAcquisition().getExamination().getStudyId();
		} else {
			// We have a processed dataset -> acquisition is null but study id is set.
			studyId = ds.getStudyId();
		}

		shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, ds.getStudyId()));
		rabbitTemplate.convertAndSend(RabbitMQConfiguration.RELOAD_BIDS, objectMapper.writeValueAsString(studyId));
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
		try {
			Long studyId;
			if (ds.getDatasetProcessing() == null) {
				studyId = ds.getDatasetAcquisition().getExamination().getStudyId();
			} else {
				studyId = ds.getStudyId();
			}
			shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, studyId));
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.RELOAD_BIDS, objectMapper.writeValueAsString(studyId));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error while updating a dataset", e);
		}
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
	public List<Dataset> findDatasetAndOutputByExaminationId(Long examinationId) {
        return StreamSupport.stream(repository.findAllById(repository.findDatasetAndOutputByExaminationId(examinationId)).spliterator(), false).toList();
	}

	@Override
	public void deleteNiftis(Long studyId) {
		List<Dataset> datasets = this.findByStudyId(studyId);
		for (Dataset dataset : datasets) {
			deleteNifti(dataset);
		}
	}

	/**
	 * Deletes nifti on file server
	 * @param dataset
	 */
	private void deleteNifti(Dataset dataset) {
		List<DatasetExpression> expressionsToDelete = new ArrayList<>();

		for (Iterator<DatasetExpression> iterex = dataset.getDatasetExpressions().iterator(); iterex.hasNext(); ) {
			DatasetExpression expression = iterex.next();
			if (!DatasetExpressionFormat.NIFTI_SINGLE_FILE.equals(expression.getDatasetExpressionFormat())) {
				continue;
			}
			for (Iterator<DatasetFile> iter = expression.getDatasetFiles().iterator(); iter.hasNext(); ) {
				DatasetFile file = iter.next();
				URL url = null;
				try {
					url = new URL(file.getPath().replaceAll("%20", " "));
					File srcFile = new File(UriUtils.decode(url.getPath(), StandardCharsets.UTF_8.name()));
					if (srcFile.exists()) {
						LOG.error("Deleting: " + srcFile.getAbsolutePath());
						FileUtils.delete(srcFile);
					}
					// We are forced to detach elements here to be able to delete them from DB
					file.setDatasetExpression(null);
					iter.remove();
				} catch (Exception e) {
					LOG.error("Could not delete nifti file: {}", file.getPath(), e);
				}
			}
			expression.setDataset(null);
			iterex.remove();
			expressionsToDelete.add(expression);
		}
		if (expressionsToDelete.isEmpty()) {
			return;
		}
		this.datasetExpressionRepository.deleteAll(expressionsToDelete);
	}

	/**
	 * Get study Id from dataset. If processed, recursively get it through processing inputs
	 *
	 * @param dataset
	 * @return
	 */
	@Override
	public Long getStudyId(Dataset dataset) {
		if (dataset.getStudyId() != null) {
			return dataset.getStudyId();
		}
		if (dataset.getDatasetProcessing() != null) {
			return dataset.getDatasetProcessing().getStudyId();
		}
		if(dataset.getDatasetAcquisition() != null && dataset.getDatasetAcquisition().getExamination() != null){
			return dataset.getDatasetAcquisition().getExamination().getStudyId();
		}
		try {
			LOG.error(objectMapper.writeValueAsString(dataset));
			return null;
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get examination from dataset. If processed, recursively get it through processing inputs
	 *
	 * @param dataset
	 * @return
	 */
	@Override
	public Examination getExamination(Dataset dataset){
		DatasetAcquisition acquisition = this.getAcquisition(dataset);
		if(acquisition != null){
			return acquisition.getExamination();
		}
		return null;
	}

	@Override
	public DatasetAcquisition getAcquisition(Dataset dataset) {
		if(dataset.getDatasetAcquisition() != null){
			return dataset.getDatasetAcquisition();
		}
		if(dataset.getDatasetProcessing().getInputDatasets() != null){
			for(Dataset ds : dataset.getDatasetProcessing().getInputDatasets()){
				DatasetAcquisition acq = this.getAcquisition(ds);
				if(acq != null){
					return acq;
				}
			}
		}
		return null;
	}
}
