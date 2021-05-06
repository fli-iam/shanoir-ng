package org.shanoir.ng.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

	@Autowired
	private StudyService studyService;

	@Autowired
	private DistantShanoirService distantShanoir;

	private List<ManufacturerModel> distantModels = null;
	
	private List<AcquisitionEquipment> distantEquipements = null;
	
	private List<Manufacturer> distantManufacturers = null;

	/**
	 * Migrates a given study and all its elements
	 * This method is synchronized to avoid multiple threads running the same
	 * @param studyId the study to migrate
	 */
	public synchronized void migrateStudy(Long studyId, Long userId) {
		Study study = this.studyService.findById(studyId);
		
		Map<Long,Long> subjectMap = new HashMap<>();

		// Remove all groupsOfSubjects
		study.setExperimentalGroupsOfSubjects(null);
		
		// Move all concerned subjects
		for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
			Subject subj = subjectStudy.getSubject();
			subj.setUserPersonalCommentList(null);
			Long oldId = subj.getId();
			subj.setId(null);
			subj.setSubjectStudyList(Collections.emptyList());
			Subject sub = distantShanoir.createSubject(subj);

			// Keep updated a map of oldSubjectId => distantSubjectId
			subjectMap.put(oldId, sub.getId());
		}
		
		// Move all centers
		List<StudyCenter> centers = moveCenters(study.getStudyCenterList());
		study.setStudyCenterList(centers);

		// Migrate study
		Long oldStudyId = study.getId();
		
		// Reset ID
		study.setId(null);
		
		// Reset SubjectStudy to update with subjects
		for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
			subjectStudy.setId(null);
			// Replace subject Id
			subjectStudy.getSubject().setId(subjectMap.get(subjectStudy.getId()));
		}
		// Reset studyExaminations
		study.setExaminationIds(Collections.emptyList());
		
		StudyUser su = new StudyUser();
		
		for (StudyUser sUser : study.getStudyUserList()) {
			if (sUser.getUserId().equals(KeycloakUtil.getTokenUserId())) {
				su = sUser;
				break;
			}
		}
		// Set StudyUser to only current user
		su.setStudy(study);
		su.setUserId(userId);
		
		study.setStudyUserList(Collections.singletonList(su));
		
		distantShanoir.createStudy(study);
		
		// Add protocol/ DUA files
		for (String file : study.getProtocolFilePaths()) {
			File fileAsFile = new File(studyService.getStudyFilePath(oldStudyId, file));
			distantShanoir.addProtocoleFile(fileAsFile);
		}

		// Reset lists
		distantModels = null;
		distantEquipements = null;
		distantManufacturers = null;
		
		// Send a message over rabbitMQ to move all other microservices
		// In the event message, put the subject/subject map and the new study Service (use a specific usefull object used in common ?)
	}

	/**
	 * Migrate all centers
	 * @param centers
	 * @return the list of new centers
	 */
	private List<StudyCenter> moveCenters(List<StudyCenter> centers) {
		// 1 Get all distant centers
		List<IdName> distantCenters = distantShanoir.getAllCenters();
		List<StudyCenter> toSet = new ArrayList<>();

		for (StudyCenter studyCenter : centers) {
			boolean found = false;
			Center center = studyCenter.getCenter();
			
			for (IdName distantCenter : distantCenters) {
				// If center already exists, use it.
				if (distantCenter.getName().equals(center.getName())) {
					studyCenter.getCenter().setId(distantCenter.getId());
					found = true;
					break;
				}
			}
			// Otherwise create a new one
			if (!found) {
				center.setId(null);
				center.setAcquisitionEquipments(Collections.emptyList());
				Center newCenter = distantShanoir.createCenter(center);
				List<AcquisitionEquipment> equipements = moveAcquisitionEquipements(center.getAcquisitionEquipments(), newCenter);
				studyCenter.getCenter().setId(newCenter.getId());
				studyCenter.getCenter().setAcquisitionEquipments(equipements);
			}
			toSet.add(studyCenter);
		}
		return toSet;
	}

	/**
	 * Migrate all acquisition equipements
	 * @param acquisitionEquipments
	 * @param centerId the center ID
	 * @return
	 */
	private List<AcquisitionEquipment> moveAcquisitionEquipements(List<AcquisitionEquipment> acquisitionEquipments, Center center) {
		distantEquipements = distantEquipements != null? distantEquipements : distantShanoir.getAcquisitionEquipements();
		List<AcquisitionEquipment> toSet = new ArrayList<>();
		for (AcquisitionEquipment equipement : acquisitionEquipments) {
			boolean found = false;
			for (AcquisitionEquipment distantEquipement : distantEquipements) {
				// If equipement already exists, use it.
				if (distantEquipement.getSerialNumber().equals(equipement.getSerialNumber())) {
					equipement.setId(distantEquipement.getId());
					found = true;
					break;
				}
			}
			// Otherwise, create it
			if (!found) {
				// Migrate all manufacturer models if necessary
				ManufacturerModel model = this.moveManufacturerModel(equipement.getManufacturerModel());
				equipement.setId(null);
				equipement.setManufacturerModel(model);
				equipement = distantShanoir.createEquipement(equipement);
				this.distantEquipements.add(equipement);
			}
			equipement.setCenter(center);
			toSet.add(equipement);
		}
		return toSet;
	}

	/**
	 * Moves a manufaturer's model
	 * @param manufacturerModel
	 * @return
	 */
	private ManufacturerModel moveManufacturerModel(ManufacturerModel manufacturerModel) {
		distantModels = distantModels != null ? distantModels : distantShanoir.getModels();
		boolean found = false;
		for (ManufacturerModel distantModel : distantModels) {
			if (distantModel.getName().equals(manufacturerModel.getName())) {
				// If the manufacturer model exists, use it
				manufacturerModel.setId(distantModel.getId());
				manufacturerModel.setManufacturer(distantModel.getManufacturer());
				found = true;
				break;
			}
		}
		// Otherwise, create a new one
		if (!found) {
			manufacturerModel.setId(null);
			Manufacturer manufToSet = moveManufacturer(manufacturerModel.getManufacturer());
			manufacturerModel.setManufacturer(manufToSet);
			manufacturerModel = distantShanoir.createManufacturerModel(manufacturerModel);
		}
		return manufacturerModel;
	}

	/**
	 * Moves a manufacturer
	 * @param manufacturer the manufacturer to move
	 * @return the moved manufacturer
	 */
	private Manufacturer moveManufacturer(Manufacturer manufacturer) {
		distantManufacturers = distantManufacturers != null ? distantManufacturers : this.distantShanoir.getManufacturers();
		
		boolean found = false;
		for (Manufacturer distantManufacturer : distantManufacturers) {
			if (distantManufacturer.getName().equals(manufacturer.getName())) {
				// If the manufacturer model exists, use it
				manufacturer.setId(distantManufacturer.getId());
				found = true;
				break;
			}
		}
		// Otherwise, create a new one
		if (!found) {
			manufacturer.setId(null);
			manufacturer = distantShanoir.createManufacturer(manufacturer);
			distantManufacturers.add(manufacturer);
		}
		
		return manufacturer;
	}

}
