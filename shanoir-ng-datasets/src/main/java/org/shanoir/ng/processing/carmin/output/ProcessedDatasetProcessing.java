package org.shanoir.ng.processing.carmin.output;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.ng.dataset.modality.MeshDataset;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.service.ProcessingResourceService;
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

@Service
public class ProcessedDatasetProcessing extends OutputProcessing {

	public static final String JSON_INFILE = "infile";
	@Value("${vip.result-file-name}")
	private String resultFileName;

	@Value("${vip.file-formats}")
	private String[] listOfNiftiExt;

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

	private static final Logger LOG = LoggerFactory.getLogger(ProcessedDatasetProcessing.class);


	@Override
	public boolean canProcess(CarminDatasetProcessing processing) {
		return true;
	}

	@Override
	public void manageTarGzResult(List<File> resultFiles, File parent, CarminDatasetProcessing processing) throws OutputProcessingException {

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
				throw new OutputProcessingException("No input datasets found.", null);
			}

			if(outputFiles.isEmpty()){
				throw new OutputProcessingException("No processable file found in Tar result.", null);
			}

			this.createProcessedDatasets(outputFiles, processing, inputDatasets);

		} catch (Exception e) {
			importerService.createFailedJob(parent.getPath());
			throw new OutputProcessingException("An error occured while extracting result from result archive.", e);
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
				Object value = json.get(key);
				if (value instanceof JSONArray) {
					// case "["resource_id+XXX+filename.nii", "resource_id+YYY+filename.nii", ...]"
					JSONArray array = (JSONArray) value;
					for (int i = 0; i < array.length(); i++) {
						candidates.add(array.getString(i));
					}
				} else if (value instanceof String){
					// Case "resource_id+XXX+filename.nii"
					candidates.add((String) value);
				}
			}
		}

		for (String name : candidates) {
			datasetIds.addAll(this.getDatasetIdsFromFilename(name));
		}

		return datasetService.findByIdIn(new ArrayList<>(datasetIds));
	}

	public HashSet<Long> getDatasetIdsFromFilename(String name){
		HashSet<Long> ids = new HashSet<>();
		// "resource_id+[processing resource id]+whatever.nii"
		Matcher matcher = Pattern.compile("resource_id\\+(.+)\\+.*").matcher(name);
		if (matcher.matches()) {
			ids.addAll(this.processingResourceService.findDatasetIdsByResourceId(matcher.group(1)));
		}

		return ids;
	}

	/**
	 * Creates a list of processed dataset and a dataset processing associated to the list of files given in entry.
	 * @param processedFiles the list of files to treat as processed files
	 * @param carminDatasetProcessing The carmin dataset processing created before the execution
	 * @throws EntityNotFoundException 
	 * @throws IOException 
	 */
	private void createProcessedDatasets(List<File> processedFiles, CarminDatasetProcessing carminDatasetProcessing, List<Dataset> inputDatasets) throws EntityNotFoundException, IOException {

		// Create dataset processing
		DatasetProcessing processing = this.createProcessing(carminDatasetProcessing, inputDatasets);

		Study study = studyRepository.findById(carminDatasetProcessing.getStudyId())
				.orElseThrow(() -> new NotFoundException("Study [" + carminDatasetProcessing.getStudyId() + "] not found."));

		for (File file : processedFiles) {

			LOG.info("Processing [{}]...", file.getAbsolutePath());

			ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
			processedDataset.setDatasetProcessing(processing);
			processedDataset.setProcessedDatasetFilePath(file.getAbsolutePath());
			processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
			processedDataset.setStudyId(carminDatasetProcessing.getStudyId());
			processedDataset.setStudyName(study.getName());
			processedDataset.setProcessedDatasetName(carminDatasetProcessing.getName());

			if(inputDatasets.size() != 0) {

				List<Long> subjectIds = inputDatasets.stream().map(Dataset::getSubjectId).collect(Collectors.toList());

				Predicate<Long> predicate = obj -> Objects.equals(inputDatasets.get(0).getSubjectId(), obj);

				if (subjectIds.stream().allMatch(predicate)) {
					Subject subject = subjectRepository.findById(inputDatasets.get(0).getSubjectId())
							.orElseThrow(() -> new NotFoundException("Subject [" + inputDatasets.get(0).getSubjectId() + "] not found"));

					processedDataset.setSubjectId(subject.getId());
					processedDataset.setSubjectName(subject.getName());
					processedDataset.setDatasetType(inputDatasets.get(0).getType());
				} else {
					processedDataset.setDatasetType(MeshDataset.datasetType);
				}
			} else {
				// default ?
				processedDataset.setDatasetType(MeshDataset.datasetType);
			}

			importerService.createProcessedDataset(processedDataset);

			LOG.info("Processed dataset [{}] has been created from [{}].", processedDataset.getProcessedDatasetName(), file.getAbsolutePath());

		}

		datasetProcessingService.update(processing);

		// Remove datasets from current Carmin processing
		carminDatasetProcessing.setInputDatasets(Collections.emptyList());
		datasetProcessingService.update(carminDatasetProcessing);

	}

	private DatasetProcessing createProcessing(CarminDatasetProcessing carminDatasetProcessing, List<Dataset> inputDatasets) {
		DatasetProcessing processing = new DatasetProcessing();
		processing.setComment(carminDatasetProcessing.getPipelineIdentifier());
		processing.setInputDatasets(inputDatasets);
		processing.setProcessingDate(carminDatasetProcessing.getProcessingDate());
		processing.setStudyId(carminDatasetProcessing.getStudyId());
		processing.setDatasetProcessingType(carminDatasetProcessing.getDatasetProcessingType());
		processing.setOutputDatasets(new ArrayList<>());
		processing = datasetProcessingService.create(processing);
		return processing;
	}

	private String getNameWithoutExtension(String file) {
		int dotIndex = file.indexOf('.');
		return (dotIndex == -1) ? file : file.substring(0, dotIndex);
	}
}
