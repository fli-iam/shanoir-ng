package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
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
 * 
 * @author mkain
 *
 */
@Service
public class DicomFileAnalyzer {

	private static final Logger LOG = LoggerFactory.getLogger(DicomFileAnalyzer.class);
	
	@Value("${shanoir.import.upload.folder}")
	private String uploadFolder;

	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * This method walks through the JsonNode tree and accesses to files
	 * on using the instanceFilePath from the instances. Jackson 2.8.3
	 * at time of this development does not support JsonPath expressions,
	 * that is why 4 loops have to be used to walk through the tree.
	 * @throws FileNotFoundException 
	 */
	public void analyzeDicomFiles(JsonNode dicomDirJson) throws FileNotFoundException {
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
									int index = 1;
									ArrayNode nonImages = mapper.createArrayNode();
									ArrayNode images = mapper.createArrayNode();
									for (JsonNode instance : instances) {
										String indexString = new Integer(index).toString();
										String instanceFilePath = instance.path(indexString).asText();
										File instanceFile = new File(instanceFilePath);
										if (instanceFile.exists()) {
											processDicomFile(instanceFile, serie, instances, instance, index, nonImages, images);
										} else {
											throw new FileNotFoundException("InstanceFilePath in DicomDir: missing file: " + instanceFilePath);
										}
										index = index + 1;
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

	private void processDicomFile(File dicomFile, JsonNode serie, JsonNode instances, JsonNode instance, int index, ArrayNode nonImages, ArrayNode images) {
		DicomInputStream dIS = null;
		try {
			dIS = new DicomInputStream(dicomFile);
			Attributes datasetAttributes = dIS.readDataset(-1, -1);

			String indexString = new Integer(index).toString();
			String instanceFilePath = instance.path(indexString).asText();

			String sopClassUID = datasetAttributes.getString(Tag.SOPClassUID);
			((ObjectNode) serie).put("sopClassUID", sopClassUID);
			// Some DICOM files with a particular SOP Class UID are to be ignored: such as Raw Data Storage
			if (sopClassUID.startsWith("1.2.840.10008.5.1.4.1.1.66")) {
				//((ArrayNode) instances).remove(index);
				// do nothing here as instances array will be deleted after split
			} else {
				// divide here between non-images and images, non-images at first
				if (UID.PrivateSiemensCSANonImageStorage.equals(sopClassUID)
					|| UID.MRSpectroscopyStorage.equals(sopClassUID)) {
					ObjectNode nonImage = mapper.createObjectNode();
					nonImage.put(indexString, instanceFilePath);
					nonImages.add(nonImage);
				// images at the second
				} else {
					//ObjectNode image = mapper.createObjectNode();
					//image.put(indexString, instanceFilePath);
					String pattern = Pattern.quote(uploadFolder);
					String instanceSuffixPath = instanceFilePath.split(pattern)[1];
					images.add(instanceSuffixPath);
				}
			}
			checkIsMultiFrame(serie, datasetAttributes, sopClassUID);
			checkSeriesDescription(serie, datasetAttributes);
			checkSeriesDate(serie, datasetAttributes);
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
	 * Get seriesDescription from DicomDir, if null or empty, get the seriesDescription
	 * from .dcm file, if existing replace it in JsonNode tree.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void checkSeriesDescription(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("seriesDescription").isNull()) {
			String seriesDescriptionDicomFile = datasetAttributes.getString(Tag.SeriesDescription);
			if (seriesDescriptionDicomFile != null && !seriesDescriptionDicomFile.isEmpty()) {
				((ObjectNode) serie).put("seriesDescription", seriesDescriptionDicomFile);
			}
		}
	}
	
	/**
	 * Get seriesDate from DicomDir, if null or empty, get the seriesDate
	 * from .dcm file, if existing replace it in JsonNode tree.
	 * @param serie
	 * @param datasetAttributes
	 */
	private void checkSeriesDate(JsonNode serie, Attributes datasetAttributes) {
		if (serie.path("seriesDate").isNull()) {
			String seriesDateDicomFile = datasetAttributes.getString(Tag.SeriesDate);
			if (seriesDateDicomFile != null && !seriesDateDicomFile.isEmpty()) {
				((ObjectNode) serie).put("seriesDate", seriesDateDicomFile);
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
		if (serie.path("isMultiFrame").isNull()) {
			if (UID.EnhancedMRImageStorage.equals(sopClassUID)) {
				((ObjectNode) serie).put("isMultiFrame", "true");
				String frameCount = new Integer(getFrameCount(datasetAttributes)).toString();
				((ObjectNode) serie).put("frameCount", frameCount);				
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

}
