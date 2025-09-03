/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.processing.controler;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.processing.service.ProcessingDownloaderService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

@Controller
public class DatasetProcessingApiController implements DatasetProcessingApi {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetProcessingApiController.class);
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	@Autowired
	private DatasetMapper datasetMapper;

	@Autowired
	private DatasetProcessingMapper datasetProcessingMapper;

	@Autowired
	private DatasetProcessingService datasetProcessingService;

	@Autowired
	private ProcessingDownloaderService processingDownloaderService;

	@Autowired
	private ExaminationService examinationService;

	public DatasetProcessingApiController(){}

	public ResponseEntity<Void> deleteDatasetProcessing(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
			throws RestServiceException {

		try {
			datasetProcessingService.deleteById(datasetProcessingId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (IOException | SolrServerException | ShanoirException e) {
			LOG.error("Error while deleting datasets: ", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<DatasetProcessingDTO> findDatasetProcessingById(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		if (!datasetProcessing.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(datasetProcessing.get()), HttpStatus.OK);
	}

	public ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessings() {
		final List<DatasetProcessing> datasetProcessings = datasetProcessingService.findAll();
		if (datasetProcessings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingsToDatasetProcessingDTOs(datasetProcessings), HttpStatus.OK);
	}

	public ResponseEntity<List<DatasetProcessingDTO>> getProcessingsByInputDataset(@Parameter(description = "id of the input dataset", required = true) @PathVariable("datasetId") Long datasetId) {
		final List<DatasetProcessing> datasetProcessings = datasetProcessingService.findByInputDatasetId(datasetId);
		if (datasetProcessings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingsToDatasetProcessingDTOs(datasetProcessings), HttpStatus.OK);
	}

	public ResponseEntity<List<DatasetDTO>> getInputDatasets(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		List<Dataset> inputDatasets = datasetProcessing.get().getInputDatasets();
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(inputDatasets), HttpStatus.OK);
	}

	public ResponseEntity<List<DatasetDTO>> getOutputDatasets(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		List<Dataset> outputDatasets = datasetProcessing.get().getOutputDatasets();
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(outputDatasets), HttpStatus.OK);
	}

	public ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(
			@Parameter(description = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {

		/* set authenticated username */
		datasetProcessing.setUsername(KeycloakUtil.getTokenUserName());
		
		/* Validation */
		validate(result);
		datasetProcessingService.validateDatasetProcessing(datasetProcessing);

		/* Save dataset processing in db. */
		final DatasetProcessing createdDatasetProcessing = datasetProcessingService.create(datasetProcessing);
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(createdDatasetProcessing), HttpStatus.OK);
	}

	public ResponseEntity<Void> updateDatasetProcessing(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
			@Parameter(description = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {

		validate(result);
		datasetProcessingService.validateDatasetProcessing(datasetProcessing);

		try {
			datasetProcessingService.update(datasetProcessing);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private void validate(BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}

	public void massiveDownloadByProcessingIds(
			@Parameter(description = "ids of processing", required=true) @Valid
			@RequestBody List<Long> processingIds,
			@Parameter(description = "outputs to extract") @Valid
			@RequestParam(value = "resultOnly") boolean resultOnly,
			HttpServletResponse response) throws RestServiceException {

		List<DatasetProcessing> processingList = new ArrayList<>();
		for (Long processingId : processingIds) {
			DatasetProcessing processing = null;
			try {
				if(processingId == null){
					throw new Exception();
				}
				processing = datasetProcessingService.findById(processingId).get();
				processingList.add(processing);
			}catch (Exception e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.FORBIDDEN.value(), processingId + " is not a valid processing id."));
			}
		}
		processingDownloaderService.massiveDownload(processingList, resultOnly, "dcm" , response, false, null);
	}

	public void massiveDownloadProcessingByExaminationIds(
			@Parameter(description = "ids of examination", required=true) @Valid
			@RequestBody List<Long> examinationIds,
			@Parameter(description = "comment of the desired processings") @Valid
			@RequestParam(value = "processingComment", required = false) String processingComment,
			@Parameter(description = "outputs to extract") @Valid
			@RequestParam(value = "resultOnly") boolean resultOnly,
			HttpServletResponse response) throws RestServiceException {

		List<Examination> examinationList = new ArrayList<>();
		for (Long examinationId : examinationIds) {
			Examination examination = null;
			try {
				if(examinationId == null){
					throw new Exception();
				}
				examination = examinationService.findById(examinationId);

				if(Objects.isNull(examination)){
					throw new Exception();
				}
				examinationList.add(examination);
			}catch (Exception e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.FORBIDDEN.value(), examinationId + " is not a valid examination id."));
			}
		}
		processingDownloaderService.massiveDownloadByExaminations(examinationList, processingComment, resultOnly, "dcm" , response, false, null);
	}

	public void complexMassiveDownload(
			@Parameter(description = "parameters for download", required = true)
			@Valid @RequestBody JsonNode jsonRequest,
			HttpServletResponse response) throws Exception {
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
		File zipFile = new File(userDir + File.separator + "processingOutputsExtraction.zip");
		if(zipFile.exists()) {
			zipFile.delete();
		}
		zipFile.createNewFile();

		try (FileOutputStream fos = new FileOutputStream(zipFile);
			 	ZipOutputStream zos = new ZipOutputStream(fos)) {
			LOG.info("Starting complex download for process data.");
			processingDownloaderService.complexMassiveDownload(jsonRequest, zos);
			LOG.info("Complex download completed.");
		}catch (Exception e) {
			LOG.error("Error while downloading processing data.", e);
		}
	}

	public ResponseEntity<Resource> downloadProcessingsOutputs() {
		try {
			String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
			File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
			File zipFile = new File(userDir, "processingOutputsExtraction.zip");

			if (!zipFile.exists()) {
				LOG.error("Processing output file not found: {}", zipFile.getAbsolutePath());
				return ResponseEntity.notFound().build();
			}

			InputStreamResource resource = new InputStreamResource(Files.newInputStream(zipFile.toPath()));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + zipFile.getName() + "\"")
					.contentType(MediaType.APPLICATION_OCTET_STREAM)  // use octet-stream for downloads
					.contentLength(zipFile.length())
					.body(resource);

		} catch (Exception e) {
			LOG.error("Error during download of processing outputs");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Scheduled(cron = "0 0 * * * *", zone="Europe/Paris")
	public void deleteProcessingOutputsArchive() {
		try {
			String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
			File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
			Path directoryPath = Paths.get(userDir.getPath());

			long currentTime = System.currentTimeMillis();
			long sixHoursInMillis = TimeUnit.HOURS.toMillis(6);
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath);

			for (Path filePath : directoryStream) {
				if (filePath.getFileName().toString().startsWith("processingOutputsExtraction")) {
					BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
					FileTime creationTime = attrs.creationTime();
					long creationTimeMillis = creationTime.toMillis();

					if ((currentTime - creationTimeMillis) > sixHoursInMillis) {
						Files.delete(filePath);
						LOG.error("Processing outputs file deleted after 6 hours : " + filePath.getFileName());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
