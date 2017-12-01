package org.shanoir.ng.importer.dcm2nii;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The NIfTIConverter does the actual conversion of dcm to nii files.
 * To use the converter the dcm files have to be put in separate folders.
 * 
 * 1) all images for one serie are moved into /SERIES/{seriesID} and
 * 2) all images are concerning the acquisitionNumber, echoNumbers and the
 * imageOrientationPatient informations moved into /dataset{index} folders.
 * 
 * Inside each dataset folder the nii conversion is called.
 * 
 * @author mkain
 *
 */
@Service
public class NIfTIConverter {

	private static final String DOUBLE_EQUAL = "==";

	private static final String SEMI_COLON = ";";

	private static final Logger LOG = LoggerFactory.getLogger(NIfTIConverter.class);

	private static final String SERIES = "SERIES";
	
	private static final String DATASET = "dataset";
	
	@Value("${shanoir.import.series.donotseparatedatasetsinserie}")
	private String doNotSeparateDatasetsInSerie;
	
	public void prepareConversion(JsonNode dicomDirJson, File unzipFolderFile) throws RestServiceException {
		File seriesFolderFile = new File(unzipFolderFile.getAbsolutePath() + File.separator + SERIES);
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
								File serieIDFolderFile = createSerieIDFolder(seriesFolderFile, serie);
								if (checkSerieForSeparationOfDatasetsInSerie(serie)) {
									separateDatasetsInSerie(serieIDFolderFile, serie);
								}
							}
						}
					}
				}
			}
		}	
	}
	
	/**
	 * This method uses the properties string donotseparatedatasetsinserie to check
	 * if a serie is processed, where the datasets should be separated in folders or not.
	 */
	private boolean checkSerieForSeparationOfDatasetsInSerie(final JsonNode serie) {
		final String[] seriesDescriptionsToNotSeparateDatasetsInSerie = doNotSeparateDatasetsInSerie.split(SEMI_COLON);
		for (final String item : seriesDescriptionsToNotSeparateDatasetsInSerie) {
			final String tag = item.split(DOUBLE_EQUAL)[0];
			final String value = item.split(DOUBLE_EQUAL)[1];
			LOG.debug("checkDicomFromProperties : tag=" + tag + ", value=" + value);
			final String dicomValue = serie.path(tag).asText();
			String wildcard = wildcardToRegex(value);
			if (dicomValue != null && dicomValue.matches(wildcard)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This method separates the datasets from a single serie in separate
	 * folders. 3 distinct dicom tags are essential for separating the datasets:
	 * acquisition number, echo numbers and image orientation(patient).
	 * Iterate over all images and sort images in groups with the same value
	 * for ImageOrientation, EchoNumbers and AcquisitionNumber.
	 * 
	 * @param serie
	 */
	private void separateDatasetsInSerie(final File serieIDFolderFile, final JsonNode serie) {
		final HashMap<SerieToDatasetsSeparator, List<JsonNode>>
			datasetMap = new HashMap<SerieToDatasetsSeparator, List<JsonNode>>();

		JsonNode images = serie.path("images");
		if (images.isArray()) {
			for (JsonNode image : images) {
				final int acquisitionNumber = image.path("acquisitionNumber").asInt();
				// echoNumbers conversion
				JsonNode echoNumbersNode = image.path("echoNumbers");
				ArrayList<Integer> echoNumbers = new ArrayList<Integer>();
				for (JsonNode echoNumber : echoNumbersNode) {
					echoNumbers.add(echoNumber.asInt());
				}
				int[] echoNumbersIntArray = convertIntegers(echoNumbers);
				// imageOrientationPatients conversion
				JsonNode imageOrientationPatientNode = image.path("imageOrientationPatient");
				ArrayList<Double> imageOrientationPatients = new ArrayList<Double>();
				for (JsonNode imageOrientationPatient : imageOrientationPatientNode) {
					imageOrientationPatients.add(imageOrientationPatient.asDouble());
				}
				double[] imageOrientationPatientsDoubleArray = convertDoubles(imageOrientationPatients);
				SerieToDatasetsSeparator seriesToDatasetsSeparator =
						new SerieToDatasetsSeparator(acquisitionNumber, echoNumbersIntArray, imageOrientationPatientsDoubleArray);
				boolean found = false;
				for (SerieToDatasetsSeparator seriesToDatasetsComparatorIterate : datasetMap.keySet()) {
					if (seriesToDatasetsComparatorIterate.equals(seriesToDatasetsSeparator)) {
						found = true;
						seriesToDatasetsSeparator = seriesToDatasetsComparatorIterate;
						break;
					}
				}
				if (found) {
					datasetMap.get(seriesToDatasetsSeparator).add(image);
				} else {
					final List<JsonNode> imageList = new ArrayList<JsonNode>();
					imageList.add(image);
					datasetMap.put(seriesToDatasetsSeparator, imageList);
				}
			}
		}

		boolean success = true;
		// create a separate folder for each group of images
		int index = 0;
		for (final SerieToDatasetsSeparator datasets : datasetMap.keySet()) {
			// create a folder
			final File folder = new File(serieIDFolderFile.getAbsolutePath() + File.separator + DATASET + index);
			success = folder.mkdirs();
			if (!success) {
				LOG.error("deleteFolder : the creation of " + folder + " failed");
			}
			// move the files into the folder
			for (final JsonNode image : datasetMap.get(datasets)) {
				String path = image.path("path").asText();
				final File oldFile = new File(path);
				if (oldFile.exists()) {
					final File newFile = new File(folder, oldFile.getName());
					success = oldFile.renameTo(newFile);
					((ObjectNode) image).put("path", newFile.getAbsolutePath());
					if (!success) {
						LOG.error("deleteFolder : moving of " + oldFile + " failed");
					}					
				}
			}
			index++;
		}
		if (!success) {
			LOG.error("Error while converting to nifti in separateDatasetsInSerie.");
		}
	}
	
	/**
	 * @param seriesFolderFile
	 * @param serie
	 * @throws RestServiceException
	 */
	private File createSerieIDFolder(File seriesFolderFile, JsonNode serie) throws RestServiceException {
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
		return serieIDFolderFile;
	}

	/**
	 * @param serieIDFolderFile
	 * @param images
	 * @throws RestServiceException
	 */
	private void moveFiles(File serieIDFolderFile, JsonNode images) throws RestServiceException {
		if (images.isArray()) {
			for (JsonNode image : images) {
				// the path has been set in processDicomFile in DicomFileAnalyzer before
				String filePath = image.path("path").asText();
				File oldFile = new File(filePath);
				if (oldFile.exists()) {
					File newFile = new File(serieIDFolderFile.getAbsolutePath() + File.separator + oldFile.getName());
					oldFile.renameTo(newFile);
					((ObjectNode) image).put("path", newFile.getAbsolutePath());
				} else {
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating serie id folder: file to copy does not exist.", null));
				}
			}
		}
	}
	
	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}
	
	public static double[] convertDoubles(List<Double> doubles) {
		double[] ret = new double[doubles.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = doubles.get(i).doubleValue();
		}
		return ret;
	}
	
	/**
	 * Convert a String with a wildcard to a regular expression.
	 *
	 * @param wildcard the wildcard
	 *
	 * @return the string
	 */
	public static String wildcardToRegex(String wildcard) {
		StringBuffer s = new StringBuffer(wildcard.length());
		s.append('^');
		for (int i = 0, is = wildcard.length(); i < is; i++) {
			char c = wildcard.charAt(i);
			switch (c) {
			case '*':
				s.append(".*");
				break;
			case '?':
				s.append(".");
				break;
				// escape special regexp-characters
			case '(':
			case ')':
			case '[':
			case ']':
			case '$':
			case '^':
			case '.':
			case '{':
			case '}':
			case '|':
			case '\\':
				s.append("\\");
				s.append(c);
				break;
			default:
				s.append(c);
				break;
			}
		}
		s.append('$');
		return (s.toString());
	}

}
