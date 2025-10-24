package org.shanoir.ng.importer.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tomcat.util.json.ParseException;
import org.json.JSONException;
import org.shanoir.ng.dataset.modality.BidsDataType;
import org.shanoir.ng.dataset.modality.BidsDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.bids.BidsDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BidsImporterService {

	private static final String SUBJECT_PREFIX = "sub-";

	private static final String SESSION_PREFIX = "ses-";

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ExaminationRepository examinationRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	@Value("${datasets-data}")
	private String niftiStorageDir;

	private static final Logger LOG = LoggerFactory.getLogger(BidsImporterService.class);

	/**
	 * Create BIDS dataset.
	 * @param importJob the import job
	 * @param userId the user id
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@RabbitListener(queues = RabbitMQConfiguration.IMPORTER_BIDS_DATASET_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	@Transactional
	public void createAllBidsDatasetAcquisition(Message importJobStr) throws AmqpRejectAndDontRequeueException {
		ShanoirEvent event = null;
		try {
			SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
			ImportJob importJob = objectMapper.readValue(importJobStr.getBody(), ImportJob.class);
			Long userId = importJob.getUserId();
			event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), userId, "Starting import...", ShanoirEvent.IN_PROGRESS, importJob.getStudyId());
			eventService.publishEvent(event);
			File workfolder = new File(importJob.getWorkFolder());

			// Check data type according to data type
			switch (workfolder.getName()) {
				case "anat":
					importDataset(importJob, BidsDataType.ANAT, DatasetModalityType.MR_DATASET, event);
					break;
				case "func":
					importDataset(importJob, BidsDataType.FUNC, DatasetModalityType.MR_DATASET, event);
					break;
				case "dwi":
					importDataset(importJob, BidsDataType.DWI, DatasetModalityType.MR_DATASET, event);
					break;
				case "fmap":
					importDataset(importJob, BidsDataType.FMAP, DatasetModalityType.MR_DATASET, event);
					break;
				case "perf":
					importDataset(importJob, BidsDataType.PERF, DatasetModalityType.MR_DATASET, event);
					break;
				case "meg":
					importDataset(importJob, BidsDataType.MEG, DatasetModalityType.MEG_DATASET, event);
					break;
				case "ieeg":
					importDataset(importJob, BidsDataType.IEEG, DatasetModalityType.IEEG_DATASET, event);
					break;
				case "eeg":
					importDataset(importJob, BidsDataType.EEG, DatasetModalityType.EEG_DATASET, event);
					break;
				case "ct":
					importDataset(importJob, BidsDataType.CT, DatasetModalityType.CT_DATASET, event);
					break;
				case "beh":
					importDataset(importJob, BidsDataType.BEH, DatasetModalityType.BEH_DATASET, event);
					break;
				case "pet":
					importDataset(importJob, BidsDataType.PET, DatasetModalityType.PET_DATASET, event);
					break;
				case "micr":
					importDataset(importJob, BidsDataType.MICR, DatasetModalityType.MICR_DATASET, event);
					break;
				case "nirs":
					importDataset(importJob, BidsDataType.NIRS, DatasetModalityType.NIRS_DATASET, event);
					break;
				case "xa":
					importDataset(importJob, BidsDataType.XA, DatasetModalityType.XA_DATASET, event);
					break;
				default:
					if (event != null) {
						String msg = "The data type folder is not recognized (given: " + workfolder.getName() + "). Please update your BIDS archive following the rules.";
						LOG.error(msg);
						event.setStatus(ShanoirEvent.ERROR);
						event.setMessage(msg);
						event.setProgress(-1f);
						eventService.publishEvent(event);
					}
					break;
			}
		} catch (Exception e) {
			LOG.error("An unexpected exception occured during the import of a BIDS dataset.", e);
			if (event != null) {
				event.setStatus(ShanoirEvent.ERROR);
				event.setMessage("An unexpected error occured, please contact an administrator.");
				event.setProgress(-1f);
				eventService.publishEvent(event);
			}

			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	/**
	 * Import some nifti datasets
	 * @param bidsDataType 
	 * @param modalityType 
	 * @param event 
	 * @param workfolder the work folder we are working in
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws JSONException 
	 */
	private void importDataset(ImportJob importJob, BidsDataType bidsDataType, DatasetModalityType modalityType, ShanoirEvent event) throws IOException, ParseException, JSONException {

		DatasetAcquisition datasetAcquisition = new BidsDatasetAcquisition();

		// Get examination
		Examination examination = examinationRepository.findById(importJob.getExaminationId()).get();
		datasetAcquisition.setExamination(examination);

		Set<Dataset> datasets = new HashSet<>();
		float progress = 0f;

		File[] filesToImport = new File(importJob.getWorkFolder()).listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String name) {
				return !name.startsWith(".DS_Store") && !name.startsWith("__MAC") && !name.startsWith("._") && !name.startsWith(".AppleDouble");
			}});
		
		Map<String, BidsDataset> datasetsByName = new HashMap<>();
		
		Map<String, Integer> equipments = objectMapper.readValue((String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.ACQUISITION_EQUIPMENT_CODE_QUEUE, "all"), Map.class);
		Long equipmentId = 0L;

		for (File importedFile : filesToImport) {
			progress += 1f / filesToImport.length;
			event.setMessage("Bids dataset for examination " + importJob.getExaminationId());
			event.setProgress(progress);
			eventService.publishEvent(event);

			String name = importedFile.getName().replaceAll("\\.", "_");

			// Parse name to get acquisition / session / run / task
			
			BidsDataset datasetToCreate;
			DatasetExpression expression;

			if (datasetsByName.get(name) != null) {
				datasetToCreate = datasetsByName.get(name);
				expression = datasetToCreate.getDatasetExpressions().get(0);
			} else {
				// Create the dataset with informations from job
				datasetToCreate = new BidsDataset();
				datasetToCreate.setSubjectId(examination.getSubject() != null ? examination.getSubject().getId() : null);
				datasetToCreate.setCreationDate(LocalDate.now());
				datasetToCreate.setDatasetAcquisition(datasetAcquisition);

				// Metadata
				DatasetMetadata originMetadata = new DatasetMetadata();
				originMetadata.setName(name);
				originMetadata.setDatasetModalityType(modalityType);
				originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);
				
				datasetToCreate.setOriginMetadata(originMetadata);
				datasetToCreate.setUpdatedMetadata(originMetadata);
				datasetToCreate.setBidsDataType(bidsDataType.getFolderName());
				
				// DatasetExpression with list of files
				expression = new DatasetExpression();
				expression.setCreationDate(LocalDateTime.now());
				expression.setDatasetExpressionFormat(DatasetExpressionFormat.BIDS);
				expression.setDataset(datasetToCreate);
				expression.setDatasetFiles(new ArrayList<>());
				datasetToCreate.setDatasetExpressions(Collections.singletonList(expression));
			}

			List<DatasetFile> files = expression.getDatasetFiles();

			// Copy the data to "BIDS" folder
			final String subLabel = SUBJECT_PREFIX + importJob.getSubjectName();
			final String sesLabel = SESSION_PREFIX + importJob.getExaminationId();

			final File outDir = new File(niftiStorageDir + File.separator + subLabel + File.separator + sesLabel + File.separator + bidsDataType.getFolderName());
			outDir.mkdirs();

			// remove old subject and session names from files names
			String filename = importedFile.getName()
					.replaceFirst("sub-[^_]+_", "")
					.replaceFirst("ses-[^_]+_", "");

			String outPath = outDir.getAbsolutePath() + File.separator + filename;
			Path importedFileFinalLocation = Files.copy(importedFile.toPath(), Paths.get(outPath), StandardCopyOption.REPLACE_EXISTING);

			DatasetFile dsFile = new DatasetFile();
			dsFile.setDatasetExpression(expression);
			dsFile.setPacs(false);
			dsFile.setPath(importedFileFinalLocation.toUri().toString().replaceAll(" ", "%20"));
			files.add(dsFile);
			if(equipmentId == 0L && importedFile.getName().endsWith(".json") && Files.size(Path.of(importedFile.getPath())) < 1000000) {
				// Check equipment in json file
				//JSONParser json = new JSONParser(new FileReader(importedFile));
				// LinkedHashMap jsonObject = (LinkedHashMap) json.parse();
				ObjectMapper jsonMapper = new ObjectMapper();
				// Parse JSON file into a LinkedHashMap
				LinkedHashMap<String, Object> jsonObject = jsonMapper.readValue(importedFile, LinkedHashMap.class);
				if (jsonObject.get("DeviceSerialNumber") != null) {
					String code = (String) jsonObject.get("DeviceSerialNumber");
					equipmentId = equipments.get(code) != null ? Long.valueOf(equipments.get(code)) : 0L;
				}
			}
			
			expression.setDatasetFiles(files);
			expression.setSize(Files.size(importedFileFinalLocation));
			datasets.add(datasetToCreate);
			datasetsByName.put(name, datasetToCreate);
		}

		datasetAcquisition.setDatasets(new ArrayList<>(datasets));
		datasetAcquisition.setAcquisitionEquipmentId(equipmentId);
		datasetAcquisition.setImportDate(LocalDateTime.now().toLocalDate());
		datasetAcquisition.setUsername(importJob.getUsername());


		datasetAcquisitionRepository.save(datasetAcquisition);
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_ACQUISITION_EVENT, datasetAcquisition.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, examination.getStudyId()));
		
		event.setStatus(ShanoirEvent.SUCCESS);

		event.setMessage("[" + importJob.getStudyName() + " (n°" + importJob.getStudyId() + ")]"
				+" Successfully created datasets for subject [" + importJob.getSubjectName()
				+ "] in examination [" + examination.getId() + "]");
		event.setProgress(1f);
		eventService.publishEvent(event);
	}

}