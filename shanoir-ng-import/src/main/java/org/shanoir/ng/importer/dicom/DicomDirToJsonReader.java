package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.media.DicomDirReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class uses mainly the dcm4che3 DicomDirReader to read in a DICOMDIR.
 * It creates a Json tree on using Jackson JsonNode that corresponds to the
 * content of the DICOMDIR in the DICOM specific tree order: patient - study
 * - serie - instance(image and non-images).
 * @author mkain
 *
 */
public class DicomDirToJsonReader {

	private static final Logger LOG = LoggerFactory.getLogger(DicomDirToJsonReader.class);

	private ObjectMapper mapper = new ObjectMapper();

	private DicomDirReader dicomDirReader;
	
	public DicomDirToJsonReader(final File file) throws IOException {
		dicomDirReader = new DicomDirReader(file);
	}

	/**
	 * This method reads a DICOMDIR and returns its higher-level content
	 * as a Json string.
	 * @return String - Json of DICOM tree hierarchy
	 */
	public String readDicomDirToJson() {
		JsonNode dicomDirJsonTree = mapper.createObjectNode();
		try {
			// patient level
			ArrayNode patients = mapper.createArrayNode();
			Attributes patientRecord = dicomDirReader.findPatientRecord();
			while(patientRecord != null) {
				ObjectNode patient = createPatientObjectNode(patientRecord);
				patients.add(patient);
				// study level
				ArrayNode studies = mapper.createArrayNode();
				Attributes studyRecord = dicomDirReader.findStudyRecord(patientRecord);
				while(studyRecord != null) {
					ObjectNode study = createStudyObjectNode(studyRecord);
					studies.add(study);
					// serie level
					ArrayNode series = mapper.createArrayNode();
					Attributes serieRecord = dicomDirReader.findSeriesRecord(studyRecord);
					while(serieRecord != null) {
						ObjectNode serie = createSerieObjectNode(serieRecord);
						series.add(serie);
						// instance level: could be image or non-image (to filter later)
						ArrayNode instances = mapper.createArrayNode();
						Attributes instanceRecord = dicomDirReader.findLowerInstanceRecord(serieRecord, true);
						while(instanceRecord != null) {
							handleInstanceRecord(instances, instanceRecord);
							instanceRecord = dicomDirReader.findNextInstanceRecord(instanceRecord, true);
						}
						((ObjectNode) serie).set("instances", instances);
						serieRecord = dicomDirReader.findNextSeriesRecord(serieRecord);
					}
					((ObjectNode) study).set("series", series);					
					studyRecord = dicomDirReader.findNextStudyRecord(studyRecord);				
				}
				((ObjectNode) patient).set("studies", studies);
				patientRecord = dicomDirReader.findNextPatientRecord(patientRecord);
			}
			((ObjectNode) dicomDirJsonTree).set("patients", patients);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dicomDirJsonTree);
		} catch (IOException e) {
			LOG.error("Error while reading first root record of DICOM file: " + e.getMessage());
		}
		return null;
	}

	/**
	 * This method gets an instance record from the DICOMDIR and reads the tag
	 * ReferencedFileID, that gives back a list of file path names. The file
	 * path names are combined to a valid File.separator file path and stored
	 * in the instance. At this moment, when reading in the DICOMDIR, we only
	 * not that we handle files, it could be images or everything else, that
	 * is why we talk about instances here.
	 * @param instances
	 * @param instanceRecord
	 */
	private void handleInstanceRecord(ArrayNode instances, Attributes instanceRecord) {
		ObjectNode instance = mapper.createObjectNode();
		String[] instancePathArray = instanceRecord.getStrings(Tag.ReferencedFileID);
		if (instancePathArray != null) {
			String instancePath = dicomDirReader.getFile().getParentFile().getAbsolutePath() + File.separator;
			for (int count = 0; count < instancePathArray.length; count++) {
				instancePath += instancePathArray[count];
				if (count != (instancePathArray.length - 1)) {
					instancePath += File.separator;
				}
			}
			instance.put("instanceFilePath", instancePath);
			instances.add(instance);
		} else {
			LOG.warn("Error in DICOMDIR: instanceRecord with empty Tag.ReferencedFileID");
		}
	}

	private ObjectNode createPatientObjectNode(Attributes patientRecord) {
		ObjectNode patient = mapper.createObjectNode();
		patient.put("patientID", patientRecord.getString(Tag.PatientID));
		patient.put("patientName", patientRecord.getString(Tag.PatientName));
		patient.put("patientBirthDate", patientRecord.getString(Tag.PatientBirthDate));
		patient.put("patientSex", patientRecord.getString(Tag.PatientSex));
		return patient;
	}
	
	private ObjectNode createStudyObjectNode(Attributes studyRecord) {
		ObjectNode study = mapper.createObjectNode();
		study.put("studyInstanceUID", studyRecord.getString(Tag.StudyInstanceUID));
		study.put("studyDate", studyRecord.getString(Tag.StudyDate));
		study.put("studyDescription", studyRecord.getString(Tag.StudyDescription));
		return study;
	}
	
	private ObjectNode createSerieObjectNode(Attributes serieRecord) {
		ObjectNode serie = mapper.createObjectNode();		
		serie.put("seriesInstanceUID", serieRecord.getString(Tag.SeriesInstanceUID));
		serie.put("modality", serieRecord.getString(Tag.Modality));
		serie.put("protocolName", serieRecord.getString(Tag.ProtocolName));
		serie.put("seriesDescription", serieRecord.getString(Tag.SeriesDescription));
		serie.put("seriesDate", serieRecord.getString(Tag.SeriesDate));
		serie.put("seriesNumber", serieRecord.getString(Tag.SeriesNumber));
		serie.put("numberOfSeriesRelatedInstances", serieRecord.getString(Tag.NumberOfSeriesRelatedInstances));
		return serie;
	}
	
}
