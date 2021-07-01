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
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.shanoir.ng.shared.migration.MigrationJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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

	/**
	 * Migrates all the datasets from a study using a MigrationJob
	 * @throws ShanoirException
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_MIGRATION_PRECLINICAL_QUEUE)
	@Transactional
	public void migrate(String migrationJobAsString) throws AmqpRejectAndDontRequeueException {
		try {
			MigrationJob job = mapper.readValue(migrationJobAsString, MigrationJob.class);
			distantKeycloakConfigurationService.setRefreshToken(job.getRefreshToken());
			String keycloakURL = job.getShanoirUrl() + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
			distantKeycloakConfigurationService.setServer(job.getShanoirUrl());
			distantKeycloakConfigurationService.setAccessToken(job.getAccessToken());
			distantKeycloakConfigurationService.refreshToken(keycloakURL);

			LOG.error("receiving job " + job);

			this.migratePreclinical(job);

		} catch (Exception e) {
			LOG.error("Error while moving preclinical elements: ", e);
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
		prepareReferences();

		// Animal_subject
		for (Entry<Long, Long> entry : job.getSubjectsMap().entrySet()) {
			for (AnimalSubject animalSubject : animalSubjectRepository.findBy("subjectId", entry.getKey())) {
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
		examAnes.setId(null);
		migrateAnesthetic(examAnes.getAnesthetic(), job);
		migrateReference(examAnes.getDoseUnit(), job);
		examAnes.setExaminationId(job.getExaminationMap().get(examAnes.getExaminationId()));
		distantShanoir.createExaminationAnesthatic(examAnes, job.getExaminationMap().get(examAnes.getExaminationId()));
	}

	private Anesthetic migrateAnesthetic(Anesthetic anes, MigrationJob job) throws ShanoirException {
		if (anesthetics.get(anes.getName()) != null) {
			return anesthetics.get(anes.getName());
		}
		Set<AnestheticIngredient> oldIngredients = anes.getIngredients();
		anes.setId(null);
		Anesthetic cretedAnesthetic = distantShanoir.createAnesthetic(anes);
		for(AnestheticIngredient ingredient : oldIngredients) {
			migrateAnestheticIngredient(ingredient, cretedAnesthetic, job);
		}
		return cretedAnesthetic;
	}

	private AnestheticIngredient migrateAnestheticIngredient(AnestheticIngredient ingredient, Anesthetic anesth, MigrationJob job) throws ShanoirException {
		ingredient.setConcentrationUnit(migrateReference(ingredient.getConcentrationUnit(), job));
		ingredient.setId(null);
		ingredient.setAnesthetic(anesth);
		return distantShanoir.createIngredient(ingredient, anesth.getId());
	}

	private void migrateSubject(AnimalSubject animalSubject, MigrationJob job) throws ShanoirException {
		// Migrate all subject references
		animalSubject.setId(null);
		
		// Migrate Subject
		AnimalSubject createdSubject = distantShanoir.createAnimalSubject(animalSubject);

		// Subject_pathology
		for (SubjectPathology subjectPatho : subjectPathologyRepository.findByAnimalSubject(animalSubject)) {
			migrateSubjectPathology(subjectPatho, job, createdSubject);
		}

		// Subject_therapy
		for (SubjectTherapy subjecttherap: subjectTherapyRespository.findByAnimalSubject(animalSubject)) {
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
		if (therapies.get(therapy.getName()) != null) {
			return therapies.get(therapy.getName());
		}
		therapy.setId(null);
		return distantShanoir.createTherapy(therapy);
	}

	private PathologyModel migratePathologyModel(PathologyModel pathologyModel) throws ShanoirException {
		if (pathologyModels.get(pathologyModel.getName()) != null) {
			return pathologyModels.get(pathologyModel.getName());
		}
		pathologyModel.setId(null);
		return distantShanoir.createPathologyModel(pathologyModel);
	}

	private Pathology migratePathology(Pathology pathology) throws ShanoirException {
		if (pathologies.get(pathology.getName()) != null) {
			return pathologies.get(pathology.getName());
		}
		pathology.setId(null);
		return distantShanoir.createPathology(pathology);
	}

	private Reference migrateReference(Reference ref, MigrationJob job) throws ShanoirException {
		if (references.get(ref.getCategory() + ref.getReftype() + ref.getValue()) != null) {
			return references.get(ref.getCategory() + ref.getReftype() + ref.getValue());
		}
		ref.setId(null);
		return distantShanoir.createReference(ref);
	}
}
