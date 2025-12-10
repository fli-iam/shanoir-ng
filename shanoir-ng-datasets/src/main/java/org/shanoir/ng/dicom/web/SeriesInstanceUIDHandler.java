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

import java.util.List;
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
import org.shanoir.ng.utils.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class SeriesInstanceUIDHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SeriesInstanceUIDHandler.class);

    private static final String WADO_URI_SERIES_UID_OBJECT_UID = "seriesUID=(.*?)\\&objectUID";

    private static final String WADO_RS_SERIES_UID_INSTANCES_UID = "/series/(.*?)/instances/";

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

    @LogExecutionTime
    public String findSeriesInstanceUID(DatasetAcquisition acquisition) {
        String seriesInstanceUIDDb = acquisition.getSeriesInstanceUID();
        if (seriesInstanceUIDDb != null && !seriesInstanceUIDDb.isEmpty())
            return seriesInstanceUIDDb;
        if (acquisition instanceof MrDatasetAcquisition
                || acquisition instanceof CtDatasetAcquisition
                || acquisition instanceof PetDatasetAcquisition
                || acquisition instanceof XaDatasetAcquisition
                || acquisition instanceof GenericDatasetAcquisition) {
            List<Dataset> datasets = acquisitionService.getDatasets(acquisition);
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
