package org.shanoir.ng.importer.service;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ProcessedDatasetType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidsImporterService {

	@Autowired
	private ShanoirEventService eventService;
	
	/**
	 * Create BIDS dataset.
	 * @param importJob the import job
	 * @param userId the user id
	 */
	public void createAllBidsDatasetAcquisition(BidsImportJob importJob, Long userId) {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), userId, "Starting import...", ShanoirEvent.IN_PROGRESS, 0f);
		eventService.publishEvent(event);

		try {
			DatasetAcquisition datasetAcquisition = new BidsDatasetAcquisition();

			// Get examination
			Examination examination = examinationRepository.findOne(importJob.getExaminationId());

			datasetAcquisition.setExamination(examination);

			File[] niftisToImport = new File(importJob.getWorkFolder()).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".nii");
				}
			});

			List<Dataset> datasets = new ArrayList<>();
			float progress = 0f;

			for (File niftiFile : niftisToImport) {
				progress += 1f / niftisToImport.length;
				event.setMessage("Bids dataset for examination " + importJob.getExaminationId());
				event.setProgress(progress);
				eventService.publishEvent(event);

				// Metadata
				DatasetMetadata originMetadata = new DatasetMetadata();
				originMetadata.setProcessedDatasetType(ProcessedDatasetType.NONRECONSTRUCTEDDATASET);
				originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
				originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);

				// Create the dataset with informations from job
				BidsDataset datasetToCreate = new BidsDataset();
				datasetToCreate.setSubjectId(examination.getSubjectId());
				datasetToCreate.setModality(importJob.getModality());

				// DatasetExpression with list of files
				DatasetExpression expression = new DatasetExpression();
				expression.setCreationDate(LocalDateTime.now());
				expression.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
				expression.setDataset(datasetToCreate);

				List<DatasetFile> files = new ArrayList<>();

				// Copy the data to BIDS folder
				final String subLabel = SUBJECT_PREFIX + importJob.getSubjectName();
				final String sesLabel = SESSION_PREFIX + importJob.getExaminationId();

				final File outDir = new File(niftiStorageDir + File.separator + File.separator + subLabel + File.separator + sesLabel + File.separator + importJob.getModality());
				outDir.mkdirs();
				String outPath = outDir.getAbsolutePath() + File.separator + niftiFile.getName();
				Path niftiFinalLocation = Files.copy(niftiFile.toPath(), Paths.get(outPath), StandardCopyOption.REPLACE_EXISTING);

				DatasetFile dsFile = new DatasetFile();
				dsFile.setDatasetExpression(expression);
				dsFile.setPacs(false);
				dsFile.setPath(niftiFinalLocation.toUri().toString().replaceAll(" ", "%20"));
				files.add(dsFile);

				// Add corresponding json if existing
				File jsonFile = new File(niftiFile.getAbsolutePath().replace(".nii", ".json"));
				if (jsonFile.exists()) {
					String outPathJson = outDir.getAbsolutePath() + File.separator + jsonFile.getName();
					Path niftiFinalLocationJson = Files.copy(jsonFile.toPath(), Paths.get(outPathJson), StandardCopyOption.REPLACE_EXISTING);

					DatasetFile jsonFileDs = new DatasetFile();
					jsonFileDs.setDatasetExpression(expression);
					jsonFileDs.setPacs(false);
					jsonFileDs.setPath(niftiFinalLocationJson.toUri().toString().replaceAll(" ", "%20"));
					files.add(jsonFileDs);
				}

				expression.setDatasetFiles(files);
				datasetToCreate.setDatasetExpressions(Collections.singletonList(expression));

				// Fill dataset with informations
				datasetToCreate.setCreationDate(LocalDate.now());
				datasetToCreate.setDatasetAcquisition(datasetAcquisition);
				datasetToCreate.setOriginMetadata(originMetadata);

				datasets.add(datasetToCreate);
			}

			datasetAcquisition.setDatasets(datasets);
			datasetAcquisition.setAcquisitionEquipmentId(1L);
			datasetAcquisitionRepository.save(datasetAcquisition);

			event.setStatus(ShanoirEvent.SUCCESS);
			event.setMessage("Success");
			event.setProgress(1f);
			eventService.publishEvent(event);

			// Complete BIDS with data
			try {
				bidsService.addDataset(examination, importJob.getSubjectName(), importJob.getStudyName());
			} catch (Exception e) {
				LOG.error("Something went wrong creating the bids data: ", e);
			}
		} catch (Exception e) {
			LOG.error("Error while importing BIDS dataset: ", e);
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("An unexpected error occured, please contact an administrator.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			throw new AmqpRejectAndDontRequeueException(e);
		}

	}
}
