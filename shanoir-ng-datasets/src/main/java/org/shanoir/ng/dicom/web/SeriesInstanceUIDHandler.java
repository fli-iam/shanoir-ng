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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import jakarta.annotation.PostConstruct;

@Component
public class SeriesInstanceUIDHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SeriesInstanceUIDHandler.class);

    private static final String WADO_URI_SERIES_UID_OBJECT_UID = "seriesUID=(.*?)\\&objectUID";

    private static final String WADO_RS_SERIES_UID_INSTANCES_UID = "/series/(.*?)/instances/";

    private static final String DICOM_TAG_SERIES_INSTANCE_UID = "0020000E";

    private static final String DICOM_TAG_RETRIEVE_URL = "00081190";

    private static final String VALUE = "Value";

    public static final String PREFIX = UIDGeneration.ROOT + ".";

    @Autowired
    private DatasetAcquisitionService acquisitionService;

    private ConcurrentHashMap<String, String> acquisitionUIDToSeriesInstanceUIDCache;

    @PostConstruct
    public void init() {
        acquisitionUIDToSeriesInstanceUIDCache = new ConcurrentHashMap<String, String>(1000);
        LOG.info("DICOMWeb cache created: acquisitionUIDToSeriesInstanceUIDCache");
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Paris")
    public void clearAcquisitionIdToSeriesInstanceUIDCache() {
        acquisitionUIDToSeriesInstanceUIDCache.clear();
        LOG.info("DICOMWeb cache cleared: acquisitionUIDToSeriesInstanceUIDCache");
    }

    /**
     * Resolves the real SeriesInstanceUID for PACS queries: acquisitionUIDs
     * (virtual UIDs exposed to the viewer) are translated via cache/database,
     * real SeriesInstanceUIDs are returned unchanged.
     *
     * @param seriesOrAcquisitionUID
     * @return
     */
    public String resolveSeriesInstanceUID(String seriesOrAcquisitionUID) {
        if (isAcquisitionUID(seriesOrAcquisitionUID)) {
            return findSeriesInstanceUIDFromCacheOrDatabase(seriesOrAcquisitionUID);
        }
        return seriesOrAcquisitionUID;
    }

    /**
     * An acquisitionUID is built as ROOT + "." + acquisition id: its suffix
     * always fits into a Long. Real UIDs generated during pseudonymization
     * (see UIDGeneration) end with a 40-digit number, that never fits.
     *
     * @param uid
     * @return
     */
    public boolean isAcquisitionUID(String uid) {
        if (uid == null || !uid.startsWith(PREFIX)) {
            return false;
        }
        try {
            Long.parseLong(uid.substring(PREFIX.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Maps the real SeriesInstanceUID of each DICOM acquisition of an examination
     * to its acquisitionUID (virtual UID exposed to the viewer).
     *
     * @param examinationId
     * @return
     */
    public Map<String, String> findSeriesToAcquisitionUIDs(Long examinationId) {
        Map<String, String> seriesToAcquisitionUIDs = new LinkedHashMap<>();
        List<DatasetAcquisition> acquisitions = acquisitionService.findByExamination(examinationId);
        for (DatasetAcquisition acquisition : acquisitions) {
            String acquisitionUID = PREFIX + acquisition.getId();
            String seriesInstanceUID = acquisitionUIDToSeriesInstanceUIDCache.get(acquisitionUID);
            if (seriesInstanceUID == null) {
                seriesInstanceUID = findSeriesInstanceUID(acquisition);
                if (seriesInstanceUID != null) {
                    acquisitionUIDToSeriesInstanceUIDCache.putIfAbsent(acquisitionUID, seriesInstanceUID);
                }
            }
            if (seriesInstanceUID != null) {
                seriesToAcquisitionUIDs.put(seriesInstanceUID, acquisitionUID);
            }
        }
        return seriesToAcquisitionUIDs;
    }

    /**
     * Replaces in the Json returned from the PACS each real SeriesInstanceUID
     * with its acquisitionUID, so the viewer only ever sees virtual UIDs.
     *
     * @param root
     * @param seriesToAcquisitionUIDs
     */
    public void replaceSeriesInstanceUIDs(JsonNode root, Map<String, String> seriesToAcquisitionUIDs) {
        if (root.isArray()) {
            for (JsonNode series : root) {
                replaceSeriesInstanceUIDs(series, seriesToAcquisitionUIDs);
            }
        } else if (root.isObject()) {
            String seriesInstanceUID = root.path(DICOM_TAG_SERIES_INSTANCE_UID).path(VALUE).path(0).asText();
            String acquisitionUID = seriesToAcquisitionUIDs.get(seriesInstanceUID);
            if (acquisitionUID != null) {
                replaceSeriesInstanceUID(root, seriesInstanceUID, acquisitionUID);
            }
        }
    }

    /**
     * Recursively replaces one real SeriesInstanceUID with its acquisitionUID
     * in all SeriesInstanceUID (0020000E) and RetrieveURL (00081190) values,
     * including nested sequences, e.g. in instance metadata responses.
     *
     * @param root
     * @param seriesInstanceUID
     * @param acquisitionUID
     */
    public void replaceSeriesInstanceUID(JsonNode root, String seriesInstanceUID, String acquisitionUID) {
        if (root.isArray()) {
            for (JsonNode element : root) {
                replaceSeriesInstanceUID(element, seriesInstanceUID, acquisitionUID);
            }
        } else if (root.isObject()) {
            replaceInValues(root.get(DICOM_TAG_SERIES_INSTANCE_UID), seriesInstanceUID, acquisitionUID);
            replaceInValues(root.get(DICOM_TAG_RETRIEVE_URL), seriesInstanceUID, acquisitionUID);
            for (JsonNode child : root) {
                if (child.isContainerNode()) {
                    replaceSeriesInstanceUID(child, seriesInstanceUID, acquisitionUID);
                }
            }
        }
    }

    private void replaceInValues(JsonNode tagNode, String seriesInstanceUID, String acquisitionUID) {
        if (tagNode == null || !tagNode.path(VALUE).isArray()) {
            return;
        }
        ArrayNode values = (ArrayNode) tagNode.path(VALUE);
        for (int i = 0; i < values.size(); i++) {
            JsonNode value = values.get(i);
            if (value.isTextual() && value.asText().contains(seriesInstanceUID)) {
                String replaced = value.asText().replace(seriesInstanceUID, acquisitionUID);
                values.remove(i);
                values.insert(i, replaced);
            }
        }
    }

    public String findSeriesInstanceUIDFromCacheOrDatabase(String acquisitionUID) {
        String seriesInstanceUID = acquisitionUIDToSeriesInstanceUIDCache.get(acquisitionUID);
        if (seriesInstanceUID == null) {
            Long acquisitionId = extractAcquisitionId(acquisitionUID);
            DatasetAcquisition acquisition = acquisitionService.findById(acquisitionId);
            if (acquisition != null) {
                seriesInstanceUID = findSeriesInstanceUID(acquisition);
                if (seriesInstanceUID != null) {
                    String existing = acquisitionUIDToSeriesInstanceUIDCache.putIfAbsent(acquisitionUID, seriesInstanceUID);
                    if (existing == null) {
                        LOG.info("DICOMWeb cache adding: {}, {}", acquisitionUID, seriesInstanceUID);
                        LOG.info("DICOMWeb cache, size: {}", acquisitionUIDToSeriesInstanceUIDCache.size());
                    }
                }
            }
        }
        return seriesInstanceUID;
    }

    public String findSeriesInstanceUID(DatasetAcquisition acquisition) {
        String seriesInstanceUIDDb = acquisition.getSeriesInstanceUID();
        if (seriesInstanceUIDDb != null && !seriesInstanceUIDDb.isEmpty())
            return seriesInstanceUIDDb;
        if (acquisition instanceof MrDatasetAcquisition
                || acquisition instanceof CtDatasetAcquisition
                || acquisition instanceof PetDatasetAcquisition
                || acquisition instanceof XaDatasetAcquisition
                || acquisition instanceof GenericDatasetAcquisition) {
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

    private String findSeriesInstanceUID(String path) {
        Pattern p = Pattern.compile(WADO_URI_SERIES_UID_OBJECT_UID);
        Matcher m = p.matcher(path);
        while (m.find()) {
            return m.group(1);
        }
        p = Pattern.compile(WADO_RS_SERIES_UID_INSTANCES_UID);
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
