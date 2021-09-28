package org.shanoir.ng.preclinical.migration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticRepository;
import org.shanoir.ng.preclinical.anesthetics.examination_anesthetics.ExaminationAnesthetic;
import org.shanoir.ng.preclinical.anesthetics.examination_anesthetics.ExaminationAnestheticRepository;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyRepository;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.RefsRepository;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectRepository;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyRepository;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.shanoir.ng.shared.migration.MigrationJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PreclinicalMigrationService {

	
	private static final Logger LOG = LoggerFactory.getLogger(PreclinicalMigrationService.class);

	@Autowired
	ObjectMapper mapper;

	@Autowired
	private DistantKeycloakConfigurationService distantKeycloakConfigurationService;

	@Autowired
	EntityManager entityManager;

	@Autowired
	RefsRepository referenceRepository;

	@Autowired
	AnimalSubjectRepository animalSubjectRepository;

	@Autowired
	SubjectTherapyRepository subjectTherapyRespository;

	@Autowired
	SubjectPathologyRepository subjectPathologyRepository;

	@Autowired
	AnestheticRepository anestheticRepository;
	
	@Autowired
	TherapyRepository therapyRepository;

	@Autowired
	ExaminationAnestheticRepository examAnetheticRepository;

	@Autowired
	DistantPreclinicalShanoirService distantShanoir;
	
	Map<String, Reference> references;

	Map<String, Anesthetic> anesthetics;

	Map<String, PathologyModel> pathologyModels;
	
	Map<String, Pathology> pathologies;

	Map<String, Therapy> therapies;
	
	@Autowired
	ShanoirEventService eventService;
	
	ShanoirEvent event;

	/**
	 * Migrates all the datasets from a study using a MigrationJob
	 * @throws ShanoirException
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_MIGRATION_PRECLINICAL_QUEUE)
	@RabbitHandler
	@Transactional(readOnly = true)
	public void migrate(String migrationJobAsString) throws AmqpRejectAndDontRequeueException {
		try {
			MigrationJob job = mapper.readValue(migrationJobAsString, MigrationJob.class);
			distantKeycloakConfigurationService.setRefreshToken(job.getRefreshToken());
			String keycloakURL = job.getShanoirUrl() + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
			distantKeycloakConfigurationService.setServer(job.getShanoirUrl());
			distantKeycloakConfigurationService.setAccessToken(job.getAccessToken());
			distantKeycloakConfigurationService.refreshToken(keycloakURL);
			
			event = job.getEvent();
			publishEvent("Finishing migration with preclinical elements...", 1f);

			this.migratePreclinical(job);
			event.setStatus(ShanoirEvent.SUCCESS);
			publishEvent("Successfully migrated study " + job.getStudy().getName() +  " to server " + job.getShanoirUrl(), 1f);
		} catch (Exception e) {
			LOG.error("Error while moving preclinical elements: ", e);
			event.setStatus(ShanoirEvent.ERROR);
			publishEvent("An error occured while migrating preclinical elements, please contact an administrator.", 1f);
			throw new AmqpRejectAndDontRequeueException(e);
		} finally {
			// Stop token refresh
			distantKeycloakConfigurationService.stop();
		}
	}

	private void prepareReferences() throws ShanoirException {
		// Get all distant preclinical references once
		references = new HashMap<>();
		distantShanoir.getPreclinicalReferences().stream().forEach(element -> references.put(element.getValue() + element.getCategory() + element.getReftype(), element));
		
		anesthetics = new HashMap<>();
		distantShanoir.getAnesthetics().stream().forEach(element -> anesthetics.put(element.getName(), element));
		
		pathologyModels = new HashMap<>();
		distantShanoir.getPathologyModels().stream().forEach(element -> pathologyModels.put(element.getName(), element));
		
		pathologies = new HashMap<>();
		distantShanoir.getPathologies().stream().forEach(element -> pathologies.put(element.getName(), element));
		
		therapies = new HashMap<>();
		distantShanoir.getTherapies().stream().forEach(element -> therapies.put(element.getName(), element));
	}
	
	private void migratePreclinical(MigrationJob job) throws ShanoirException {
		LOG.error("Retrieving all preclinical references");
		prepareReferences();

		// Animal_subject
		for (Entry<Long, Long> entry : job.getSubjectsMap().entrySet()) {
			for (AnimalSubject animalSubject : animalSubjectRepository.findBySubjectId(entry.getKey())) {
				LOG.error("moving Animal subject " + animalSubject.getId() + animalSubject.getSubjectId());
				migrateSubject(animalSubject, job);
			}
		}
		
		// Migrate AnestheticExamination
		for (Long exam : job.getExaminationMap().keySet()) {
			for (ExaminationAnesthetic examAnes :  examAnetheticRepository.findByExaminationId(exam)) {
				migrateAnestheticExamination(examAnes, job);
			}
		}
	}

	private void migrateAnestheticExamination(ExaminationAnesthetic examAnes, MigrationJob job) throws ShanoirException {
		Anesthetic anesth = migrateAnesthetic(examAnes.getAnesthetic(), job);
		Reference doseUnit = migrateReference(examAnes.getDoseUnit(), job);
		entityManager.detach(examAnes);
		examAnes.setId(null);
		examAnes.setAnesthetic(anesth);
		examAnes.setDoseUnit(doseUnit);
		LOG.error("Examination anesthetic: " + examAnes.getExaminationId() + " " + job.getExaminationMap().get(examAnes.getExaminationId()));
		examAnes.setExaminationId(job.getExaminationMap().get(examAnes.getExaminationId()));
		distantShanoir.createExaminationAnesthatic(examAnes, examAnes.getExaminationId());
	}

	private Anesthetic migrateAnesthetic(Anesthetic anes, MigrationJob job) throws ShanoirException {
		LOG.error("moving Anesthetic");
		if (anesthetics.get(anes.getName()) != null) {
			return anesthetics.get(anes.getName());
		}
		Set<AnestheticIngredient> oldIngredients = anes.getIngredients();
		entityManager.detach(anes);

		anes.setId(null);
		anes.setIngredients(null);
		Anesthetic cretedAnesthetic = distantShanoir.createAnesthetic(anes);
		for(AnestheticIngredient ingredient : oldIngredients) {
			migrateAnestheticIngredient(ingredient, cretedAnesthetic, job);
		}
		return cretedAnesthetic;
	}

	private AnestheticIngredient migrateAnestheticIngredient(AnestheticIngredient ingredient, Anesthetic anesth, MigrationJob job) throws ShanoirException {
		LOG.error("moving ingredient");
		ingredient.setConcentrationUnit(migrateReference(ingredient.getConcentrationUnit(), job));
		entityManager.detach(ingredient);
		ingredient.setId(null);
		Anesthetic anesthDTO = new Anesthetic();
		anesthDTO.setId(anesth.getId());
		ingredient.setAnesthetic(anesthDTO);
		return distantShanoir.createIngredient(ingredient, anesthDTO.getId());
	}

	private void migrateSubject(AnimalSubject animalSubject, MigrationJob job) throws ShanoirException {
		// Migrate all subject references
		entityManager.detach(animalSubject);

		Long oldId = animalSubject.getId();
		Long oldSubjectId = animalSubject.getSubjectId();
		animalSubject.setId(null);
		animalSubject.setSubjectId(job.getSubjectsMap().get(animalSubject.getSubjectId()));
		migrateReference(animalSubject.getBiotype(), job);
		migrateReference(animalSubject.getProvider(), job);
		migrateReference(animalSubject.getSpecie(), job);
		migrateReference(animalSubject.getStabulation(), job);
		migrateReference(animalSubject.getStrain(), job);

		// Migrate Subject
		AnimalSubject createdSubject = distantShanoir.createAnimalSubject(animalSubject);

		AnimalSubject animalDTO = new AnimalSubject();
		animalDTO.setId(oldId);
		animalDTO.setSubjectId(oldSubjectId);
		entityManager.detach(animalDTO);

		// Subject_pathology
		for (SubjectPathology subjectPatho : subjectPathologyRepository.findByAnimalSubject(animalDTO)) {
			LOG.error("moving subject pathology");
			entityManager.detach(subjectPatho);
			migrateSubjectPathology(subjectPatho, job, createdSubject);
		}

		entityManager.detach(animalDTO);

		// Subject_therapy
		for (SubjectTherapy subjecttherap: subjectTherapyRespository.findByAnimalSubject(animalDTO)) {
			LOG.error("moving subject therapy");
			entityManager.detach(subjecttherap);
			migrateSubjectTherapy(subjecttherap, job, createdSubject);
		}
	}

	private void migrateSubjectTherapy(SubjectTherapy subjecttherap, MigrationJob job, AnimalSubject createdSubject) throws ShanoirException {
		// Migrate therapy
		Therapy therap = this.migrateTherapy(subjecttherap.getTherapy(), job);
		
		// Migrate doseUnit reference
		Reference doseUnit = migrateReference(subjecttherap.getDoseUnit(), job);
		subjecttherap.setDoseUnit(doseUnit);
		subjecttherap.setTherapy(therap);
		
		// Migrate subjectTherapy
		distantShanoir.createSubjectTherapy(subjecttherap, createdSubject.getId());
	}

	private void migrateSubjectPathology(SubjectPathology subjectPatho, MigrationJob job, AnimalSubject createdSubject) throws ShanoirException {
		Pathology patho = migratePathology(subjectPatho.getPathology());
		PathologyModel model =  migratePathologyModel(subjectPatho.getPathologyModel());
		Reference location = migrateReference(subjectPatho.getLocation(), job);
		
		subjectPatho.setPathology(patho);
		subjectPatho.setPathologyModel(model);
		subjectPatho.setLocation(location);
		subjectPatho.setId(null);
		
		distantShanoir.createSubjectPathology(subjectPatho, createdSubject.getId());
	}
	
	private Therapy migrateTherapy(Therapy therapy, MigrationJob job) throws ShanoirException {
		LOG.error("moving therapy");
		if (therapies.get(therapy.getName()) != null) {
			return therapies.get(therapy.getName());
		}
		therapy.setId(null);
		return distantShanoir.createTherapy(therapy);
	}

	private PathologyModel migratePathologyModel(PathologyModel pathologyModel) throws ShanoirException {
		LOG.error("moving patho model");
		if (pathologyModels.get(pathologyModel.getName()) != null) {
			return pathologyModels.get(pathologyModel.getName());
		}
		pathologyModel.setId(null);
		return distantShanoir.createPathologyModel(pathologyModel);
	}

	private Pathology migratePathology(Pathology pathology) throws ShanoirException {
		LOG.error("moving patho");
		if (pathologies.get(pathology.getName()) != null) {
			return pathologies.get(pathology.getName());
		}
		pathology.setId(null);
		return distantShanoir.createPathology(pathology);
	}

	private Reference migrateReference(Reference ref, MigrationJob job) throws ShanoirException {
		LOG.error("moving reference");
		if (references.get(ref.getValue() + ref.getCategory() + ref.getReftype()) != null) {
			return references.get(ref.getValue() + ref.getCategory() + ref.getReftype());
		}
		ref.setId(null);
		return distantShanoir.createReference(ref);
	}

	private void publishEvent(String message, float progress) {
		event.setMessage(message);
		event.setProgress(progress);
		eventService.publishEvent(event);
	}
}
