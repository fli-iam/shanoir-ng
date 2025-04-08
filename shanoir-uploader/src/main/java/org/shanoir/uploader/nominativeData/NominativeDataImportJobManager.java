package org.shanoir.uploader.nominativeData;

import java.io.File;
import java.io.IOException;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class manages NominativeDataImportJob.
 * @author lvallet
 *
 */
public class NominativeDataImportJobManager {
	
	private static final Logger logger = LoggerFactory.getLogger(NominativeDataImportJobManager.class);

	private File nominativeDataJobFile; 
	
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
		logger.debug("ImportJobManager initialized with file: "
			+ this.nominativeDataJobFile.getAbsolutePath());
	}
	
	/**
	 * Initialize ImportJobManager with ImportJob file.
	 * @param importJobFile
	 */
	public NominativeDataImportJobManager(final File importJobFile) {
		this.nominativeDataJobFile = importJobFile;
		logger.debug("ImportJobManager initialized with file: "
			+ this.nominativeDataJobFile.getAbsolutePath());
	}

	public File getImportJobFile() {
		return nominativeDataJobFile;
	}

	public void setImportJobFile(File importJobFile) {
		this.nominativeDataJobFile = importJobFile;
	}

	public ImportJob readImportJob() {
		try {
        	ObjectMapper objectMapper = new ObjectMapper();
        	ImportJob importJob = objectMapper.readValue(this.nominativeDataJobFile, ImportJob.class);
        	return importJob;
    	} catch (IOException e) {
        	logger.error("Error during import-job.json reading: {}", e.getMessage(), e);
    	}
    	return null;
	}

	public void writeImportJob(ImportJob importJob) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.nominativeDataJobFile, importJob);
		} catch (IOException e) {
			logger.error("Error during import-job.json writing: {}", e.getMessage(), e);
		}
	}
	
}
