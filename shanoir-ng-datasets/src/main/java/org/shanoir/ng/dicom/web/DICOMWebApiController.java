package org.shanoir.ng.dicom.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.solr.common.StringUtils;
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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
public class DICOMWebApiController implements DICOMWebApi {
	
	private static final Logger LOG = LoggerFactory.getLogger(DICOMWebApiController.class);

	@Autowired
	private ExaminationService examinationService;
	
	@Autowired
	private DICOMWebService dicomWebService;
	
	private HashMap<Long, String> examinationIdToStudyInstanceUIDCache;
	
	@PostConstruct
	public void init() {
		examinationIdToStudyInstanceUIDCache = new HashMap<Long, String>(1000);
	}
	
	@Override
	public ResponseEntity<String> findPatients() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findStudies(Map<String,String> allParams) throws RestServiceException {
		String response = dicomWebService.findStudies();
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> findSeries() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findSeriesOfStudy(Long examinationId)
			throws RestServiceException, JsonMappingException, JsonProcessingException {
		String studyInstanceUID = findStudyInstanceUIDFromCacheOrDatabase(examinationId);
		if (studyInstanceUID != null) {
			String response = dicomWebService.findSeriesOfStudy(studyInstanceUID);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(response);
//			if (jsonNode..get("0020000D").isArray()) {
//				for (JsonNode node : jsonNode.get("0020000D")) {
//					ObjectNode objectNode = (ObjectNode) node;
//					if (objectNode.hasNonNull("Value")) {
//						String val = objectNode.get("Value").asText();
//						objectNode.put("Value", examinationId);
//					}
//				}
//			}
			return new ResponseEntity<String>(mapper.writeValueAsString(jsonNode), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<String> findSerieMetadataOfStudy(Long examinationId, String serieId)
			throws RestServiceException {
		String studyInstanceUID = findStudyInstanceUIDFromCacheOrDatabase(examinationId);
		if (studyInstanceUID != null && serieId != null) {
			String response = dicomWebService.findSerieMetadataOfStudy(studyInstanceUID, serieId);
			return new ResponseEntity<String>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Override
	public ResponseEntity<InputStream> findFrameOfStudyOfSerieOfInstance(Long examinationId, String serieInstanceUID,
			String sopInstanceUID, String frame) throws RestServiceException {
		String studyInstanceUID = findStudyInstanceUIDFromCacheOrDatabase(examinationId);
		if (!StringUtils.isEmpty(studyInstanceUID) && !StringUtils.isEmpty(serieInstanceUID)
				&& !StringUtils.isEmpty(sopInstanceUID) && !StringUtils.isEmpty(frame))  {
			return dicomWebService.findFrameOfStudyOfSerieOfInstance(studyInstanceUID, serieInstanceUID, sopInstanceUID, frame);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudyOfSerie(String studyInstanceUID, String serieInstanceUID)
			throws RestServiceException {
		return null;
	}

	private String findStudyInstanceUIDFromCacheOrDatabase(Long examinationId) {
		String studyInstanceUID = examinationIdToStudyInstanceUIDCache.get(examinationId);
		if (studyInstanceUID == null) {
			Examination examination = examinationService.findById(examinationId);
			if (examination != null) {
				studyInstanceUID = findStudyInstanceUID(examination);
				if (studyInstanceUID != null) {
					examinationIdToStudyInstanceUIDCache.put(examination.getId(), studyInstanceUID);
				}
			}
		}
		return studyInstanceUID;
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

}
