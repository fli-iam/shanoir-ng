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

package org.shanoir.ng.dicom.web;

import java.util.ArrayList;
import java.util.Comparator;
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
import com.fasterxml.jackson.databind.node.ArrayNode;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DICOMWebApiController implements DICOMWebApi {

    private static final Logger LOG = LoggerFactory.getLogger(DICOMWebApiController.class);

    private static final String INCLUDEFIELD = "includefield";

    private static final String LIMIT = "limit";

    private static final String OFFSET = "offset";

    private static final String PATIENT_ID = "00100020";

    private static final String PATIENT_NAME = "PatientName";

    private static final String SERIES_NUMBER = "00200011";

    private static final String STUDY_INSTANCE_UID = "StudyInstanceUID";

    private static final String SERIES_INSTANCE_UID = "SeriesInstanceUID";

    private static final String VALUE = "Value";

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @Autowired
    private SeriesInstanceUIDHandler seriesInstanceUIDHandler;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public ResponseEntity<String> findPatients() throws RestServiceException {
        return null;
    }

    @Override
    public ResponseEntity<String> findStudies(Map<String, String> allParams) throws RestServiceException, JsonMappingException, JsonProcessingException {
        Page<Examination> examinations = null;
        int offset = Integer.valueOf(allParams.get(OFFSET));
        int limit = Integer.valueOf(allParams.get(LIMIT));
        Pageable pageable = PageRequest.of(offset, limit);
        String includeField = allParams.get(INCLUDEFIELD);
        // 1. Search for studies==examinations with patient name
        // (DICOM patientID does not make sense in case of Shanoir)
        String patientIDTagParam = allParams.get(PATIENT_ID);
        String patientNameParam = allParams.get(PATIENT_NAME);
        if (patientIDTagParam != null || patientNameParam != null) {
            String subjectName;
            if (patientIDTagParam != null) {
                subjectName = patientIDTagParam;
            } else {
                subjectName = patientNameParam;
            }
            // Remove leading and trailing asterix here to search with subject name
            subjectName = subjectName.replaceAll("^\\*+", "").replaceAll("\\*+$", "");
            examinations = examinationService.findPage(pageable, subjectName);
        } else {
            // 2. Manage still existing case with single study instance UID
            List<Examination> examinationList = new ArrayList<>();
            String studyInstanceUID = allParams.get(STUDY_INSTANCE_UID);
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
    public ResponseEntity<String> findSeriesOfStudy(String examinationUID, Map<String, String> allParams)
            throws JsonProcessingException {
        String acquisitionUID = "";
        String includefield = "";
        String seriesInstanceUID = "";
        String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
        if (allParams.containsKey("includefield")) {
            includefield = allParams.get(INCLUDEFIELD);
        }
        if (allParams.containsKey("SeriesInstanceUID")) {
            acquisitionUID = allParams.get(SERIES_INSTANCE_UID);
            seriesInstanceUID = seriesInstanceUIDHandler.findSeriesInstanceUIDFromCacheOrDatabase(acquisitionUID);
        }
        if (studyInstanceUID != null) {
            String response = dicomWebService.findSeriesOfStudy(studyInstanceUID, includefield, seriesInstanceUID);
            if (response != null) {
                JsonNode root = mapper.readTree(response);
                root = sortSeriesBySeriesNumber(root);
                studyInstanceUIDHandler.replaceStudyInstanceUIDsWithExaminationUIDs(root, examinationUID, false);
                return new ResponseEntity<String>(mapper.writeValueAsString(root), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private JsonNode sortSeriesBySeriesNumber(JsonNode root) {
        if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            List<JsonNode> jsonNodes = new ArrayList<>();
            arrayNode.forEach(jsonNodes::add);
            jsonNodes.sort(Comparator.comparingInt(node -> {
                JsonNode seriesNumberNode = node.path(SERIES_NUMBER).path(VALUE).get(0);
                return seriesNumberNode.asInt();
            }));
            ArrayNode sortedArrayNode = mapper.createArrayNode();
            jsonNodes.forEach(sortedArrayNode::add);
            return (JsonNode) sortedArrayNode;
        }
        return root;
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
                && !StringUtils.isEmpty(sopInstanceUID) && !StringUtils.isEmpty(frame)) {
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
                && !StringUtils.isEmpty(sopInstanceUID)) {
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
