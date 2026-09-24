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

import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.download.PacsTransferStats;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class ProcessingResourceApiController implements ProcessingResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingResourceApiController.class);

    @Qualifier("datasetDownloaderServiceImpl")
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Autowired
    private DatasetRepository  datasetRepository;

    @Autowired
    private HikariDataSource dataSource;

    private static final AtomicInteger NUMBER_OF_DOWNLOAD = new AtomicInteger(0);

    @Value("${vip.download.max-concurrent:60}")
    private int maxConcurrentDownloads;

    @Value("${vip.download.permit-wait-seconds:20}")
    private long permitWaitSeconds;

    private Semaphore downloadPermits;

    @PostConstruct
    void initDownloadPermits() {
        downloadPermits = new Semaphore(maxConcurrentDownloads, true);
    }

    @Override
    public ResponseEntity<?> getPath(String completePath, String action, final String format, Long converterId, String sorting, HttpServletResponse response)
            throws IOException, RestServiceException, EntityNotFoundException {
        LOG.debug("completePath: {}, action: {}, format: {}, converterId: {}, response: {}", completePath, action, format, converterId, response);
        // TODO implement those actions
        try {
            switch (action) {
                case "exists":
                case "list":
                case "md5":
                case "properties":
                    return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
                case "content":
                    if (!acquireDownloadPermit(completePath)) {
                        return new ResponseEntity<Void>(HttpStatus.SERVICE_UNAVAILABLE);
                    }
                    try {
                        List<Dataset> datasets = datasetRepository.findByResourceIdWithDatasetFiles(completePath);
                        NUMBER_OF_DOWNLOAD.incrementAndGet();
                        if (datasets.isEmpty()) {
                            LOG.error("No dataset found for resource id [{}]", completePath);
                            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                        }

                        PacsTransferStats pacsStats = PacsTransferStats.start();
                        try {
                            datasetDownloaderService.massiveDownload(format, datasets, response, true, converterId, true, sorting);
                        } finally {
                            PacsTransferStats.stop();
                            LOG.info("VIP download [{}]: {} PACS responses, average PACS response time: {} ms, bytes received: {}, flow rate: {} MB/s",
                                    completePath, pacsStats.getResponseCount(),
                                    String.format("%.1f", pacsStats.getAverageResponseMillis()),
                                    pacsStats.getTotalBytes(),
                                    String.format("%.2f", pacsStats.getBytesPerSecond() / 1_000_000));
                        }
                        return new ResponseEntity<Void>(HttpStatus.OK);
                    } finally {
                        downloadPermits.release();
                    }
                default:
                    ErrorModel errorModel = new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Action " + action + " not supported");
                    throw new RestServiceException(errorModel);
            }
        } catch (Exception e) {
            LOG.error("Error while VIP downloading data", e);
            throw e;
        }
    }

    private boolean acquireDownloadPermit(String completePath) {
        try {
            if (downloadPermits.tryAcquire(permitWaitSeconds, TimeUnit.SECONDS)) {
                return true;
            }
            LOG.warn("VIP download [{}] rejected: no download slot freed within {} seconds ({} downloads max)",
                    completePath, permitWaitSeconds, maxConcurrentDownloads);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("VIP download [{}] interrupted while waiting for a download slot", completePath);
        }
        return false;
    }
}
