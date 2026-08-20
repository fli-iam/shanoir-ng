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

package org.shanoir.ng.vip.output.service;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.handler.DefaultHandler;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.handler.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OutputServiceImpl implements OutputService {

    private static final Logger LOG = LoggerFactory.getLogger(OutputServiceImpl.class);

    @Value("${vip-data-folder:'SECRET'}")
    private String importDir;

    private static final String VIP_UPLOAD_FOLDER = "output_uploads";

    @Autowired
    private List<OutputHandler> outputHandlers;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Autowired
    @Lazy
    private DatasetProcessingRepository processingRepository;

    @Transactional(readOnly = true)
    public void process(ExecutionMonitoring monitoring) throws ResultHandlerException, EntityNotFoundException {
        process(monitoring, outputHandlers);
    }

    /**
     *
     * Process the result of the given execution with the given output handlers
     *
     * @param monitoring
     * @param selectedOutputHandlers
     * @throws ResultHandlerException
     */
    protected void process(ExecutionMonitoring monitoring, List<OutputHandler> selectedOutputHandlers) throws ResultHandlerException {
        List<File> outputFiles;

        File userImportDir = new File(this.importDir + File.separator + VIP_UPLOAD_FOLDER + File.separator + monitoring.getResultsLocation());
        if (userImportDir.exists()) {
            for (File archive : getArchivesToProcess(userImportDir)) {
                LOG.info("Processing archive : " + archive.getAbsolutePath());
                File cacheFolder = new File(userImportDir.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(archive.getName()));
                try {
                    String resourceId = archive.getName().split("\\+")[1];
                    outputFiles = extractTarIntoCache(archive, cacheFolder);

                    for (OutputHandler outputHandler : selectedOutputHandlers) {
                        if (outputHandler.canProcess(monitoring.getName())) {
                            LOG.info("Processing result file [{}] with [{}] output processing", archive.getAbsolutePath(), outputHandler.getClass().getSimpleName());
                            outputHandler.manageTarGzResult(outputFiles, userImportDir, monitoring, resourceId);
                        }
                    }
                } finally {
                    System.gc();
                    deleteDirectory(cacheFolder);
                }
            }
        }
        if (selectedOutputHandlers.stream().anyMatch(handler -> handler instanceof DefaultHandler) && monitoring.getPipelineIdentifier().endsWith("post_processing")) {
            LOG.info("Output processing for monitoring " +  monitoring.getId() + " finished. Output kept for post-processing.");
        } else {
            deleteDirectory(userImportDir);
            processingResourceRepository.deleteByProcessingId(monitoring.getId());
        }
    }

    @Transactional
    public void postProcess(Long processingId, OutputHandler outputHandler) throws EntityNotFoundException {
        DatasetProcessing processing = processingRepository.findById(processingId)
                .orElseThrow(() -> new EntityNotFoundException(DatasetProcessing.class, processingId));
        List<File> outputFiles = processing.getOutputDatasets().stream().map(this::toOutputFile).toList();
        if (!outputFiles.isEmpty()) {
            LOG.info("Processing result file [{}] with [{}] output processing", outputFiles.getFirst().getAbsolutePath(), outputHandler.getClass().getSimpleName());
            outputHandler.manageDelayedOutput(outputFiles, processing);
        }
    }

    /**
     * Get archives to process from vip output directory
     */
    private List<File> getArchivesToProcess(File userImportDir) throws ResultHandlerException {
        LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

        final PathMatcher matcher = userImportDir.toPath().getFileSystem().getPathMatcher("glob:**/*.{tgz,tar.gz}");

        try (Stream<Path> stream = Files.list(userImportDir.toPath())) {
            return  stream.filter(matcher::matches).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new ResultHandlerException("I/O error while listing files in import directory", e);
        }
    }

    /**
     * Extract files from .tar archive and store them in cacheFolder
     */
    private List<File> extractTarIntoCache(File archive, File cacheFolder) throws ResultHandlerException {
        List<File> outputFiles = new ArrayList<>();

        try (TarArchiveInputStream fin = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(archive)))) {
            TarArchiveEntry entry;

            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
            }

            while ((entry = fin.getNextTarEntry()) != null) {
                String parsedEntry = entry.getName();

                if (entry.isDirectory()) {
                    continue;
                }

                File currentFile = new File(cacheFolder, Paths.get(parsedEntry).getFileName().toString());
                IOUtils.copy(fin, Files.newOutputStream(currentFile.toPath()));
                outputFiles.add(currentFile);
            }

        } catch (IOException e) {
            throw new ResultHandlerException("I/O error while extracting files from result archive [" + archive.getAbsolutePath() + "]", e);
        }

        if (outputFiles.isEmpty()) {
            throw new ResultHandlerException("No processable file found in result archive [" + archive.getAbsolutePath() + "]", null);
        }
        return outputFiles;
    }

    /**
     * Remove directory given as parameter
     */
    private void deleteDirectory(File directory) {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            LOG.error("I/O error while deleting cache dir [{}]", directory.getAbsolutePath());
            LOG.error(e.getCause().getMessage(), e);
        }
    }

    private File toOutputFile(Dataset ds) {
        String path = ds.getDatasetExpressions().getFirst()
                .getDatasetFiles().getFirst()
                .getPath();
        try {
            return new File(new URI(path));
        } catch (URISyntaxException e) {
            throw new UncheckedIOException("Invalid URI for dataset path: " + path, new IOException(e));
        }
    }
}
