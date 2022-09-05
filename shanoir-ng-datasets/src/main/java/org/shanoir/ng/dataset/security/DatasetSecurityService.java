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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
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
	SubjectRepository subjectRepository;
	
	@Autowired
	StudyRightsService commService;

	@Autowired
	StudyRepository studyRepository;
	
	@Autowired
	private StudyInstanceUIDHandler studyInstanceUIDHandler;

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
	 * Check that the connected user has the given right for the given subject.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnSubjectId(Long subjectId, String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	Optional<Subject> subject = subjectRepository.findById(subjectId);
    	if (subject.isEmpty()) {
    		return false;
    	}
    	for (SubjectStudy subjectStudy : subject.get().getSubjectStudyList()) {
    		boolean hasRight = commService.hasRightOnStudy(subjectStudy.getStudy().getId(), rightStr);
    		if (hasRight) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
	 * Check that the connected user has the given right for the given subject.
	 * 
	 * @param subjectName the study name
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnSubjectName(String subjectName, String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	Subject subject = subjectRepository.findByName(subjectName);
    	if (subject == null) {
    		return false;
    	}
    	for (SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
    		boolean hasRight = commService.hasRightOnStudy(subjectStudy.getStudy().getId(), rightStr);
    		if (hasRight) {
    			return true;
    		}
    	}
    	return false;
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (studyCardId == null) {
			return false;
		}
    	StudyCard sc = studyCardRepository.findById(studyCardId).orElse(null);
    	if (sc == null) {
			throw new EntityNotFoundException("Cannot find study card with id " + studyCardId);
		}
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (studyId == null) {
			return false;
		}
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (studyCardId == null) {
			return false;
		}
    	StudyCard sc = studyCardRepository.findById(studyCardId).orElse(null);
    	if (sc == null) {
			throw new EntityNotFoundException("Cannot find study card with id " + studyCardId);
		}
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (studyCard == null) {
			return false;
		}
    	StudyCard dbStudyCard = studyCardRepository.findById(studyCard.getId()).orElse(null);
    	if (dbStudyCard == null) {
			throw new EntityNotFoundException("Cannot find study card with id " + studyCard.getId());
		}
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
    	Dataset dataset = datasetRepository.findById(datasetId).orElse(null);
        if (dataset == null) {
			throw new EntityNotFoundException("Cannot find dataset with id " + datasetId);
		}
        return hasRightOnTrustedDataset(dataset, rightStr);
    }
    
    /**
     * Check that the connected user has the given right for the given dataset.
     * 
     * @param datasetId the dataset id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnNewDataset(Dataset dataset, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	if (dataset == null) {
    		throw new IllegalArgumentException("Dataset can't be null here");
    	}
    	if (dataset.getId() != null) {
    		throw new IllegalStateException("Id must be null for a new dataset. Use another security method for an existing dataset.");
    	}
    	if (dataset.getDatasetAcquisition() == null || dataset.getDatasetAcquisition().getExamination() == null) {
    		return true;
    	} else if (dataset.getDatasetAcquisition().getExamination().getCenterId() == null) {
    		if (dataset.getDatasetAcquisition().getExamination().getStudyId() == null) {
    			return true;
    		} else {
    			return hasRightOnStudy(dataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr);    			
    		}
    	} else {
    		return hasRightOnStudyCenter(dataset.getDatasetAcquisition().getExamination().getCenterId(), dataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr);    		
    	}
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	DatasetAcquisition datasetAcq = datasetAcquisitionRepository.findById(datasetAcquisitionId).orElse(null);
        if (datasetAcq == null) {
			throw new EntityNotFoundException("Cannot find dataset acquisition with id " + datasetAcquisitionId);
		}
        if (datasetAcq.getExamination() == null || datasetAcq.getExamination().getStudyId() == null) {
			return false;
		}
        return this.hasRightOnStudyCenter(datasetAcq.getExamination().getCenterId(), datasetAcq.getExamination().getStudyId(), rightStr);
    }
    
    /**
     * Check that the connected user has the given right for the given dataset acquisitions.
     * !!! The acquisitions must be trusted, meaning they must come from the database, not from the user !!!
     * 
     * @param datasetId the dataset acquisition id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnEveryTrustedDatasetAcquisition(List<DatasetAcquisition> datasetAcquisitions, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	// If the entry is empty, return an empty list
    	if (datasetAcquisitions == null || datasetAcquisitions.isEmpty()) {
			return true;
		}
    	
    	Set<Long> studyIds = new HashSet<Long>();
    	for (DatasetAcquisition acq : datasetAcquisitions) {
    		studyIds.add(acq.getExamination().getStudyId());
    	}
    	return studyIds.equals(commService.hasRightOnStudies(studyIds, rightStr));
    }

    /**
     * Check that the connected user has the given right for at least one of the given datasets.
     * 
     * @param datasetId the datasets ids
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasAtLeastRightOnOneDataset(List<Long> datasetIds, String rightStr) throws EntityNotFoundException {
    	boolean hasRight = false;
    	for(Long datasetId : datasetIds) {
    		hasRight = hasRight || hasRightOnDataset(datasetId, rightStr);
    	}
    	return hasRight;
    }
    
    /**
     * Reject if one dataset doesn't have the right.
     * DatasetIds list is also cleaned here
     * 
     * @param datasets the datasets
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnEveryDataset(List<Long> datasetIds, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	// If the entry is empty, return an empty list
    	if (datasetIds == null || datasetIds.isEmpty()) {
			return true;
		}
    	
    	Iterable<Dataset> datasets = datasetRepository.findAllById(datasetIds);
    	Set<Long> studyIds = new HashSet<Long>();
    	 
    	boolean hasRight = true;
    	for (Dataset dataset : datasets) {
    		studyIds.add(dataset.getStudyId());
    		hasRight &= this.hasRightOnStudyCenter(dataset.getDatasetAcquisition().getExamination().getCenterId(), dataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr);
    	}
    	return hasRight;
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
        if (dataset.getDatasetAcquisition().getExamination().getStudyId() == null) {
			return false;
		}
        Set<Long> studies = new HashSet<>();
        studies.add(dataset.getStudyId());
        List<Long> studiesRelated = studyRepository.findByDatasetId(dataset.getId()).stream().map(BigInteger::longValue).collect(Collectors.toList());

        if (studiesRelated != null && !studiesRelated.isEmpty()) {
        	studies.addAll(studiesRelated);
        }
        return hasRightOnStudiesCenter(dataset.getDatasetAcquisition().getExamination().getCenterId(), studies, rightStr);
    }

    public boolean hasRightOnStudyCenter(Long centerId, Long studyId, String rightStr) {
    	return commService.hasRightOnStudy(studyId, rightStr) && commService.hasRightOnCenter(studyId, centerId);
    }

    private boolean hasRightOnStudiesCenter(Long centerId, Set<Long> studies, String rightStr) {
        return !commService.hasRightOnStudies(new HashSet<>(studies), rightStr).isEmpty() && commService.hasRightOnCenter(studies, centerId);
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
    	Dataset dbDataset = datasetRepository.findById(dataset.getId()).orElse(null);
    	if (dbDataset == null) {
			throw new EntityNotFoundException("Cannot find dataset with id " + dataset.getId());
		}
    	if (dataset.getStudyId() == dbDataset.getStudyId()) { // study hasn't changed
    		return this.hasRightOnStudyCenter(dataset.getDatasetAcquisition().getExamination().getCenterId(), dataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr);
    	} else { // study has changed : check user has right on both studies
    		return this.hasRightOnStudyCenter(dataset.getDatasetAcquisition().getExamination().getCenterId(), dataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr) 
    				&& this.hasRightOnStudyCenter(dbDataset.getDatasetAcquisition().getExamination().getCenterId(), dbDataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr);
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (datasetAcq == null) {
			throw new IllegalArgumentException("Dataset acquisition cannot be null here.");
		}
    	if (datasetAcq.getId() == null) {
			throw new IllegalArgumentException("Dataset acquisition id cannot be null here.");
		}
    	if (datasetAcq.getExamination() == null || datasetAcq.getExamination().getStudyId() == null) {
			return false;
		}
    	DatasetAcquisition dbDatasetAcq = datasetAcquisitionRepository.findById(datasetAcq.getId()).orElse(null);
    	if (dbDatasetAcq == null) {
			throw new EntityNotFoundException("Cannot find dataset acquisition with id " + datasetAcq.getId());
		}
    	if (datasetAcq.getExamination().getStudyId() == dbDatasetAcq.getExamination().getStudyId()) { // study hasn't changed
    		return this.hasRightOnStudyCenter(datasetAcq.getExamination().getCenterId(), datasetAcq.getExamination().getStudyId(), rightStr);
    	} else { // study has changed : check user has right on both studies
    		return this.hasRightOnStudyCenter(datasetAcq.getExamination().getCenterId(), datasetAcq.getExamination().getStudyId(), rightStr) &&
    				this.hasRightOnStudyCenter(dbDatasetAcq.getExamination().getCenterId(), dbDatasetAcq.getExamination().getStudyId(), rightStr);
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
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (studyCard == null) {
			throw new IllegalArgumentException("Study card cannot be null here.");
		}
    	if (studyCard.getId() == null) {
			throw new IllegalArgumentException("Dataset acquisition id cannot be null here.");
		}
    	if (studyCard.getStudyId() == null) {
			return false;
		}
    	StudyCard dbStudyCard = studyCardRepository.findById(studyCard.getId()).orElse(null);
    	if (dbStudyCard == null) {
			throw new EntityNotFoundException("Cannot find dataset acquisition with id " + studyCard.getId());
		}
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
    public boolean checkDatasetPage(Iterable<Dataset> page, String rightStr) {
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((Dataset dataset) -> studyIds.add(dataset.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr); //
    	
    	// Also check for centers    	
    	for (Dataset dataset : page) {
    		if (!checkedIds.contains(dataset.getStudyId())) {
    			return false;
    		} else {
    			if (!this.hasRightOnStudyCenter(dataset.getDatasetAcquisition().getExamination().getCenterId(), dataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    /**
     * Check that page checking the connected user has the right on those datasets.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true or false
     */
    public boolean checkDatasetDTOPage(Iterable<DatasetDTO> page, String rightStr) {
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((DatasetDTO dataset) -> studyIds.add(dataset.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr); //
    	
    	// Also check for centers    	
    	for (DatasetDTO dataset : page) {
    		if (!checkedIds.contains(dataset.getStudyId())) {
    			return false;
    		} else {
    			if (!this.hasRightOnStudyCenter(dataset.getCenterId(), dataset.getStudyId(), rightStr)) {
    				return false;
    			}
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
    	Set<Dataset> toRemove = new HashSet<>();
    	list.forEach((Dataset ds) -> {
        	if (!this.hasRightOnStudyCenter(ds.getDatasetAcquisition().getExamination().getCenterId(), ds.getDatasetAcquisition().getExamination().getStudyId(), rightStr)) {
        		toRemove.add(ds);
        	}
    	});
    	list.removeAll(toRemove);
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
    	if (studyCards == null) {
			return true;
		}
    	List<StudyCard> newList = new ArrayList<>();
    	Map<Long, List<StudyCard>> map = new HashMap<>();
    	Set<Long> studyIds = new HashSet<Long>();
    	for (StudyCard sc : studyCards) {
    		if (!map.containsKey(sc.getId())) {
				map.put(sc.getId(), new ArrayList<StudyCard>());
			}
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
    	if (page == null) return true;
    	Set<Long> studyIds = new HashSet<>();
    	page.forEach((Examination exam) -> studyIds.add(exam.getStudyId()));
    	Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
    	for (Examination exam : page) {
    		if (!checkedIds.contains(exam.getStudyId())) {
				return false;
			} else {
    			if (!this.hasRightOnStudyCenter(exam.getCenterId(), exam.getStudyId(), rightStr)) {
    				return false;
    			}
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
    	Set<Examination> toRemove = new HashSet<>();
    	list.forEach((Examination exam) -> {
        	if (!this.hasRightOnStudyCenter(exam.getCenterId(), exam.getStudyId(), rightStr)) {
        		toRemove.add(exam);
        	}
    	});
    	list.removeAll(toRemove);
    	return true;
    }
    
    /**
     * Filter datasets checking the connected user has the right on those.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterDatasetDTOList(List<DatasetDTO> list, String rightStr) throws EntityNotFoundException {
    	if (list == null) {
			return true;
		}
    	Set<DatasetDTO> dsRemove = new HashSet<>();
    	for(DatasetDTO dto : list) {
    		if (!this.hasRightOnDataset(dto.getId(), rightStr)) {
    			dsRemove.add(dto);
        	}
    	}
    	list.removeAll(dsRemove);
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
    	Set<DatasetAcquisition> toRemove = new HashSet<>();
    	list.forEach((DatasetAcquisition ds) -> {
        	if (!this.hasRightOnStudyCenter(ds.getExamination().getCenterId(), ds.getExamination().getStudyId(), rightStr)) {
        		toRemove.add(ds);
        	}
    	});
    	list.removeAll(toRemove);
    	return true;
    }

    /**
     * Filter dataset acquisitions checking the connected user has the right on those.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterDatasetAcquisitionDTOList(List<DatasetAcquisitionDTO> list, String rightStr) {
    	if (list == null) {
			return true;
		}
    	Set<DatasetAcquisitionDTO> acqsToRemove = new HashSet<>();
    	list.forEach((DatasetAcquisitionDTO dsa) -> {
        	if (!this.hasRightOnStudyCenter(dsa.getExamination().getCenterId(), dsa.getExamination().getStudyId(), rightStr)) {
        		acqsToRemove.add(dsa);
        	}
    	});
    	list.removeAll(acqsToRemove);
    	return true;
    }
    

	/**
	* Filter dataset acquisitions checking the connected user has the right on those.
	* 
	* @param page the page
	* @param rightStr the right
	* @return true
	*/
	public boolean filterExaminationDatasetAcquisitionDTOList(List<ExaminationDatasetAcquisitionDTO> list, String rightStr) throws EntityNotFoundException {
	   if (list == null) {
	                   return true;
	           }
	   Set<Long> checkedIds = new HashSet<Long>();
	   for (ExaminationDatasetAcquisitionDTO edsa : list) {
		   if (hasRightOnExamination(edsa.getExaminationId(), rightStr)) {
			   checkedIds.add(edsa.getExaminationId());
		   }
	   }
	   list.removeIf((ExaminationDatasetAcquisitionDTO edsa) -> !checkedIds.contains(edsa.getExaminationId()));
	   return true;
	}

    
    /**
     * Filter dataset acquisitions checking the connected user has the right on those.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean checkDatasetAcquisitionDTOPage(Page<DatasetAcquisitionDTO> page, String rightStr) {
    	if (page == null) {
			return true;
		}
    	for (DatasetAcquisitionDTO acquisition : page) {
    		if (!hasRightOnStudyCenter(acquisition.getExamination().getCenterId(), acquisition.getExamination().getStudyId(), rightStr)) {
				return false;
			}
    	}
    	return true;
    }
    
    /**
     * Filter dataset acquisitions checking the connected user has the right on those.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean checkDatasetAcquisitionPage(Page<DatasetAcquisition> page, String rightStr) {
    	if (page == null) {
			return true;
		}
    	for (DatasetAcquisition acquisition : page) {
    		if (!hasRightOnStudyCenter(acquisition.getExamination().getCenterId(), acquisition.getExamination().getStudyId(), rightStr)) {
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
    public boolean filterStudyCardList(List<StudyCard> list, String rightStr) {
    	if (list == null) {
			return true;
		}
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
    	if (page == null) {
			return true;
		}
    	for (ExaminationDTO exam : page) {
    		if (!this.hasRightOnStudyCenter(exam.getCenterId(), exam.getStudyId(), rightStr)) {
				return false;
			}
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
	if (list == null) return true;
    	Set<ExaminationDTO> examsToRemove = new HashSet<>();
    	for(ExaminationDTO exam : list) {
    		if (!hasRightOnStudyCenter(exam.getCenterId(), exam.getStudyId(), rightStr)) {
    			examsToRemove.add(exam);
			}
    	}
    	list.removeAll(examsToRemove);
    	return true;
    }
    
    /**
     * Filter examinations in that list checking the connected user has the right on those examinations.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterSubjectExaminationDTOList(List<SubjectExaminationDTO> list, String rightStr) {
	if (list == null) return true;
    	Set<SubjectExaminationDTO> examsToRemove = new HashSet<>();
    	for(SubjectExaminationDTO exam : list) {
    		if (!hasRightOnStudyCenter(exam.getCenterId(), exam.getStudyId(), rightStr)) {
    			examsToRemove.add(exam);
			}
    	}
    	list.removeAll(examsToRemove);
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
    	Examination exam = examinationRepository.findById(examinationId).orElse(null);
        if (exam == null) {
			throw new EntityNotFoundException("Cannot find examination with id " + examinationId);
		}
        if (exam.getStudyId() == null) {
			return false;
		}
        return this.hasRightOnStudyCenter(exam.getCenterId(), exam.getStudyId(), rightStr);
    }
    
    
    /**
     * Check that the connected user has the given right for the given examination.
     * 
     * @param examinationId the examination id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnTrustedExaminationDTO(ExaminationDTO examination, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
        if (examination == null) {
			return true;
		}
        if (examination.getStudyId() == null) {
			return false;
		}
        return this.hasRightOnStudyCenter(examination.getCenterId(), examination.getStudyId(), rightStr);
    }
    
    
    public boolean hasRightOnExamination(String examinationUID, String rightStr) throws EntityNotFoundException {
		Long id = studyInstanceUIDHandler.extractExaminationId(examinationUID);
		return hasRightOnExamination(id, rightStr);
    }
   
}
