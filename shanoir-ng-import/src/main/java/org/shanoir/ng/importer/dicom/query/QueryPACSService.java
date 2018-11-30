package org.shanoir.ng.importer.dicom.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.tool.findscu.FindSCU.InformationModel;
import org.shanoir.ng.importer.model.EquipmentDicom;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;

@Service
public class QueryPACSService {
	
	private static final Logger LOG = LoggerFactory.getLogger(QueryPACSService.class);

	@Value("${shanoir.import.pacs.aet.calling.name}")
	private String callingName;

	@Value("${shanoir.import.pacs.aet.calling.host}")
	private String callingHost;
	
	@Value("${shanoir.import.pacs.aet.calling.port}")
	private Integer callingPort;
	
	@Value("${shanoir.import.pacs.aet.called.name}")
	private String calledName;

	@Value("${shanoir.import.pacs.aet.called.host}")
	private String calledHost;
	
	@Value("${shanoir.import.pacs.aet.called.port}")
	private Integer calledPort;
	
	public ImportJob queryPACS(DicomQuery dicomQuery) throws ShanoirImportException {
		// Initialize connection configuration parameters here: to be used for all queries
		DicomNode calling = new DicomNode(callingName);
		DicomNode called = new DicomNode(calledName, calledHost, calledPort);
		
		ImportJob importJob = new ImportJob();
		importJob.setFromPacs(true);

		/**
		 * In case of any patient specific search field is filled, work on patient level. Highest priority.
		 */
		if (StringUtils.isNotEmpty(dicomQuery.getPatientName())
			|| StringUtils.isNotEmpty(dicomQuery.getPatientID())
			|| StringUtils.isNotEmpty(dicomQuery.getPatientBirthDate())) {
			// @Todo: implement wild card search
			// Do Fuzzy search on base of patient name here
//			if (StringUtils.isNotEmpty(dicomQuery.getPatientName())
//				&& (dicomQuery.getPatientName().contains("*") || !dicomQuery.getPatientName().contains("^"))) {
			queryPatientLevel(dicomQuery, calling, called, importJob);
			// Do precise search here, using name, id or date
//			} else {	
//			}
		/**
		 * In case of any study specific search field is filled, work on study level. Second priority.
		 */
		} else if (StringUtils.isNotEmpty(dicomQuery.getStudyDescription())
			|| StringUtils.isNotEmpty(dicomQuery.getStudyDate())) {
			queryStudyLevel(dicomQuery, calling, called, importJob);
		} else {
			throw new ShanoirImportException("DicomQuery: missing parameters.");
		}
		return importJob;
	}

	/**
	 * This method queries on patient root level.
	 * @param dicomQuery
	 * @param calling
	 * @param called
	 * @param importJob
	 */
	private void queryPatientLevel(DicomQuery dicomQuery, DicomNode calling, DicomNode called, ImportJob importJob) {
		DicomParam patientName = initDicomParam(Tag.PatientName, dicomQuery.getPatientName());
		DicomParam patientID = initDicomParam(Tag.PatientID, dicomQuery.getPatientID());
		DicomParam patientBirthDate = initDicomParam(Tag.PatientBirthDate, dicomQuery.getPatientBirthDate());
		DicomParam[] params = { patientName, patientID, patientBirthDate, new DicomParam(Tag.PatientBirthName), new DicomParam(Tag.PatientSex) };
		List<Attributes> attributesPatients = queryCFIND(params, QueryRetrieveLevel.PATIENT, calling, called);
		if (attributesPatients != null) {
			List<Patient> patients = new ArrayList<Patient>();
			for (int i = 0; i < attributesPatients.size(); i++) {
				Patient patient = initPatient(attributesPatients.get(i));
				patients.add(patient);
				queryStudies(calling, called, patient);
			}
			importJob.setPatients(patients);
		}
	}

	/**
	 * This method queries on study root level.
	 * @param dicomQuery
	 * @param calling
	 * @param called
	 * @param importJob
	 */
	private void queryStudyLevel(DicomQuery dicomQuery, DicomNode calling, DicomNode called, ImportJob importJob) {
		DicomParam studyDescription = initDicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription());
		DicomParam studyDate = initDicomParam(Tag.StudyDate, dicomQuery.getStudyDate());
		DicomParam[] params = { studyDescription, studyDate, new DicomParam(Tag.PatientName),
			new DicomParam(Tag.PatientID), new DicomParam(Tag.PatientBirthDate), new DicomParam(Tag.PatientBirthName),
			new DicomParam(Tag.PatientSex), new DicomParam(Tag.StudyInstanceUID) };
		List<Attributes> attributesStudies = queryCFIND(params, QueryRetrieveLevel.STUDY, calling, called);
		if (attributesStudies != null) {
			List<Patient> patients = new ArrayList<Patient>();
			for (int i = 0; i < attributesStudies.size(); i++) {
				// handle patients
				Patient patient = initPatient(attributesStudies.get(i));
				patient = addPatientIfNotExisting(patients, patient);
				// handle studies
				Study study = initStudy(attributesStudies.get(i));
				patient.getStudies().add(study);
				querySeries(calling, called, study);
			}
			importJob.setPatients(patients);
		}
	}

	/**
	 * This method adds a new patient to the patients list, if not already existing.
	 * @param patients
	 * @param newPatient
	 */
	private Patient addPatientIfNotExisting(List<Patient> patients, Patient newPatient) {
		for (Iterator iterator = patients.iterator(); iterator.hasNext();) {
			Patient patientInList = (Patient) iterator.next();
			if (patientInList.getPatientID().equals(newPatient.getPatientID())) {
				return patientInList;
			}
		}
		newPatient.setStudies(new ArrayList<Study>());
		patients.add(newPatient);
		return newPatient;
	}
	
	/**
	 * This method returns a created DicomParam given tag and value.
	 * @param tag
	 * @param value
	 * @return
	 */
	private DicomParam initDicomParam(int tag, String value) {
		DicomParam dicomParam;
		if (StringUtils.isNotEmpty(value)) {
			dicomParam = new DicomParam(tag, value);
		} else {
			dicomParam = new DicomParam(tag);
		}
		return dicomParam;
	}

	/**
	 * This method queries for studies, creates studies and adds them to patients.
	 * @param calling
	 * @param called
	 * @param patient
	 */
	private void queryStudies(DicomNode calling, DicomNode called, Patient patient) {
		DicomParam[] params = { new DicomParam(Tag.PatientID, patient.getPatientID()),
				new DicomParam(Tag.StudyInstanceUID), new DicomParam(Tag.StudyDate), new DicomParam(Tag.StudyDescription)};
		List<Attributes> attributesStudies = queryCFIND(params, QueryRetrieveLevel.STUDY, calling, called);
		if (attributesStudies != null) {
			List<Study> studies = new ArrayList<Study>();
			for (int i = 0; i < attributesStudies.size(); i++) {
				Study study = initStudy(attributesStudies.get(i));
				studies.add(study);
				querySeries(calling, called, study);
			}
			patient.setStudies(studies);
		}
	}

	/**
	 * This method queries for series, creates them and adds them to studies.
	 * @param calling
	 * @param called
	 * @param study
	 */
	private void querySeries(DicomNode calling, DicomNode called, Study study) {
		DicomParam[] params = {
			new DicomParam(Tag.StudyInstanceUID, study.getStudyInstanceUID()),
			new DicomParam(Tag.SeriesInstanceUID),
			new DicomParam(Tag.SeriesDescription),
			new DicomParam(Tag.SeriesDate),
			new DicomParam(Tag.SeriesNumber),
			new DicomParam(Tag.Modality),
			new DicomParam(Tag.ProtocolName),
			new DicomParam(Tag.Manufacturer),
			new DicomParam(Tag.ManufacturerModelName),
			new DicomParam(Tag.DeviceSerialNumber)	
		};
		List<Attributes> attributesSeries = queryCFIND(params, QueryRetrieveLevel.SERIES, calling, called);
		if (attributesSeries != null) {
			List<Serie> series = new ArrayList<Serie>();
			for (int i = 0; i < attributesSeries.size(); i++) {
				Serie serie = initSerie(attributesSeries.get(i));
				if (serie.getModality() != null && !"PR".equals(serie.getModality()) && !"SR".equals(serie.getModality())) {
					series.add(serie);
				}
			}
			study.setSeries(series);
		}
	}
	
	/**
	 * This method does a C-FIND query and returns the results.
	 * @param params
	 * @param level
	 * @param calling
	 * @param called
	 * @return
	 */
	private List<Attributes> queryCFIND(DicomParam[] params, QueryRetrieveLevel level, final DicomNode calling, final DicomNode called) {
		AdvancedParams options = new AdvancedParams();
		if (level.equals(QueryRetrieveLevel.PATIENT)) {
			options.setInformationModel(InformationModel.PatientRoot);
		} else if (level.equals(QueryRetrieveLevel.STUDY)) {
			options.setInformationModel(InformationModel.StudyRoot);
		} else if (level.equals(QueryRetrieveLevel.SERIES)) {
			options.setInformationModel(InformationModel.StudyRoot);
		}
		logQuery(params, options);
		DicomState state = CFind.process(options, calling, called, 0, level, params);
		return state.getDicomRSP();
	}

	/**
	 * This method logs the params and options of the PACS query.
	 * @param params
	 * @param options
	 */
	private void logQuery(DicomParam[] params, AdvancedParams options) {
		LOG.info("Calling PACS, C-FIND with level: " + options.getInformationModel().toString() + " and params:");
		for (int i = 0; i < params.length; i++) {
			LOG.info("Tag: " + params[i].getTagName() + ", Value: " + Arrays.toString(params[i].getValues())); 
		}
	}
	
	/**
	 * Initialize patient from Attributes.
	 * @param attributes
	 * @return
	 */
	public Patient initPatient(final Attributes attributes) {
		final Patient patient = new Patient(
			attributes.getString(Tag.PatientID),
			attributes.getString(Tag.PatientName),
			attributes.getString(Tag.PatientBirthName),
			attributes.getDate(Tag.PatientBirthDate),
			attributes.getString(Tag.PatientSex));
		return patient;
	}
	
	/**
	 * Initialize study from Attributes.
	 * @param attributes
	 * @return
	 */
	public Study initStudy(final Attributes attributes) {
		final Study study = new Study(
			attributes.getString(Tag.StudyInstanceUID),
			attributes.getDate(Tag.StudyDate),
			attributes.getString(Tag.StudyDescription));
		return study;
	}
	
	/**
	 * Initialize serie from Attributes.
	 * @param attributes
	 * @return
	 */
	public Serie initSerie(final Attributes attributes) {
		final Serie serie = new Serie(
			attributes.getString(Tag.SeriesInstanceUID),
			attributes.getString(Tag.SeriesDescription),
			attributes.getDate(Tag.SeriesDate),
			attributes.getString(Tag.SeriesNumber),
			attributes.getString(Tag.Modality),
			attributes.getString(Tag.ProtocolName));
		final EquipmentDicom equipmentDicom = new EquipmentDicom(
			attributes.getString(Tag.Manufacturer),
			attributes.getString(Tag.ManufacturerModelName),
			attributes.getString(Tag.DeviceSerialNumber));
		serie.setEquipment(equipmentDicom);
		return serie;
	}

}
