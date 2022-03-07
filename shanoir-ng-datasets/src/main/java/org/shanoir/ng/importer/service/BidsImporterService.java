package org.shanoir.ng.importer.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.mysql.cj.xdevapi.JsonParser;

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
	@RabbitListener(queues = RabbitMQConfiguration.IMPORTER_BIDS_DATASET_QUEUE)
	@RabbitHandler
	@Transactional
	public void createAllBidsDatasetAcquisition(Message importJobStr) throws AmqpRejectAndDontRequeueException {
		ShanoirEvent event = null;
		try {
			SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
			Long userId = Long.valueOf("" + importJobStr.getMessageProperties().getHeaders().get("x-user-id"));
			ImportJob importJob = objectMapper.readValue(importJobStr.getBody(), ImportJob.class);
	
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
			default:
				if (event != null) {
					LOG.error("The data type folder is not recognized. Please update your BIDS archive following the rules.");

					event.setStatus(ShanoirEvent.ERROR);
					event.setMessage("The data type folder is not recognized. Please update your BIDS archive following the rules.");
					event.setProgress(1f);
					eventService.publishEvent(event);
				}
				break;
			}
		} catch (Exception e) {
			LOG.error("An unexpected exception occured during the import of a BIDS dataset.", e);
			if (event != null) {
				event.setStatus(ShanoirEvent.ERROR);
				event.setMessage("An unexpected error occured, please contact an administrator.");
				event.setProgress(1f);
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

		List<Dataset> datasets = new ArrayList<>();
		float progress = 0f;

		File[] filesToImport = new File(importJob.getWorkFolder()).listFiles();
		
		Long equipmentId = 0L;

		for (File importedFile : filesToImport) {
			progress += 1f / filesToImport.length;
			event.setMessage("Bids dataset for examination " + importJob.getExaminationId());
			event.setProgress(progress);
			eventService.publishEvent(event);

			// Metadata
			DatasetMetadata originMetadata = new DatasetMetadata();
			originMetadata.setDatasetModalityType(modalityType);
			originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);

			// Create the dataset with informations from job
			BidsDataset datasetToCreate = new BidsDataset();
			datasetToCreate.setSubjectId(examination.getSubjectId());

			// DatasetExpression with list of files
			DatasetExpression expression = new DatasetExpression();
			expression.setCreationDate(LocalDateTime.now());
			expression.setDatasetExpressionFormat(DatasetExpressionFormat.BIDS);
			expression.setDataset(datasetToCreate);

			List<DatasetFile> files = new ArrayList<>();

			// Copy the data to "BIDS" folder
			final String subLabel = SUBJECT_PREFIX + importJob.getSubjectName();
			final String sesLabel = SESSION_PREFIX + importJob.getExaminationId();

			final File outDir = new File(niftiStorageDir + File.separator + File.separator + subLabel + File.separator + sesLabel + File.separator + bidsDataType.getFolderName());
			outDir.mkdirs();
			String outPath = outDir.getAbsolutePath() + File.separator + importedFile.getName();
			Path importedFileFinalLocation = Files.copy(importedFile.toPath(), Paths.get(outPath), StandardCopyOption.REPLACE_EXISTING);

			DatasetFile dsFile = new DatasetFile();
			dsFile.setDatasetExpression(expression);
			dsFile.setPacs(false);
			dsFile.setPath(importedFileFinalLocation.toUri().toString().replaceAll(" ", "%20"));
			files.add(dsFile);
			if(equipmentId == 0L && importedFile.getName().endsWith(".json") && Files.size(Path.of(importedFile.getPath())) < 1000000) {
				// Check equipment in json file
				JSONParser json = new JSONParser(new FileReader(importedFile));
				LinkedHashMap jsonObject = (LinkedHashMap) json.parse();
				
				if (jsonObject.get("DeviceSerialNumber") != null) {
					String value = (String) jsonObject.get("DeviceSerialNumber");
					Long equipId = (Long) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_CODE_QUEUE, value);
					if (equipId != null) {
						equipmentId = equipId;
					}
				}
			}

			expression.setDatasetFiles(files);
			datasetToCreate.setDatasetExpressions(Collections.singletonList(expression));

			// Fill dataset with informations
			datasetToCreate.setCreationDate(LocalDate.now());
			datasetToCreate.setDatasetAcquisition(datasetAcquisition);
			datasetToCreate.setOriginMetadata(originMetadata);
			datasetToCreate.setUpdatedMetadata(originMetadata);
			datasetToCreate.setBidsDataType(bidsDataType.getFolderName());

			datasets.add(datasetToCreate);
		}

		datasetAcquisition.setDatasets(datasets);
		datasetAcquisition.setAcquisitionEquipmentId(equipmentId);
		datasetAcquisitionRepository.save(datasetAcquisition);
		
		event.setStatus(ShanoirEvent.SUCCESS);
		event.setMessage("(" + importJob.getStudyId() + ")"
				+": Successfully created datasets for subject " + importJob.getSubjectName()
				+ " in examination " + examination.getId());
		event.setProgress(1f);
		eventService.publishEvent(event);
	}
}
