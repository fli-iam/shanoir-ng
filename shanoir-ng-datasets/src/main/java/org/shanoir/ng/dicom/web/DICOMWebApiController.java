package org.shanoir.ng.dicom.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

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
		Page<Examination> examinations = null;
		int offset = Integer.valueOf(allParams.get("offset"));
		int limit = Integer.valueOf(allParams.get("limit"));
		String includeField = allParams.get("includefield");
		Pageable pageable = PageRequest.of(offset, limit);
		// 1. Search for studies==examinations with patient name
		// (DICOM patientID does not make sense in case of Shanoir)
		String patientName = allParams.get("00100020");
		if (patientName != null) {
			// Remove leading and trailing asterix here
			patientName = patientName.replaceAll("^\\*+", "").replaceAll("\\*+$", "");
			examinations = examinationService.findPage(pageable, patientName);
		} else {
			List<Examination> examinationList = new ArrayList<>();
			// 2. Manage still existing case with single study instance UID
			String studyInstanceUID = allParams.get("StudyInstanceUID");
			if (studyInstanceUID != null) {
				String examinationIdString = studyInstanceUID.substring(studyInstanceUID.lastIndexOf(".") + 1, studyInstanceUID.length());
				Examination examination = examinationService.findById(Long.valueOf(examinationIdString));
				examinationList.add(examination);
			}
			// 3. Manage case, that nothing specific was found and return depending on pageable
			if (examinationList.isEmpty()) {
				examinations = examinationService.findPage(pageable, null);
			} else {
				examinations = new PageImpl<>(examinationList);
			}
		}
		if (examinations == null || examinations.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		String studiesJson = queryDicomServerForDicomWeb(examinations, includeField);
		return new ResponseEntity<String>(studiesJson, HttpStatus.OK);
	}

	private String queryDicomServerForDicomWeb(Page<Examination> examinations, String includeField)
			throws JsonProcessingException, JsonMappingException {
		StringBuffer studies = new StringBuffer();
		studies.append("[");
		Iterator<Examination> iterator = examinations.iterator();
		while (iterator.hasNext()) {
			Examination examination = iterator.next();
			String examinationUID = StudyInstanceUIDHandler.PREFIX + examination.getId();
			String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
			if (studyInstanceUID == null) {
				// continue: e.g. empty examination without dataset acquisition
				continue;
			}
			String studyJson = dicomWebService.findStudy(studyInstanceUID, includeField);
			if (studyJson == null) {
				// continue: avoid viewer loading error in case an examination exists in database,
				// but no images remain in DICOM server anymore
				continue;
			}
			JsonNode root = mapper.readTree(studyJson);
			studyInstanceUIDHandler.replaceStudyInstanceUIDsWithExaminationUIDs(root, examinationUID, true);
			studyJson = mapper.writeValueAsString(root);
			studyJson = studyJson.substring(1, studyJson.length() - 1);
			studies.append(studyJson);
			if (iterator.hasNext()) {
				studies.append(",");
			}
		}
		studies.append("]");
		return studies.toString();
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
