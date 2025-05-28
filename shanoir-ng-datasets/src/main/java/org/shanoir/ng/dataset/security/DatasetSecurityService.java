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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetForRights;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionForRights;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.ExaminationForRightsDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.UserRights;
import org.shanoir.ng.studycard.model.Card;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
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
    QualityCardRepository qualityCardRepository;
	
	@Autowired
	ExaminationRepository examinationRepository;
	
	@Autowired
	SubjectRepository subjectRepository;
	
	@Autowired
	StudyRightsService commService;
	
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
	 * Check that the connected user has the given right for all the given studies.
	 *
	 * @param studyIds the studies ids
	 * @param rightStr the right
	 * @return true or false
	 */
	public boolean hasRightOnStudies(List<Long> studyIds, String rightStr) {
		if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
		if (studyIds == null|| studyIds.isEmpty()) {
			return false;
		}

		Set<Long> givenIds = new HashSet<>(studyIds);

		return givenIds.size() == commService.hasRightOnStudies(givenIds, rightStr).size();
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
     * Check that the connected user has the given right for the given subject studies.
     * 
     * @param subjectName the study name
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnSubjectStudies(List<SubjectStudy> subjectstudies, String rightStr) {
        if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
            return true;
        } 
        if (subjectstudies != null) {
            for (SubjectStudy subjectStudy : subjectstudies) {
                boolean hasRight = commService.hasRightOnStudy(subjectStudy.getStudy().getId(), rightStr);
                if (!hasRight) {
                    return false;
                }
            }
        }
        return true;
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
     * Check that the connected user has the given right for the given quality card.
     * 
     * @param qualityCardId the study card id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnQualityCard(Long qualityCardId, String rightStr) throws EntityNotFoundException {
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return true;
        }
        if (qualityCardId == null) {
            return false;
        }
        QualityCard qc = qualityCardRepository.findById(qualityCardId).orElse(null);
        if (qc == null) {
            throw new EntityNotFoundException("Cannot find quality card with id " + qualityCardId);
        }
        return commService.hasRightOnStudy(qc.getStudyId(), rightStr);
    }
    
    /**
     * Check that the connected user has the given right for the given study card.
     * 
     * @param studyCardId the study card id
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnCard(Long cardId, String type, String rightStr) throws EntityNotFoundException {
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return true;
        }
        if (cardId == null) {
            return false;
        }
        Card card;
        if ("study".equals(type)) {
            card = studyCardRepository.findById(cardId).orElse(null);            
        } else if ("quality".equals(type)) {
            card = qualityCardRepository.findById(cardId).orElse(null);
        } else throw new IllegalArgumentException("Bad type argument '" + type + "', should be 'study' or 'quality'");
        if (card == null) {
            throw new EntityNotFoundException("Cannot find card with id " + cardId);
        }
        return commService.hasRightOnStudy(card.getStudyId(), rightStr);
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
    		return hasRightOnStudyCenter(
				dataset.getDatasetAcquisition().getExamination().getCenterId(), 
				dataset.getDatasetAcquisition().getExamination().getStudyId(), 
				rightStr);    		
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
     * 
     * @param datasetAcquisitionIds the dataset acquisition ids
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnEveryDatasetAcquisition(List<Long> datasetAcquisitionIds, String rightStr) throws EntityNotFoundException {
    	List<DatasetAcquisitionForRights> acqs = datasetAcquisitionRepository.findAllForRightsById(datasetAcquisitionIds)
			.stream()
			.map(a -> new DatasetAcquisitionForRights(a.getId(), a.getCenterId(), a.getStudyId()))
			.collect(Collectors.toList());
    	UserRights userRights = commService.getUserRights();
		for (DatasetAcquisitionForRights acq : acqs) {
			Long studyId = acq.getStudyId();
			Long centerId = acq.getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				return false;
			}
    	}
		return true;
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
		List<DatasetForRights> dtos = datasetRepository.findDatasetsForRights(datasetIds)
			.stream()
			.map(ds -> new DatasetForRights(ds.getId(), ds.getCenterId(), ds.getStudyId(), ds.getRelatedStudiesIds()))
			.collect(Collectors.toList());
		UserRights userRights = commService.getUserRights();
		for (DatasetForRights dataset : dtos) {
			Set<Long> studyIds = dataset.getAllStudiesIds();
			Long centerId = dataset.getCenterId();
			if (!userRights.hasStudiesCenterRights(studyIds, centerId, rightStr)) {
				return false;
			}
		}
		return true;
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

		Long studyId = getStudyIdFromDataset(dataset);

		Set<Long> studies = new HashSet<>();
		studies.add(studyId);
		CollectionUtils.emptyIfNull(dataset.getRelatedStudies()).forEach(s -> studies.add(s.getId()));
		return hasRightOnStudiesCenter(dataset.getCenterId(), studies, rightStr);
    }

	private static Long getStudyIdFromDataset(Dataset dataset) {
		Long studyId;
		if (dataset.getDatasetProcessing() != null) {
			studyId = dataset.getDatasetProcessing().getStudyId();
		} else if (dataset.getDatasetAcquisition() != null
				&& dataset.getDatasetAcquisition().getExamination() != null
				&& dataset.getDatasetAcquisition().getExamination().getStudyId() != null) {
			studyId = dataset.getDatasetAcquisition().getExamination().getStudyId();
		} else {
			throw new IllegalStateException("Cannot check dataset n°" + dataset.getId() + " rights, this dataset has neither examination nor processing parent !");
		}
		return studyId;
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
    	if (dataset.getDatasetProcessing() != null) {
    		//Forbid processed dataset update for the moment
    		return false;
    	}
    	Dataset dbDataset = datasetRepository.findById(dataset.getId()).orElse(null);
    	if (dbDataset == null) {
			throw new EntityNotFoundException("Cannot find dataset with id " + dataset.getId());
		}
    	return this.hasRightOnStudyCenter(dbDataset.getDatasetAcquisition().getExamination().getCenterId(), dbDataset.getDatasetAcquisition().getExamination().getStudyId(), rightStr);
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
		if (datasetAcq.getExamination().getStudyId().equals(dbDatasetAcq.getExamination().getStudyId())) { // study hasn't changed
			return this.hasRightOnStudyCenter(datasetAcq.getExamination().getCenterId(), datasetAcq.getExamination().getStudyId(), rightStr);
		} else { // study has changed : check user has right on both studies
			return this.hasRightOnStudyCenter(datasetAcq.getExamination().getCenterId(), datasetAcq.getExamination().getStudyId(), rightStr) &&
					this.hasRightOnStudyCenter(dbDatasetAcq.getExamination().getCenterId(), dbDatasetAcq.getExamination().getStudyId(), rightStr);
		}
	}

    /**
     * Check the connected user has the given right for the given dataset acquisition DTO.
     * If the study is updated, check the user has the given right in both former and new studies.
     * 
     * @param datasetAcqDto the dataset acquisition dto
     * @param rightStr the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasUpdateRightOnDatasetAcquisitionDTO(DatasetAcquisitionDTO datasetAcqDto, String rightStr) throws EntityNotFoundException {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
    	if (datasetAcqDto == null) {
			throw new IllegalArgumentException("Dataset acquisition cannot be null here.");
		}
    	if (datasetAcqDto.getId() == null) {
			throw new IllegalArgumentException("Dataset acquisition id cannot be null here.");
		}
    	if (datasetAcqDto.getExamination() == null || datasetAcqDto.getExamination().getStudyId() == null) {
			return false;
		}
    	DatasetAcquisition dbDatasetAcq = datasetAcquisitionRepository.findById(datasetAcqDto.getId()).orElse(null);
    	if (dbDatasetAcq == null) {
			throw new EntityNotFoundException("Cannot find dataset acquisition with id " + datasetAcqDto.getId());
		}
    	if (datasetAcqDto.getExamination().getStudyId().equals(dbDatasetAcq.getExamination().getStudyId())) { // study hasn't changed
			return this.hasRightOnStudyCenter(datasetAcqDto.getExamination().getCenterId(), datasetAcqDto.getExamination().getStudyId(), rightStr);
		} else { // study has changed : check user has right on both studies
			return this.hasRightOnStudyCenter(datasetAcqDto.getExamination().getCenterId(), datasetAcqDto.getExamination().getStudyId(), rightStr) &&
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
        return hasUpdateRightOnCard(studyCard, rightStr);
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
    public boolean hasUpdateRightOnQualityCard(QualityCard qualityCard, String rightStr) throws EntityNotFoundException {
        return hasUpdateRightOnCard(qualityCard, rightStr);
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
   public boolean hasUpdateRightOnCard(Card card, String rightStr) throws EntityNotFoundException {
       if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
           return true;
       }
       if (card == null) {
           throw new IllegalArgumentException("Study card cannot be null here.");
       }
       if (card.getId() == null) {
           throw new IllegalArgumentException("Study card id cannot be null here.");
       }
       if (card.getStudyId() == null) {
           return false;
       }
       Card dbCard;
       if (card instanceof StudyCard) {
           dbCard = studyCardRepository.findById(card.getId()).orElse(null);
           if (dbCard == null) {
               throw new EntityNotFoundException("Cannot find study card with id " + card.getId());
           }           
       } else if (card instanceof QualityCard) {
           dbCard = qualityCardRepository.findById(card.getId()).orElse(null);
           if (dbCard == null) {
               throw new EntityNotFoundException("Cannot find quality card with id " + card.getId());
           }           
       } else throw new IllegalStateException("Cannot find the type of card");
       if (card.getStudyId().equals(dbCard.getStudyId())) { // study hasn't changed
           return commService.hasRightOnStudy(card.getStudyId(), rightStr);
       } else { // study has changed : check user has right on both studies
           return commService.hasRightOnStudy(card.getStudyId(), rightStr) && commService.hasRightOnStudy(dbCard.getStudyId(), rightStr);
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
		UserRights userRights = commService.getUserRights();
		for (Dataset dataset : page) {
			Long studyId = dataset.getDatasetAcquisition().getExamination().getStudyId();
			Long centerId = dataset.getDatasetAcquisition().getExamination().getCenterId();
			if(!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				return false;
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
    	UserRights userRights = commService.getUserRights();
		for (DatasetDTO dataset : page) {
			Long studyId = dataset.getStudyId();
			Long centerId = dataset.getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
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
		UserRights userRights = commService.getUserRights();
    	Set<Dataset> toRemove = new HashSet<>();
    	list.forEach((Dataset ds) -> {
            if (ds.getDatasetAcquisition() == null 
                    || ds.getDatasetAcquisition().getExamination() == null
                    || ds.getDatasetAcquisition().getExamination().getStudyId() == null) {
				// if processed dataset (no center)
                if (ds.getDatasetProcessing() != null && ds.getDatasetProcessing().getInputDatasets() != null) {
					if (userRights.hasStudyRights(ds.getStudyId(), rightStr)) {
						// filter the input datasets as well
						filterDatasetList(ds.getDatasetProcessing().getInputDatasets(), rightStr);
					} else {
						toRemove.add(ds);
					}
                } else {
                    throw new IllegalStateException("Cannot check dataset n°" + ds.getId() + " rights, this dataset has neither examination nor processing parent !");                
                }
            } else { // general case
				Long studyId = ds.getDatasetAcquisition().getExamination().getStudyId();
				Long centerId = ds.getDatasetAcquisition().getExamination().getCenterId();
				// check rightStr on study, then check center rights
				if (!userRights.hasStudyRights(studyId, rightStr) || !userRights.hasStudyCenterRights(studyId, centerId)) {
					toRemove.add(ds);
				}
			}
    	});
    	list.removeAll(toRemove);
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
		UserRights userRights = commService.getUserRights();
		for (Examination exam : page) {
			Long studyId = exam.getStudyId();
			Long centerId = exam.getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
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
    	Set<Examination> toRemove = new HashSet<>();
		UserRights userRights = commService.getUserRights();
		for (Examination exam : list) {
			Long studyId = exam.getStudyId();
			Long centerId = exam.getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				toRemove.add(exam);
			}
		}
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
		if (list == null || list.isEmpty()) {
			return true;
		}
		List<Long> dsIds = list.stream().map(dto -> dto.getId()).collect(Collectors.toList());
		List<DatasetForRights> dtos = datasetRepository.findDatasetsForRights(dsIds)
			.stream()
			.map(ds -> new DatasetForRights(ds.getId(), ds.getCenterId(), ds.getStudyId(), ds.getRelatedStudiesIds()))
			.collect(Collectors.toList());
		Set<Long> dsRemove = new HashSet<>();
		UserRights userRights = commService.getUserRights();
		for (DatasetForRights ds : dtos) {
			Set<Long> studyIds = ds.getAllStudiesIds();
			Long centerId = ds.getCenterId();
			for (Long studyId : studyIds) {
				if (userRights.hasStudyRights(studyId, rightStr)) {
					if (userRights.hasCenterRestrictionsFor(studyId) && !userRights.hasStudyCenterRights(studyId, centerId)) {
						dsRemove.add(ds.getId());
						break;
					}
				} else {
					dsRemove.add(ds.getId());
					break;
				}
			}
		}
    	list.removeIf(a -> dsRemove.contains(a.getId()));
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
		if (list == null || list.isEmpty()) return true;
		Set<DatasetAcquisition> toRemove = new HashSet<>();
		UserRights userRights = commService.getUserRights();
		for (DatasetAcquisition da : list) {
			Long studyId = da.getExamination().getStudyId();
			Long centerId = da.getExamination().getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				toRemove.add(da);
			}
		}
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
		if (list == null || list.isEmpty()) return true;
		Set<DatasetAcquisitionDTO> toRemove = new HashSet<>();
		UserRights userRights = commService.getUserRights();
		for (DatasetAcquisitionDTO da : list) {
			Long studyId = da.getExamination().getStudyId();
			Long centerId = da.getExamination().getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				toRemove.add(da);
			}
		}
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
	public boolean filterExaminationDatasetAcquisitionDTOList(List<ExaminationDatasetAcquisitionDTO> list, String rightStr) throws EntityNotFoundException {
		if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
	   	if (list == null || list.isEmpty()) {
			return true;
		}
		List<Long> examinationIds = list.stream().map(dto -> dto.getId()).collect(Collectors.toList());
		Set<Long> examsToRemove = new HashSet<>();
		UserRights userRights = commService.getUserRights();
		List<ExaminationForRightsDTO> exams = examinationRepository.findExaminationsForRights(examinationIds);
		for (ExaminationForRightsDTO exam : exams) {
			Long studyId = exam.getStudyId();
			Long centerId = exam.getCenterId();
			if(!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				examsToRemove.add(exam.getId());
			}
		}
    	list.removeIf(e -> examsToRemove.contains(e.getId()));
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
		if (page == null || page.isEmpty()) {
			return true;
		}
		UserRights userRights = commService.getUserRights();
		for (DatasetAcquisitionDTO acquisition : page) {
			Long studyId = acquisition.getExamination().getStudyId();
			Long centerId = acquisition.getExamination().getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
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
		if (page == null || page.isEmpty()) {
			return true;
		}
		UserRights userRights = commService.getUserRights();
		for (DatasetAcquisition acquisition : page) {
			Long studyId = acquisition.getExamination().getStudyId();
			Long centerId = acquisition.getExamination().getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				return false;
			}
		}
		return true;
    }
    
    /**
     * Filter study cards in that page checking the connected user has the right on those cards.
     * 
     * @param page the page
     * @param rightStr the right
     * @return true
     */
    public boolean filterCardList(List<Card> list, String rightStr) {
        if (list == null) {
            return true;
        }
        Set<Long> studyIds = new HashSet<>();
        list.forEach((Card sc) -> {
            studyIds.add(sc.getStudyId());
        });
        Set<Long> checkedIds = commService.hasRightOnStudies(studyIds, rightStr);
        list.removeIf((Card sc) -> !checkedIds.contains(sc.getStudyId()));
        
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
    	if (page == null || page.isEmpty()) {
			return true;
		}
		UserRights userRights = commService.getUserRights();
		for (ExaminationDTO exam : page) {
			Long studyId = exam.getStudyId();
			Long centerId = exam.getCenterId();
			if (!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
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
		if (list == null || list.isEmpty()) {
			return true;
		}
		UserRights userRights = commService.getUserRights();
		Set<ExaminationDTO> examsToRemove = new HashSet<>();
		for (ExaminationDTO exam : list) {
			Long studyId = exam.getStudyId();
			Long centerId = exam.getCenterId();
			if(!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
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

	public boolean hasRightOnEveryDatasetOfProcessings(List<Long> processingIds, String rightStr) {
		if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN) || processingIds == null || processingIds.isEmpty()) {
			return true;
		}
		List<DatasetForRights> datasets = datasetRepository.findAllInputsByProcessingId(processingIds)
			.stream()
			.map(ds -> new DatasetForRights(ds.getId(), ds.getCenterId(), ds.getStudyId(), ds.getRelatedStudiesIds()))
			.collect(Collectors.toList());
		UserRights userRights = commService.getUserRights();
		for (DatasetForRights dataset : datasets) {
			Set<Long> studyIds = dataset.getAllStudiesIds();
			Long centerId = dataset.getCenterId();
			if (!userRights.hasStudiesCenterRights(studyIds, centerId, rightStr)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check that the connected user has the given right for the given examination.
	 *
	 * @param examinationIds the examination ids
	 * @param rightStr the right
	 * @return true or false
	 * @throws EntityNotFoundException
	 */
	public boolean hasRightOnExaminations(List<Long> examinationIds, String rightStr) throws EntityNotFoundException {
		if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
		List<ExaminationForRightsDTO> exams = examinationRepository.findExaminationsForRights(examinationIds);
		UserRights userRights = commService.getUserRights();
		for (ExaminationForRightsDTO exam : exams) {
			Long studyId = exam.getStudyId();
			Long centerId = exam.getCenterId();
			if(!userRights.hasStudyCenterRights(studyId, centerId, rightStr)) {
				return false;
			}
		}
		return true;
	}
}
