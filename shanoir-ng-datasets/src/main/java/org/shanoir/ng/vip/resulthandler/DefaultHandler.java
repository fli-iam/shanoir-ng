package org.shanoir.ng.vip.resulthandler;

import jakarta.ws.rs.NotFoundException;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.resource.ProcessingResourceService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DefaultHandler extends ResultHandler {

	@Value("${vip.result-file-name}")
	private String resultFileName;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private DatasetProcessingService datasetProcessingService;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private ProcessingResourceService processingResourceService;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultHandler.class);


	@Override
	public boolean canProcess(ExecutionMonitoring processing) {
		return true;
	}

	@Override
	public void manageTarGzResult(List<File> resultFiles, File parent, ExecutionMonitoring monitoring) throws ResultHandlerException {

		try {

			List<File> outputFiles = new ArrayList<>();
			File resultJson = null;

			for(File file : resultFiles){
				if (file.getAbsolutePath().endsWith("/" + this.resultFileName)) {
					resultJson = file;
				} else {
					// For all other files that are not a result.json or a folder, create a processed dataset and a dataset processing
					outputFiles.add(file);
					LOG.info("Output file [{}] found in archive.", file.getAbsolutePath());
				}
			}

			List<Dataset> inputDatasets = this.getInputDatasets(resultJson, parent.getName());

			if(inputDatasets.isEmpty()) {
				throw new ResultHandlerException("No input datasets found.", null);
			}

			if(outputFiles.isEmpty()){
				throw new ResultHandlerException("No processable file found in Tar result.", null);
			}

			this.createProcessedDatasets(outputFiles, monitoring, inputDatasets);

		} catch (Exception e) {
			importerService.createFailedJob(parent.getPath());
			throw new ResultHandlerException("An error occured while extracting result from result archive.", e);
		}
	}


	private List<Dataset> getInputDatasets(File resultJson, String tarName) throws IOException, JSONException {

		HashSet<Long> datasetIds = new HashSet<>();
		List<String> candidates = new ArrayList<>();

		candidates.add(tarName);

		if (resultJson == null) {
			LOG.info("No result JSON found in archive.");
		} else if (resultJson.length() == 0) {
			LOG.warn("Result JSON [{}] is present but empty.", resultJson.getAbsolutePath());
		} else {
			LOG.info("Processing result JSON [{}]...", resultJson.getName());

			JSONObject json;
			try (InputStream is = new FileInputStream(resultJson)) {
				json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
			}

			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONArray array = json.optJSONArray(key);
				if (array != null) {
					// case "["resource_id+XXX+filename.nii", "resource_id+YYY+filename.nii", ...]"
					for (int i = 0; i < array.length(); i++) {
						String value = array.optString(i);
						if(value != null){
							candidates.add(array.getString(i));
						}
					}
				} else {
					String value = json.optString(key);
					if(value != null){
						// Case "resource_id+XXX+filename.nii"
						candidates.add(value);
					}
				}
			}
		}

		for (String name : candidates) {
			datasetIds.addAll(this.getDatasetIdsFromFilename(name));
		}

		return datasetService.findByIdIn(new ArrayList<>(datasetIds));
	}

	public List<Long> getDatasetIdsFromFilename(String name){
		// "resource_id+[processing resource id]+whatever.nii"
		Matcher matcher = Pattern.compile("resource_id\\+(.+)\\+.*").matcher(name);
		if (matcher.matches()) {
			return this.processingResourceService.findDatasetIdsByResourceId(matcher.group(1));
		}

		return new ArrayList<>();
	}

	/**
	 * Creates a list of processed dataset and a dataset processing associated to the list of files given in entry.
	 * @param processedFiles the list of files to treat as processed files
	 * @param execution The execution monitoring
	 * @throws EntityNotFoundException 
	 * @throws IOException 
	 */
	private void createProcessedDatasets(List<File> processedFiles, ExecutionMonitoring execution, List<Dataset> inputDatasets) throws Exception {

		// Create dataset processing
		DatasetProcessing processing = this.createProcessing(execution, inputDatasets);

		for (File file : processedFiles) {

			LOG.info("Processing [{}]...", file.getAbsolutePath());

			ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
			processedDataset.setDatasetProcessing(processing);
			processedDataset.setProcessedDatasetFilePath(file.getAbsolutePath());
			processedDataset.setProcessedDatasetType(ProcessedDatasetType.EXECUTION_RESULT);
			String datasetName = file.getName();
			if (datasetName.contains("resource_id")) {
				datasetName = datasetName.substring(datasetName.lastIndexOf("+") + 1);
			}
			processedDataset.setProcessedDatasetName(datasetName);

			if(!inputDatasets.isEmpty()) {

				Long studyId = datasetService.getStudyId(inputDatasets.get(0));
				Study study = studyRepository.findById(studyId)
						.orElseThrow(() -> new NotFoundException("Study [" + studyId + "] not found."));

				processedDataset.setStudyId(studyId);
				processedDataset.setStudyName(study.getName());

				List<Long> subjectIds = inputDatasets.stream().map(Dataset::getSubjectId).toList();

				Predicate<Long> predicate = obj -> Objects.equals(inputDatasets.get(0).getSubjectId(), obj);

				if (subjectIds.stream().allMatch(predicate)) {
					Subject subject = subjectRepository.findById(inputDatasets.get(0).getSubjectId())
							.orElseThrow(() -> new NotFoundException("Subject [" + inputDatasets.get(0).getSubjectId() + "] not found"));

					processedDataset.setSubjectId(subject.getId());
					processedDataset.setSubjectName(subject.getName());
				}
			}
			processedDataset.setDatasetType(GenericDataset.datasetType);

			importerService.createProcessedDataset(processedDataset);

			LOG.info("Processed dataset [{}] has been created from [{}].", processedDataset.getProcessedDatasetName(), file.getAbsolutePath());

		}

		datasetProcessingService.update(processing);

	}

	private DatasetProcessing createProcessing(ExecutionMonitoring execution, List<Dataset> inputDatasets) {
		DatasetProcessing processing = new DatasetProcessing();
		processing.setParent(execution);
		processing.setComment(execution.getPipelineIdentifier());
		processing.setUsername(execution.getUsername());
		processing.setInputDatasets(inputDatasets);
		processing.setProcessingDate(execution.getProcessingDate());
		processing.setStudyId(execution.getStudyId());
		processing.setDatasetProcessingType(execution.getDatasetProcessingType());
		processing.setOutputDatasets(new ArrayList<>());
		processing = datasetProcessingService.create(processing);
		return processing;
	}

	private String getNameWithoutExtension(String file) {
		int dotIndex = file.indexOf('.');
		return (dotIndex == -1) ? file : file.substring(0, dotIndex);
	}
}
