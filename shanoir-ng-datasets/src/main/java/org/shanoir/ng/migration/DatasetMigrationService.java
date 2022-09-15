package org.shanoir.ng.migration;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
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
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.shanoir.ng.shared.migration.MigrationConstants;
import org.shanoir.ng.shared.migration.MigrationJob;
import org.shanoir.ng.shared.model.DiffusionGradient;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardAssignment;
import org.shanoir.ng.studycard.model.StudyCardCondition;
import org.shanoir.ng.studycard.model.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DatasetMigrationService {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetMigrationService.class);

	@Autowired
	ObjectMapper mapper;

	@Autowired
	ExaminationRepository examRepository;

	@Autowired
	ExaminationService examService;

	@Autowired
	SubjectRepository subjectRepository;

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

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ShanoirEventService eventService;
	
	@Value("${migration-folder}")
	private String migrationFolder;
	
	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;

	@Value("${dcm4chee-arc.dicom.wado.uri}")
	private String dicomWADOURI;
	
	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;
	
	@Value("${dcm4chee-arc.dicom.web}")
	private boolean dicomWeb;

	private ShanoirEvent event;

	/**
	 * Migrates all the datasets from a study using a MigrationJob
	 * @throws ShanoirException
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_MIGRATION_QUEUE)
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

			this.event = job.getEvent();
			publishEvent("Starting dataset migration", 0f);

			this.migrateStudy(job);

			// Re-set event as it was updated
			job.setEvent(event);
			job.setAccessToken(distantKeycloakConfigurationService.getAccessToken());
			job.setRefreshToken(distantKeycloakConfigurationService.getRefreshToken());

			rabbitTemplate.convertAndSend(RabbitMQConfiguration.STUDY_MIGRATION_PRECLINICAL_QUEUE, mapper.writeValueAsString(job));

		} catch (Exception e) {
			event.setStatus(ShanoirEvent.ERROR);
			publishEvent("An error occured while migrating datasets, please contact an administrator.", 1f);
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
			publishEvent("Migrating study card : " + oldId , 0f);
			sc = moveStudyCard(sc, job);
			studyCardsMap.put(oldId, sc.getId());
		}
		job.setStudyCardsMap(studyCardsMap);

		// Migrate all examinations
		List<Examination> examinations = examRepository.findByStudyId(job.getOldStudyId());
		Map<Long, Long> examMap = new HashMap<>();

		for (Examination exam : examinations) {
			Long oldId = exam.getId();
			publishEvent("Migrating Examination: " + oldId , event.getProgress() +  1 / examinations.size());

			Examination createdExam = migrateExamination(exam, job);
			examMap.put(oldId, createdExam.getId());
		}
		job.setExaminationMap(examMap);
	}

	private StudyCard moveStudyCard(StudyCard sc, MigrationJob job) throws ShanoirException {
		LOG.error("StudyCard " + job);

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
	 * @return
	 * @throws ShanoirException
	 */
	private Examination migrateExamination(Examination exam, MigrationJob job) throws ShanoirException {
		job.setExaminationMap(new HashMap<>());

		List<String> extraDataFilePath = exam.getExtraDataFilePathList();

		long oldId = exam.getId();
		exam.setId(null);
		exam.setCenterId(job.getCentersMap().get(exam.getCenterId()));
		IdName subject = job.getSubjectsMap().get(exam.getSubject().getId());
		exam.setSubject(new Subject(subject.getId(), subject.getName()));
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
		LOG.error("Exam " + exam);
		Examination createdExam = distantShanoir.createExamination(exam);
		entityManager.detach(exam);

		job.getExaminationMap().put(oldId, createdExam.getId());

		event.setMessage("Migrating acquisitions...");
		eventService.publishEvent(event);
		// Migrate datasetAcquisition
		float prog = event.getProgress() / job.getExaminationMap().size();
		int i = 1;
		for(DatasetAcquisition acq : dsAcq) {
			publishEvent("Migrating acquisition : " + acq.getId(), event.getProgress() + prog * i++ / dsAcq.size());
			migrateAcquisition(acq, createdExam.getId(), oldId, job);
		}

		for (String fileName : extraDataFilePath) {
			String filePath = examService.getExtraDataFilePath(oldId, fileName);
			distantShanoir.addExminationExtraData(new File(filePath), createdExam.getId());
		}
		return createdExam;
	}

	/**
	 * Migrates a dataset acquisition
	 * @param acq
	 * @param examId
	 * @param job
	 * @throws ShanoirException
	 */
	private void migrateAcquisition(DatasetAcquisition acq, Long examId, Long oldExamId, MigrationJob job) throws ShanoirException {
		acq.setId(null);
		if (acq.getAcquisitionEquipmentId() != null) {
			acq.setAcquisitionEquipmentId(job.getEquipmentMap().get(acq.getAcquisitionEquipmentId()));
		}
		Examination examDto = new Examination();
		examDto.setId(examId);
		examDto.setStudyId(job.getStudy().getId());
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

		//  Migrate acquisition
		LOG.error("Acq " + acq);
		DatasetAcquisition newacq = distantShanoir.createAcquisition(acq);
		entityManager.detach(acq);

		// Update datasets
		for (Dataset ds : datasets) {
			publishEvent("Migrating dataset : " + ds.getName() + " of examination " + examId, event.getProgress());
			migrateDataset(ds, newacq, oldExamId, job);
		}
	}

	private Dataset migrateDataset(Dataset ds, DatasetAcquisition acq, Long oldExamId, MigrationJob job) throws ShanoirException {
		ds.setId(null);
		ds.setSubjectId(job.getSubjectsMap().get(ds.getSubjectId()).getId());
		DatasetAcquisition acqDTO = new MrDatasetAcquisition();
		acqDTO.setId(acq.getId());
		Examination examDTO = new Examination();
		examDTO.setStudyId(job.getStudy().getId());
		acqDTO.setExamination(examDTO);
		ds.setDatasetAcquisition(acqDTO);

		if ("Mr".equals(ds.getType())) {
			MrDataset mrDs = (MrDataset) ds;
			if (!CollectionUtils.isEmpty(mrDs.getFlipAngle())) {
				for (DiffusionGradient element : mrDs.getDiffusionGradients()) {
					element.setId(null);
					element.setMrDataset(mrDs);
				}
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
					element.setMrDataset(mrDs);
				}
			}
			if (!CollectionUtils.isEmpty(mrDs.getEchoTime())) {
				for (EchoTime element : mrDs.getEchoTime()) {
					element.setId(null);
					element.setMrDataset(mrDs);
				}
			}
			if (!CollectionUtils.isEmpty(mrDs.getInversionTime())) {
				for (InversionTime element : mrDs.getInversionTime()) {
					element.setId(null);
					element.setMrDataset(mrDs);
				}
			}
			if (!CollectionUtils.isEmpty(mrDs.getRepetitionTime())) {
				for (RepetitionTime element : mrDs.getRepetitionTime()) {
					element.setId(null);
					element.setMrDataset(mrDs);
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

		List<DatasetExpression> expressions = ds.getDatasetExpressions();
		ds.setDatasetExpressions(null);

		Dataset createdDataset = distantShanoir.createDataset(ds);
		ds.getDatasetAcquisition().setExamination(null);
		entityManager.detach(ds);

		for (DatasetExpression expression : expressions) {
			migrateDatasetExpression(expression, createdDataset, oldExamId, job);
		}
		return null;
	}

	private void migrateDatasetExpression(DatasetExpression expression, Dataset createdDataset,  Long oldExamId, MigrationJob job) throws ShanoirException {
		List<DatasetFile> oldFiles = expression.getDatasetFiles();

		expression.setId(null);
		Dataset datasetDTO = new MrDataset();
		datasetDTO.setId(createdDataset.getId());
		expression.setDataset(datasetDTO);
		expression.setDatasetFiles(null);

		DatasetExpression createdExpression = distantShanoir.createDatasetExpression(expression);
		entityManager.detach(expression);
		DatasetFile createdFile = null;
		for (DatasetFile file : oldFiles) {
			createdFile = migrateDatasetFile(file, createdExpression, createdDataset, oldExamId, job);
		}
		// Once all files of the expression were loaded, add them to the pacs if necessary
		if (DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat())) {
			distantShanoir.moveDatasetFiles(createdFile.getId());
		}
	}

	private DatasetFile migrateDatasetFile(DatasetFile file, DatasetExpression createdExpression, Dataset createdDataset, Long oldExamId, MigrationJob job) throws ShanoirException {
		File workFolder = new File(migrationFolder, "/Migration_" + LocalDateTime.now());
		DatasetFile createdFile;
		try {
			workFolder.mkdirs();

			LOG.error("Loading file..." + file.getPath());
			String result = null;
			if (DatasetExpressionFormat.DICOM.equals(file.getDatasetExpression().getDatasetExpressionFormat())) {
				// Dicom
				result = downloader.downloadDicomFilesForURL(file.getPath(), workFolder, "", createdDataset, 0);
			} else {
				// Nifti
				URL url = new URL(file.getPath().replaceAll("%20", " "));
				File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));
				result = srcFile.getAbsolutePath();
			}

			file.setId(null);
			DatasetExpression expressionDTO = new DatasetExpression();
			expressionDTO.setId(createdExpression.getId());
			file.setDatasetExpression(expressionDTO);

			// Update path for the new  server (replace usual paths)
			if (!file.isPacs()) {
				file.setPath(file.getPath().replace("study-" + job.getOldStudyId() , "study-" + job.getStudy().getId()));
				file.setPath(file.getPath().replace("examination" + oldExamId , "examination" + job.getExaminationMap().get(oldExamId)));
				file.setPath(file.getPath().replace(
						"-" + createdDataset.getSubjectId()							  + "/ses-" + oldExamId ,
						"-" + job.getSubjectsMap().get(createdDataset.getSubjectId()) + "/ses-" + job.getExaminationMap().get(oldExamId)));
				file.setPath(file.getPath().replace(
						"/ses-" + oldExamId ,
						"/ses-" + job.getExaminationMap().get(oldExamId)));
			} else {
				// Change PACS url with constants
				String path = file.getPath();
				path = path.replace(dcm4cheeProtocol, MigrationConstants.DCM4CHEE_PROTOCOL_CONSTANT);
				path = path.replace(dcm4cheeHost, MigrationConstants.DCM4CHEE_HOST_CONSTANT);
				path = path.replace(dcm4cheePortWeb, MigrationConstants.DCM4CHEE_PORT_CONSTANT);
				if (dicomWeb) {
					path = path.replace(dicomWADOURI, MigrationConstants.DCM4CHEE_WADO_URI_CONSTANT);
				} else {
					path = path.replace(dicomWebRS, MigrationConstants.DCM4CHEE_WEB_RS_CONSTANT);
				}
				file.setPath(path);
			}

			// Move file
			entityManager.detach(file);
			createdFile = distantShanoir.createDatasetFile(file);
			distantShanoir.moveDatasetFile(createdFile, new File(result));

		} catch (Exception e) {
			throw new ShanoirException("Error while creating the new dataset file", e);
		} finally {
			FileUtils.deleteQuietly(workFolder);
		}
		return createdFile;
	}

	private void publishEvent(String message, float progress) {
		event.setMessage(message);
		event.setProgress(progress);
		eventService.publishEvent(event);
	}
}
