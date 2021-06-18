package org.shanoir.ng.migration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.shanoir.ng.shared.migration.MigrationJob;
import org.shanoir.ng.shared.model.DiffusionGradient;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardAssignment;
import org.shanoir.ng.studycard.model.StudyCardCondition;
import org.shanoir.ng.studycard.model.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DatasetMigrationService {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetMigrationService.class);

	@Autowired
	ObjectMapper mapper;

	@Autowired
	ExaminationRepository examRepository;

	@Autowired
	DistantDatasetShanoirService distantShanoir;

	@Autowired
	private WADODownloaderService downloader;

	@Autowired
	private StudyCardRepository studyCardService;

	@Autowired
	private DistantKeycloakConfigurationService distantKeycloakConfigurationService;

	@Autowired
	EntityManager entityManager;

	private HashMap<String, Long> oldIdMap = new HashMap<>();

	private HashMap<Long, File> fileMap = new HashMap<>();

	/**
	 * Migrates all the datasets from a study using a MigrationJob
	 * @throws ShanoirException
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_MIGRATION_QUEUE)
	@Transactional
	public void migrate(String migrationJobAsString) throws AmqpRejectAndDontRequeueException {
		try {
			MigrationJob job = mapper.readValue(migrationJobAsString, MigrationJob.class);
			distantKeycloakConfigurationService.setRefreshToken(job.getRefreshToken());
			String keycloakURL = job.getShanoirUrl() + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
			distantKeycloakConfigurationService.setServer(job.getShanoirUrl());
			distantKeycloakConfigurationService.setAccessToken(job.getAccessToken());
			distantKeycloakConfigurationService.refreshToken(keycloakURL);

			this.migrateStudy(job);
		} catch (Exception e) {
			LOG.error("Error while moving datasets: ", e);
			throw new AmqpRejectAndDontRequeueException(e);
		} finally {
			// Stop token refresh
			distantKeycloakConfigurationService.stop();
		}
	}

	private void migrateStudy(MigrationJob job) throws ShanoirException {
		// Migrate all studyCards
		List<StudyCard> studyCards = studyCardService.findByStudyId(job.getOldStudyId());
		Map<Long, Long> studyCardsMap = new HashMap<>();
		for (StudyCard sc : studyCards) {
			long oldId = sc.getId();
			sc = moveStudyCard(sc, job);
			studyCardsMap.put(oldId, sc.getId());
		}
		job.setStudyCardsMap(studyCardsMap);

		// Migrate all examinations
		List<Examination> examinations = examRepository.findByStudyId(job.getOldStudyId());

		for (Examination exam : examinations) {
			migrateExamination(exam, job);
		}
	}

	private StudyCard moveStudyCard(StudyCard sc, MigrationJob job) throws ShanoirException {
		sc.setId(null);
		sc.setStudyId(job.getStudy().getId());
		if (sc.getAcquisitionEquipmentId() != null) {
			sc.setAcquisitionEquipmentId(job.getEquipmentMap().get(sc.getAcquisitionEquipmentId()));
		}
		for (StudyCardRule oldRule : sc.getRules()) {
			oldRule.setId(null);
			for (StudyCardAssignment assignment : oldRule.getAssignments()) {
				assignment.setId(null);
			}
			for (StudyCardCondition condition : oldRule.getConditions()) {
				condition.setId(null);
			}
		}
		entityManager.detach(sc);
		sc = distantShanoir.createStudyCard(sc);
		return sc;
	}

	/**
	 * Migrates an exam
	 * @param exam
	 * @param job
	 * @throws ShanoirException
	 */
	private Examination migrateExamination(Examination exam, MigrationJob job) throws ShanoirException {
		exam.setId(null);
		exam.setCenterId(job.getCentersMap().get(exam.getCenterId()));
		exam.setSubjectId(job.getSubjectsMap().get(exam.getSubjectId()));
		exam.setStudyId(job.getStudy().getId());

		// We just remove these elements that are not pertinent
		exam.setInstrumentBasedAssessmentList(null);
		exam.setTimepointId(null);
		exam.setInvestigatorId(null);
		exam.setInvestigatorCenterId(null);

		List<DatasetAcquisition> dsAcq = exam.getDatasetAcquisitions();
		// Reset this (relationship is hold by dataset acquisition)
		exam.setDatasetAcquisitions(null);

		// Move examination
		exam = distantShanoir.createExamination(exam);

		// Migrate datasetAcquisition
		for(DatasetAcquisition acq : dsAcq) {
			migrateAcquisition(acq, exam.getId(), job);
		}

		// TODO: Move examination extra-data

		return exam;
	}

	/**
	 * Migrates a dataset acquisition
	 * @param acq
	 * @param examId
	 * @param job
	 * @throws ShanoirException
	 */
	private void migrateAcquisition(DatasetAcquisition acq, Long examId, MigrationJob job) throws ShanoirException {
		acq.setId(null);
		if (acq.getAcquisitionEquipmentId() != null) {
			acq.setAcquisitionEquipmentId(job.getEquipmentMap().get(acq.getAcquisitionEquipmentId()));
		}
		Examination examDto = new Examination();
		examDto.setId(examId);
		acq.setExamination(examDto);
		if (acq.getStudyCard() != null) {
			StudyCard scDto = new StudyCard();
			scDto.setId(job.getStudyCardsMap().get(acq.getStudyCard().getId()));
			acq.setStudyCard(scDto);
		}
		List<Dataset> datasets = acq.getDatasets();
		acq.setDatasets(null);

		switch (acq.getType()) {
		case "Mr":
			MrDatasetAcquisition mrAcq = (MrDatasetAcquisition) acq;
			if (mrAcq.getMrProtocol() != null) {
				mrAcq.getMrProtocol().setId(null);
				if (mrAcq.getMrProtocol().getOriginMetadata() != null) {
					mrAcq.getMrProtocol().getOriginMetadata().setId(null);
				}
				if (mrAcq.getMrProtocol().getUpdatedMetadata() != null) {
					mrAcq.getMrProtocol().getUpdatedMetadata().setId(null);
				}
			}
			break;
		case "Ct":
			CtDatasetAcquisition ctAcq = (CtDatasetAcquisition) acq;
			if (ctAcq.getCtProtocol() != null) {
				ctAcq.getCtProtocol().setId(null);
			}
			break;
		case "Pet":
			PetDatasetAcquisition petAcq = (PetDatasetAcquisition) acq;
			if (petAcq.getPetProtocol() != null) {
				petAcq.getPetProtocol().setId(null);
			}
			break;
		default:
			// Do nothing for others, no specific objets to migrate
			break;
		}

		// Update datasets
		for (Dataset ds : datasets) {
			migrateDataset(ds, acq, job);
		}

		//  Migrate acquisition
		distantShanoir.createAcquisition(acq);

		// Migrate files
		for (Dataset ds : acq.getDatasets()) {
			for (DatasetExpression exp : ds.getDatasetExpressions()) {
				for (DatasetFile dsFile : exp.getDatasetFiles()) {
					Long oldFileId = oldIdMap.get(dsFile.getPath());
					File fileToMove = fileMap.get(oldFileId);

					distantShanoir.moveDatasetFile(dsFile, fileToMove);
				}
			}
		}
		oldIdMap = new HashMap<>();
		fileMap = new HashMap<>();
	}

	private void migrateDataset(Dataset ds, DatasetAcquisition acq, MigrationJob job) throws ShanoirException {
		ds.setId(null);
		ds.setDatasetAcquisition(null);

		if ("Mr".equals(ds.getType())) {
			MrDataset mrDs = (MrDataset) ds;
			if (mrDs.getFlipAngle() != null) {
				mrDs.getFlipAngle().get(0).setId(null);
			}
			if (mrDs.getDatasetProcessing() != null) {
				mrDs.getDatasetProcessing().setId(null);
			}
			if (mrDs.getOriginMrMetadata() != null) {
				mrDs.getOriginMrMetadata().setId(null);
			}
			if (mrDs.getUpdatedMrMetadata() != null) {
				mrDs.getUpdatedMrMetadata().setId(null);
			}
			if (!CollectionUtils.isEmpty(mrDs.getDiffusionGradients())) {
				for (DiffusionGradient element : mrDs.getDiffusionGradients()) {
					element.setId(null);
				}
			}
			if (!CollectionUtils.isEmpty(mrDs.getEchoTime())) {
				for (EchoTime element : mrDs.getEchoTime()) {
					element.setId(null);
				}
			}
			if (!CollectionUtils.isEmpty(mrDs.getInversionTime())) {
				for (InversionTime element : mrDs.getInversionTime()) {
					element.setId(null);
				}
			}
			if (!CollectionUtils.isEmpty(mrDs.getRepetitionTime())) {
				for (RepetitionTime element : mrDs.getRepetitionTime()) {
					element.setId(null);
				}
			}
		}
		if (ds.getOriginMetadata() != null) {
			ds.getOriginMetadata().setId(null);
		}
		if (ds.getUpdatedMetadata() != null) {
			ds.getUpdatedMetadata().setId(null);
		}
		ds.getDatasetProcessing();
		ds.getReferencedDatasetForSuperimposition();

		try {
			for (DatasetExpression expression : ds.getDatasetExpressions()) {
				expression.setId(null);
				for (DatasetFile file : expression.getDatasetFiles()) {

					String result = null;
					File workFolder = new File("/tmp/Migration_" + LocalDateTime.now());
					workFolder.mkdirs();
					if (DatasetExpressionFormat.DICOM.equals(file.getDatasetExpression().getDatasetExpressionFormat())) {
						// Dicom
						result = downloader.downloadDicomFilesForURL(file.getPath(), workFolder, "");
					} else if (DatasetExpressionFormat.NIFTI_SINGLE_FILE.equals(file.getDatasetExpression().getDatasetExpressionFormat())){
						// Nifti
						result = file.getPath().replaceAll("%20", " ");
					} else if (DatasetExpressionFormat.EEG.equals(file.getDatasetExpression().getDatasetExpressionFormat())) {
						// EEG
						result = file.getPath().replaceAll("%20", " ");
					}
					Long oldId = file.getId();
					// Move files
					file.setId(null);

					// Update path
					file.setPath(file.getPath().replace("study-" + job.getOldStudyId() , "study-" + job.getStudy().getId()));

					oldIdMap.put(file.getPath(), oldId);
					fileMap.put(oldId, new File(result));
				}
			}
		} catch (Exception e) {
			throw new ShanoirException("Error while copying datasets", e);
		}
	}
}
