package org.shanoir.uploader.nominativeData;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class manages NominativeDataImportJob.
 * @author lvallet
 *
 */
public class NominativeDataImportJobManager {

    private static final Logger LOG = LoggerFactory.getLogger(NominativeDataImportJobManager.class);

    private File nominativeDataJobFile;

    /**
     * Lock for synchronizing access to the import job file.
     */
    private static final Map<String, ReentrantLock> FILE_LOCKS = new ConcurrentHashMap<>();

    /**
     * Initialize ImportJobManager empty and reset importJobFile
     * with method setImportJobFile.
     */
    public NominativeDataImportJobManager() {
    }

    /**
     * Initialize MoninativeDataImportJobManager with current nominative data folder path.
     * @param importFolderPath
     */
    public NominativeDataImportJobManager(final String importFolderPath) {
        this.nominativeDataJobFile = new File(
            importFolderPath
            + File.separatorChar
            + ShUpConfig.IMPORT_JOB_JSON);
        LOG.debug("ImportJobManager initialized with file: " + this.nominativeDataJobFile.getAbsolutePath());
    }

    /**
     * Initialize ImportJobManager with ImportJob file.
     * @param importJobFile
     */
    public NominativeDataImportJobManager(final File importJobFile) {
        this.nominativeDataJobFile = importJobFile;
        LOG.debug("ImportJobManager initialized with file: " + this.nominativeDataJobFile.getAbsolutePath());
    }

    private ReentrantLock getLock() {
        return FILE_LOCKS.computeIfAbsent(nominativeDataJobFile.getAbsolutePath(), k -> new ReentrantLock());
    }

    public File getImportJobFile() {
        return nominativeDataJobFile;
    }

    public void setImportJobFile(File importJobFile) {
        this.nominativeDataJobFile = importJobFile;
    }

    public ImportJob readImportJob() {
        ReentrantLock lock = getLock();
        lock.lock();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(this.nominativeDataJobFile, ImportJob.class);
        } catch (IOException e) {
            LOG.error("Error during import-job.json reading: {}", e.getMessage(), e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void writeImportJob(ImportJob importJob) {
        ReentrantLock lock = getLock();
        lock.lock();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.nominativeDataJobFile, importJob);
        } catch (IOException e) {
            LOG.error("Error during import-job.json writing: {}", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

}
