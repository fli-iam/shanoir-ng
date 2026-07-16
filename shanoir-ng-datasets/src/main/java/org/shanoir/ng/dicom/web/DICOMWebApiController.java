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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    private static final String SERIES_INSTANCE_UID_TAG = "0020000E";

    private static final String STUDY_INSTANCE_UID = "StudyInstanceUID";

    private static final String SERIES_INSTANCE_UID = "SeriesInstanceUID";

    private static final String VALUE = "Value";

    private static final String BULK_DATA_URI = "BulkDataURI";

    private static final String BULKDATA_PATH = "/bulkdata/";

    private static final String STUDIES_PATH = "/studies/";

    private static final Pattern BULK_DATA_PATH_PATTERN = Pattern.compile("[0-9a-fA-F]+(?:/[0-9a-fA-F]+)*");

    private static final Pattern BULK_DATA_QUERY_PATTERN = Pattern.compile("(?:offset|length)=\\d+(?:&(?:offset|length)=\\d+)*");

    private static final Pattern ACCEPT_HEADER_PATTERN = Pattern.compile("[\\w!#$%&'*+.^`|~/=;,\" -]+");

    private static final int MAX_ACCEPT_HEADER_LENGTH = 512;

    @Value("${viewer.ohif.url.base}")
    private String viewerBaseUrl;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private DatasetAcquisitionRepository datasetAcquisitionRepository;

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDAndSubjectNameHandler;

    @Autowired
    private SeriesInstanceUIDHandler seriesInstanceUIDHandler;

    @Autowired
    private DatasetSecurityService datasetSecurityService;

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
        String patientIDParam = allParams.get(PATIENT_ID);
        String patientNameParam = allParams.get(PATIENT_NAME);
        if (patientIDParam != null || patientNameParam != null) {
            String subjectName;
            if (patientIDParam != null) {
                subjectName = patientIDParam;
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
            String examinationUID = StudyInstanceUIDAndSubjectNameHandler.PREFIX + examination.getId();
            String studyInstanceUID = studyInstanceUIDAndSubjectNameHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
            String subjectName = studyInstanceUIDAndSubjectNameHandler
                    .findSubjectNameFromCacheOrDatabase(examinationUID);
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
            studyInstanceUIDAndSubjectNameHandler.replaceStudyInstanceUIDAndPatientInfo(root, examinationUID, true, subjectName);
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
        String includefield = "";
        String seriesInstanceUID = "";
        String studyInstanceUID = studyInstanceUIDAndSubjectNameHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
        String subjectName = studyInstanceUIDAndSubjectNameHandler
                .findSubjectNameFromCacheOrDatabase(examinationUID);
        if (allParams.containsKey(INCLUDEFIELD)) {
            includefield = allParams.get(INCLUDEFIELD);
        }
        Map<String, String> seriesToVirtualUIDs;
        String filterUID = allParams.get(SERIES_INSTANCE_UID);
        if (seriesInstanceUIDHandler.isAcquisitionUID(filterUID)) {
            // an acquisition can span multiple series in the PACS (SEG/SR
            // datasets), so query all series of the study and filter locally
            // on the full series set of the acquisition
            seriesToVirtualUIDs = seriesInstanceUIDHandler
                    .findSeriesToVirtualUIDsOfAcquisition(seriesInstanceUIDHandler.extractAcquisitionId(filterUID));
        } else {
            if (filterUID != null) {
                seriesInstanceUID = seriesInstanceUIDHandler.resolveSeriesInstanceUID(filterUID);
            }
            Long examinationID = studyInstanceUIDAndSubjectNameHandler.extractExaminationId(examinationUID);
            seriesToVirtualUIDs = seriesInstanceUIDHandler.findSeriesToVirtualUIDs(examinationID);
        }
        if (studyInstanceUID != null) {
            String response = dicomWebService.findSeriesOfStudy(studyInstanceUID, includefield, seriesInstanceUID);
            if (response != null) {
                JsonNode root = mapper.readTree(response);
                root = filterAndSortSeries(root, seriesToVirtualUIDs);
                studyInstanceUIDAndSubjectNameHandler.replaceStudyInstanceUIDAndPatientInfo(root, examinationUID, false, subjectName);
                seriesInstanceUIDHandler.replaceSeriesInstanceUIDs(root, seriesToVirtualUIDs);
                return new ResponseEntity<String>(mapper.writeValueAsString(root), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private JsonNode filterAndSortSeries(JsonNode root, Map<String, String> seriesToVirtualUIDs) {
        if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            List<JsonNode> jsonNodes = new ArrayList<>();
            arrayNode.forEach(jsonNodes::add);
            List<JsonNode> filteredAndSorted = jsonNodes.stream()
                    .filter(node -> {
                        String seriesInstanceUID = node.path(SERIES_INSTANCE_UID_TAG).path(VALUE).path(0).asText();
                        String virtualUID = seriesToVirtualUIDs.get(seriesInstanceUID);
                        // keep only series of this exam (in the map) the user is
                        // allowed to visualize: an owned annotation, e.g. a SEG/SR,
                        // is hidden from the list for everyone but its annotator or
                        // a reviewer
                        return virtualUID != null && datasetSecurityService.hasRightToVisualizeSeries(virtualUID);
                    })
                    .sorted(Comparator.comparingInt(
                            node -> node.path(SERIES_NUMBER).path(VALUE).path(0).asInt(Integer.MAX_VALUE)))
                    .collect(Collectors.toList());
            ArrayNode resultArrayNode = mapper.createArrayNode();
            filteredAndSorted.forEach(resultArrayNode::add);
            return resultArrayNode;
        }
        return root;
    }

    @Override
    public ResponseEntity<String> findSerieMetadataOfStudy(String examinationUID, String serieId)
            throws RestServiceException, JsonMappingException, JsonProcessingException {
        String studyInstanceUID = studyInstanceUIDAndSubjectNameHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
        String subjectName = studyInstanceUIDAndSubjectNameHandler
                .findSubjectNameFromCacheOrDatabase(examinationUID);
        String serieInstanceUID = seriesInstanceUIDHandler.resolveSeriesInstanceUID(serieId);
        if (studyInstanceUID != null && serieInstanceUID != null) {
            String response = dicomWebService.findSerieMetadataOfStudy(studyInstanceUID, serieInstanceUID);
            JsonNode root = mapper.readTree(response);
            studyInstanceUIDAndSubjectNameHandler.replaceStudyInstanceUIDAndPatientInfo(root, examinationUID, false, subjectName);
            // replace all real series UIDs of the exam, not only the requested
            // one: SEG/SR objects reference their source series in the
            // ReferencedSeriesSequence and the viewer only knows virtual UIDs
            Long examinationID = studyInstanceUIDAndSubjectNameHandler.extractExaminationId(examinationUID);
            Map<String, String> seriesToVirtualUIDs = seriesInstanceUIDHandler.findSeriesToVirtualUIDs(examinationID);
            for (Map.Entry<String, String> seriesToVirtualUID : seriesToVirtualUIDs.entrySet()) {
                seriesInstanceUIDHandler.replaceSeriesInstanceUID(root, seriesToVirtualUID.getKey(), seriesToVirtualUID.getValue());
            }
            // the study is referenced from within sequences as well, e.g. by an
            // RT Structure Set in the RTReferencedStudySequence, and the viewer
            // only knows the examinationUID
            studyInstanceUIDAndSubjectNameHandler.replaceStudyInstanceUID(root, studyInstanceUID, examinationUID);
            rewriteBulkDataURIs(root, studyInstanceUID, examinationUID, serieInstanceUID, serieId);
            return new ResponseEntity<String>(mapper.writeValueAsString(root), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * The PACS does not inline large binary attributes, e.g. overlay data
     * (60xx3000), in the serie metadata, but references them with an absolute
     * BulkDataURI, that points to the PACS itself, only reachable within the
     * internal Docker network, and contains the real UIDs. So rewrite each
     * BulkDataURI to the public DICOMWeb facade URL with the virtual UIDs:
     * the viewer can reach it and never sees real UIDs.
     */
    private void rewriteBulkDataURIs(JsonNode root, String studyInstanceUID, String examinationUID,
            String serieInstanceUID, String acquisitionUID) {
        if (root.isArray()) {
            for (JsonNode element : root) {
                rewriteBulkDataURIs(element, studyInstanceUID, examinationUID, serieInstanceUID, acquisitionUID);
            }
        } else if (root.isObject()) {
            JsonNode bulkDataURINode = root.get(BULK_DATA_URI);
            if (bulkDataURINode != null && bulkDataURINode.isTextual()) {
                String rewritten = rewriteBulkDataURI(bulkDataURINode.asText(), studyInstanceUID, examinationUID,
                        serieInstanceUID, acquisitionUID);
                if (rewritten != null) {
                    ((ObjectNode) root).put(BULK_DATA_URI, rewritten);
                }
            }
            for (JsonNode child : root) {
                if (child.isContainerNode()) {
                    rewriteBulkDataURIs(child, studyInstanceUID, examinationUID, serieInstanceUID, acquisitionUID);
                }
            }
        }
    }

    private String rewriteBulkDataURI(String bulkDataURI, String studyInstanceUID, String examinationUID,
            String serieInstanceUID, String acquisitionUID) {
        int index = bulkDataURI.indexOf(STUDIES_PATH);
        if (index == -1) {
            return null;
        }
        String[] segments = bulkDataURI.substring(index + 1).split("/");
        for (int i = 1; i < segments.length; i++) {
            if ("studies".equals(segments[i - 1]) && segments[i].equals(studyInstanceUID)) {
                segments[i] = examinationUID;
            } else if ("series".equals(segments[i - 1]) && segments[i].equals(serieInstanceUID)) {
                segments[i] = acquisitionUID;
            }
        }
        return viewerBaseUrl + "/" + String.join("/", segments);
    }

    @Override
    public ResponseEntity findFrameOfStudyOfSerieOfInstance(String examinationUID, String serieInstanceUID,
                                                            String sopInstanceUID, String frame, String accept) throws RestServiceException {
        String studyInstanceUID = studyInstanceUIDAndSubjectNameHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
        serieInstanceUID = seriesInstanceUIDHandler.resolveSeriesInstanceUID(serieInstanceUID);
        if (!StringUtils.isEmpty(studyInstanceUID) && !StringUtils.isEmpty(serieInstanceUID)
                && !StringUtils.isEmpty(sopInstanceUID) && !StringUtils.isEmpty(frame)) {
            return dicomWebService.findFrameOfStudyOfSerieOfInstance(studyInstanceUID, serieInstanceUID, sopInstanceUID,
                    frame, sanitizeAcceptHeader(accept));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * The Accept header of the viewer is forwarded to the PACS, so that the
     * viewer can negotiate a transfer syntax it is able to decode. As the value
     * ends up in an outgoing request, only sane media type characters are kept:
     * anything else is dropped and the PACS applies its own default.
     *
     * @param accept the Accept header received from the viewer, may be null
     * @return the header to forward or null, if there is nothing to forward
     */
    private String sanitizeAcceptHeader(String accept) {
        if (StringUtils.isEmpty(accept) || accept.length() > MAX_ACCEPT_HEADER_LENGTH) {
            return null;
        }
        if (!ACCEPT_HEADER_PATTERN.matcher(accept).matches()) {
            LOG.warn("DICOMWeb: ignoring unexpected Accept header for frame request.");
            return null;
        }
        return accept;
    }

    @Override
    public ResponseEntity findBulkDataOfStudyOfSerieOfInstance(String examinationUID, String serieInstanceUID,
                                                               String sopInstanceUID, HttpServletRequest request) throws RestServiceException {
        String studyInstanceUID = studyInstanceUIDAndSubjectNameHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
        serieInstanceUID = seriesInstanceUIDHandler.resolveSeriesInstanceUID(serieInstanceUID);
        String bulkDataPath = extractBulkDataPath(request);
        if (!StringUtils.isEmpty(studyInstanceUID) && !StringUtils.isEmpty(serieInstanceUID)
                && !StringUtils.isEmpty(sopInstanceUID) && !StringUtils.isEmpty(bulkDataPath)) {
            return dicomWebService.findBulkDataOfStudyOfSerieOfInstance(studyInstanceUID, serieInstanceUID, sopInstanceUID, bulkDataPath);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private String extractBulkDataPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        int index = requestURI.indexOf(BULKDATA_PATH);
        if (index == -1) {
            return null;
        }
        String bulkDataPath = requestURI.substring(index + BULKDATA_PATH.length());
        if (!BULK_DATA_PATH_PATTERN.matcher(bulkDataPath).matches()) {
            return null;
        }
        String queryString = request.getQueryString();
        if (queryString != null) {
            if (!BULK_DATA_QUERY_PATTERN.matcher(queryString).matches()) {
                return null;
            }
            bulkDataPath += "?" + queryString;
        }
        return bulkDataPath;
    }

    @Override
    public ResponseEntity<String> findInstancesOfStudyOfSerie(String examinationUID, String serieInstanceUID)
            throws RestServiceException {
        return null;
    }

    @Override
    public ResponseEntity findInstance(String examinationUID, String serieInstanceUID, String sopInstanceUID)
            throws RestServiceException {
        String studyInstanceUID = studyInstanceUIDAndSubjectNameHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
        String subjectName = studyInstanceUIDAndSubjectNameHandler.findSubjectNameFromCacheOrDatabase(examinationUID);
        serieInstanceUID = seriesInstanceUIDHandler.resolveSeriesInstanceUID(serieInstanceUID);
        if (!StringUtils.isEmpty(studyInstanceUID) && !StringUtils.isEmpty(serieInstanceUID)
                && !StringUtils.isEmpty(sopInstanceUID)) {
            return dicomWebService.findInstance(studyInstanceUID, serieInstanceUID, sopInstanceUID, subjectName);
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
