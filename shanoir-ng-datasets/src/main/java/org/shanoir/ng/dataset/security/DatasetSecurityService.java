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

package org.shanoir.ng.dataset.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class DatasetSecurityService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DatasetSecurityService.class);

	
	@Autowired
	DatasetRepository datasetRepository;
	
	@Autowired
	ExaminationRepository examinationRepository;
	
	@Autowired
	StudyRightsService commService;
		
	
	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyId == null) return false;
        return commService.hasRightOnStudy(studyId, rightStr);
    }
    
    
    /**
     * Check that the connected user has the given right for the given dataset.
     * 
     * @param datasetId the dataset id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException 
     */
    public boolean hasRightOnDataset(Long datasetId, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	Dataset dataset = datasetRepository.findOne(datasetId);
        if (dataset == null) throw new EntityNotFoundException("Cannot find dataset with id " + datasetId);
        if (dataset.getStudyId() == null) return false;
        return commService.hasRightOnStudy(dataset.getStudyId(), rightStr);
    }
    
    
    /**
     * Check that the connected user has the given right for the given dataset.
     * 
     * @param dataset the dataset
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnTrustedDataset(Dataset dataset, String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
        if (dataset == null) throw new IllegalArgumentException("Dataset cannot be null here.");
        if (dataset.getStudyId() == null) return false;
        return commService.hasRightOnStudy(dataset.getStudyId(), rightStr);
    }
    
    /**
     * Check the connected user has the given right for the given dataset.
     * If the study is updated, check the user has the given right in both former and new studies.
     * 
     * @param dataset the dataset
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException 
     */
    public boolean hasUpdateRightOnDataset(Dataset dataset, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (dataset == null) throw new IllegalArgumentException("Dataset cannot be null here.");
    	if (dataset.getId() == null) throw new IllegalArgumentException("Dataset id cannot be null here.");
    	if (dataset.getStudyId() == null) return false;
    	Dataset dbDataset = datasetRepository.findOne(dataset.getId());
    	if (dbDataset == null) throw new EntityNotFoundException("Cannot find dataset with id " + dataset.getId());
    	if (dataset.getStudyId() == dbDataset.getStudyId()) { // study hasn't changed
    		return commService.hasRightOnStudy(dataset.getStudyId(), rightStr);
    	} else { // study has changed : check user has right on both studies
    		return commService.hasRightOnStudy(dataset.getStudyId(), rightStr) && commService.hasRightOnStudy(dbDataset.getStudyId(), rightStr);    		
    	}
    }
    
    /**
     * Check that page checking the connected user has the right on those datasets.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true or false
     */
    public boolean checkDatasetPage(Page<Dataset> page, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	page.forEach((Dataset dataset) -> {
    		studyIds.add(dataset.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr); //
    	for (Dataset dataset : page) {
    		if (!checkedIds.contains(dataset.getStudyId())) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Filter datasets in that page checking the connected user has the right on those datasets.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterDatasetList(List<Dataset> list, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	list.forEach((Dataset dataset) -> studyIds.add(dataset.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
		list.removeIf((Dataset dataset) -> !checkedIds.contains(dataset.getStudyId()));
    	return true;
    }
        
    /**
     * Filter examinations in that page checking the connected user has the right on those examinations.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterExaminationPage(Page<Examination> page, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	page.forEach((Examination exam) -> {
    		studyIds.add(exam.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (Examination exam : page) {
    		if (!checkedIds.contains(exam.getStudyId())) return false;
    	}
    	return true;
    }
    
    /**
     * Filter examinations in that page checking the connected user has the right on those examinations.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterExaminationList(List<Examination> list, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	list.forEach((Examination exam) -> {
    		studyIds.add(exam.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	list.removeIf((Examination exam) -> !checkedIds.contains(exam.getStudyId()));
    	return true;
    }
    
    /**
     * Filter examinations in that page checking the connected user has the right on those examinations.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterExaminationDTOPage(Page<ExaminationDTO> page, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	page.forEach((ExaminationDTO exam) -> {
    		if (exam.getStudy() != null) studyIds.add(exam.getStudy().getId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (ExaminationDTO exam : page) {
    		if (exam.getStudy() == null || !checkedIds.contains(exam.getStudy().getId())) return false;
    	}
    	return true;
    }
    
    /**
     * Filter examinations in that list checking the connected user has the right on those examinations.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterExaminationDTOList(List<ExaminationDTO> list, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	list.forEach((ExaminationDTO exam) -> {
    		if (exam.getStudy() != null) studyIds.add(exam.getStudy().getId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	list.removeIf((ExaminationDTO exam) -> exam.getStudy() == null || !checkedIds.contains(exam.getStudy().getId()));
    	return true;
    }
   
    /**
     * Filter datasets in that page checking the connected user has the right on those datasets.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterDatasetDTOPage(Page<DatasetDTO> page, String rightStr) {
    	if (page == null) return true;
    	Set<Long> studyIds = new HashSet<Long>();
    	page.forEach((DatasetDTO dataset) -> {
    		studyIds.add(dataset.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (DatasetDTO dataset : page) {
    		if (!checkedIds.contains(dataset.getStudyId())) return false;
    	}
    	return true;
    }
    
    /**
     * Check that the connected user has the given right for the given examination.
     * 
     * @param examinationId the examination id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException 
     */
    public boolean hasRightOnExamination(Long examinationId, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	Examination exam = examinationRepository.findOne(examinationId);
        if (exam == null) throw new EntityNotFoundException("Cannot find examination with id " + examinationId);
        if (exam.getStudyId() == null) return false;
        return commService.hasRightOnStudy(exam.getStudyId(), rightStr);
    }
   
}