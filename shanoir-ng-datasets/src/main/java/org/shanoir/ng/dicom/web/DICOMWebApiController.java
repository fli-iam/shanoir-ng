package org.shanoir.ng.dicom.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.solr.common.StringUtils;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Iterator;
import java.util.Map;

@Controller
public class DICOMWebApiController implements DICOMWebApi {
	
	private static final Logger LOG = LoggerFactory.getLogger(DICOMWebApiController.class);
	
	@Autowired
	private ExaminationService examinationService;
	
	@Autowired
	private DICOMWebService dicomWebService;

	@Autowired
	private StudyInstanceUIDHandler studyInstanceUIDHandler;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Override
	public ResponseEntity<String> findPatients() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findStudies(Map<String, String> allParams) throws RestServiceException, JsonMappingException, JsonProcessingException {
		int offset = Integer.valueOf(allParams.get("offset"));
		int limit = Integer.valueOf(allParams.get("limit"));
		Pageable pageable = PageRequest.of(offset, limit);
		// Search for studies with patient name (DICOM patientID does not make sense in case of Shanoir)
		String patientName = allParams.get("PatientName");
		Page<Examination> examinations = examinationService.findPage(pageable, patientName);
		if (examinations.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		StringBuffer studies = new StringBuffer();
		studies.append("[");
		Iterator<Examination> iterator = examinations.iterator();
		while (iterator.hasNext()) {
			Examination examination = iterator.next();
			String examinationUID = StudyInstanceUIDHandler.PREFIX + examination.getId();
			String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
			if (studyInstanceUID != null) {
				String studyJson = dicomWebService.findStudy(studyInstanceUID);
				JsonNode root = mapper.readTree(studyJson);
				studyInstanceUIDHandler.replaceStudyInstanceUIDsWithExaminationUIDs(root, examinationUID, true);
				studyJson = mapper.writeValueAsString(root);
				studyJson = studyJson.substring(1, studyJson.length() - 1);
				studies.append(studyJson);
				if (iterator.hasNext()) {
					studies.append(",");
				}
			}
		}
		studies.append("]");
		return new ResponseEntity<String>(studies.toString(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> findSeries() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findSeriesOfStudy(String examinationUID)
			throws RestServiceException, JsonMappingException, JsonProcessingException {
		String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
		if (studyInstanceUID != null) {
			String response = dicomWebService.findSeriesOfStudy(studyInstanceUID);
			JsonNode root = mapper.readTree(response);
			studyInstanceUIDHandler.replaceStudyInstanceUIDsWithExaminationUIDs(root, examinationUID, false);
			return new ResponseEntity<String>(mapper.writeValueAsString(root), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Override
	public ResponseEntity<String> findSerieMetadataOfStudy(String examinationUID, String serieId)
			throws RestServiceException, JsonMappingException, JsonProcessingException {
		String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
		if (studyInstanceUID != null && serieId != null) {
			String response = dicomWebService.findSerieMetadataOfStudy(studyInstanceUID, serieId);
			JsonNode root = mapper.readTree(response);
			studyInstanceUIDHandler.replaceStudyInstanceUIDsWithExaminationUIDs(root, examinationUID, false);
			return new ResponseEntity<String>(mapper.writeValueAsString(root), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Override
	public ResponseEntity findFrameOfStudyOfSerieOfInstance(String examinationUID, String serieInstanceUID,
			String sopInstanceUID, String frame) throws RestServiceException {
		String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
		if (!StringUtils.isEmpty(studyInstanceUID) && !StringUtils.isEmpty(serieInstanceUID)
				&& !StringUtils.isEmpty(sopInstanceUID) && !StringUtils.isEmpty(frame))  {
			return dicomWebService.findFrameOfStudyOfSerieOfInstance(studyInstanceUID, serieInstanceUID, sopInstanceUID, frame);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudyOfSerie(String examinationUID, String serieInstanceUID)
			throws RestServiceException {
		return null;
	}
	
	@Override
	public ResponseEntity findInstance(String examinationUID, String serieInstanceUID, String sopInstanceUID)
			throws RestServiceException {
		String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
		if (!StringUtils.isEmpty(studyInstanceUID) && !StringUtils.isEmpty(serieInstanceUID)
				&& !StringUtils.isEmpty(sopInstanceUID))  {
			return dicomWebService.findInstance(studyInstanceUID, serieInstanceUID, sopInstanceUID);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<String> findInstances() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudy(String studyInstanceUID) throws RestServiceException {
		return null;
	}

	/**
	 * This method might never be implemented, as for DICOM file imports shanoir-ng has its
	 * specific classic import, that manages pseudonymization and relation of DICOM files to
	 * the Shanoir research study/project and the subjects, that are pure shanoir entities.
	 * In the future there might be an option for temporary file storage via stow-rs and then
	 * using the dicom files from this tmp folder to process a classic shanoir DICOM import.
	 * The implementation of stow-sr for the ohif-viewer can be found in the MultipartRequestFilter. 
	 */
	@Override
	public ResponseEntity<Void> stow(HttpServletRequest request) throws RestServiceException {
		return null;
	}
	
}
