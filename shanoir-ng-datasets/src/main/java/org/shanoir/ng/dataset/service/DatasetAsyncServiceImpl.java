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

package org.shanoir.ng.dataset.service;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
public class DatasetAsyncServiceImpl implements DatasetAsyncService {

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private ShanoirEventService eventService;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetAsyncService.class);

    public void deleteDatasetFilesFromDiskAndPacs(List<DatasetFile> datasetFiles, boolean isDicom, Long datasetId) throws ShanoirException {
        deleteDatasetFilesFromDiskAndPacsAsync(datasetFiles, isDicom, datasetId);
    }

    @Override
    @Async
    public void deleteDatasetFilesFromDiskAndPacsAsync(List<DatasetFile> datasetFiles, boolean isDicom, Long datasetId) throws ShanoirException {
        ShanoirEvent event = null;
        event = new ShanoirEvent(
                ShanoirEventType.DELETE_DATASET_EVENT,
                String.valueOf(datasetId),
                KeycloakUtil.getTokenUserId(),
                "Delete dataset with id :" + datasetId,
                ShanoirEvent.IN_PROGRESS,
                0f,
                null);

        eventService.publishEvent(event);

        for (DatasetFile file : datasetFiles) {
            // DICOM
            if (isDicom && file.isPacs()) {
                dicomWebService.rejectDatasetFromPacs(file.getPath());
                float progress = event.getProgress();
                progress += 1f / datasetFiles.size();
                event.setProgress(progress);
                eventService.publishEvent(event);
                // NIfTI
            } else if (!file.isPacs()) {
                try {
                    URL url = new URL(file.getPath().replaceAll("%20", " "));
                    File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));
                    FileUtils.deleteQuietly(srcFile);
                } catch (MalformedURLException e) {
                    throw new ShanoirException("Error while deleting dataset file.", e);
                }
            }
        }

        event.setMessage("Dataset " + datasetId + " deleted.");
        event.setProgress(1f);
        event.setStatus(ShanoirEvent.SUCCESS);
        eventService.publishEvent(event);
    }
}
