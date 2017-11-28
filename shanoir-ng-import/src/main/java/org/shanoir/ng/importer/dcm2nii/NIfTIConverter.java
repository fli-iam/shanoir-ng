package org.shanoir.ng.importer.dcm2nii;

import java.io.File;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NIfTIConverter {

	private static final Logger LOG = LoggerFactory.getLogger(NIfTIConverter.class);

	private ObjectMapper mapper = new ObjectMapper();
	
	private JsonNode dicomDirJson;
	
	private File unzipFolderFile;
	
	public NIfTIConverter(JsonNode dicomDirJson, File unzipFolderFile) {
		this.dicomDirJson = dicomDirJson;
		this.unzipFolderFile = unzipFolderFile;
	}
	
	public void prepareConversion() throws RestServiceException {
		File seriesFolderFile = new File(unzipFolderFile.getAbsolutePath() + File.separator + "/SERIES");
		if(!seriesFolderFile.exists()) {
			seriesFolderFile.mkdirs();
		} else {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating series folder: folder already exists.", null));
		}
		// patient level
		JsonNode patients = dicomDirJson.path("patients");
		if (patients.isArray()) {
			for (JsonNode patient : patients) {
				// study level
				JsonNode studies = patient.path("studies");
				if (studies.isArray()) {
					for (JsonNode study : studies) {
						// serie level
						JsonNode series = study.path("series");
						if (series.isArray()) {
							for (JsonNode serie : series) {
								createSerieIDFolder(seriesFolderFile, serie);
							}
						}
					}
				}
			}
		}	
	}

	/**
	 * @param seriesFolderFile
	 * @param serie
	 * @throws RestServiceException
	 */
	private void createSerieIDFolder(File seriesFolderFile, JsonNode serie) throws RestServiceException {
		String serieID = serie.path("seriesInstanceUID").asText();
		File serieIDFolderFile = new File(seriesFolderFile.getAbsolutePath() + File.separator + serieID);
		if(!serieIDFolderFile.exists()) {
			serieIDFolderFile.mkdirs();
		} else {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating serie id folder: folder already exists.", null));
		}
		JsonNode images = serie.path("images");
		moveFiles(serieIDFolderFile, images);
		JsonNode nonImages = serie.path("nonImages");
		moveFiles(serieIDFolderFile, nonImages);
	}

	/**
	 * @param serieIDFolderFile
	 * @param images
	 * @throws RestServiceException
	 */
	private void moveFiles(File serieIDFolderFile, JsonNode images) throws RestServiceException {
		if (images.isArray()) {
			for (JsonNode node : images) {
				String fileName = node.asText();
				File oldFile = new File(unzipFolderFile.getParentFile().getAbsolutePath() + File.separator + fileName);
				if (oldFile.exists()) {
					File newFile = new File(serieIDFolderFile.getAbsolutePath() + File.separator + oldFile.getName());
					oldFile.renameTo(newFile);
				} else {
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating serie id folder: file to copy does not exist.", null));
				}
			}
		}
	}
		
}
