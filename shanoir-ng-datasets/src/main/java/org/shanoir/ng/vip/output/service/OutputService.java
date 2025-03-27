package org.shanoir.ng.vip.output.service;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.handler.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OutputService {

    private static final Logger LOG = LoggerFactory.getLogger(OutputService.class);

    @Value("${vip.upload-folder}")
    private String importDir;

    @Autowired
    private List<OutputHandler> outputHandlers;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    /**
     *
     * Process the result of the given execution
     *
     * @param monitoring
     * @throws ResultHandlerException
     */
    public void process(ExecutionMonitoring monitoring) throws ResultHandlerException, EntityNotFoundException {
        //final File userImportDir = new File(this.importDir + File.separator + monitoring.getResultsLocation());
        String userImportPath = this.importDir + File.separator + monitoring.getResultsLocation();
        File userImportDir = new File(userImportPath.substring(0, userImportPath.lastIndexOf("/") + 1));

        for (File archive : this.getArchivesToProcess(userImportDir)) {
            File cacheFolder = new File(userImportDir.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(archive.getName()));
            List<File> outputFiles = this.extractTarIntoCache(archive, cacheFolder);

            for (OutputHandler outputHandler : outputHandlers) {
                if (outputHandler.canProcess(monitoring)) {
                    LOG.info("Processing result file [{}] with [{}] output processing", archive.getAbsolutePath(), outputHandler.getClass().getSimpleName());
                    outputHandler.manageTarGzResult(outputFiles, userImportDir, monitoring);
                }
            }
            this.deleteCache(cacheFolder);
        }

        // Remove processed datasets from current execution monitoring
        monitoring.setInputDatasets(Collections.emptyList());
        datasetProcessingService.update(monitoring);
        processingResourceRepository.deleteByProcessingId(monitoring.getId());
    }

    private List<File> getArchivesToProcess(File userImportDir) throws ResultHandlerException {
        LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

        final PathMatcher matcher = userImportDir.toPath().getFileSystem().getPathMatcher("glob:**/*.{tgz,tar.gz}");

        try (Stream<Path> stream = Files.list(userImportDir.toPath())) {
            return  stream.filter(matcher::matches).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new ResultHandlerException("I/O error while listing files in import directory", e);
        }
    }

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

        if(outputFiles.isEmpty()) {
            throw new ResultHandlerException("No processable file found in result archive [" + archive.getAbsolutePath() + "]", null);
        }
        return outputFiles;
    }

    private void deleteCache(File cacheFolder) {
        try {
            FileUtils.deleteDirectory(cacheFolder);
        } catch (IOException e) {
            LOG.error("I/O error while deleting cache dir [{}]", cacheFolder.getAbsolutePath());
        }
    }
}
