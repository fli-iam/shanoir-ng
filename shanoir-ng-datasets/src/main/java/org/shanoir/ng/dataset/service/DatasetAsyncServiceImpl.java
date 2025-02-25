package org.shanoir.ng.dataset.service;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class DatasetAsyncServiceImpl implements DatasetAsyncService {

    @Value("${dcm4chee-arc.dicom.web}")
    private boolean dicomWeb;
    @Autowired
    private DICOMWebService dicomWebService;
    @Autowired
    ShanoirEventService eventService;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetAsyncService.class);

    @Async
    public void deleteDatasetFromDiskAndPacs(Dataset dataset) throws ShanoirException {
        if (!dicomWeb) {
            return;
        }
        Long id = dataset.getId();
        ShanoirEvent event = null;
        event = new ShanoirEvent(
                ShanoirEventType.DELETE_DATASET_EVENT,
                String.valueOf(id),
                KeycloakUtil.getTokenUserId(),
                "Delete dataset with id :" + id,
                ShanoirEvent.IN_PROGRESS,
                0f,
                null);

        eventService.publishEvent(event);

        for (DatasetExpression expression : dataset.getDatasetExpressions()) {
            boolean isDicom = DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat());
            for (DatasetFile file : expression.getDatasetFiles()) {
                    // DICOM
                if (isDicom && file.isPacs()) {
                    dicomWebService.rejectDatasetFromPacs(file.getPath());
                    float progress = event.getProgress();
                    progress += 1f / expression.getDatasetFiles().size();
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
        }
        event.setMessage("Dataset " + id + " deleted.");
        event.setProgress(1f);
        event.setStatus(ShanoirEvent.SUCCESS);
        eventService.publishEvent(event);
    }
}
