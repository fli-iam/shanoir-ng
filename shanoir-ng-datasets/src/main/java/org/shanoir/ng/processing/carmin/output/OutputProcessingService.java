package org.shanoir.ng.processing.carmin.output;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OutputProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(OutputProcessingService.class);

    @Value("${vip.upload-folder}")
    private String importDir;

    @Autowired
    private List<OutputProcessing> outputProcessings;

    /**
     *
     * Process the result of the given processing
     *
     * @param processing
     * @throws OutputProcessingException
     */
    public void process(CarminDatasetProcessing processing) throws OutputProcessingException {

        final File userImportDir = new File(
                this.importDir + File.separator +
                        processing.getResultsLocation());

        for (File archive : this.getArchivesToProcess(userImportDir)) {

            File cacheFolder = new File(userImportDir.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(archive.getName()));

            List<File> outputFiles = this.extractTarIntoCache(archive, cacheFolder);

            for (OutputProcessing outputProcessing : outputProcessings) {
                if (outputProcessing.canProcess(processing)) {
                    LOG.info("Processing result file [{}] with [{}] output processing", archive.getAbsolutePath(), outputProcessing.getClass().getSimpleName());
                    outputProcessing.manageTarGzResult(outputFiles, userImportDir, processing);
                }
            }

            this.deleteCache(cacheFolder);

        }
    }

    private List<File> getArchivesToProcess(File userImportDir) throws OutputProcessingException {

        LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

        final PathMatcher matcher = userImportDir.toPath().getFileSystem()
                .getPathMatcher("glob:**/*.{tgz,tar.gz}");

        try (Stream<Path> stream = Files.list(userImportDir.toPath())) {

            return  stream.filter(matcher::matches).map(Path::toFile).collect(Collectors.toList());

        } catch (IOException e) {
            throw new OutputProcessingException("I/O error while listing files in import directory", e);
        }

    }

    private List<File> extractTarIntoCache(File archive, File cacheFolder) throws OutputProcessingException {

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
            throw new OutputProcessingException("I/O error while extracting files from result archive [" + archive.getAbsolutePath() + "]", e);
        }

        if(outputFiles.isEmpty()) {
            throw new OutputProcessingException("No processable file found in result archive [" + archive.getAbsolutePath() + "]", null);
        }

        return outputFiles;

    }

    private void deleteCache(File cacheFolder) {

        try {
            Files.walk(cacheFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            LOG.error("I/O error while deleting cache dir [{}]", cacheFolder.getAbsolutePath());
        }
    }

}
