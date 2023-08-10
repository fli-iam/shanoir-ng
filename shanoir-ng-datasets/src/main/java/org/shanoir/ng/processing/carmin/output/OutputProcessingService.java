package org.shanoir.ng.processing.carmin.output;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
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

    public void process(CarminDatasetProcessing processing) throws OutputProcessingException {

        final File userImportDir = new File(
                this.importDir + File.separator +
                        processing.getResultsLocation());

        for (File file : this.getFilesToProcess(userImportDir)) {
            for (OutputProcessing outputProcessing : outputProcessings) {
                if (outputProcessing.canProcess(processing)) {
                    LOG.info("Processing result file [{}]...", file.getAbsolutePath());
                    outputProcessing.manageTarGzResult(file, userImportDir, processing);
                }
            }

        }
    }

    private List<File> getFilesToProcess(File userImportDir) throws OutputProcessingException {

        LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

        final PathMatcher matcher = userImportDir.toPath().getFileSystem()
                .getPathMatcher("glob:**/*.{tgz,tar.gz}");

        try (Stream<Path> stream = Files.list(userImportDir.toPath())) {

            return  stream.filter(matcher::matches).map(Path::toFile).collect(Collectors.toList());

        } catch (IOException e) {
            throw new OutputProcessingException("I/O error while listing files in import directory", e);
        }

    }

}
