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

package org.shanoir.ng.examination.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.service.SecurityService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Examination service implementation.
 * 
 * @author ifakhfakh
 *
 */
@Service
public class ExaminationServiceImpl implements ExaminationService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationServiceImpl.class);

	@Autowired
	private ExaminationRepository examinationRepository;
	
	@Autowired
	private SecurityService securityService;

	@Autowired
	private SolrService solrService;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private SubjectRepository subjectService;

	@Autowired
	private DatasetService datasetService;
	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;
	
	@Value("${datasets-data}")
	private String dataDir;
	
	@Override
	public void deleteById(final Long id) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException {
		Optional<Examination> examinationOpt = examinationRepository.findById(id);
		if (!examinationOpt.isPresent()) {
			throw new EntityNotFoundException(Examination.class, id);
		}
		Long tokenUserId = KeycloakUtil.getTokenUserId();
		Examination examination = examinationOpt.get();
		String studyIdAsString = examination.getStudyId().toString();

		List<Examination> childExam = examinationRepository.findBySourceId(id);
		if (!CollectionUtils.isEmpty(childExam)) {
			throw new RestServiceException(
					new ErrorModel(
							HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"This examination is linked to another examination that was copied."
					));
		} else {
			if (examination.getDatasetAcquisitions() != null) {
				for (DatasetAcquisition dsAcq : examination.getDatasetAcquisitions()) {
					this.datasetAcquisitionService.deleteById(dsAcq.getId());
				}
			}
			examinationRepository.deleteById(id);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_EXAMINATION_EVENT, id.toString(), tokenUserId, studyIdAsString, ShanoirEvent.SUCCESS, examination.getStudyId()));

		}
	}

	@Override
	public void deleteFromRabbit(Examination exam) throws ShanoirException, SolrServerException, IOException, RestServiceException {
		Long tokenUserId = KeycloakUtil.getTokenUserId();
		String studyIdAsString = exam.getStudyId().toString();
		// Iterate over datasets acquisitions and datasets to send events and remove them from solr
		for (DatasetAcquisition dsAcq : exam.getDatasetAcquisitions()) {
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_DATASET_ACQUISITION_EVENT, dsAcq.getId().toString(), tokenUserId, studyIdAsString, ShanoirEvent.SUCCESS));
			for (Dataset ds : dsAcq.getDatasets())  {
				datasetService.deleteById(ds.getId());
			}
		}
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_EXAMINATION_EVENT, exam.getId().toString(), tokenUserId, studyIdAsString, ShanoirEvent.SUCCESS, exam.getStudyId()));
		examinationRepository.deleteById(exam.getId());
	}

	@Override
	public List<Examination> findAll() {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return examinationRepository.findAll();
		} else {
			List<Pair<Long, Long>> studyCenters = new ArrayList<>();
			Set<Long> unrestrictedStudies = new HashSet<Long>();
			securityService.getStudyCentersAndUnrestrictedStudies(studyCenters, unrestrictedStudies);
			return examinationRepository.findAllByStudyCenterOrStudyIdIn(studyCenters, unrestrictedStudies);
		}
	}
	
	@Override
	public Page<Examination> findPage(final Pageable pageable, boolean preclinical) {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return examinationRepository.findAllByPreclinical(pageable, preclinical);
		} else {
			List<Pair<Long, Long>> studyCenters = new ArrayList<>();
			Set<Long> unrestrictedStudies = new HashSet<Long>();
			securityService.getStudyCentersAndUnrestrictedStudies(studyCenters, unrestrictedStudies);
			return examinationRepository.findPageByStudyCenterOrStudyIdIn(studyCenters, unrestrictedStudies, pageable, preclinical);
		}
	}

	@Override
	public Page<Examination> findPage(final Pageable pageable, String patientName) {
		if (patientName.length() > 64) {
			throw new IllegalArgumentException("A patient name cannot be longer than 64 chars, it exceed the data representation limit");
		}
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			if (StringUtils.isNotEmpty(patientName)) {
				return examinationRepository.findAllBySubjectName(patientName, pageable);
			} else {
				return examinationRepository.findAll(pageable);
			}
		} else {
			List<Pair<Long, Long>> studyCenters = new ArrayList<>();
			Set<Long> unrestrictedStudies = new HashSet<Long>();
			securityService.getStudyCentersAndUnrestrictedStudies(studyCenters, unrestrictedStudies);
			if (StringUtils.isNotEmpty(patientName)) {
				return examinationRepository.findPageByStudyCenterOrStudyIdInAndSubjectName(studyCenters, unrestrictedStudies, patientName, pageable);
			} else {
				return examinationRepository.findPageByStudyCenterOrStudyIdIn(studyCenters, unrestrictedStudies, pageable);
			}
		}
	}

	@Override
	public List<Examination> findBySubjectId(final Long subjectId) {
		return examinationRepository.findBySubjectId(subjectId);
	}

	@Override
	public List<Long> findIdsByStudyId(Long studyId) {
		return examinationRepository.findIdsByStudyId(studyId);
	}

	@Override
	public List<Examination> findByStudyId(Long studyId) {
		return examinationRepository.findByStudy_Id(studyId);
	}

	@Override
	public Examination findById(final Long id) {
		return examinationRepository.findById(id).orElse(null);
	}

	@Override
	public Examination save(final Examination examination) {
		Examination savedExamination = null;
		Subject subToSet = this.subjectService.findById(examination.getSubject().getId()).get();
		examination.setSubject(subToSet);
		savedExamination = examinationRepository.save(examination);
		return savedExamination;
	}

	@Override
	public Examination update(final Examination examination) throws EntityNotFoundException, ShanoirException {
		final Examination examinationDb = examinationRepository.findById(examination.getId()).orElse(null);
		if (examinationDb == null) {
			throw new EntityNotFoundException(Examination.class, examination.getId());
		}
		if (!KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN") && !examinationDb.getCenterId().equals(examination.getCenterId())) {
			throw new AccessDeniedException("Cannot update the center of the examination, please ask an administrator.");
		}
		if (!KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN") && !examinationDb.getStudyId().equals(examination.getStudyId())) {
			throw new AccessDeniedException("Cannot update the study of the examination, please ask an administrator.");
		}
		if (!KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN") 
				&& !((examinationDb.getSubject() == null && examination.getSubject() == null) || examinationDb.getSubject().getId().equals(examination.getSubject().getId()))) {
			throw new AccessDeniedException("Cannot update the subject of the examination, please ask an administrator.");
		}
		updateExaminationValues(examinationDb, examination);
		examinationRepository.save(examinationDb);
		return examinationDb;
	}

	/**
	 * Update some values of examination to save them in database.
	 * 
	 * @param examinationDb examination found in database.
	 * @param examination examination with new values.
	 * @return database examination with new values.
	 */
	private Examination updateExaminationValues(final Examination examinationDb, final Examination examination) {
		// Update extra data paths => delete files not present anymore
		if (examinationDb.getExtraDataFilePathList() != null) {
			for(String filePath : examinationDb.getExtraDataFilePathList()) {
				if (!examination.getExtraDataFilePathList().contains(filePath)) {
					// Delete file
					String filePathToDelete = getExtraDataFilePath(examinationDb.getId(), filePath);
					FileUtils.deleteQuietly(new File(filePathToDelete));
				}
			}
		}
		examinationDb.setSubject(examination.getSubject());
		examinationDb.setCenterId(examination.getCenterId());
		examinationDb.setComment(examination.getComment());
		examinationDb.setExaminationDate(examination.getExaminationDate());
		examinationDb.setNote(examination.getNote());
		examinationDb.setStudy(examination.getStudy());
		examinationDb.setSubjectWeight(examination.getSubjectWeight());
		examinationDb.setWeightUnitOfMeasure(examination.getWeightUnitOfMeasure());
		examinationDb.setExtraDataFilePathList(examination.getExtraDataFilePathList());
		examinationDb.setInstrumentBasedAssessmentList(examination.getInstrumentBasedAssessmentList());
		return examinationDb;
	}

	@Override
	public List<Examination> findBySubjectIdStudyId(Long subjectId, Long studyId) {
		return examinationRepository.findBySubjectIdAndStudy_Id(subjectId, studyId);
	}

	@Override
	public Long getExtraDataSizeByStudyId(Long studyId){

		List<Examination> exams = this.findByStudyId(studyId);

		long size = 0L;
		for(Examination exam : exams){
			for(String path : exam.getExtraDataFilePathList()){
				File f = new File(this.getExtraDataFilePath(exam.getId(), path));
				if(f.exists()){
					size += f.length();
				}
			}
		}
		return size;
	}

	@Override
	public String addExtraData(final Long examinationId, final MultipartFile file) {
		String filePath = getExtraDataFilePath(examinationId, file.getOriginalFilename());
		File fileToCreate = new File(filePath);
		fileToCreate.getParentFile().mkdirs();
		try {
			LOG.info("Saving file {} to destination: {}", file.getOriginalFilename(), filePath);
			file.transferTo(new File(filePath));
		} catch (Exception e) {
			LOG.error("Error while loading files on examination: {}. File not uploaded. {}", examinationId, e);
			e.printStackTrace();
			return null;
		}
		return filePath;
	}

	@Override
	public String addExtraDataFromFile(final Long examinationId, final File file) {
		String filePath = getExtraDataFilePath(examinationId, file.getName());
		File fileToCreate = new File(filePath);
		fileToCreate.getParentFile().mkdirs();
		try {
			LOG.info("Saving file {} to destination: {}", file.getName(), filePath);
			Files.copy(Path.of(file.getAbsolutePath()), Path.of(filePath));
		} catch (Exception e) {
			LOG.error("Error while loading files on examination: {}. File not uploaded. {}", examinationId, e);
			e.printStackTrace();
			return null;
		}
		return filePath;
	}

	/**
	 * Gets the extra data file path
	 * @param examinationId id of the examination
	 * @param fileName name of the file
	 * @return the file path of the file
	 */
	@Override
	public String getExtraDataFilePath(Long examinationId, String fileName) {
		return dataDir + "/examination-" + examinationId + "/" + fileName;
	}

}
