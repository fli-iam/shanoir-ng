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
import java.util.Collections;
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
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.rt.RtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeriesInstanceUIDHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SeriesInstanceUIDHandler.class);

    private static final String WADO_URI_SERIES_UID_OBJECT_UID = "seriesUID=(.*?)\\&objectUID";

    private static final String WADO_RS_SERIES_UID_INSTANCES_UID = "/series/(.*?)/instances/";

    private static final String DICOM_TAG_SERIES_INSTANCE_UID = "0020000E";

    private static final String DICOM_TAG_RETRIEVE_URL = "00081190";

    private static final String VALUE = "Value";

    public static final String PREFIX = UIDGeneration.ROOT + ".";

    /**
     * Datasets, that live in their own DICOM series within an acquisition,
     * e.g. SEG or SR objects created by the viewer, are exposed with a
     * dataset-level virtual UID: ROOT + ".0." + dataset id. The "0" segment
     * distinguishes them from acquisitionUIDs, as ROOT + ".0" can never
     * start an acquisitionUID (no acquisition with id 0 exists) and
     * "0.<datasetId>" never parses as a Long.
     */
    public static final String DATASET_PREFIX = PREFIX + "0.";

    @Autowired
    private DatasetAcquisitionService acquisitionService;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;

    @Autowired
    private DatasetAcquisitionRepository acquisitionRepository;

    /**
     * The viewer requests the metadata of each serie of an examination
     * separately, so the series-to-virtual-UID map of the examination is
     * cached shortly to avoid rebuilding it from the database per serie.
     * The short TTL keeps new series visible in time, e.g. a SEG object
     * stored by the viewer or a fresh processing output.
     */
    private static final long EXAMINATION_SERIES_TTL_MS = 30000;

    private record ExaminationSeriesCacheEntry(long creationTime, Map<String, String> seriesToVirtualUIDs) {
    }

    private ConcurrentHashMap<String, String> virtualUIDToSeriesInstanceUIDCache;

    private ConcurrentHashMap<Long, ExaminationSeriesCacheEntry> examinationToSeriesVirtualUIDsCache;

    @PostConstruct
    public void init() {
        virtualUIDToSeriesInstanceUIDCache = new ConcurrentHashMap<String, String>(1000);
        examinationToSeriesVirtualUIDsCache = new ConcurrentHashMap<Long, ExaminationSeriesCacheEntry>(1000);
        LOG.info("DICOMWeb caches created: virtualUIDToSeriesInstanceUIDCache, examinationToSeriesVirtualUIDsCache");
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Paris")
    public void clearVirtualUIDCaches() {
        virtualUIDToSeriesInstanceUIDCache.clear();
        examinationToSeriesVirtualUIDsCache.clear();
        LOG.info("DICOMWeb caches cleared: virtualUIDToSeriesInstanceUIDCache, examinationToSeriesVirtualUIDsCache");
    }

    public String resolveSeriesInstanceUID(String seriesOrVirtualUID) {
        if (isAcquisitionUID(seriesOrVirtualUID)) {
            return findSeriesInstanceUIDFromCacheOrDatabase(seriesOrVirtualUID);
        }
        if (isDatasetUID(seriesOrVirtualUID)) {
            return findSeriesInstanceUIDOfDatasetFromCacheOrDatabase(seriesOrVirtualUID);
        }
        return seriesOrVirtualUID;
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

    public boolean isDatasetUID(String uid) {
        if (uid == null || !uid.startsWith(DATASET_PREFIX)) {
            return false;
        }
        try {
            Long.parseLong(uid.substring(DATASET_PREFIX.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Maps the real SeriesInstanceUID of each DICOM series of an examination
     * to its virtual UID: the acquisitionUID for the primary series of each
     * acquisition and the datasetUID for each dataset within an acquisition,
     * that lives in its own DICOM series, e.g. SEG or SR objects.
     *
     * @param examinationId
     * @return
     */
    public Map<String, String> findSeriesToVirtualUIDs(Long examinationId) {
        ExaminationSeriesCacheEntry entry = examinationToSeriesVirtualUIDsCache.get(examinationId);
        if (entry != null && System.currentTimeMillis() - entry.creationTime() < EXAMINATION_SERIES_TTL_MS) {
            return entry.seriesToVirtualUIDs();
        }
        List<DatasetAcquisition> acquisitions = acquisitionRepository.findByExaminationId(examinationId);
        Map<String, String> seriesToVirtualUIDs = Collections.unmodifiableMap(buildSeriesToVirtualUIDs(acquisitions));
        examinationToSeriesVirtualUIDsCache.put(examinationId,
                new ExaminationSeriesCacheEntry(System.currentTimeMillis(), seriesToVirtualUIDs));
        return seriesToVirtualUIDs;
    }

    public Map<String, String> findSeriesToVirtualUIDsOfAcquisition(Long acquisitionId) {
        DatasetAcquisition acquisition = acquisitionService.findById(acquisitionId);
        if (acquisition == null) {
            return Collections.emptyMap();
        }
        return buildSeriesToVirtualUIDs(List.of(acquisition));
    }

    private Map<String, String> buildSeriesToVirtualUIDs(List<DatasetAcquisition> acquisitions) {
        Map<String, String> seriesToVirtualUIDs = new LinkedHashMap<>();
        List<Long> datasetIds = new ArrayList<>();
        for (DatasetAcquisition acquisition : acquisitions) {
            addSeriesOfAcquisition(acquisition, seriesToVirtualUIDs);
            for (Dataset dataset : acquisition.getDatasets()) {
                datasetIds.add(dataset.getId());
            }
        }
        // outputs of processings, that used these datasets as input,
        // e.g. segmentations computed by a pipeline, are exposed as
        // dataset series as well (DICOM-in-PACS outputs only: NIfTI
        // outputs have no series and are skipped by the UID extraction)
        if (!datasetIds.isEmpty()) {
            for (DatasetProcessing processing : datasetProcessingRepository.findAllByInputDatasets_IdIn(datasetIds)) {
                for (Dataset outputDataset : processing.getOutputDatasets()) {
                    addSeriesOfDataset(outputDataset, seriesToVirtualUIDs);
                }
            }
        }
        return seriesToVirtualUIDs;
    }

    private void addSeriesOfAcquisition(DatasetAcquisition acquisition, Map<String, String> seriesToVirtualUIDs) {
        String acquisitionUID = PREFIX + acquisition.getId();
        String primarySeriesInstanceUID = virtualUIDToSeriesInstanceUIDCache.get(acquisitionUID);
        if (primarySeriesInstanceUID == null) {
            primarySeriesInstanceUID = findSeriesInstanceUID(acquisition);
            if (primarySeriesInstanceUID != null) {
                virtualUIDToSeriesInstanceUIDCache.putIfAbsent(acquisitionUID, primarySeriesInstanceUID);
            }
        }
        if (primarySeriesInstanceUID != null) {
            seriesToVirtualUIDs.put(primarySeriesInstanceUID, acquisitionUID);
        }
        // deliberately type-agnostic (no instanceof on the dataset): SEG
        // datasets can be stored as Generic in the database, the series
        // extracted from the PACS file path is the only reliable criterion
        for (Dataset dataset : acquisition.getDatasets()) {
            addSeriesOfDataset(dataset, seriesToVirtualUIDs);
        }
    }

    private void addSeriesOfDataset(Dataset dataset, Map<String, String> seriesToVirtualUIDs) {
        String datasetUID = DATASET_PREFIX + dataset.getId();
        String seriesInstanceUID = virtualUIDToSeriesInstanceUIDCache.get(datasetUID);
        if (seriesInstanceUID == null) {
            seriesInstanceUID = findSeriesInstanceUID(dataset);
            if (seriesInstanceUID != null) {
                virtualUIDToSeriesInstanceUIDCache.putIfAbsent(datasetUID, seriesInstanceUID);
            }
        }
        if (seriesInstanceUID != null && !seriesToVirtualUIDs.containsKey(seriesInstanceUID)) {
            seriesToVirtualUIDs.put(seriesInstanceUID, datasetUID);
        }
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
                values.set(i, TextNode.valueOf(replaced));
            }
        }
    }

    @Transactional(readOnly = true)
    public String findSeriesInstanceUIDFromCacheOrDatabase(String acquisitionUID) {
        String seriesInstanceUID = virtualUIDToSeriesInstanceUIDCache.get(acquisitionUID);
        if (seriesInstanceUID == null) {
            Long acquisitionId = extractAcquisitionId(acquisitionUID);
            DatasetAcquisition acquisition = acquisitionService.findById(acquisitionId);
            if (acquisition != null) {
                seriesInstanceUID = findSeriesInstanceUID(acquisition);
                if (seriesInstanceUID != null) {
                    String existing = virtualUIDToSeriesInstanceUIDCache.putIfAbsent(acquisitionUID, seriesInstanceUID);
                    if (existing == null) {
                        LOG.info("DICOMWeb cache adding: {}, {}", acquisitionUID, seriesInstanceUID);
                        LOG.info("DICOMWeb cache, size: {}", virtualUIDToSeriesInstanceUIDCache.size());
                    }
                }
            }
        }
        return seriesInstanceUID;
    }

    public String findSeriesInstanceUIDOfDatasetFromCacheOrDatabase(String datasetUID) {
        String seriesInstanceUID = virtualUIDToSeriesInstanceUIDCache.get(datasetUID);
        if (seriesInstanceUID == null) {
            Long datasetId = extractDatasetId(datasetUID);
            Dataset dataset = datasetRepository.findById(datasetId).orElse(null);
            if (dataset != null) {
                seriesInstanceUID = findSeriesInstanceUID(dataset);
                if (seriesInstanceUID != null) {
                    String existing = virtualUIDToSeriesInstanceUIDCache.putIfAbsent(datasetUID, seriesInstanceUID);
                    if (existing == null) {
                        LOG.info("DICOMWeb cache adding: {}, {}", datasetUID, seriesInstanceUID);
                        LOG.info("DICOMWeb cache, size: {}", virtualUIDToSeriesInstanceUIDCache.size());
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
                || acquisition instanceof RtDatasetAcquisition
                || acquisition instanceof GenericDatasetAcquisition) {
            List<Dataset> datasets = acquisition.getDatasets();
            if (!datasets.isEmpty()) {
                return findSeriesInstanceUID(datasets.get(0));
            }
        }
        return null;
    }

    public String findSeriesInstanceUID(Dataset dataset) {
        List<DatasetExpression> expressions = dataset.getDatasetExpressions();
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

    public Long extractDatasetId(String datasetUID) {
        String datasetUIDWithoutPrefix = datasetUID.substring(DATASET_PREFIX.length());
        Long id = Long.parseLong(datasetUIDWithoutPrefix);
        return id;
    }

}
