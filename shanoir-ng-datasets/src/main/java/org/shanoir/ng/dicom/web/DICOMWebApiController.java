package org.shanoir.ng.dicom.web;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DICOMWebApiController implements DICOMWebApi {
	
	private static final Logger LOG = LoggerFactory.getLogger(DICOMWebApiController.class);

	@Autowired
	private ExaminationService examinationService;
	
	@Autowired
	private DICOMWebService dicomWebService;
	
	@Override
	public ResponseEntity<String> findPatients() throws RestServiceException {
		return null;
	}

	@Override
	public String findStudies(Map<String,String> allParams) throws RestServiceException {
		String response = dicomWebService.get();
		return response;
	}

	@Override
	public ResponseEntity<String> findSeries() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findSeriesOfStudy(Long examinationId) throws RestServiceException {
		Examination examination = examinationService.findById(examinationId);
		if (examination == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String studyInstanceUID = findStudyInstanceUID(examination);
		if (studyInstanceUID != null) {
			String response = dicomWebService.findSeriesOfStudy(studyInstanceUID);
			return new ResponseEntity<String>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<String> findSerieMetadataOfStudy(Long examinationId, String serieId)
			throws RestServiceException {
		Examination examination = examinationService.findById(examinationId);
		if (examination == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String studyInstanceUID = findStudyInstanceUID(examination);
		if (studyInstanceUID != null && serieId != null) {
			String response = dicomWebService.findSerieMetadataOfStudy(studyInstanceUID, serieId);
			return new ResponseEntity<String>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private String findStudyInstanceUID(Examination examination) {
		// get StudyInstanceUID from dataset_file table
		// TODO this should be optimized by probably storing the StudyInstanceUID on exam level by default
		List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
		if (!acquisitions.isEmpty()) {
			DatasetAcquisition acquisition = acquisitions.get(0);
			List<Dataset> datasets = acquisition.getDatasets();
			if (!datasets.isEmpty()) {
				Dataset dataset = datasets.get(0);
				List<DatasetExpression> expressions = dataset.getDatasetExpressions();
				if (!expressions.isEmpty()) {
					for (DatasetExpression expression : expressions) {
						// only DICOM is of interest here
						if (expression.getDatasetExpressionFormat().equals(DatasetExpressionFormat.DICOM)) {
							List<DatasetFile> files = expression.getDatasetFiles();
							if (!files.isEmpty()) {
								DatasetFile file = files.get(0);
								String path = file.getPath();
								Pattern p = Pattern.compile("studyUID=(.*?)\\&seriesUID");
								Matcher m = p.matcher(path);
								while (m.find()) {
									String studyInstanceUID = m.group(1);
									return studyInstanceUID;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public ResponseEntity<String> findInstances() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudy(String studyInstanceUID) throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudyOfSerie(String studyInstanceUID, String serieInstanceUID)
			throws RestServiceException {
		return null;
	}

}
