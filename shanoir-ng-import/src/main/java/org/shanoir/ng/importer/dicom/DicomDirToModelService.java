/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.media.DicomDirReader;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class uses mainly the dcm4che3 DicomDirReader to read in a DICOMDIR.
 * It creates a Shanoir Dicom model that corresponds to the
 * content of the DICOMDIR in the DICOM specific tree order: patient - study
 * - serie - instance(image and non-images). Series with modalities outside
 * of medical imaging are ignored.
 * 
 * @author mkain
 *
 */
@Service
public class DicomDirToModelService {

	private static final Logger LOG = LoggerFactory.getLogger(DicomDirToModelService.class);

	/**
	 * This method reads a DICOMDIR and returns its higher-level content as a list of patients.
	 * 
	 * @param file DICOMDIR file
	 * @return List<Patient>
	 * @throws IOException
	 */
	public List<Patient> readDicomDirToPatients(final File file) throws IOException {
		try (DicomDirReader dicomDirReader = new DicomDirReader(file)) {
			// patient level
			List<Patient> patients = new ArrayList<Patient>();
			Attributes patientRecord = dicomDirReader.findPatientRecord();
			while(patientRecord != null) {
				Patient patient = new Patient(patientRecord);
				patients.add(patient);
				// study level
				List<Study> studies = new ArrayList<Study>();
				Attributes studyRecord = dicomDirReader.findStudyRecord(patientRecord);
				while(studyRecord != null) {
					Study study = new Study(studyRecord);
					studies.add(study);
					// serie level
					List<Serie> series = new ArrayList<Serie>();
					Attributes serieRecord = dicomDirReader.findSeriesRecord(studyRecord);
					while(serieRecord != null) {
						handleSerieAndInstanceRecords(series, serieRecord, dicomDirReader);
						serieRecord = dicomDirReader.findNextSeriesRecord(serieRecord);
					}
					series.sort(new SeriesNumberOrAcquisitionTimeOrDescriptionSorter());
					study.setSeries(series);
					studyRecord = dicomDirReader.findNextStudyRecord(studyRecord);
				}
				studies.sort(new StudyDateSorter());
				patient.setStudies(studies);
				patientRecord = dicomDirReader.findNextPatientRecord(patientRecord);
			}
			return patients;
		} catch (IOException e) {
			LOG.error("Error while reading first root record of DICOM file: {}", e.getMessage());
		}
		return Collections.emptyList();
	}

	/**
	 * Handles Serie and Instance records.
	 * 
	 * @param series
	 * @param serieRecord
	 * @param dicomDirReader
	 * @throws IOException
	 */
	private void handleSerieAndInstanceRecords(List<Serie> series, Attributes serieRecord, DicomDirReader dicomDirReader) throws IOException {
		Serie serie = new Serie(serieRecord);
		if (!DicomSerieAndInstanceAnalyzer.checkSerieIsIgnored(serieRecord)) {
			List<Instance> instances = new ArrayList<Instance>();
			Attributes instanceRecord = dicomDirReader.findLowerInstanceRecord(serieRecord, false);
			while(instanceRecord != null) {
				Instance instance = new Instance(instanceRecord);
				if (!DicomSerieAndInstanceAnalyzer.checkInstanceIsIgnored(instanceRecord)) {
					instances.add(instance);
				}
				instanceRecord = dicomDirReader.findNextInstanceRecord(instanceRecord, false);
			}
			if (!instances.isEmpty()) {
				instances.sort(new InstanceNumberSorter());
				serie.setInstances(instances);
			} else {
				LOG.warn("Serie found with empty instances and therefore ignored (SeriesDescription: {}, SerieInstanceUID: {}).", serie.getSeriesDescription(), serie.getSeriesInstanceUID());
				serie.setIgnored(true);
				serie.setSelected(false);
			}
		} else {
			LOG.warn("Serie found with no-imaging modality and therefore ignored (SeriesDescription: {}, SerieInstanceUID: {}).", serie.getSeriesDescription(), serie.getSeriesInstanceUID());
			serie.setIgnored(true);
			serie.setSelected(false);
		}
		series.add(serie);
	}
	
}
