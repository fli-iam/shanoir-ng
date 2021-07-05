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
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.shanoir.ng.shared.migration.MigrationJob;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyMigrationService {

	private static final Logger LOG = LoggerFactory.getLogger(StudyMigrationService.class);

	@Autowired
	private StudyService studyService;

	@Autowired
	private DistantStudiesShanoirService distantShanoir;

	private List<ManufacturerModel> distantModels = null;

	private List<AcquisitionEquipment> distantEquipements = null;

	private List<Manufacturer> distantManufacturers = null;

	private Map <Long, Long> equipmentMap = new HashMap<>();

	private Map <Long, Long> centerMap = new HashMap<>();

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	DistantKeycloakConfigurationService distantKeycloak;

	@Autowired ObjectMapper objectMapper;

	/**
	 * Migrates a given study and all its elements
	 * This method is synchronized to avoid multiple threads running the same
	 * @param studyId the study to migrate
	 * @throws Exception
	 */
	public synchronized void migrateStudy(Long studyId, Long userId, String username, String url) throws Exception {
		Long currentUserId = KeycloakUtil.getTokenUserId();
		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.MIGRATE_STUDY_EVENT, ""+studyId, currentUserId, "Starting migration...", ShanoirEvent.IN_PROGRESS, 0f);
		eventService.publishEvent(event);
		try {
			Study study = this.studyService.findById(studyId);

			Map<Long,Long> subjectMap = new HashMap<>();

			// Remove all groupsOfSubjects
			study.setExperimentalGroupsOfSubjects(null);

			event.setMessage("Migrating subjects...");
			eventService.publishEvent(event);

			// Move all concerned subjects
			for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				Subject subj = subjectStudy.getSubject();
				subj.setUserPersonalCommentList(null);
				Long oldId = subj.getId();
				subj.setId(null);
				subj.setSubjectStudyList(Collections.emptyList());
				Subject sub = distantShanoir.createSubject(subj);
				subj.setId(oldId);
				// Keep updated a map of oldSubjectId => distantSubjectId
				subjectMap.put(oldId, sub.getId());
			}

			event.setMessage("Migrating centers...");
			eventService.publishEvent(event);

			// Move all centers
			List<StudyCenter> centers = moveCenters(study.getStudyCenterList());
			study.setStudyCenterList(centers);

			event.setMessage("Migrating study...");
			eventService.publishEvent(event);

			// Migrate study
			Long oldStudyId = study.getId();

			// Reset ID
			study.setId(null);

			// Reset SubjectStudy to update with subjects
			for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				subjectStudy.setId(null);
				subjectStudy.setStudy(null);
				Subject newSubject = new Subject();
				newSubject.setId(subjectMap.get(subjectStudy.getSubject().getId()));
				subjectStudy.setSubject(newSubject);
			}
			// Reset studyExaminations
			study.setExaminationIds(Collections.emptyList());

			// Set StudyUser to only current user
			List<StudyUser> suList = new ArrayList<>();
			StudyUser su = new StudyUser();
			su.setUserId(userId);
			su.setConfirmed(true);
			List<StudyUserRight> rights = new ArrayList<>();
			rights.add(StudyUserRight.CAN_SEE_ALL);
			rights.add(StudyUserRight.CAN_IMPORT);
			rights.add(StudyUserRight.CAN_DOWNLOAD);
			rights.add(StudyUserRight.CAN_ADMINISTRATE);
			su.setStudyUserRights(rights);
			su.setStudy(new Study());
			su.setUserName(username);
			suList.add(su);
			study.setStudyUserList(suList);

			StudyDTO newStudy = distantShanoir.createStudy(study);

			// Add protocol/ DUA files
			// Can easily be done manually

			for (String fileName : study.getProtocolFilePaths()) {
				File file = new File(studyService.getStudyFilePath(oldStudyId, fileName));
				distantShanoir.addProtocoleFile(file, newStudy.getId());
			}

			// Send a message over rabbitMQ to move all other microservices
			// In the event message, put the subject/subject map and the new study Service (use a specific usefull object used in common ?)
			MigrationJob job = new MigrationJob();
			job.setOldStudyId(oldStudyId);
			job.setStudy(new IdName(newStudy.getId(), newStudy.getName()));
			job.setCentersMap(centerMap);
			job.setEquipmentMap(equipmentMap);
			job.setSubjectsMap(subjectMap);
			job.setEvent(event);
			job.setShanoirUrl(url);
			job.setAccessToken(distantKeycloak.getAccessToken());
			job.setRefreshToken(this.distantKeycloak.getRefreshToken());

			event.setMessage("Migrating data...");
			eventService.publishEvent(event);

			rabbitTemplate.convertAndSend(RabbitMQConfiguration.STUDY_MIGRATION_QUEUE, objectMapper.writeValueAsString(job));

		} catch (ShanoirException se) {
			event.setStatus(ShanoirEvent.ERROR);
			event.setProgress(0f);
			event.setMessage(se.getMessage());
			eventService.publishEvent(event);
			throw se;
		} catch (Exception e) {
			event.setStatus(ShanoirEvent.ERROR);
			event.setProgress(0f);
			event.setMessage("An unexpected error occured during study migration, please contact an administrator.");
			eventService.publishEvent(event);
			throw e;
		}

		// Reset lists
		distantModels = null;
		distantEquipements = null;
		distantManufacturers = null;
		equipmentMap = new HashMap<>();
		centerMap = new HashMap<>();
	}

	/**
	 * Migrate all centers
	 * @param centers
	 * @return the list of new centers
	 * @throws ShanoirException
	 */
	private List<StudyCenter> moveCenters(List<StudyCenter> centers) throws ShanoirException {
		// 1 Get all distant centers
		List<IdName> distantCenters = distantShanoir.getAllCenters();
		List<StudyCenter> toSet = new ArrayList<>();
		LOG.error("Migration Center" + centers);


		for (StudyCenter studyCenter : centers) {
			boolean found = false;
			Center center = studyCenter.getCenter();
			Long oldId = center.getId();

			for (IdName distantCenter : distantCenters) {
				// If center already exists, use it.
				if (distantCenter.getName().equals(center.getName())) {
					studyCenter.getCenter().setId(distantCenter.getId());
					found = true;
					moveAcquisitionEquipements(center.getAcquisitionEquipments(), distantCenter.getId());
					break;
				}
			}
			// Otherwise create a new one
			if (!found) {
				center.setId(null);
				center.setStudyCenterList(null);
				List<AcquisitionEquipment> oldEquipments = center.getAcquisitionEquipments();
				center.setAcquisitionEquipments(null);
				Center newCenter = distantShanoir.createCenter(center);
				moveAcquisitionEquipements(oldEquipments, newCenter.getId());
				Center newCenterToSet = new Center();
				newCenterToSet.setId(newCenter.getId());
				studyCenter.setCenter(newCenterToSet);
			}
			studyCenter.setId(null);
			studyCenter.getCenter().setStudyCenterList(null);
			studyCenter.setStudy(null);
			toSet.add(studyCenter);
			centerMap.put(oldId, studyCenter.getCenter().getId());
		}
		return toSet;
	}

	/**
	 * Migrate all acquisition equipements
	 * @param acquisitionEquipments
	 * @param centerId the center ID
	 * @return
	 * @throws ShanoirException
	 */
	private void moveAcquisitionEquipements(List<AcquisitionEquipment> acquisitionEquipments, Long centerId) throws ShanoirException {
		LOG.error("Migration Acq" + acquisitionEquipments);

		distantEquipements = distantEquipements != null? distantEquipements : distantShanoir.getAcquisitionEquipements();
		for (AcquisitionEquipment equipement : acquisitionEquipments) {
			Long oldId = equipement.getId();
			boolean found = false;
			for (AcquisitionEquipment distantEquipement : distantEquipements) {
				// If equipement already exists, use it.
				if (distantEquipement.getSerialNumber() != null && distantEquipement.getSerialNumber().equals(equipement.getSerialNumber())) {
					equipement.setId(distantEquipement.getId());
					found = true;
					// No need to move manufacturer and model here is acquisition equipment already exist,
					// then we consider it has adapted references
					break;
				}
			}
			// Otherwise, create it
			if (!found) {
				// Migrate all manufacturer models if necessary
				ManufacturerModel model = this.moveManufacturerModel(equipement.getManufacturerModel());
				equipement.setId(null);
				equipement.setManufacturerModel(model);
				Center dtoCenter = new Center();
				dtoCenter.setId(centerId);
				equipement.setCenter(dtoCenter );
				equipement = distantShanoir.createEquipement(equipement);
				this.distantEquipements.add(equipement);
			}
			this.equipmentMap.put(oldId, equipement.getId());
		}
	}

	/**
	 * Moves a manufaturer's model
	 * @param manufacturerModel
	 * @return
	 * @throws ShanoirException
	 */
	private ManufacturerModel moveManufacturerModel(ManufacturerModel manufacturerModel) throws ShanoirException {
		LOG.error("Migration Model" + manufacturerModel);

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
			this.distantModels.add(manufacturerModel);
		}
		return manufacturerModel;
	}

	/**
	 * Moves a manufacturer
	 * @param manufacturer the manufacturer to move
	 * @return the moved manufacturer
	 * @throws ShanoirException
	 */
	private Manufacturer moveManufacturer(Manufacturer manufacturer) throws ShanoirException {
		LOG.error("Migration manuf" + manufacturer);

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
