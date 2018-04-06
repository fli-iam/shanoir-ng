package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.dcmr.AcquisitionModality;
import org.dcm4che3.media.DicomDirReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class uses mainly the dcm4che3 DicomDirReader to read in a DICOMDIR.
 * It creates a Json tree on using Jackson JsonNode that corresponds to the
 * content of the DICOMDIR in the DICOM specific tree order: patient - study
 * - serie - instance(image and non-images). Series with modalities outside
 * of medical imaging are ignored and an attribute "selected" is added to each
 * serie for later usage in import process.
 * 
 * @author mkain
 *
 */
@Service
public class DicomDirToJsonReaderService {

	private static final Logger LOG = LoggerFactory.getLogger(DicomDirToJsonReaderService.class);

	private ObjectMapper mapper = new ObjectMapper();

	private DicomDirReader dicomDirReader;

	/**
	 * This method reads a DICOMDIR and returns its higher-level content
	 * as a Json string.
	 * @return String - Json of DICOM tree hierarchy
	 */
	public JsonNode readDicomDirToJsonNode(final File file) throws IOException {
		dicomDirReader = new DicomDirReader(file);
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
						handleSerieRecord(series, serieRecord);
						serieRecord = dicomDirReader.findNextSeriesRecord(serieRecord);
					}
					((ObjectNode) study).set("series", series);					
					studyRecord = dicomDirReader.findNextStudyRecord(studyRecord);				
				}
				((ObjectNode) patient).set("studies", studies);
				patientRecord = dicomDirReader.findNextPatientRecord(patientRecord);
			}
			((ObjectNode) dicomDirJsonTree).set("patients", patients);
			return dicomDirJsonTree;
		} catch (IOException e) {
			LOG.error("Error while reading first root record of DICOM file: " + e.getMessage());
		}
		return null;
	}

	/**
	 * @param series
	 * @param serieRecord
	 * @throws IOException
	 */
	private void handleSerieRecord(ArrayNode series, Attributes serieRecord) throws IOException {
		String modality = serieRecord.getString(Tag.Modality);
		// Use dcm4che3 class here: ignore everything outside medical imaging
		if (AcquisitionModality.codeOf(modality) != null) {
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
		} else {
			LOG.info("Serie found with non medical imaging modality and therefore ignored.");
		}
	}

	public ObjectMapper getMapper() {
		return mapper;
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
			String instanceNumber = instanceRecord.getString(Tag.InstanceNumber);
			instance.put(instanceNumber, instancePath);
			instances.add(instance);
		} else {
			LOG.warn("Error in DICOMDIR: instanceRecord with empty Tag.ReferencedFileID");
		}
	}

	private ObjectNode createPatientObjectNode(Attributes patientRecord) {
		ObjectNode patient = mapper.createObjectNode();
		patient.put("patientID", patientRecord.getString(Tag.PatientID));
		patient.put("patientName", patientRecord.getString(Tag.PatientName));
		patient.put("patientBirthDate", patientRecord.getDate(Tag.PatientBirthDate).getTime());
		patient.put("patientSex", patientRecord.getString(Tag.PatientSex));
		return patient;
	}
	
	private ObjectNode createStudyObjectNode(Attributes studyRecord) {
		ObjectNode study = mapper.createObjectNode();
		study.put("studyInstanceUID", studyRecord.getString(Tag.StudyInstanceUID));
		study.put("studyDate", studyRecord.getDate(Tag.StudyDate).getTime());
		study.put("studyDescription", studyRecord.getString(Tag.StudyDescription));
		return study;
	}
	
	private ObjectNode createSerieObjectNode(Attributes serieRecord) {
		ObjectNode serie = mapper.createObjectNode();
		serie.put("selected", false);
		serie.put("seriesInstanceUID", serieRecord.getString(Tag.SeriesInstanceUID));
		serie.put("modality", serieRecord.getString(Tag.Modality));
		serie.put("protocolName", serieRecord.getString(Tag.ProtocolName));
		serie.put("seriesDescription", serieRecord.getString(Tag.SeriesDescription));
		serie.put("seriesDate", serieRecord.getDate(Tag.SeriesDate).getTime());
		serie.put("seriesNumber", serieRecord.getString(Tag.SeriesNumber));
		serie.put("numberOfSeriesRelatedInstances", serieRecord.getString(Tag.NumberOfSeriesRelatedInstances));
		return serie;
	}
	
}
