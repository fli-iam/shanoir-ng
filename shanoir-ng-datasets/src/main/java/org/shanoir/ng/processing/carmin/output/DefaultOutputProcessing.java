package org.shanoir.ng.processing.carmin.output;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
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
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ShanoirException;
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
public class DefaultOutputProcessing extends OutputProcessing {

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

	private static final Logger LOG = LoggerFactory.getLogger(DefaultOutputProcessing.class);

	@Override
	public void manageTarGzResult(File in, File parent, CarminDatasetProcessing processing) {

		LOG.info("Processing result file [{}]...", in.getAbsolutePath());

		try (TarArchiveInputStream fin = new TarArchiveInputStream(
				new GzipCompressorInputStream(new FileInputStream(in)))) {
			TarArchiveEntry entry;

			File cacheFolder = new File(parent.getAbsolutePath() + File.separator + "cache");

			if (!cacheFolder.exists()) {
				cacheFolder.mkdirs();
			}

			List<File> outputFiles = new ArrayList<>();

			File resultJson = null;

			// first, find "result.json" file
			while ((entry = fin.getNextTarEntry()) != null) {

				String parsedEntry = entry.getName();

				if (entry.isDirectory()) {
					continue;
				}

				File currentFile = new File(cacheFolder, Paths.get(parsedEntry).getFileName().toString());
				IOUtils.copy(fin, Files.newOutputStream(currentFile.toPath()));

				if (parsedEntry.endsWith(this.resultFileName)) {
					resultJson = currentFile;
				} else {
					// For all other files that are not a result.json or a folder, create a processed dataset and a dataset processing
					outputFiles.add(currentFile);
					LOG.info("Output file [{}] found in archive.", parsedEntry);
				}
			}

			List<Dataset> inputDatasets = this.getInputDatasets(resultJson, in.getName());

			if(inputDatasets.isEmpty()) {
				throw new Exception("No input datasets found.");
			}

			if(outputFiles.isEmpty()){
				throw new Exception("No processable file found in Tar result.");
			}

			this.createProcessedDatasets(outputFiles, processing, inputDatasets);

			this.deleteCacheDir(Paths.get(cacheFolder.getAbsolutePath()));

		} catch (Exception e) {
			LOG.error("An error occured while extracting result from result archive.", e);
			importerService.createFailedJob(in.getPath());
		}
	}


	private List<Dataset> getInputDatasets(File resultJson, String tarName) throws IOException, JSONException {

		List<Long> datasetIds = new ArrayList<>();
		List<String> candidates = new ArrayList<>();

		candidates.add(tarName);

		if (resultJson == null) {
			LOG.info("No result JSON found in archive.");
		} else {
			LOG.info("Processing result JSON [{}]...", resultJson.getName());

			InputStream is = new FileInputStream(resultJson);
			JSONObject json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = json.get(key);
				if (value instanceof JSONArray) {
					// case "["id+XXX+filename.nii", "YYY+filename.nii", ...]"
					JSONArray array = (JSONArray) value;
					for (int i = 0; i < array.length(); i++) {
						candidates.add(array.getString(i));
					}
				} else {
					// Case "id+XXX+filename.nii"
					candidates.add((String) value);
				}
			}
		}

		for (String name : candidates) {
			Long id = this.getDatasetIdFromFilename(name);

			if (id != null && !datasetIds.contains(id)) {
				datasetIds.add(id);
			}
		}

		return datasetService.findByIdIn(datasetIds);
	}

	private Long getDatasetIdFromFilename(String name){
		// Ugly pattern to get dataset id
		// TODO: check that the "+" is mandatory. What if no ? What if not a file but a number ?
		Pattern p = Pattern.compile("id\\+(\\d+)\\+.*");
		Matcher m = p.matcher(name);
		// If there is not match, it's not a file parameter => Do not search dataset
		if (m.matches()) {
			return Long.valueOf(m.group(1));
		}
		return null;
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
			processedDataset.setProcessedDatasetName(getNameWithoutExtension(file.getName()));

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

	private void deleteCacheDir(Path directory) {
		try {
			Files.walk(directory)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
		} catch (IOException e) {
			LOG.error("I/O error while deleting cache dir [{}]", directory.toString());
		}
	}

	private String getNameWithoutExtension(String file) {
		int dotIndex = file.indexOf('.');
		return (dotIndex == -1) ? file : file.substring(0, dotIndex);
	}
}
