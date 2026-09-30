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

package org.shanoir.ng.vip.processingResource.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.download.DownloadAbortedException;
import org.shanoir.ng.download.PacsTransferStats;
import org.shanoir.ng.download.TarWriter;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

@Controller
public class ProcessingResourceApiController implements ProcessingResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingResourceApiController.class);

    @Qualifier("datasetDownloaderServiceImpl")
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private DatasetRepository  datasetRepository;

    @Value("${vip.download.parallel-count:4}")
    private int parallelCount;

    /** Caps the download thread count, to avoid PACS saturation. */
    private Semaphore dlPermits;

    @Value("${vip.download.db-permits:60}")
    private int dbPermitCount;

    /** Caps the connections VIP downloads take from the Hikari pool, leaving the rest for other traffic. */
    private Semaphore dbPermits;

    @PostConstruct
    void initSemaphore() {
        dbPermits = new Semaphore(dbPermitCount, true);
        dlPermits = new Semaphore(parallelCount, true);
    }

    @Override
    public ResponseEntity<?> getPath(String completePath, String action, final String format, Long converterId, String sorting, HttpServletResponse response)
            throws IOException, RestServiceException, EntityNotFoundException {
        LOG.debug("completePath: {}, action: {}, format: {}, converterId: {}, response: {}", completePath, action, format, converterId, response);
        // TODO implement those actions
        switch (action) {
            case "exists":
            case "list":
            case "md5":
            case "properties":
                return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
            case "content":
                List<Dataset> datasets = findDatasetsWithSemaphore(completePath);
                if (datasets.isEmpty()) {
                    LOG.error("No dataset found for resource id [{}]", completePath);
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }

                PacsTransferStats pacsStats = PacsTransferStats.start();
                try {
                    dlPermits.acquire();
                    datasetDownloaderService.massiveDownload(format, datasets, response, true, converterId, true, sorting, new TarWriter(response.getOutputStream()), true);
                    dlPermits.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RestServiceException(new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "Interrupted while waiting for a download thread."));
                } finally {
                    PacsTransferStats.stop();
                    LOG.info("VIP download [{}]: {} PACS responses, average PACS response time: {} ms, bytes received: {}, flow rate: {} MB/s, "
                            + "waiting for PACS: {} ms, zip writing: {} ms (compression: {} ms, network: {} ms)",
                            completePath, pacsStats.getResponseCount(),
                            String.format("%.1f", pacsStats.getAverageResponseMillis()),
                            pacsStats.getTotalBytes(),
                            String.format("%.2f", pacsStats.getBytesPerSecond() / 1_000_000),
                            pacsStats.getWaitMillis(),
                            pacsStats.getZipMillis(),
                            Math.max(0, pacsStats.getZipMillis() - pacsStats.getNetworkMillis()),
                            pacsStats.getNetworkMillis());
                }
                return new ResponseEntity<Void>(HttpStatus.OK);
            default:
                ErrorModel errorModel = new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Action " + action + " not supported");
                throw new RestServiceException(errorModel);
        }
    }

    private List<Dataset> findDatasetsWithSemaphore(String resourceId) throws RestServiceException {
        try {
            dbPermits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestServiceException(new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Interrupted while waiting for a database connection"));
        }
        try {
            return datasetRepository.findByResourceIdWithDatasetFiles(resourceId);
        } finally {
            dbPermits.release();
        }
    }

    /**
     * Rethrown on purpose: checked before GlobalExceptionHandler, which would otherwise append a JSON
     * error body to the already committed zip and end the response normally. Reaching Tomcat, the
     * exception makes it cut the connection, so the client sees a failed transfer.
     */
    @ExceptionHandler(DownloadAbortedException.class)
    public void rethrowDownloadAborted(DownloadAbortedException e) {
        throw e;
    }
}
