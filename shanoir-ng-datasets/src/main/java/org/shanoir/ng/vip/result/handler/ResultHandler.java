package org.shanoir.ng.vip.result.handler;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.processingResource.service.ProcessingResourceService;
import org.shanoir.ng.vip.result.exception.ResultHandlerException;
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
public abstract class ResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ResultHandler.class);

    @Value("${vip.upload-folder}")
    private String importDir;

    @Autowired
    private List<ResultHandler> resultHandlers;
    @Autowired
    private DatasetProcessingService datasetProcessingService;
    @Autowired
    private ProcessingResourceService processingResourceService;

    /**
     * Return true if the implementation can process the result of the given processing
     *
     * @param monitoring ExecutionMonitoring
     * @return
     */
    public abstract boolean canProcess(ExecutionMonitoring monitoring) throws ResultHandlerException;

    /**
     * This methods manages the single result of an execution
     *
     * @param resultFiles  the result file as tar.gz of the processing
     * @param parentFolder the temporary arent folder in which we are currently working
     * @param processing   the corresponding dataset processing.
     */

    public abstract void manageTarGzResult(List<File> resultFiles, File parentFolder, ExecutionMonitoring processing) throws ResultHandlerException;

    /**
     *
     * Process the result of the given execution
     *
     * @param monitoring
     * @throws ResultHandlerException
     */
    public void process(ExecutionMonitoring monitoring) throws ResultHandlerException, EntityNotFoundException {

        final File userImportDir = new File(
                this.importDir + File.separator +
                        monitoring.getResultsLocation());

        for (File archive : this.getArchivesToProcess(userImportDir)) {

            File cacheFolder = new File(userImportDir.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(archive.getName()));

            List<File> outputFiles = this.extractTarIntoCache(archive, cacheFolder);

            for (ResultHandler resultHandler : resultHandlers) {
                if (resultHandler.canProcess(monitoring)) {
                    LOG.info("Processing result file [{}] with [{}] output processing", archive.getAbsolutePath(), resultHandler.getClass().getSimpleName());
                    resultHandler.manageTarGzResult(outputFiles, userImportDir, monitoring);
                }
            }

            this.deleteCache(cacheFolder);
        }
        // Remove processed datasets from current execution monitoring
        monitoring.setInputDatasets(Collections.emptyList());
        datasetProcessingService.update(monitoring);
        processingResourceService.deleteByProcessingId(monitoring.getId());
    }

    private List<File> getArchivesToProcess(File userImportDir) throws ResultHandlerException {

        LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

        final PathMatcher matcher = userImportDir.toPath().getFileSystem()
                .getPathMatcher("glob:**/*.{tgz,tar.gz}");

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
