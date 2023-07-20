package org.shanoir.ng.processing.carmin.output;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
			List<Dataset> inputDatasets = new ArrayList<>();

			// first, find "result.json" file
			while ((entry = fin.getNextTarEntry()) != null) {

				String parsedEntry = entry.getName();
				LOG.info("Tar entry [{}]", parsedEntry);

				if (entry.isDirectory()) {
					continue;
				}

				File currentFile = new File(cacheFolder, Paths.get(parsedEntry).getFileName().toString());
				IOUtils.copy(fin, Files.newOutputStream(currentFile.toPath()));

				if (parsedEntry.endsWith(this.resultFileName)) {

					LOG.info("Processing result JSON [{}]...", parsedEntry);
					try {
						inputDatasets = this.getDatasetsFromResultJSON(currentFile);

						if(inputDatasets.isEmpty()){
							LOG.warn("No datasets found in result JSON [{}].", parsedEntry);
						}

					} catch (Exception e) {
						LOG.error("Could not read result JSON file", e);
					}

				} else {
					// For all other files that are not a result.json or a folder, create a processed dataset and a dataset processing
					outputFiles.add(currentFile);
				}
			}

			if(outputFiles.isEmpty()){
				LOG.warn("No processable file found in Tar result.");
			}

			this.createProcessedDatasets(outputFiles, processing, inputDatasets);

			this.deleteCacheDir(Paths.get(cacheFolder.getAbsolutePath()));

		} catch (Exception e) {
			LOG.error("An error occured while extracting result from tar.gz file: ", e);
		}
	}


	private List<Dataset> getDatasetsFromResultJSON(File resultJson) throws IOException, JSONException {

		InputStream is = new FileInputStream(resultJson);
		JSONObject json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

		List<Long> datasetIds = new ArrayList<>();

		List<String> filenames = new ArrayList<>();

		if(!json.has(JSON_INFILE)){
			LOG.error("No key [{}] found in [{}]", JSON_INFILE, resultJson.getAbsolutePath());
			return new ArrayList<>();
		}

		Object infiles = json.get(JSON_INFILE);

		if (infiles instanceof JSONArray) {
			// case "["id+XXX+filename.nii", "YYY+filename.nii"]"
			JSONArray array = (JSONArray) infiles;
			for (int i=0 ; i < array.length(); i++) {
				filenames.add(array.getString(i));
			}
		} else {
			// Case "id+XXX+filename.nii"
			String value = (String) infiles;
			filenames.add(value);
		}

		for (String name : filenames) {
			// Ugly pattern to get dataset id
			// TODO: check that the "+" is mandatory. What if no ? What if not a file but a number ?
			Pattern p = Pattern.compile("id\\+(\\d+)\\+.*");
			Matcher m = p.matcher(name);
			// If there is not match, it's not a file parameter => Do not search dataset
			if (m.matches()) {
				datasetIds.add(Long.valueOf(m.group(1)));
			}
		}

		return datasetService.findByIdIn(datasetIds);
	}

	/**
	 * Creates a list of processed dataset and a dataset processing associated to the list of files given in entry.
	 * @param processedFiles the list of files to treat as processed files
	 * @param carminDatasetProcessing The carmin dataset processing created before the execution
	 * @throws EntityNotFoundException 
	 * @throws IOException 
	 */
	private void createProcessedDatasets(List<File> processedFiles, CarminDatasetProcessing carminDatasetProcessing, List<Dataset> inputDatasets) throws EntityNotFoundException, IOException {

		List<Dataset> outputDatasets = new ArrayList<>();
		
		// Create dataset processing
		DatasetProcessing processing = new DatasetProcessing();
		processing.setComment(carminDatasetProcessing.getPipelineIdentifier());
		processing.setInputDatasets(inputDatasets);
		processing.setProcessingDate(carminDatasetProcessing.getProcessingDate());
		processing.setStudyId(carminDatasetProcessing.getStudyId());
		processing.setDatasetProcessingType(carminDatasetProcessing.getDatasetProcessingType());
		processing = datasetProcessingService.create(processing);

		Study study = studyRepository.findById(carminDatasetProcessing.getStudyId())
				.orElseThrow(() -> new NotFoundException("Study [" + carminDatasetProcessing.getStudyId() + "] not found."));

		for (File niiftiFile : processedFiles) {

			LOG.info("Processing [{}]...", niiftiFile.getAbsolutePath());

			ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();

			processedDataset.setDatasetProcessing(processing);

			processedDataset.setProcessedDatasetFilePath(niiftiFile.getAbsolutePath());
			processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
			processedDataset.setStudyId(carminDatasetProcessing.getStudyId());
			processedDataset.setStudyName(study.getName());

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

			processedDataset.setProcessedDatasetName(getNameWithoutExtension(niiftiFile.getName())); 
			importerService.createProcessedDataset(processedDataset);

			LOG.info("Processed dataset [{}] has been created from [{}].", processedDataset.getProcessedDatasetName(), niiftiFile.getAbsolutePath());

		}

		processing.setOutputDatasets(outputDatasets);
		datasetProcessingService.update(processing);

		// Remove datasets from current Carmin processing
		carminDatasetProcessing.setInputDatasets(Collections.emptyList());
		datasetProcessingService.update(carminDatasetProcessing);

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
