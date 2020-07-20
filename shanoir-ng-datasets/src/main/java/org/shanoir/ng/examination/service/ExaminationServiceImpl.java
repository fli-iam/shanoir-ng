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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
	private StudyUserRightsRepository rightsRepository;

	@Autowired
	private SolrService solrService;

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException {
		Examination exam = examinationRepository.findOne(id);
		List<Long> datasets = new ArrayList<>();

		if (exam.getDatasetAcquisitions() != null) {
			for (DatasetAcquisition acq : exam.getDatasetAcquisitions()) {
				for (Dataset ds : acq.getDatasets()) {
					datasets.add(ds.getId());
				}
			}
		}
		// Delete examination
		examinationRepository.delete(id);
		
		for (Long dsId : datasets) {
			solrService.deleteFromIndex(dsId);
		}
	}

	@Value("${datasets-data}")
	private String dataDir;

	@Override
	public Page<Examination> findPage(final Pageable pageable, boolean preclinical) {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return examinationRepository.findAllByPreclinical(pageable, preclinical);
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());
			return examinationRepository.findByStudyIdIn(studyIds, pageable);
		}
	}

	@Override
	public List<Examination> findBySubjectId(final Long subjectId) {
		return examinationRepository.findBySubjectId(subjectId);
	}
	
	@Override
	public List<Examination> findByStudyId(Long studyId) {
		return examinationRepository.findByStudyId(studyId);
	}
	
	@Override
	public Examination findById(final Long id) {
		return examinationRepository.findOne(id);
	}

	@Override
	public Examination save(final Examination examination) {
		Examination savedExamination = null;
		savedExamination = examinationRepository.save(examination);
		return savedExamination;
	}
	
	@Override
	public Examination update(final Examination examination) throws EntityNotFoundException {
		final Examination examinationDb = examinationRepository.findOne(examination.getId());
		if (examinationDb == null) {
			throw new EntityNotFoundException(Examination.class, examination.getId());
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

		examinationDb.setCenterId(examination.getCenterId());
		examinationDb.setComment(examination.getComment());
		examinationDb.setExaminationDate(examination.getExaminationDate());
		examinationDb.setNote(examination.getNote());
		examinationDb.setStudyId(examination.getStudyId());
		examinationDb.setSubjectWeight(examination.getSubjectWeight());
		return examinationDb;
	}

	@Override
	public List<Examination> findBySubjectIdStudyId(Long subjectId, Long studyId) {
		return examinationRepository.findBySubjectIdAndStudyId(subjectId, studyId);
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
