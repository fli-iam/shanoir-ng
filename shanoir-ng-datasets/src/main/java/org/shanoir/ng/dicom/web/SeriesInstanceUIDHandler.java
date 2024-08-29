package org.shanoir.ng.dicom.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SeriesInstanceUIDHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SeriesInstanceUIDHandler.class);

    private static final String WADO_URI_STUDY_UID_SERIES_UID = "studyUID=(.*?)\\&seriesUID";

    private static final String WADO_RS_STUDY_UID_SERIES_UID = "/studies/(.*?)/series/";

    private static final String DICOM_TAG_SERIES_INSTANCE_UID = "0020000E";

    private static final String DICOM_TAG_RETRIEVE_URL = "00081190";

    private static final String VALUE = "Value";

    private static final String RETRIEVE_URL_SERIE_LEVEL = "/studies/(.*)/series/";

    private static final String RETRIEVE_URL_STUDY_LEVEL = "/studies/(.*)";

    private static final String STUDIES = "/studies/";

    private static final String SERIES = "/series/";

    public static final String PREFIX = UIDGeneration.ROOT + ".";

    @Autowired
    private DatasetAcquisitionService acquisitionService;

    private ConcurrentHashMap<String, String> acquisitionUIDToSeriesInstanceUIDCache;

    @PostConstruct
    public void init() {
        acquisitionUIDToSeriesInstanceUIDCache = new ConcurrentHashMap<String, String>(1000);
        LOG.info("DICOMWeb cache created: acquisitionUIDToSeriesInstanceUIDCache");
    }

    @Scheduled(cron = "0 0 6 * * *", zone="Europe/Paris")
    public void clearAcquisitionIdToSeriesInstanceUIDCache() {
        acquisitionUIDToSeriesInstanceUIDCache.clear();
        LOG.info("DICOMWeb cache cleared: acquisitionUIDToSeriesInstanceUIDCache");
    }

    public void replaceSeriesInstanceUIDsWithAcquisitionUIDs(JsonNode root, String acquisitionUID, boolean studyLevel) {
        if (root.isObject()) {
            // find attribute: SeriesInstanceUID
            JsonNode seriesInstanceUIDNode = root.get(DICOM_TAG_SERIES_INSTANCE_UID);
            if (seriesInstanceUIDNode != null) {
                ArrayNode studyInstanceUIDArray = (ArrayNode) seriesInstanceUIDNode.path(VALUE);
                for (int i = 0; i < studyInstanceUIDArray.size(); i++) {
                    studyInstanceUIDArray.remove(i);
                    studyInstanceUIDArray.insert(i,  acquisitionUID);
                }
            }
            // find attribute: RetrieveURL
            JsonNode retrieveURLNode = root.get(DICOM_TAG_RETRIEVE_URL);
            if (retrieveURLNode != null) {
                ArrayNode retrieveURLArray = (ArrayNode) retrieveURLNode.path(VALUE);
                for (int i = 0; i < retrieveURLArray.size(); i++) {
                    JsonNode arrayElement = retrieveURLArray.get(i);
                    String retrieveURL = arrayElement.asText();
                    if (studyLevel) { // serie level
                        retrieveURL = retrieveURL.replaceFirst(RETRIEVE_URL_SERIE_LEVEL, STUDIES + acquisitionUID + SERIES);
                        retrieveURLArray.remove(i);
                        retrieveURLArray.insert(i, retrieveURL);
                    }
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                replaceSeriesInstanceUIDsWithAcquisitionUIDs(arrayElement, acquisitionUID, studyLevel);
            }
        }
    }

    public String findSeriesInstanceUIDFromCacheOrDatabase(String acquisitionUID) {
        LOG.error("findSeries uid : " + acquisitionUID);
        String seriesInstanceUID = acquisitionUIDToSeriesInstanceUIDCache.get(acquisitionUID);
        if (seriesInstanceUID == null) {
            Long acquisitionId = extractAcquisitionId(acquisitionUID);
            DatasetAcquisition acquisition = acquisitionService.findById(acquisitionId);
            if (acquisition != null) {
                seriesInstanceUID = findSeriesInstanceUID(acquisition);
                if (seriesInstanceUID != null) {
                    acquisitionUIDToSeriesInstanceUIDCache.put(acquisitionUID, seriesInstanceUID);
                    LOG.info("DICOMWeb cache adding: " + acquisitionUID + ", " + seriesInstanceUID);
                    LOG.info("DICOMWeb cache, size: " + acquisitionUIDToSeriesInstanceUIDCache.size());
                }
            }
        }
        LOG.error("findSeries return seriesInstanceUID : " + seriesInstanceUID);
        return seriesInstanceUID;
    }

    private String findSeriesInstanceUID(DatasetAcquisition acquisition) {
        if (acquisition instanceof MrDatasetAcquisition
                || acquisition instanceof CtDatasetAcquisition
                || acquisition instanceof PetDatasetAcquisition) {
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
                                if (file.isPacs()) {
                                    String path = file.getPath();
                                    return findSeriesInstanceUID(path);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method extracts the StudyInstanceUID from a WADO string.
     * It tries first WADO-URI, and then WADO-RS, in case of nothing
     * could be found for WADO-URI.
     *
     * @param path
     */
    private String findSeriesInstanceUID(String path) {
        LOG.error("findSeriesInstanceUID : ", path);
        Pattern p = Pattern.compile(WADO_URI_STUDY_UID_SERIES_UID);
        Matcher m = p.matcher(path);
        while (m.find()) {
            return m.group(1);
        }
        p = Pattern.compile(WADO_RS_STUDY_UID_SERIES_UID);
        m = p.matcher(path);
        while (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public Long extractAcquisitionId(String acquisitionUID) {
        String acquisitionUIDWithoutPrefix = acquisitionUID.substring(PREFIX.length());
        Long id = Long.parseLong(acquisitionUIDWithoutPrefix);
        return id;
    }

}
