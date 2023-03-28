package org.shanoir.ng.processing.carmin.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.json.JSONObject;
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
				LOG.info("Tar entry :" + parsedEntry);

				if (entry.isDirectory()) {
					continue;
				}

				if (parsedEntry.endsWith(this.resultFileName)) {
					// We have the result => Read the file to get the parent datasets
					/*
						 {
							"infile" : ["dateset-id+filename.nii"]
						}
					 */
					try {
						BufferedReader br = null;
						StringBuilder sb = new StringBuilder();
						br = new BufferedReader(new InputStreamReader(fin));
						String line;
						while ((line = br.readLine()) != null) {
							sb.append(line);
						}
						JSONObject obj = new JSONObject(sb.toString());

						// We need to parse file parameters here.

						// Can be "["XXX+filename.nii", "YYY+filename.nii"]"
						String datasetIdsValue = (String) obj.get("infile");
						String[] values = datasetIdsValue.split(",");
						List<Long> datasetIds = new ArrayList<>();
						for (String value : values) {
							LOG.error(value);
							Pattern p = Pattern.compile("\\[?\\\"(\\d+)\\+.*");
							Matcher m = p.matcher(datasetIdsValue);
							if (m.matches()) {
								datasetIds.add(Long.valueOf(m.group(1)));
							}
						}
						// get datasets
						inputDatasets = datasetService.findByIdIn(datasetIds);
						LOG.error("datasets" + datasetIds);

					} catch (Exception e) {
						LOG.error("Could not read JSON file", e);
					}
				} else {

					if (parsedEntry.contains("/")) {
						parsedEntry = parsedEntry.substring(parsedEntry.lastIndexOf("/") + 1);
					}

					File currentFile = new File(cacheFolder, parsedEntry);
					File parentOfCurrent = currentFile.getParentFile();

					if (!parent.exists()) {
						parent.mkdirs();
					}

					IOUtils.copy(fin, Files.newOutputStream(currentFile.toPath()));

					// For all other files that are not a result.json or a folder, create a processed dataset and a dataset processing
					outputFiles.add(currentFile);
				}
			}
			this.createProcessedDatasets(outputFiles, cacheFolder.getAbsolutePath(), processing, inputDatasets);
		} catch (Exception e) {
			LOG.error("An error occured while extracting result from tar.gz file: ", e);
		}
	}

	/**
	 * Creates a list of processed dataset and a dataset processing associated to the list of files given in entry.
	 * @param processedFiles the list of files to treat as processed files
	 * @param destDir the destinatino directory where to create these processed datasets
	 * @param carminDatasetProcessing The carmin dataset processing created before the execution
	 * @throws EntityNotFoundException 
	 */
	private void createProcessedDatasets(List<File> processedFiles, String destDir, CarminDatasetProcessing carminDatasetProcessing, List<Dataset> inputDatasets) throws EntityNotFoundException {

		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists()) {
			dir.mkdirs();
		}

		List<Dataset> outputDatasets = new ArrayList<>();

		Study study = studyRepository.findById(carminDatasetProcessing.getStudyId())
				.orElseThrow(() -> new NotFoundException("study not found"));

		for (File niiftiFile : processedFiles) {
			ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();

			processedDataset.setDatasetProcessing(carminDatasetProcessing);

			processedDataset.setProcessedDatasetFilePath(niiftiFile.getAbsolutePath());
			processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
			processedDataset.setStudyId(carminDatasetProcessing.getStudyId());
			processedDataset.setStudyName(study.getName());

			if(inputDatasets.size() != 0) {

				List<Long> subjectIds = inputDatasets.stream().map(dataset -> dataset.getSubjectId()).collect(Collectors.toList());

				Predicate<Long> predicate = obj -> Objects.equals(inputDatasets.get(0).getSubjectId(), obj);

				if (subjectIds.stream().allMatch(predicate)) {
					Subject subject = subjectRepository.findById(inputDatasets.get(0).getSubjectId())
							.orElseThrow(() -> new NotFoundException("subject not found"));

					processedDataset.setSubjectId(subject.getId());
					processedDataset.setSubjectName(subject.getName());
					processedDataset.setDatasetType(inputDatasets.get(0).getType());
				} else {
					processedDataset.setDatasetType("Mesh");
				}
			} else {
				// default ?
				processedDataset.setDatasetType("Mesh");
			}

			processedDataset.setProcessedDatasetName(getNameWithoutExtension(niiftiFile.getName())); 
			importerService.createProcessedDataset(processedDataset);
		}

		// Create dataset processing
		DatasetProcessing processing = new DatasetProcessing();
		processing.setComment(carminDatasetProcessing.getPipelineIdentifier());
		processing.setInputDatasets(inputDatasets);
		processing.setProcessingDate(carminDatasetProcessing.getProcessingDate());
		processing.setStudyId(carminDatasetProcessing.getStudyId());
		processing.setDatasetProcessingType(carminDatasetProcessing.getDatasetProcessingType());
		processing.setOutputDatasets(outputDatasets);

		datasetProcessingService.create(processing);

		// Remove datasets from cuurent Carmin processing
		carminDatasetProcessing.setInputDatasets(Collections.emptyList());
		datasetProcessingService.update(carminDatasetProcessing);

		deleteCacheDir(Paths.get(destDir));
	}

	private void deleteCacheDir(Path directory) {
		try {
			Files.walk(directory)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
		} catch (IOException e) {

		}
	}

	private String getNameWithoutExtension(String file) {
		int dotIndex = file.indexOf('.');
		return (dotIndex == -1) ? file : file.substring(0, dotIndex);
	}
}
