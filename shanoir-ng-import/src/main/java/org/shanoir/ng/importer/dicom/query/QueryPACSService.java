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

package org.shanoir.ng.importer.dicom.query;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.IntStream;

import javax.swing.JProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.shanoir.ng.importer.dicom.DicomSerieAndInstanceAnalyzer;
import org.shanoir.ng.importer.dicom.InstanceNumberSorter;
import org.shanoir.ng.importer.dicom.PatientNameSorter;
import org.shanoir.ng.importer.dicom.SeriesNumberOrDescriptionSorter;
import org.shanoir.ng.importer.dicom.StudyDateSorter;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;

import jakarta.annotation.PostConstruct;

@Service
public class QueryPACSService {
	
	private static final Logger LOG = LoggerFactory.getLogger(QueryPACSService.class);

	@Value("${shanoir.import.pacs.query.aet.calling.name}")
	private String callingName;

	@Value("${shanoir.import.pacs.query.aet.calling.host}")
	private String callingHost;
	
	@Value("${shanoir.import.pacs.query.aet.calling.port}")
	private Integer callingPort;
	
	@Value("${shanoir.import.pacs.query.aet.called.name}")
	private String calledName;

	@Value("${shanoir.import.pacs.query.aet.called.host}")
	private String calledHost;
	
	@Value("${shanoir.import.pacs.query.aet.called.port}")
	private Integer calledPort;
	
	@Value("${shanoir.import.pacs.query.maxPatients}")
	private Integer maxPatientsFromPACS;
	
	private DicomNode calling;
	
	private DicomNode called;
	
	@Value("${shanoir.import.pacs.store.aet.called.name}")
	private String calledNameSCP;
	
	public QueryPACSService() {} // for ShUp usage
	
	/**
	 * Used within microservice MS Import on the server, via PostConstruct.
	 */
	@PostConstruct
	private void initDicomNodes() {
		// Initialize connection configuration parameters here: to be used for all queries
		this.calling = new DicomNode(callingName, callingHost, callingPort);
		this.called = new DicomNode(calledName, calledHost, calledPort);
		LOG.info("Query: DicomNodes initialized via CDI: calling ({}, {}, {}) and called ({}, {}, {})",
				callingName, callingHost, callingPort, calledName, calledHost, calledPort);
	}
	
	/**
	 * Do configuration of QueryPACSService from outside. Used by ShanoirUploader.
	 * 
	 * @param calling
	 * @param called
	 * @param calledNameSCP
	 */
	public void setDicomNodes(DicomNode calling, DicomNode called, String calledNameSCP) {
		this.calling = calling;
		this.called = called;
		this.calledNameSCP = calledNameSCP;
		this.maxPatientsFromPACS = 20;
		LOG.info("Query: DicomNodes initialized via method call (ShUp): calling ({}, {}, {}) and called ({}, {}, {})",
				calling.getAet(), calling.getHostname(), calling.getPort(), called.getAet(), called.getHostname(), called.getPort());
	}
	
	private Association connectAssociation(DicomNode calling, DicomNode called, boolean cfind) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        try {
        	// calling: create a device, a connection and an application entity
        	Device device = new Device(this.getClass().getName());
            device.setExecutor(executor);
            device.setScheduledExecutor(scheduledExecutor);
        	ApplicationEntity callingAE = new ApplicationEntity(calling.getAet());
            Connection callingConn = new Connection();
            device.addConnection(callingConn);
            device.addApplicationEntity(callingAE);
            callingAE.addConnection(callingConn);
            // called: create a connection and an AAssociateRQ
            Connection calledConn = new Connection(null, called.getHostname(), called.getPort());
            AAssociateRQ aarq = new AAssociateRQ();
            aarq.setCallingAET(calling.getAet());
            aarq.setCalledAET(called.getAet());
            if (cfind) {
	            aarq.addPresentationContext(new PresentationContext(1,
	                    UID.Verification, UID.ImplicitVRLittleEndian));
	            aarq.addPresentationContext(new PresentationContext(2,
	            		UID.PatientRootQueryRetrieveInformationModelFind, UID.ImplicitVRLittleEndian));
	            aarq.addPresentationContext(new PresentationContext(3,
	            		UID.StudyRootQueryRetrieveInformationModelFind, UID.ImplicitVRLittleEndian));
            } else {
                aarq.addPresentationContext(new PresentationContext(1,
                		UID.StudyRootQueryRetrieveInformationModelMove, UID.ImplicitVRLittleEndian));
            }
            Association association = callingAE.connect(calledConn, aarq);
            LOG.info("connectAssociation finished between calling {} and called {}", calling.getAet(), called.getAet());
            return association;
        } catch (IOException | InterruptedException | IncompatibleConnectionException | GeneralSecurityException e) {
			LOG.error(e.getMessage(), e);
			throw e;
        }
	}

	private void releaseAssociation(Association association) {
		try {
			association.release();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		ExecutorService executorService = (ExecutorService) association.getDevice().getExecutor();
		executorService.shutdown();
		association.getDevice().getScheduledExecutor().shutdown();
		LOG.info("releaseAssociation finished between calling {} and called {}", calling.getAet(), called.getAet());
	}
	
	public ImportJob queryCFIND(DicomQuery dicomQuery) throws Exception {
		LOG.debug("--------------------");
		LOG.debug("--- START C-FIND ---");
		LOG.debug("--------------------");
		long start = System.currentTimeMillis();
		Association association = connectAssociation(calling, called, true);
		ImportJob importJob = new ImportJob();
		if (StringUtils.isNotBlank(dicomQuery.getPatientName())
			|| StringUtils.isNotBlank(dicomQuery.getPatientID())
			|| StringUtils.isNotBlank(dicomQuery.getPatientBirthDate())
			|| StringUtils.isNotBlank(dicomQuery.getStudyDescription())
			|| StringUtils.isNotBlank(dicomQuery.getStudyDate())) {
			// patient root query level
			if (!dicomQuery.isStudyRootQuery()) {
				queryPatientLevel(association, dicomQuery, importJob);
			// study root query level
			} else {
				queryStudyLevel(association, dicomQuery, importJob);
			}
		} else {
			throw new ShanoirImportException("DicomQuery: missing parameters.");
		}
		releaseAssociation(association);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		LOG.debug("Duration of all calls of queryCFIND " + timeElapsed + "ms.");
		LOG.debug("--------------------");
		LOG.debug("--- END C-FIND -----");
		LOG.debug("--------------------");
		return importJob;
	}

	public void queryCFINDInstances(String studyInstanceUID, Serie serie) throws Exception {
		LOG.debug("------------------------------");
		LOG.debug("--- START C-FIND Instances ---");
		LOG.debug("------------------------------");
		long start = System.currentTimeMillis();
		Association association = connectAssociation(calling, called, true);
		LOG.info("Query instances/images (before c-move) for serie: " + serie.getSeriesDescription());
		queryInstances(association, studyInstanceUID, serie);
		releaseAssociation(association);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		LOG.debug("Duration of all calls of queryCFIND " + timeElapsed + "ms.");
		LOG.debug("------------------------------");
		LOG.debug("--- END C-FIND Instances -----");
		LOG.debug("------------------------------");
	}

	public void queryCFINDsInstances(String studyInstanceUID, List<Serie> selectedSeries) throws Exception {
		LOG.debug("------------------------------");
		LOG.debug("--- START C-FIND Instances ---");
		LOG.debug("------------------------------");
		long start = System.currentTimeMillis();
		Association association = connectAssociation(calling, called, true);
		for (Serie serie : selectedSeries) {
			LOG.info("Query instances/images (before c-move) for serie: " + serie.getSeriesDescription());
			queryInstances(association, studyInstanceUID, serie);
		}
		releaseAssociation(association);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		LOG.debug("Duration of all calls of queryCFIND " + timeElapsed + "ms.");
		LOG.debug("------------------------------");
		LOG.debug("--- END C-FIND Instances -----");
		LOG.debug("------------------------------");
	}

	public void queryCMOVEs(String studyInstanceUID, Set<Serie> selectedSeries, JProgressBar progressBar) throws Exception {
		LOG.info("--------------------");
		LOG.info("--- START C-MOVES --");
		LOG.info("--------------------");
		long start = System.currentTimeMillis();
		Association association = connectAssociation(calling, called, false);
		int totalPercent = 0;
		int serieNumber = 0;
		int numberOfSeries = selectedSeries.size();
		for (Serie serie : selectedSeries) {
			serieNumber++;
			queryCMOVEPerSerie(studyInstanceUID, serie, association);
			totalPercent = Math.round(((float) serieNumber / numberOfSeries) * 100);
			progressBar.setValue(totalPercent);
		}
		releaseAssociation(association);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		LOG.info("Duration of all series " + timeElapsed + "ms.");
		LOG.info("--------------------");
		LOG.info("--- END C-MOVES ----");
		LOG.info("--------------------");
	}
	
	public void queryCMOVE(String studyInstanceUID, Serie serie) throws Exception {
		LOG.info("--------------------");
		LOG.info("--- START C-MOVE ---");
		LOG.info("--------------------");
		long start = System.currentTimeMillis();
		Association association = connectAssociation(calling, called, false);
		queryCMOVEPerSerie(studyInstanceUID, serie, association);
		releaseAssociation(association);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		LOG.info("Duration of one serie " + timeElapsed + "ms.");
		LOG.info("--------------------");
		LOG.info("--- END C-MOVE -----");
		LOG.info("--------------------");
	}

	private void queryCMOVEPerSerie(String studyInstanceUID, Serie serie, Association association) {
		DicomParam[] params = {
			new DicomParam(Tag.QueryRetrieveLevel, "SERIES"),
			new DicomParam(Tag.StudyInstanceUID, studyInstanceUID),
			new DicomParam(Tag.SeriesInstanceUID, serie.getSeriesInstanceUID()) };
		LOG.info("Calling DICOM server, C-MOVE for serie: {} of study: {}", serie.getSeriesDescription(), studyInstanceUID);
		queryCMove(association, params);
	}
	
	public boolean queryECHO(String calledAET, String hostName, int port, String callingAET) {
		LOG.info("DICOM ECHO: Starting with configuration {}, {}, {} <- {}", calledAET, hostName, port, callingAET);
        try {
    		DicomNode called = new DicomNode(calledAET, hostName, port);
    		DicomNode calling = new DicomNode(callingAET);
    		Association association = connectAssociation(calling, called, true);
        	association.cecho();
            releaseAssociation(association);
        } catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
        return true;
	}

	/**
	 * This method queries on patient root level.
	 * @param dicomQuery
	 * @param importJob
	 */
	private void queryPatientLevel(Association association, DicomQuery dicomQuery, ImportJob importJob) {
		DicomParam modality = initDicomParam(Tag.Modality, dicomQuery.getModality());
		DicomParam patientName = initDicomParam(Tag.PatientName, dicomQuery.getPatientName());
		DicomParam patientID = initDicomParam(Tag.PatientID, dicomQuery.getPatientID());
		DicomParam patientBirthDate = initDicomParam(Tag.PatientBirthDate, dicomQuery.getPatientBirthDate());
		DicomParam studyDescription = initDicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription());
		DicomParam studyDate = initDicomParam(Tag.StudyDate, dicomQuery.getStudyDate());
		DicomParam[] params = {modality, patientName, patientID, patientBirthDate, new DicomParam(Tag.PatientBirthName), new DicomParam(Tag.PatientSex), studyDescription, studyDate };
		List<Attributes> patientsAttr = queryCFind(association, params, QueryRetrieveLevel.PATIENT);
		if (patientsAttr != null) {
			// Limit the max number of patients returned
			int patientsNbre = patientsAttr.size();
			if (maxPatientsFromPACS < patientsAttr.size()) {
				patientsNbre = maxPatientsFromPACS;
			}
			List<Patient> patients = new ArrayList<Patient>();
			IntStream.range(0, patientsNbre).sequential().forEach(i -> {
			    Patient patient = new Patient(patientsAttr.get(i));
			    boolean patientExists = patients.parallelStream().anyMatch(p -> p.getPatientID().equals(patient.getPatientID()));
			    if (!patientExists) {
			        synchronized (patients) {
			            patients.add(patient);
			        }
			        queryStudies(association, dicomQuery, patient);
			    }
			});
			patients.sort(new PatientNameSorter());
			importJob.setPatients(patients);
		}
	}

	/**
	 * This method queries on study root level.
	 * 
	 * @param dicomQuery
	 * @param calling
	 * @param called
	 * @param importJob
	 */
	private void queryStudyLevel(Association association, DicomQuery dicomQuery, ImportJob importJob) {
		DicomParam modality = initDicomParam(Tag.Modality, dicomQuery.getModality());
		DicomParam studyDescription = initDicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription());
		DicomParam studyDate = initDicomParam(Tag.StudyDate, dicomQuery.getStudyDate());
		DicomParam patientName = initDicomParam(Tag.PatientName, dicomQuery.getPatientName());
		DicomParam patientID = initDicomParam(Tag.PatientID, dicomQuery.getPatientID());
		DicomParam patientBirthDate = initDicomParam(Tag.PatientBirthDate, dicomQuery.getPatientBirthDate());
		DicomParam[] params = { modality, studyDescription, studyDate, new DicomParam(Tag.PatientName),
			new DicomParam(Tag.PatientID), new DicomParam(Tag.PatientBirthDate), new DicomParam(Tag.PatientBirthName),
			new DicomParam(Tag.PatientSex), new DicomParam(Tag.StudyInstanceUID), patientName, patientID, patientBirthDate };
		List<Attributes> studies = queryCFind(association, params, QueryRetrieveLevel.STUDY);
		// list of all corresponding DICOM studies received
		if (studies != null) {
			List<Patient> patients = new ArrayList<Patient>();
			studies.parallelStream().forEach(s -> processDICOMStudy(s, association, modality, patients));
			patients.sort(new PatientNameSorter());
			importJob.setPatients(patients);
		}
	}

	private void processDICOMStudy(Attributes studyAttr, Association association, DicomParam modality,
			List<Patient> patients) {
		// handle patient: create patient from attributes
		Patient patient = new Patient(studyAttr);
		patient.setStudies(new ArrayList<Study>());
		boolean newPatient = true;
		// patient already exists?
		synchronized (patients) {
			for (Iterator<Patient> iterator = patients.iterator(); iterator.hasNext();) {
				Patient existingPatient = iterator.next();
				if (existingPatient.getPatientID().equals(patient.getPatientID())) {
					patient = existingPatient;
					newPatient = false;
				}
			}
		}
		boolean maxPatientsFromPACSReached = false;
		if (newPatient) {
			// Limit the max number of patients processed
			if (patients.size() < maxPatientsFromPACS) {
				synchronized (patients) {
		            patients.add(patient);
		        }
			} else {
				maxPatientsFromPACSReached = true;
				// we do not stop the method here in case other studies
				// follow afterwards from patients, that exist already
				// maybe no guarantee here, that the DICOM server returns
				// studies grouped by patient
			}
		}
		if (!maxPatientsFromPACSReached) {
			handleStudy(studyAttr, association, modality, patient);
		} else {
			if (!newPatient) { // only process existing patients, in case
				handleStudy(studyAttr, association, modality, patient);
			}
		}
		synchronized (patient.getStudies()) {
			patient.getStudies().sort(new StudyDateSorter());
		}
	}

	private void handleStudy(Attributes studyAttr, Association association, DicomParam modality,
			Patient patient) {
		Study study = new Study(studyAttr);
		synchronized (patient.getStudies()) {
			patient.getStudies().add(study);
		}
		// use now study date returned from the DICOM server
		String dicomResponseStudyDate = studyAttr.getString(Tag.StudyDate);
		querySeries(association, study, modality, dicomResponseStudyDate);
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
	 * @param dicomQuery 
	 * @param patient
	 */
	private void queryStudies(Association association, DicomQuery dicomQuery, Patient patient) {
		DicomParam modality = initDicomParam(Tag.Modality, dicomQuery.getModality());
		DicomParam patientName = initDicomParam(Tag.PatientName, patient.getPatientName());
		DicomParam patientID = initDicomParam(Tag.PatientID, patient.getPatientID());
		DicomParam studyDescription = initDicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription());
		// query studies, at first using the potential study date entered by the user via the GUI
		// most users will leave this empty, when the query patient root level queries
		DicomParam dicomQueryStudyDate = initDicomParam(Tag.StudyDate, dicomQuery.getStudyDate());
		DicomParam[] params = {
			modality,
			patientName,
			patientID,
			new DicomParam(Tag.StudyInstanceUID),
			studyDescription,
			dicomQueryStudyDate
		};
		List<Attributes> studiesAttr = queryCFind(association, params, QueryRetrieveLevel.STUDY);
		if (studiesAttr != null) {
			List<Study> studies = new ArrayList<Study>();
			studiesAttr.parallelStream().forEach(studyAttr -> {
			    Study study = new Study(studyAttr);
			    synchronized (studies) {
					LOG.info("Study found in DICOM server: " + study.toString());
			        studies.add(study);
			    }
			    String dicomResponseStudyDate = studyAttr.getString(Tag.StudyDate);
			    querySeries(association, study, modality, dicomResponseStudyDate);
			});
			studies.sort(new StudyDateSorter());
			patient.setStudies(studies);
		}
	}

	/**
	 * This method queries for series, creates them and adds them to studies.
	 * 
	 * @param calling
	 * @param called
	 * @param study
	 */
	private void querySeries(Association association, Study study, DicomParam modality, String studyDateStr) {
		DicomParam studyInstanceUID = initDicomParam(Tag.StudyInstanceUID, study.getStudyInstanceUID());
		DicomParam studyDate = initDicomParam(Tag.StudyDate, studyDateStr);
		DicomParam[] params = {
			modality,
			studyInstanceUID,
			studyDate,
			new DicomParam(Tag.SeriesInstanceUID),
			new DicomParam(Tag.NumberOfSeriesRelatedInstances),
			new DicomParam(Tag.SeriesDescription),
			new DicomParam(Tag.SeriesDate),
			new DicomParam(Tag.SeriesNumber),
			new DicomParam(Tag.ProtocolName),
			new DicomParam(Tag.Manufacturer),
			new DicomParam(Tag.ManufacturerModelName),
			new DicomParam(Tag.DeviceSerialNumber)
		};
		List<Attributes> seriesAttr = queryCFind(association, params, QueryRetrieveLevel.SERIES);
		if (seriesAttr != null) {
			List<Serie> series = new ArrayList<Serie>();
			seriesAttr.parallelStream().forEach(s -> processDICOMSerie(s, association, study, modality, series));
			series.sort(new SeriesNumberOrDescriptionSorter());
			study.setSeries(series);
		}
	}

	private void processDICOMSerie(Attributes serieAttr, Association association, Study study, DicomParam modality, List<Serie> series) {
		Serie serie = new Serie(serieAttr);
		if (!DicomSerieAndInstanceAnalyzer.checkSerieIsIgnored(serieAttr)) {
			if (serie.getNumberOfSeriesRelatedInstances() > 0) {
				DicomSerieAndInstanceAnalyzer.checkSerieIsEnhanced(serie, serieAttr);
				DicomSerieAndInstanceAnalyzer.checkSerieIsSpectroscopy(serie);
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
		synchronized (series) {
			LOG.info("Serie found in DICOM server: " + serie.toString());
			series.add(serie);			
		}
	}
	
	/**
	 * This method queries for instances/images, creates them and adds them to series.
	 * 
	 * @param calling
	 * @param called
	 * @param serie
	 */
	private void queryInstances(Association association, String studyInstanceUID, Serie serie) {
		DicomParam modality = initDicomParam(Tag.Modality, serie.getModality());
		DicomParam studyInstanceUIDParam = initDicomParam(Tag.StudyInstanceUID, studyInstanceUID);
		DicomParam seriesInstanceUIDParam = initDicomParam(Tag.SeriesInstanceUID, serie.getSeriesInstanceUID());
		DicomParam[] params = {
			modality,
			studyInstanceUIDParam,
			seriesInstanceUIDParam,
			new DicomParam(Tag.SOPInstanceUID),
			new DicomParam(Tag.InstanceNumber)
		};
		List<Attributes> instancesAttr = queryCFind(association, params, QueryRetrieveLevel.IMAGE);
		if (instancesAttr != null) {
			List<Instance> instances = new ArrayList<>();
			instancesAttr.parallelStream().forEach(i -> {
				Instance instance = new Instance(i);
				if (!DicomSerieAndInstanceAnalyzer.checkInstanceIsIgnored(i)) {
					synchronized (instances) {
						LOG.debug("Adding instance: " + instance.toString());
						instances.add(instance);						
					}
				}
			});
			if (!instances.isEmpty()) {
				instances.sort(new InstanceNumberSorter());
				serie.setInstances(instances);
				LOG.info(instances.size() + " instances found for serie " + serie.getSeriesDescription());
			} else {
				LOG.warn("Serie found with empty instances and therefore ignored (SeriesDescription: {}, SerieInstanceUID: {}).", serie.getSeriesDescription(), serie.getSeriesInstanceUID());
				serie.setIgnored(true);
				serie.setSelected(false);
			}
		}
	}

	/**
	 * This method does a C-FIND query and returns the results.
	 * 
	 * The state of each c-find query is a local attribute of the method.
	 * So, when e.g. on the server 3 users call in parallel queryCFind,
	 * each one has its own DimseRSPHandler and its own state, so this
	 * might work, in case the association is not caching aspects.
	 * 
	 * @param params
	 * @param level
	 * @return
	 */
	private List<Attributes> queryCFind(Association association, DicomParam[] params, QueryRetrieveLevel level) {
		long start = System.currentTimeMillis();
		String cuid = getCUID(level);
		DicomState state = new DicomState(new DicomProgress());
		DimseRSPHandler rspHandler = createCFindRSPHandler(association, state);
		try {
			Attributes attributes = new Attributes();
			attributes.setString(Tag.QueryRetrieveLevel, VR.CS, level.name());
			for (DicomParam p : params) {
				addAttributes(attributes, p);
			}
			LOG.debug("Calling DICOM server, C-FIND with level: {}", level);
			for (int i = 0; i < params.length; i++) {
				LOG.debug("Tag: {}, Value: {}", params[i].getTagName(), Arrays.toString(params[i].getValues()));
			}
			association.cfind(cuid, Priority.NORMAL, attributes, null, rspHandler);
	        if (association.isReadyForDataTransfer()) {
	            association.waitForOutstandingRSP();
	        }
		} catch (IOException | InterruptedException e) {
			LOG.error("Error in c-find query:", e);
		}
		List<Attributes> response = state.getDicomRSP();
		LOG.info("C-FIND-RESPONSE NB. ELEMENTS: " + response.size());
		LOG.debug("C-FIND-RESPONSE CONTENT:\n" + response);
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		LOG.debug("Duration of C-FIND: " + timeElapsed + "ms.");
		return response;
	}

	private String getCUID(QueryRetrieveLevel level) {
		if (level.equals(QueryRetrieveLevel.PATIENT)) {
			return UID.PatientRootQueryRetrieveInformationModelFind;
		} else if (level.equals(QueryRetrieveLevel.STUDY)) {
			return UID.StudyRootQueryRetrieveInformationModelFind;
		} else if (level.equals(QueryRetrieveLevel.SERIES)) {
			return UID.StudyRootQueryRetrieveInformationModelFind;
		} else if (level.equals(QueryRetrieveLevel.IMAGE)) {
			return UID.StudyRootQueryRetrieveInformationModelFind;
		}
		return null;
	}

	private DimseRSPHandler createCFindRSPHandler(Association association, DicomState state) {
		DimseRSPHandler rspHandler = new DimseRSPHandler(association.nextMessageID()) {
			@Override
			public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
				super.onDimseRSP(as, cmd, data);
				int status = cmd.getInt(Tag.Status, -1);
				if (Status.isPending(status)) {
			        state.addDicomRSP(data);
				} else {
					state.setStatus(status);
				}
			}
		};
		return rspHandler;
	}

	private List<Attributes> queryCMove(Association association, DicomParam[] params) {
		DicomState state = new DicomState(new DicomProgress());
        DimseRSPHandler rspHandler = createCMoveRSPHandler(association, state);
		Attributes attributes = new Attributes();
		for (DicomParam p : params) {
			addAttributes(attributes, p);
		}
		try {
			association.cmove(UID.StudyRootQueryRetrieveInformationModelMove, Priority.NORMAL, attributes, null, calledNameSCP, rspHandler);
	        if (association.isReadyForDataTransfer()) {
	            association.waitForOutstandingRSP();
	        }
		} catch (IOException | InterruptedException e) {
			LOG.error("Error in c-move query: ", e);
		}
		List<Attributes> response = state.getDicomRSP();
		LOG.info("C-MOVE-RESPONSE NB. ELEMENTS: " + response.size());
		LOG.debug("C-MOVE-RESPONSE CONTENT:\n" + response);
		return response;
	}

	private DimseRSPHandler createCMoveRSPHandler(Association association, DicomState state) {
		DimseRSPHandler rspHandler = new DimseRSPHandler(association.nextMessageID()) {
            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                DicomProgress p = state.getProgress();
                if (p != null) {
                    p.setAttributes(cmd);
                    if (p.isCancel()) {
                        try {
                            this.cancel(as);
                        } catch (IOException e) {
                            LOG.error("Cancel C-MOVE", e);
                        }
                    }
                }
            }
        };
		return rspHandler;
	}

    private void addAttributes(Attributes attrs, DicomParam param) {
        int tag = param.getTag();
        String[] ss = param.getValues();
        VR vr = ElementDictionary.vrOf(tag, attrs.getPrivateCreator(tag));
        if (ss == null || ss.length == 0) {
            // Returning key
            if (vr == VR.SQ) {
                attrs.newSequence(tag, 1).add(new Attributes(0));
            } else {
                attrs.setNull(tag, vr);
            }
        } else {
            // Matching key
            attrs.setString(tag, vr, ss);
        }
    }

}
