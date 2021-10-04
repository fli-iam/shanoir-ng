package org.shanoir.ng.exporter.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.dataset.DatasetDescription;
import org.shanoir.ng.dataset.controler.DatasetApiController.CoordinatesSystem;
import org.shanoir.ng.dataset.modality.EegDataSetDescription;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocolSCMetadata;
import org.shanoir.ng.datasetacquisition.model.mr.MrSequenceApplication;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is a service class for BIDS purpose.
 * Create BIDS files.
 * Export Data in BIDS format at different levels: Study, Subject, Examination, Dataset.
 * In a possible future: import Data from BIDS format
 * @author JcomeD
 *
 */
@Service
public class BIDSServiceImpl implements BIDSService {

	private static final String TABULATION = "\t";

	private static final String NEW_LINE = "\n";

	private static final String SCANS_FILE_EXTENSION = "_scans.tsv";

	private static final String SESSION_PREFIX = "ses-";

	private static final String SUBJECT_PREFIX = "sub-";

	private static final String STUDY_PREFIX = "stud-";

	private static final Logger LOG = LoggerFactory.getLogger(BIDSServiceImpl.class);

	private static final String TASK = "_task_";

	private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

	private static final String README_FILE = "README";

	private static final String NULL = "null";

	private static final String IMAGED_OBJECT_CATEGORY = "imaged_object_category";

	private static final String LANGUAGE_HEMISPHERIC_DOMINANCE = "language_hemispheric_dominance";

	private static final String MANUAL_HEMISPHERIC_DOMINANCE = "manual_hemispheric_dominance";

	private static final String BIRTH_DATE = "birth_date";

	private static final String SEX = "sex";

	private static final String SUBJECT_IDENTIFIER = "subject_identifier";

	private static final String PARTICIPANT_ID = "participant_id";

	private static final String CSV_SEPARATOR = "\t";

	private static final String CSV_SPLITTER = "\n";

	private static final String[] CSV_PARTICIPANTS_HEADER = {
			PARTICIPANT_ID,
			SUBJECT_IDENTIFIER
			//IMAGED_OBJECT_CATEGORY
			//LANGUAGE_HEMISPHERIC_DOMINANCE
			//LANGUAGE_HEMISPHERIC_DOMINANCE
			//BIRTH_DATE
			//SEX
	};

	@Value("${bids-data-folder}")
	private String bidsStorageDir;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ExaminationService examService;

	@Autowired
	private StudyRepository studyRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	/**
	 * Receives a shanoirEvent as a json object, concerning a study update => Update BIDS folder too
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = {
			@QueueBinding(
					key = ShanoirEventType.DELETE_EXAMINATION_EVENT,
					value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
					exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC)),
			@QueueBinding(
					key = ShanoirEventType.DELETE_DATASET_EVENT,
					value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
					exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC)),
			@QueueBinding(
					key = ShanoirEventType.UPDATE_DATASET_EVENT,
					value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
					exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC)),
			@QueueBinding(
					key = ShanoirEventType.UPDATE_EXAMINATION_EVENT,
					value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
					exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC)),
			@QueueBinding(
					key = ShanoirEventType.CREATE_EXAMINATION_EVENT,
					value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
					exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC)),
			@QueueBinding(
					key = ShanoirEventType.CREATE_DATASET_EVENT,
					value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
					exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
	}
			)
	public void deleteBids(String eventAsString) {
		ShanoirEvent event;
		try {
			event = objectMapper.readValue(eventAsString, ShanoirEvent.class);
			if (event.getStudyId() == null) {
				LOG.error("This event did not triggered a BIDs folder deletion {}", eventAsString);
				return;
			}
			Study studyDeleted = studyRepo.findById(event.getStudyId()).orElse(null);
			this.deleteBidsFolder(studyDeleted.getId(), studyDeleted.getName());
		} catch (Exception e) {
			LOG.error("ERROR when deleting BIDS folder: please delete it manually: {}", eventAsString, e);
		}
	}

	@Override
	public void deleteBidsFolder(Long studyId, String studyName) {
		try {
			// Try to delete the BIDS folder recursively if possible
			File bidsDir = new File(bidsStorageDir + File.separator + STUDY_PREFIX + studyId + '_' + studyName);
			if (bidsDir.exists()) {
				FileUtils.deleteDirectory(bidsDir);
			}
		} catch (Exception e) {
			LOG.error("ERROR when deleting BIDS folder: please delete it manually: {}, {}", studyId, studyName, e);
		}
	}

	/**
	 * Returns data from the study formatted as BIDS in a .zip file.
	 * @param study the study we want to export as BIDS
	 * @return data from the study formatted as BIDS in a .zip file.
	 * @throws IOException
	 */
	@Override
	public File exportAsBids(final Long studyId, final String studyName) throws IOException {
		// Get folder
		String tmpFilePath = bidsStorageDir + File.separator + STUDY_PREFIX + studyId + '_' + studyName;
		File workFolder = new File(tmpFilePath);
		if (workFolder.exists()) {
			// If the file already exists, just return it
			return workFolder;
		}

		// Otherwise, create it from scratch
		File baseDir = createBaseBidsFolder(workFolder, studyName);

		// Iterate over subjects got from call to SubjectApiController.findSubjectsByStudyId() and get list of subjects
		List<Subject> subjs = getSubjectsForStudy(studyId);

		// Create participants.tsv
		participantsSerializer(baseDir, subjs);

		for (Subject subj : subjs) {
			exportAsBids(subj, studyName, baseDir);
		}

		return baseDir;
	}

	/**
	 * Get a list of subject from the study ID.
	 * @param studyId the study ID to get the subject for
	 * @return a list of users associated to the study
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	private List<Subject> getSubjectsForStudy(final Long studyId) throws JsonParseException, JsonMappingException, IOException {

		/// Get the list of subjects
		String response = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.DATASET_SUBJECT_QUEUE, studyId);
		ObjectMapper objectMapper = new ObjectMapper();

		List<Subject> myObjects = objectMapper.readValue(response, new TypeReference<List<Subject>>(){});

		return myObjects;
	}

	/**
	 * Create the study/BASE BIDS folder.
	 * @param studyName the study name
	 * @param studyId the study id
	 * @return the base folder newly created
	 */
	private File createBaseBidsFolder(File workFolder, String studyName) {
		workFolder.mkdirs();

		// 2. Create dataset_description.json and README
		DatasetDescription datasetDescription = new DatasetDescription();
		datasetDescription.setName(studyName);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDescription);
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + README_FILE), studyName);
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

		return workFolder;
	}

	/**
	 * Create all the data in a BIDS folder for a given subject
	 * @param subject the subject we want to export as BIDS
	 * @param studyName the study name
	 * @param workDir Subject BIDS directory where we are working. Will be created if null.
	 * @return data from the subject formatted as BIDS in a .zip file.
	 * @throws IOException
	 */
	private void exportAsBids(final Subject subject, final String studyName, final File workDir) throws IOException {
		File subjDir = createSubjectFolder(subject.getName(), String.valueOf(subject.getId()), workDir);

		// Get subject examinations and filter on the one with adapted study only
		final List<Examination> examinationList = examService.findBySubjectId(subject.getId());

		// Iterate over examinations to export them as BIDS
		for (Examination exam : examinationList) {
			// OTHER: can we imagine a subject in multiple studies ? Do the filter here
			exportAsBids(exam, subjDir, studyName, subject.getName());
		}
	}

	/**
	 * Create the subject/patient BIDS folder
	 * @param subjectName the subject name for which we want to create the folder
	 * @param baseDir the parent folder
	 * @param subjectId the subject id
	 * @return the newly created folder
	 * @throws IOException
	 */
	private File createSubjectFolder(final String subjectName, final String subjectId, final File baseDir) throws IOException {
		File subjectFolder = new File(baseDir.getAbsolutePath() + File.separator + SUBJECT_PREFIX + subjectId + "_" + subjectName);
		if (!subjectFolder.exists()) {
			subjectFolder.mkdirs();
		}
		getScansFile(subjectFolder);
		return subjectFolder;
	}

	/**
	 * Returns data from the examination formatted as BIDS in a .zip file.
	 * @param examination the examination we want to export as BIDS
	 * @param subjDir examination BIDS directory where we are working.
	 * @param studyName the study name
	 * @param subjectName the subject name
	 * @return data from the examination formatted as BIDS in a .zip file.
	 * @throws IOException
	 */
	private void exportAsBids(final Examination examination, final File subjDir, final String studyName, final String subjectName) throws IOException {
		// Create session folder only if there is multiple dataset acquisitions
		File examDir;
		if (examination.getDatasetAcquisitions() != null && examination.getDatasetAcquisitions().size() > 1 ) {
			examDir = createExaminationFolder(examination, subjDir);
		} else {
			examDir = subjDir;
		}

		// Iterate over acquisitions/datasets
		for (DatasetAcquisition acq : examination.getDatasetAcquisitions()) {
			List<Dataset> datasets = acq.getDatasets();
			for (Dataset ds : datasets) {
				try {
					createDatasetBidsFiles(ds, examDir, studyName, subjectName);
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * Create the session/examination BIDS folder
	 * @param examination the examination for which we want to create the folder
	 * @param subjectDir the parent folder
	 * @return the newly created folder
	 */
	private File createExaminationFolder(final Examination examination, final File subjectDir) {
		String sessionLabel = examination.getComment() != null ? examination.getComment() : examination.getId().toString();
		File examFolder = new File(subjectDir.getAbsolutePath() + File.separator + SESSION_PREFIX +  sessionLabel);
		if (!examFolder.exists()) {
			examFolder.mkdirs();
		}
		return examFolder;
	}

	/**
	 * Create the list of BIDS files associated to a dataset.
	 * @param dataset the dataset from which we want the specific BIDS files to be created
	 * @param workDir the working directory where files will be created
	 * @param studyName the study name
	 * @param subjectName the subject name
	 * @return A list of newly created specific BIDS files associated to the dataset in entry
	 * @throws IOException when we fail to create a file
	 */
	private void createDatasetBidsFiles(final Dataset dataset, final File workDir, final String studyName, final String subjectName) throws IOException {
		File dataFolder = null;

		// Create specific files (EEG, MS, MEG, etc..)
		if (dataset instanceof EegDataset) {
			dataFolder = createDataFolder("eeg", workDir);
			String examComment = dataset.getDatasetAcquisition().getExamination().getComment().toString();
			String sessionLabel = examComment != null ? examComment : dataset.getDatasetAcquisition().getExamination().getId().toString();
			exportSpecificEegFiles((EegDataset) dataset, workDir, subjectName, sessionLabel, studyName, dataset.getId().toString());
		} else if (dataset instanceof MrDataset) {
			// Here we want to know whether we have anat/func/dwi/fmap
			// We base ourselves on SeriesDescription here
			MrProtocol protocol = ((MrDatasetAcquisition) dataset.getDatasetAcquisition()).getMrProtocol();
			if (protocol != null) {
				MrProtocolSCMetadata metadata = protocol.getUpdatedMetadata();
				if (metadata != null) {
					MrSequenceApplication application = metadata.getMrSequenceApplication();
					if (application != null) {
						// CALIBRATION(1), --> fieldmap
						if (application.equals(MrSequenceApplication.CALIBRATION)) {
							dataFolder = createDataFolder("fmap", workDir);
						}
						//MORPHOMETRY(2), ==> anat
						else if (application.equals(MrSequenceApplication.MORPHOMETRY)) {
							dataFolder = createDataFolder("anat", workDir);
						}
						// DIFFUSION(8), , ==> diffusion
						else if (application.equals(MrSequenceApplication.DIFFUSION)) {
							dataFolder = createDataFolder("dwi", workDir);
						}
						// BOLD(9), , ==> functional
						else if (application.equals(MrSequenceApplication.BOLD)) {
							dataFolder = createDataFolder("func", workDir);
						}
					}
				}
			}
			// default case, dataFolder is still null => undefined folder
			if (dataFolder == null) {
				dataFolder = createDataFolder("undefined", workDir);
			}
		} else {
			dataFolder = workDir;
		}
		// Copy dataset files in the directory AS hard link to avoid duplicating files
		List<URL> pathURLs = new ArrayList<>();
		getDatasetFilePathURLs(dataset, pathURLs, null);

		for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
			URL url =  iterator.next();
			File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));

			Path pathToGo = Paths.get(dataFolder.getAbsolutePath() + File.separator + srcFile.getName());
			try {
				// Use link to avoid file duplication
				Files.createLink(pathToGo, srcFile.toPath());

				// Add the file to the scans.tsv reference
				File scansTsvFile = getScansFile(workDir.getParentFile());
				StringBuilder buffer = new StringBuilder();
				buffer.append(pathToGo.getFileName()).append(TABULATION)
				.append(dataset.getDatasetAcquisition().getExamination().getExaminationDate()).append(TABULATION)
				.append(dataset.getDatasetAcquisition().getExamination().getId())
				.append(NEW_LINE);

				Files.write(Paths.get(scansTsvFile.getAbsolutePath()), buffer.toString().getBytes(), StandardOpenOption.APPEND);

			} catch (IOException exception) {
				LOG.error("File could not be treated: {}", srcFile.getAbsolutePath(), exception);
			}
		}
	}

	private File getScansFile(File parentFile) throws IOException {
		// What if we don't have subject name ?
		File scansFile = new File(parentFile.getAbsolutePath() + File.separator + parentFile.getName() + SCANS_FILE_EXTENSION);
		if (!scansFile.exists()) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("filename").append(TABULATION)
			.append("acq_time").append(TABULATION)
			.append("session_id")
			.append(NEW_LINE);
			Files.write(Paths.get(scansFile.getAbsolutePath()), buffer.toString().getBytes());
		}
		return scansFile;
	}

	/**
	 * Create /eeg, /func ,[...] folder in BIDS file if not existing.
	 * @return the folder newly created.
	 */
	private File createDataFolder(final String folderName, final File workDir) {
		File dataFolder = new File(workDir.getAbsolutePath() + File.separator + folderName);
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		return dataFolder;
	}

	/**
	 * Reads all dataset files depending on the format attached to one dataset.
	 * @param dataset
	 * @param pathURLs
	 * @throws MalformedURLException
	 */
	private void getDatasetFilePathURLs(final Dataset dataset, final List<URL> pathURLs, final DatasetExpressionFormat format) throws MalformedURLException {
		List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
		for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
			DatasetExpression datasetExpression = itExpressions.next();
			if (format == null || datasetExpression.getDatasetExpressionFormat().equals(format)) {
				List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
				for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
					DatasetFile datasetFile = itFiles.next();
					if (!datasetFile.isPacs()) {
						URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
						pathURLs.add(url);
					}
				}
			}
		}
	}

	/**
	 * This methods export specific EEG files for BIDS export.
	 * - channel.tsv -> A list of channels from dataset.channels
	 * - event.tsv -> A list of events from dataset.events
	 * - [..]_eeg.json -> Description of EEG methods used
	 * - ? electrodes.tsv -> list of electrodes positions if existing
	 * - ? coordsystem.json -> if electrodes are defined, sets the reference
	 * See https://bids-specification.readthedocs.io/en/latest/04-modality-specific-files/03-electroencephalography.html
	 * for more informations
	 * @param dataset the dataset we want to export in BIDS
	 * @param workFolder the examination work folder in which we are working
	 * @param pathURLs list of file URL
	 * @param studyName the name of associated study
	 * @param subjectName the subject name associated
	 * @param sessionId the session ID / examination ID associated
	 * @param runId The run ID
	 * @throws RestServiceException
	 * @throws IOException
	 */
	private void exportSpecificEegFiles(final EegDataset dataset, final File workFolder, final String subjectName, final String sessionId, final String studyName, final String runId) throws IOException {
		// Create _eeg.json
		String fileName = "task_" + studyName + "_eeg.json";
		File baseDirectory = workFolder.getParentFile().getParentFile();
		String destFile = baseDirectory.getAbsolutePath() + File.separator + fileName;

		EegDataSetDescription datasetDescription = new EegDataSetDescription();
		datasetDescription.setTaskName(studyName);
		datasetDescription.setSamplingFrequency(String.valueOf(dataset.getSamplingFrequency()));
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(new File(destFile), datasetDescription);

		// Create channels.tsv file
		String destWorkFolderPath = baseDirectory.getAbsolutePath() + File.separator + SUBJECT_PREFIX + dataset.getSubjectId() + "_" + subjectName + File.separator + SESSION_PREFIX + sessionId + File.separator + "eeg" + File.separator;

		// Create the folder where we are currently working if necessary.
		File destWorkFolderFile = new File(destWorkFolderPath);
		if (!destWorkFolderFile.exists()) {
			destWorkFolderFile.mkdirs();
		}

		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_channel.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		StringBuilder buffer = new StringBuilder();
		buffer.append("name \t type \t units \t sampling_frequency \t low_cutoff \t high_cutoff \t notch \n");

		for (Channel chan: dataset.getChannels()) {
			buffer.append(chan.getName()).append(TABULATION)
			.append(chan.getReferenceType().name()).append(TABULATION)
			.append(chan.getReferenceUnits()).append(TABULATION)
			.append(dataset.getSamplingFrequency()).append(TABULATION)
			.append(chan.getLowCutoff() == 0 ? "n/a" : chan.getLowCutoff()).append(TABULATION)
			.append(chan.getHighCutoff() == 0 ? "n/a" : chan.getHighCutoff()).append(TABULATION)
			.append(chan.getNotch() == 0 ? "n/a" : chan.getNotch()).append(NEW_LINE);
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());

		// Create events.tsv file
		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_event.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuilder();
		buffer.append("onset \t duration \t sample \n");

		for (Event event: dataset.getEvents()) {
			float sample = Float.parseFloat(event.getPosition());
			float samplingFrequency = dataset.getSamplingFrequency();
			float onset = sample / samplingFrequency;
			int duration = event.getPoints();
			buffer.append(onset).append(TABULATION)
			.append(duration == 0 ? "n/a" : String.valueOf(duration)).append(TABULATION)
			.append(sample).append(NEW_LINE);
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());

		// If no coordinates system, don't create electrode.csv & _coordsystem.json files
		if (dataset.getCoordinatesSystem() == null) {
			return;
		}

		// Create electrode.csv file
		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_electrodes.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuilder();
		buffer.append("name \t x \t y \t z \n");

		for (Channel chan: dataset.getChannels()) {
			buffer.append(chan.getName()).append(TABULATION)
			.append(chan.getX()).append(TABULATION)
			.append(chan.getY()).append(TABULATION)
			.append(chan.getZ()).append(NEW_LINE);
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());

		// Create _coordsystem.json file
		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_coordsystem.json";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuilder();
		buffer.append("{\n")
		.append("\"EEGCoordinateSystem\": ").append("\"" + dataset.getCoordinatesSystem()).append("\",\n")
		.append("\"EEGCoordinateUnits\": ").append("\"" +CoordinatesSystem.valueOf(dataset.getCoordinatesSystem()).getUnit()).append("\"\n")
		.append("}");

		Files.write(Paths.get(destFile), buffer.toString().getBytes());
	}

	/**
	 * Creates the participants.tsv and participants.json file from the study
	 */
	private void participantsSerializer(File parentFolder, List<Subject> subjs) {
		File csvFile = new File(parentFolder.getAbsolutePath() + File.separator + "participants.tsv");

		if (csvFile.exists()) {
			// Recreate it everytime
			FileUtils.deleteQuietly(csvFile);
		}
		StringBuilder buffer =  new StringBuilder();
		// Headers
		for (String columnHeader : CSV_PARTICIPANTS_HEADER) {
			buffer.append(columnHeader).append(CSV_SEPARATOR);
		}
		buffer.append(CSV_SPLITTER);

		for (Subject stubject : subjs) {

			// Write in the file the values
			buffer.append(stubject.getName()).append(CSV_SEPARATOR)
			.append(stubject.getId()).append(CSV_SEPARATOR)
			.append(CSV_SPLITTER);
		}

		try {
			Files.write(Paths.get(csvFile.getAbsolutePath()), buffer.toString().getBytes());
		} catch (IOException e) {
			LOG.error("Error while creating particpants.tsv file: {}", e);
		}
	}

}
