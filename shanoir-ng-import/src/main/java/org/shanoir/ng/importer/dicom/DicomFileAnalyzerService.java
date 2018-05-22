package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class walks through the JsonNode tree and reads all instances.
 * A FileInputStream in form of a DicomInputStream is opened to all files
 * to read additional informations, e.g. missing in the DicomDir, into the
 * JsonNode tree. This class splits the instances array nodes into two
 * different array nodes: non-images and images on using the sop instance uid.
 * Before the instances are number with their instance number and added like
 * this by DicomDirToJsonReader. DicomFileAnalyzer removes/deletes the instances
 * node and splits into two nodes: images and nonImages. As this class is reading
 * the content of each dicom file already it adds as well the informations, which
 * are later necessary to separate datasets inside each serie: acquisitionNumber,
 * echoNumbers and imageOrientationsPatient.
 * 
 * @author mkain
 *
 */
@Service
public class DicomFileAnalyzerService {

	private static final Logger LOG = LoggerFactory.getLogger(DicomFileAnalyzerService.class);
	
	private static final String DOUBLE_EQUAL = "==";

	private static final String SEMI_COLON = ";";
	
	@Value("${shanoir.import.upload.folder}")
	private String uploadFolder;
	
	@Value("${shanoir.import.series.isspectroscopy}")
	private String isSpectroscopy;

	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * This method walks through the JsonNode tree and accesses to files
	 * on using the instanceFilePath from the instances. Jackson 2.8.3
	 * at time of this development does not support JsonPath expressions,
	 * that is why 4 loops have to be used to walk through the tree.
	 * @throws FileNotFoundException 
	 */
	public void analyzeDicomFiles(JsonNode dicomDirJson,String unzipFolderFileAbsolutePath) throws FileNotFoundException {
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
								// instance level
								JsonNode instances = serie.path("instances");
								if (instances.isArray()) {
									ArrayNode nonImages = mapper.createArrayNode();
									ArrayNode images = mapper.createArrayNode();
									for (JsonNode instance : instances) {
										Iterator<Entry<String, JsonNode>> children = instance.fields();
										while (children.hasNext()) {
											Map.Entry<String, JsonNode> entry =
													(Map.Entry<String, JsonNode>) children.next();
											String instanceFilePath = entry.getValue().asText();
											File instanceFile = new File(instanceFilePath);
											if (instanceFile.exists()) {
												processDicomFile(instanceFile, serie, instances, instanceFilePath, nonImages, images, unzipFolderFileAbsolutePath);
											} else {
												throw new FileNotFoundException(
														"InstanceFilePath in DicomDir: missing file: "
																+ instanceFilePath);
											}
										}
									}
									((ObjectNode) serie).set("nonImages", nonImages);
									String nonImagesSizeStr = new Integer(nonImages.size()).toString();
									((ObjectNode) serie).put("nonImagesNumber", nonImagesSizeStr);
									((ObjectNode) serie).set("images", images);
									String imagesSizeStr = new Integer(images.size()).toString();
									((ObjectNode) serie).put("imagesNumber", imagesSizeStr);
									((ObjectNode) serie).remove("instances");
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method opens the connection to each dcm file and reads its attributes
	 * and extracts meta-data from the dicom, that will be used later.
	 * @param dicomFile
	 * @param serie
	 * @param instances
	 * @param instance
	 * @param index
	 * @param nonImages
	 * @param images
	 */
	private void processDicomFile(File dicomFile, JsonNode serie, JsonNode instances, String instanceFilePath, ArrayNode nonImages, ArrayNode images,String unzipFolderFileAbsolutePath) {
		DicomInputStream dIS = null;
		try {
			dIS = new DicomInputStream(dicomFile);
			Attributes datasetAttributes = dIS.readDataset(-1, -1);
			String sopClassUID = datasetAttributes.getString(Tag.SOPClassUID);
			((ObjectNode) serie).put("sopClassUID", sopClassUID);
			checkSeriesDescription(serie, datasetAttributes);
			// Some DICOM files with a particular SOP Class UID are to be ignored: such as Raw Data Storage
			if (sopClassUID.startsWith("1.2.840.10008.5.1.4.1.1.66")) {
				// ((ArrayNode) instances).remove(index);
				// do nothing here as instances array will be deleted after split
			} else {
				// divide here between non-images and images, non-images at first
				String seriesDescription = serie.path("seriesDescription").asText();
				if (UID.PrivateSiemensCSANonImageStorage.equals(sopClassUID)
						|| UID.MRSpectroscopyStorage.equals(sopClassUID)
						|| checkSerieIsSpectroscopy(seriesDescription)) {
					ObjectNode nonImage = mapper.createObjectNode();
					nonImage.put("path", instanceFilePath.replace(unzipFolderFileAbsolutePath+"/", ""));
					nonImages.add(nonImage);
					((ObjectNode) serie).put("isSpectroscopy", true);
					LOG.warn("Attention: spectroscopy serie is included in this import!");
					// images at the second
				} else {
					// do not change here: use absolute path all time and find other solution for
					// image preview
					ObjectNode image = mapper.createObjectNode();
					image.put("path", instanceFilePath.replace(unzipFolderFileAbsolutePath+"/", ""));
					addImageSeparateDatasetsInfo(image, datasetAttributes);
					images.add(image);
					((ObjectNode) serie).put("isSpectroscopy", false);
				}
			}
			/**
			 * Attention: the below methods set informations on the serie level, that are
			 * extracted of each dicom file (== instance), as we are on the instance level
			 * here. Normally, the first file wins, as the below methods check if the node
			 * is already existing in the json created. This logic was like this before in
			 * shanoir old and I think there is no other way than taking one file (the
			 * first?) as reference for the serie. The below infos are not contained in the
			 * dicomdir, that is why we go on the file level.
			 */
			checkIsMultiFrame(serie, datasetAttributes, sopClassUID);
			checkIsEnhancedMRAndAddSequenceName(serie, datasetAttributes, sopClassUID);
			checkSeriesDate(serie, datasetAttributes);
			checkProtocolName(serie, datasetAttributes);
			addSeriesEquipment(serie, datasetAttributes);
			addSeriesIsCompressed(serie, datasetAttributes);
		} catch (IOException e) {
			LOG.error("Error during DICOM file process", e);
		} finally {
			if (dIS != null) {
				try {
					dIS.close();
				} catch (IOException e) {
					LOG.error("Error while closing DICOM input stream", e);
				}
			}
		}
	}

	/**
	 * This method uses the properties string isspectroscopy to check
	 * if a serie contains spectroscopy.
	 */
	private boolean checkSerieIsSpectroscopy(final String seriesDescription) {
		final String[] seriesDescriptionsToIdentifySpectroscopyInSerie = isSpectroscopy.split(SEMI_COLON);
		for (final String item : seriesDescriptionsToIdentifySpectroscopyInSerie) {
			final String tag = item.split(DOUBLE_EQUAL)[0];
			final String value = item.split(DOUBLE_EQUAL)[1];
			LOG.debug("checkIsSpectroscopy : tag=" + tag + ", value=" + value);
			String wildcard = ImportUtils.wildcardToRegex(value);
			if (seriesDescription != null && seriesDescription.matches(wildcard)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method adds all required infos to separate datasets within series for each image.
	 * @param image
	 * @param datasetAttributes
	 */
	private void addImageSeparateDatasetsInfo(JsonNode image, Attributes datasetAttributes) {
		if (image.path("acquisitionNumber").isMissingNode()) {
			String acquisitionNumber = datasetAttributes.getString(Tag.AcquisitionNumber);
			if (acquisitionNumber != null && !acquisitionNumber.isEmpty()) {
				((ObjectNode) image).put("acquisitionNumber", acquisitionNumber);
			}
		}
		if (image.path("echoNumbers").isMissingNode()) {
			ArrayNode echoNumbers = mapper.createArrayNode();
			int[] echoNumbersArray = datasetAttributes.getInts(Tag.EchoNumbers);
			if (echoNumbersArray != null) {
				for (int i = 0; i < echoNumbersArray.length; i++) {
					echoNumbers.add(echoNumbersArray[i]);		
				}
				((ObjectNode) image).set("echoNumbers", echoNumbers);
			} else {
				LOG.info("echoNumbersArray in dcm file null: " + image.path("path").asText());
			}
		}
		if (image.path("imageOrientationPatient").isMissingNode()) {
			ArrayNode imageOrientationPatient = mapper.createArrayNode();
			double[] imageOrientationPatientArray = datasetAttributes.getDoubles(Tag.ImageOrientationPatient);
			if (imageOrientationPatientArray != null) {
				for (int i = 0; i < imageOrientationPatientArray.length; i++) {
					imageOrientationPatient.add(imageOrientationPatientArray[i]);		
				}
				((ObjectNode) image).set("imageOrientationPatient", imageOrientationPatient);
			} else {
				LOG.info("imageOrientationPatientArray in dcm file null: " + image.path("path").asText());
			}
		}
	}
	
	/**
	 * Adds on analyzing the transfersyntaxuid if serie is compressed or not.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void addSeriesIsCompressed(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("isCompressed").isMissingNode()) {
			String transferSyntaxUID = datasetAttributes.getString(Tag.TransferSyntaxUID);
			if (transferSyntaxUID != null && transferSyntaxUID.startsWith("1.2.840.10008.1.2.4")) {
				((ObjectNode) serie).put("isCompressed", true);
			} else {
				((ObjectNode) serie).put("isCompressed", false);
			}
		}
	}
	
	/**
	 * Adds the equipment information.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void addSeriesEquipment(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("equipment").isMissingNode()) {
			String manufacturer = datasetAttributes.getString(Tag.Manufacturer);
			String manufacturerModelName = datasetAttributes.getString(Tag.ManufacturerModelName);
			String deviceSerialNumber = datasetAttributes.getString(Tag.DeviceSerialNumber);
			ObjectNode equipment = mapper.createObjectNode();
			equipment.put("manufacturer", manufacturer);
			equipment.put("manufacturerModelName", manufacturerModelName);
			equipment.put("deviceSerialNumber", deviceSerialNumber);
			((ObjectNode) serie).set("equipment", equipment);
		}
	}

	/**
	 * Normally we get the seriesDescription from the DicomDir, if not: null or empty,
	 * get the seriesDescription from the .dcm file, if existing in .dcm file add it in
	 * JsonNode tree.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void checkSeriesDescription(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("seriesDescription").isNull()) {
			// has not been found in dicomdir, so we get it from .dcm file:
			String seriesDescriptionDicomFile = datasetAttributes.getString(Tag.SeriesDescription);
			if (seriesDescriptionDicomFile != null && !seriesDescriptionDicomFile.isEmpty()) {
				((ObjectNode) serie).put("seriesDescription", seriesDescriptionDicomFile);
			}
		}
	}
	
	/**
	 * Normally we get the seriesDate from the DicomDir, if not: null or empty,
	 * get the seriesDate from the .dcm file, if existing in .dcm file add it in
	 * JsonNode tree.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void checkSeriesDate(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("seriesDate").isMissingNode()) {
			// has not been found in dicomdir, so we get it from .dcm file:
			String seriesDateDicomFile = datasetAttributes.getString(Tag.SeriesDate);
			if (seriesDateDicomFile != null && !seriesDateDicomFile.isEmpty()) {
				((ObjectNode) serie).put("seriesDate", seriesDateDicomFile);
			}
		}
	}
	
	/**
	 * Normally we get the protocolName from the DicomDir, if not: null or empty,
	 * get the protocolName from the .dcm file, if existing in .dcm file add it in
	 * JsonNode tree.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void checkProtocolName(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("protocolName").isNull()) {
			// has not been found in dicomdir, so we get it from .dcm file:
			String protocolNameDicomFile = datasetAttributes.getString(Tag.ProtocolName);
			if (protocolNameDicomFile != null && !protocolNameDicomFile.isEmpty()) {
				((ObjectNode) serie).put("protocolName", protocolNameDicomFile);
			}
		}
	}
	
	/**
	 * Checks for multi-frame dicom files.
	 * @param serie
	 * @param datasetAttributes
	 * @param sopClassUID
	 */
	private void checkIsMultiFrame(JsonNode serie, Attributes datasetAttributes, String sopClassUID) {
		if (serie.path("isMultiFrame").isMissingNode()) {
			if (UID.EnhancedMRImageStorage.equals(sopClassUID)) {
				((ObjectNode) serie).put("isMultiFrame", "true");
				String frameCount = new Integer(getFrameCount(datasetAttributes)).toString();
				((ObjectNode) serie).put("multiFrameCount", frameCount);				
			} else {
				((ObjectNode) serie).put("isMultiFrame", "false");
			}
		}
	}

	/**
	 * Get the frame count of the given dicom object.
	 *
	 * @param dcmObj
	 *            the dcmObj
	 * @return the frame count
	 */
	private int getFrameCount(final Attributes attributes) {
		if (attributes != null) {
			Attributes pffgs = attributes.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence);
			if (pffgs != null) {
				return pffgs.size();
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Checks for enhanced Dicom and sequence name.
	 * @param serie
	 * @param sopClassUID
	 */
	private void checkIsEnhancedMRAndAddSequenceName(JsonNode serie, Attributes attributes, String sopClassUID) {
		if (serie.path("isEnhancedMR").isMissingNode()) {
			if (UID.EnhancedMRImageStorage.equals(sopClassUID)) {
				((ObjectNode) serie).put("isEnhancedMR", "true");
				String sequenceName = attributes.getString(Tag.PulseSequenceName);
				((ObjectNode) serie).put("sequenceName", sequenceName);
			} else {
				((ObjectNode) serie).put("isEnhancedMR", "false");
				String sequenceName = attributes.getString(Tag.SequenceName);
				((ObjectNode) serie).put("sequenceName", sequenceName);
			}
		}
	}
	
}
