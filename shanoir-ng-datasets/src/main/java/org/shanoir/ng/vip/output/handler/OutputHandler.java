package org.shanoir.ng.vip.output.handler;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

public abstract class OutputHandler {
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
}
