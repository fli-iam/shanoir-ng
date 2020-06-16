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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class DatasetSecurityService {
	
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	
	@Autowired
	DatasetRepository datasetRepository;
	
	@Autowired
	DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	@Autowired
	StudyCardRepository studyCardRepository;
	
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
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	if (studyId == null) {
			return false;
		}
        return commService.hasRightOnStudy(studyId, rightStr);
    }
    
    /**
	 * Check that the connected user has the given right for the given study card.
	 * 
	 * @param studyCardId the study card id
	 * @param rightStr the right
	 * @return true or false
     * @throws EntityNotFoundException 
	 */
    public boolean hasRightOnStudyCard(Long studyCardId, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyCardId == null) return false;
    	StudyCard sc = studyCardRepository.findOne(studyCardId);
    	if (sc == null) throw new EntityNotFoundException("Cannot find study card with id " + studyCardId);    	
        return commService.hasRightOnStudy(sc.getStudyId(), rightStr);
    }
    
    /**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasOneRightOnStudy(Long studyId, String... rightStrs) {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyId == null) return false;
        return commService.hasOneRightOnStudy(studyId, rightStrs);
    }
    
    /**
	 * Check that the connected user has the given right for the given study card.
	 * 
	 * @param studyCardId the study card id
	 * @param rightStr the right
	 * @return true or false
     * @throws EntityNotFoundException 
	 */
    public boolean hasOneRightOnStudyCard(Long studyCardId, String... rightStrs) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyCardId == null) return false;
    	StudyCard sc = studyCardRepository.findOne(studyCardId);
    	if (sc == null) throw new EntityNotFoundException("Cannot find study card with id " + studyCardId);    	
        return commService.hasOneRightOnStudy(sc.getStudyId(), rightStrs);
    }
    
    /**
	 * Check that the connected user has the given right for updating the given study card.
	 * 
	 * @param studyCardId the study card id
	 * @param rightStr the right
	 * @return true or false
     * @throws EntityNotFoundException 
	 */
    public boolean hasOneRightOnStudyCardUpdate(StudyCard studyCard, String... rightStrs) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyCard == null) return false;
    	StudyCard dbStudyCard = studyCardRepository.findOne(studyCard.getId());
    	if (dbStudyCard == null) throw new EntityNotFoundException("Cannot find study card with id " + studyCard.getId());    	
        return commService.hasOneRightOnStudy(dbStudyCard.getStudyId(), rightStrs) && (
        		dbStudyCard.getStudyId().equals(studyCard.getStudyId()) || commService.hasOneRightOnStudy(studyCard.getStudyId(), rightStrs));
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
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	Dataset dataset = datasetRepository.findOne(datasetId);
        if (dataset == null) {
			throw new EntityNotFoundException("Cannot find dataset with id " + datasetId);
		}
        if (dataset.getStudyId() == null) {
			return false;
		}
        return commService.hasRightOnStudy(dataset.getStudyId(), rightStr);
    }

    /**
     * Check that the connected user has the given right for the given dataset acquisition.
     * 
     * @param datasetId the dataset acquisition id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException 
     */
    public boolean hasRightOnDatasetAcquisition(Long datasetAcquisitionId, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	DatasetAcquisition datasetAcq = datasetAcquisitionRepository.findOne(datasetAcquisitionId);
        if (datasetAcq == null) throw new EntityNotFoundException("Cannot find dataset acquisition with id " + datasetAcquisitionId);
        if (datasetAcq.getExamination() == null || datasetAcq.getExamination().getStudyId() == null) return false;
        return commService.hasRightOnStudy(datasetAcq.getExamination().getStudyId(), rightStr);
    }
    
    /**
     * Filters the list of datasets in entry if the given user has the right to do so.
     * DatasetIds list is also cleaned here
     * 
     * @param datasets the datasets
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public List<Dataset> hasRightOnAtLeastOneDataset(List<Dataset> datasets, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return datasets;
		}
    	// Create empty list
    	List<Dataset> filteredDatasets = new ArrayList<>();

    	// If the entry is empty, return an empty list
    	if (datasets == null || datasets.isEmpty()) {
			return filteredDatasets;
		}
    	// Get list of corresponding studies
    	Set<Long> studyIds = new HashSet<>();
    	datasets.forEach((Dataset dataset) -> {
    		studyIds.add(dataset.getStudyId());
    	});

    	// Check study right
    	Set<Long> validStudyIds = commService.hasRightOnStudies(studyIds, rightStr);

    	// Build filtered list of datasets
    	datasets.forEach((Dataset dataset) -> {
    		if (validStudyIds.contains(dataset.getStudyId())) {
    			filteredDatasets.add(dataset);
    		}
    	});
    	return filteredDatasets;
    }
    
    /**
     * Check that the connected user has the given right for the given dataset.
     * 
     * @param dataset the dataset
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnTrustedDataset(Dataset dataset, String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
        if (dataset == null) {
			throw new IllegalArgumentException("Dataset cannot be null here.");
		}
        if (dataset.getStudyId() == null) {
			return false;
		}
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
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	if (dataset == null) {
			throw new IllegalArgumentException("Dataset cannot be null here.");
		}
    	if (dataset.getId() == null) {
			throw new IllegalArgumentException("Dataset id cannot be null here.");
		}
    	if (dataset.getStudyId() == null) {
			return false;
		}
    	Dataset dbDataset = datasetRepository.findOne(dataset.getId());
    	if (dbDataset == null) {
			throw new EntityNotFoundException("Cannot find dataset with id " + dataset.getId());
		}
    	if (dataset.getStudyId() == dbDataset.getStudyId()) { // study hasn't changed
    		return commService.hasRightOnStudy(dataset.getStudyId(), rightStr);
    	} else { // study has changed : check user has right on both studies
    		return commService.hasRightOnStudy(dataset.getStudyId(), rightStr) && commService.hasRightOnStudy(dbDataset.getStudyId(), rightStr);
    	}
    }
    
    /**
     * Check the connected user has the given right for the given dataset acquisition.
     * If the study is updated, check the user has the given right in both former and new studies.
     * 
     * @param datasetAcq the dataset acquisition
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException 
     */
    public boolean hasUpdateRightOnDatasetAcquisition(DatasetAcquisition datasetAcq, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (datasetAcq == null) throw new IllegalArgumentException("Dataset acquisition cannot be null here.");
    	if (datasetAcq.getId() == null) throw new IllegalArgumentException("Dataset acquisition id cannot be null here.");
    	if (datasetAcq.getExamination() == null || datasetAcq.getExamination().getStudyId() == null) return false;
    	DatasetAcquisition dbDatasetAcq = datasetAcquisitionRepository.findOne(datasetAcq.getId());
    	if (dbDatasetAcq == null) throw new EntityNotFoundException("Cannot find dataset acquisition with id " + datasetAcq.getId());
    	if (datasetAcq.getExamination().getStudyId() == dbDatasetAcq.getExamination().getStudyId()) { // study hasn't changed
    		return commService.hasRightOnStudy(datasetAcq.getExamination().getStudyId(), rightStr);
    	} else { // study has changed : check user has right on both studies
    		return commService.hasRightOnStudy(datasetAcq.getExamination().getStudyId(), rightStr) && commService.hasRightOnStudy(dbDatasetAcq.getExamination().getStudyId(), rightStr);    		
    	}
    }
    
    /**
     * Check the connected user has the given right for the given study card.
     * If the study card is updated, check the user has the given right in both former and new study cards.
     * 
     * @param studyCard the study card
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException 
     */
    public boolean hasUpdateRightOnStudyCard(StudyCard studyCard, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyCard == null) throw new IllegalArgumentException("Study card cannot be null here.");
    	if (studyCard.getId() == null) throw new IllegalArgumentException("Dataset acquisition id cannot be null here.");
    	if (studyCard.getStudyId() == null) return false;
    	StudyCard dbStudyCard = studyCardRepository.findOne(studyCard.getId());
    	if (dbStudyCard == null) throw new EntityNotFoundException("Cannot find dataset acquisition with id " + studyCard.getId());
    	if (studyCard.getStudyId() == dbStudyCard.getStudyId()) { // study hasn't changed
    		return commService.hasRightOnStudy(studyCard.getStudyId(), rightStr);
    	} else { // study has changed : check user has right on both studies
    		return commService.hasRightOnStudy(studyCard.getStudyId(), rightStr) && commService.hasRightOnStudy(dbStudyCard.getStudyId(), rightStr);    		
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
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((Dataset dataset) -> studyIds.add(dataset.getStudyId()));
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
    	Set<Long> studyIds = new HashSet<>();
    	list.forEach((Dataset dataset) -> studyIds.add(dataset.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
		list.removeIf((Dataset dataset) -> !checkedIds.contains(dataset.getStudyId()));
    	return true;
    }
    
    /**
     * For every study of the list, check that the connected user has the given right.
	 *
     * @param dtos
     * @param rightStr
     * @return true or false
     */
    public boolean filterStudyCardHasRight(List<StudyCard> studyCards, String rightStr) {
    	if (studyCards == null) return true;
    	List<StudyCard> newList = new ArrayList<>();
    	Map<Long, List<StudyCard>> map = new HashMap<>();
    	Set<Long> studyIds = new HashSet<Long>();
    	for (StudyCard sc : studyCards) {
    		if (!map.containsKey(sc.getId())) map.put(sc.getId(), new ArrayList<StudyCard>());
    		map.get(sc.getId()).add(sc);
    		studyIds.add(sc.getStudyId());
    	}
    	for (Long id : this.commService.hasRightOnStudies(studyIds, rightStr)) {
    		newList.addAll(map.get(id));
    	}
    	studyCards = newList;
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
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((Examination exam) -> studyIds.add(exam.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (Examination exam : page) {
    		if (!checkedIds.contains(exam.getStudyId())) {
				return false;
			}
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
    	Set<Long> studyIds = new HashSet<>();
    	list.forEach((Examination exam) -> studyIds.add(exam.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	list.removeIf((Examination exam) -> !checkedIds.contains(exam.getStudyId()));
    	return true;
    }
    
    /**
     * Filter dataset acquisitions checking the connected user has the right on those.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterDatasetAcquisitionList(List<DatasetAcquisition> list, String rightStr) {
    	if (list == null) return true;
    	Set<Long> studyIds = new HashSet<Long>();
    	list.forEach((DatasetAcquisition dsa) -> {
    		studyIds.add(dsa.getExamination().getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	list.removeIf((DatasetAcquisition dsa) -> !checkedIds.contains(dsa.getExamination().getStudyId()));
    	return true;
    }
    
    /**
     * Filter examinations in that page checking the connected user has the right on those examinations.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterStudyCardList(List<StudyCard> list, String rightStr) {
    	Set<Long> studyIds = new HashSet<Long>();
    	list.forEach((StudyCard sc) -> {
    		studyIds.add(sc.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	list.removeIf((StudyCard sc) -> !checkedIds.contains(sc.getStudyId()));
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
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((ExaminationDTO exam) -> {
    		if (exam.getStudyId() != null) studyIds.add(exam.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (ExaminationDTO exam : page) {
    		if (exam.getStudyId() == null || !checkedIds.contains(exam.getStudyId())) return false;
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
    	Set<Long> studyIds = new HashSet<>();
    	list.forEach((ExaminationDTO exam) -> {
    		if (exam.getStudyId() != null) studyIds.add(exam.getStudyId());
    	});
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	list.removeIf((ExaminationDTO exam) -> exam.getStudyId() == null || !checkedIds.contains(exam.getStudyId()));
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
    	if (page == null) {
			return true;
		}
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((DatasetDTO dataset) -> studyIds.add(dataset.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (DatasetDTO dataset : page) {
    		if (!checkedIds.contains(dataset.getStudyId())) {
				return false;
			}
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
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	Examination exam = examinationRepository.findOne(examinationId);
        if (exam == null) {
			throw new EntityNotFoundException("Cannot find examination with id " + examinationId);
		}
        if (exam.getStudyId() == null) {
			return false;
		}
        return commService.hasRightOnStudy(exam.getStudyId(), rightStr);
    }
   
}