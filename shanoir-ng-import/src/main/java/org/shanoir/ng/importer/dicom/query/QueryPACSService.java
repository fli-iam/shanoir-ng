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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.shanoir.ng.importer.dicom.DicomSerieAndInstanceAnalyzer;
import org.shanoir.ng.importer.dicom.InstanceNumberSorter;
import org.shanoir.ng.importer.dicom.SeriesNumberSorter;
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
import org.weasis.dicom.op.CMove;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.param.ProgressListener;

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
	
	private Association association;
	
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
		this.maxPatientsFromPACS = 10;
		LOG.info("Query: DicomNodes initialized via method call (ShUp): calling ({}, {}, {}) and called ({}, {}, {})",
				calling.getAet(), calling.getHostname(), calling.getPort(), called.getAet(), called.getHostname(), called.getPort());
	}
	
	private void connectAssociation(DicomNode calling, DicomNode called) throws Exception {
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
            aarq.addPresentationContext(new PresentationContext(1,
                    UID.Verification, UID.ImplicitVRLittleEndian));
            aarq.addPresentationContext(new PresentationContext(2,
            		UID.PatientRootQueryRetrieveInformationModelFind, UID.ImplicitVRLittleEndian));
            aarq.addPresentationContext(new PresentationContext(3,
            		UID.StudyRootQueryRetrieveInformationModelFind, UID.ImplicitVRLittleEndian));
            this.association = callingAE.connect(calledConn, aarq);
            LOG.info("connectAssociation finished between calling {} and called {}", calling.getAet(), called.getAet());
        } catch (IOException | InterruptedException | IncompatibleConnectionException | GeneralSecurityException e) {
			LOG.error(e.getMessage(), e);
			throw e;
        }
	}
	
	public ImportJob queryCFIND(DicomQuery dicomQuery) throws Exception {
		connectAssociation(calling, called);
		ImportJob importJob = new ImportJob();
		/**
		 * In case of any patient specific search field is filled, work on patient level. Highest priority.
		 */
		if (StringUtils.isNotBlank(dicomQuery.getPatientName())
			|| StringUtils.isNotBlank(dicomQuery.getPatientID())
			|| StringUtils.isNotBlank(dicomQuery.getPatientBirthDate())) {
			// For Patient Name and Patient ID, wild card search is not allowed
			if (!dicomQuery.getPatientName().contains("*") && !dicomQuery.getPatientID().contains("*")) {
				queryPatientLevel(dicomQuery, importJob);
			}
			// @Todo: implement wild card search
			// Do Fuzzy search on base of patient name here
//			if (StringUtils.isNotEmpty(dicomQuery.getPatientName())
//				&& (dicomQuery.getPatientName().contains("*") || !dicomQuery.getPatientName().contains("^"))) {
			// Do precise search here, using name, id or date
//			} else {
//			}
		/**
		 * In case of any study specific search field is filled, work on study level. Second priority.
		 */
		} else if (StringUtils.isNotBlank(dicomQuery.getStudyDescription())
			|| StringUtils.isNotBlank(dicomQuery.getStudyDate())) {
			queryStudyLevel(dicomQuery, importJob);
		} else {
			throw new ShanoirImportException("DicomQuery: missing parameters.");
		}
		releaseAssociation();
		return importJob;
	}

	private void releaseAssociation() {
		try {
			this.association.release();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		ExecutorService executorService = (ExecutorService) this.association.getDevice().getExecutor();
		executorService.shutdown();
		this.association.getDevice().getScheduledExecutor().shutdown();
		LOG.info("releaseAssociation finished between calling {} and called {}", calling.getAet(), called.getAet());
	}

	public void queryCMOVE(Serie serie) {
		queryCMOVE(serie.getSeriesInstanceUID());
	}

	public DicomState queryCMOVE(String seriesInstanceUID) {
		DicomProgress progress = new DicomProgress();
		progress.addProgressListener(new ProgressListener() {
			@Override
			public void handleProgression(DicomProgress progress) {
				LOG.debug("Remaining operations:{}", progress.getNumberOfRemainingSuboperations());
			}
		});
		DicomParam[] params = { new DicomParam(Tag.QueryRetrieveLevel, "SERIES"),
				new DicomParam(Tag.SeriesInstanceUID, seriesInstanceUID) };
		AdvancedParams options = new AdvancedParams();
		options.getQueryOptions().add(QueryOption.RELATIONAL); // Required for QueryRetrieveLevel other than study
		return CMove.process(options, calling, called, calledNameSCP, progress, params);
	}
	
	public boolean queryECHO(String calledAET, String hostName, int port, String callingAET) {
		LOG.info("DICOM ECHO: Starting with configuration {}, {}, {} <- {}", calledAET, hostName, port, callingAET);
        try {
    		connectAssociation(calling, called);
        	this.association.cecho();
            releaseAssociation();
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
	private void queryPatientLevel(DicomQuery dicomQuery, ImportJob importJob) {
		DicomParam patientName = initDicomParam(Tag.PatientName, dicomQuery.getPatientName());
		DicomParam patientID = initDicomParam(Tag.PatientID, dicomQuery.getPatientID());
		DicomParam patientBirthDate = initDicomParam(Tag.PatientBirthDate, dicomQuery.getPatientBirthDate());
		DicomParam[] params = { patientName, patientID, patientBirthDate, new DicomParam(Tag.PatientBirthName), new DicomParam(Tag.PatientSex) };
		List<Attributes> attributesPatients = queryCFind(params, QueryRetrieveLevel.PATIENT);
		if (attributesPatients != null) {
			// Limit the max number of patients returned
			int patientsNbre = attributesPatients.size();
			if (maxPatientsFromPACS < attributesPatients.size()) {
				patientsNbre = maxPatientsFromPACS;
			}
			List<Patient> patients = new ArrayList<>();
			for (int i = 0; i < patientsNbre; i++) {
				Patient patient = new Patient(attributesPatients.get(i));
				boolean patientExists = patients.stream().anyMatch(p -> p.getPatientID().equals(patient.getPatientID()));
				if (!patientExists) {
					patients.add(patient);
					queryStudies(dicomQuery, patient);
				}
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
	private void queryStudyLevel(DicomQuery dicomQuery, ImportJob importJob) {
		DicomParam studyDescription = initDicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription());
		DicomParam studyDate = initDicomParam(Tag.StudyDate, dicomQuery.getStudyDate());
		DicomParam[] params = { studyDescription, studyDate, new DicomParam(Tag.PatientName),
			new DicomParam(Tag.PatientID), new DicomParam(Tag.PatientBirthDate), new DicomParam(Tag.PatientBirthName),
			new DicomParam(Tag.PatientSex), new DicomParam(Tag.StudyInstanceUID) };
		List<Attributes> attributesStudies = queryCFind(params, QueryRetrieveLevel.STUDY);
		if (attributesStudies != null) {
			List<Patient> patients = new ArrayList<>();
			for (int i = 0; i < attributesStudies.size(); i++) {
				// handle patients
				Patient patient = new Patient(attributesStudies.get(i));
				patient = addPatientIfNotExisting(patients, patient);
				// handle studies
				Study study = new Study(attributesStudies.get(i));
				patient.getStudies().add(study);
				querySeries(study);
			}
			// Limit the max number of patients returned
			if (maxPatientsFromPACS < patients.size()) {
				patients = patients.subList(0, maxPatientsFromPACS);
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
		for (Iterator<Patient> iterator = patients.iterator(); iterator.hasNext();) {
			Patient patientInList = iterator.next();
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
	 * @param dicomQuery 
	 * @param patient
	 */
	private void queryStudies(DicomQuery dicomQuery, Patient patient) {
		DicomParam[] params = {
			new DicomParam(Tag.PatientID, patient.getPatientID()),
			new DicomParam(Tag.PatientName, patient.getPatientName()),
			new DicomParam(Tag.StudyInstanceUID),
			new DicomParam(Tag.StudyDate, dicomQuery.getStudyDate()),
			new DicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription())
		};
		List<Attributes> attributesStudies = queryCFind(params, QueryRetrieveLevel.STUDY);
		if (attributesStudies != null) {
			List<Study> studies = new ArrayList<>();
			for (int i = 0; i < attributesStudies.size(); i++) {
				Study study = new Study(attributesStudies.get(i));
				studies.add(study);
				querySeries(study);
			}
			studies.sort((p1, p2) -> p1.getStudyDate().compareTo(p2.getStudyDate()));
			patient.setStudies(studies);
		}
	}

	/**
	 * This method queries for series, creates them and adds them to studies.
	 * @param calling
	 * @param called
	 * @param study
	 */
	private void querySeries(Study study) {
		DicomParam[] params = {
			new DicomParam(Tag.StudyInstanceUID, study.getStudyInstanceUID()),
			new DicomParam(Tag.SeriesInstanceUID),
			new DicomParam(Tag.SOPClassUID),
			new DicomParam(Tag.SeriesDescription),
			new DicomParam(Tag.SeriesDate),
			new DicomParam(Tag.SeriesNumber),
			new DicomParam(Tag.Modality),
			new DicomParam(Tag.ProtocolName),
			new DicomParam(Tag.Manufacturer),
			new DicomParam(Tag.ManufacturerModelName),
			new DicomParam(Tag.DeviceSerialNumber)
		};
		List<Attributes> attributesList = queryCFind(params, QueryRetrieveLevel.SERIES);
		if (attributesList != null) {
			List<Serie> series = new ArrayList<Serie>();
			for (int i = 0; i < attributesList.size(); i++) {
				Attributes attributes = attributesList.get(i);
				Serie serie = new Serie(attributes);
				if (!DicomSerieAndInstanceAnalyzer.checkSerieIsIgnored(attributes)) {
					queryInstances(serie, study);
					if (!serie.getInstances().isEmpty()) {
						DicomSerieAndInstanceAnalyzer.checkSerieIsEnhanced(serie, attributes);
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
				series.add(serie);
			}
			series.sort(new SeriesNumberSorter());
			study.setSeries(series);
		}
	}
	
	/**
	 * This method queries for instances/images, creates them and adds them to series.
	 * 
	 * @param calling
	 * @param called
	 * @param serie
	 */
	private void queryInstances(Serie serie, Study study) {
		DicomParam[] params = {
			new DicomParam(Tag.StudyInstanceUID, study.getStudyInstanceUID()),
			new DicomParam(Tag.SeriesInstanceUID, serie.getSeriesInstanceUID()),
			new DicomParam(Tag.SOPInstanceUID),
			new DicomParam(Tag.InstanceNumber)
		};
		List<Attributes> attributes = queryCFind(params, QueryRetrieveLevel.IMAGE);
		if (attributes != null) {
			List<Instance> instances = new ArrayList<>();
			for (int i = 0; i < attributes.size(); i++) {
				Instance instance = new Instance(attributes.get(i));
				if (!DicomSerieAndInstanceAnalyzer.checkInstanceIsIgnored(attributes.get(i))) {
					instances.add(instance);
				}
			}
			instances.sort(new InstanceNumberSorter());
			serie.setInstances(instances);
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
	 * @param keys
	 * @param level
	 * @return
	 */
	private List<Attributes> queryCFind(DicomParam[] keys, QueryRetrieveLevel level) {
		String cuid = null;
		if (level.equals(QueryRetrieveLevel.PATIENT)) {
			cuid = UID.PatientRootQueryRetrieveInformationModelFind;
		} else if (level.equals(QueryRetrieveLevel.STUDY)) {
			cuid = UID.StudyRootQueryRetrieveInformationModelFind;
		} else if (level.equals(QueryRetrieveLevel.SERIES)) {
			cuid = UID.StudyRootQueryRetrieveInformationModelFind;
		} else if (level.equals(QueryRetrieveLevel.IMAGE)) {
			cuid = UID.StudyRootQueryRetrieveInformationModelFind;
		}
		DicomState state = new DicomState(new DicomProgress());
		DimseRSPHandler rspHandler = new DimseRSPHandler(this.association.nextMessageID()) {
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
		try {
			Attributes attributes = new Attributes();
			attributes.setString(Tag.QueryRetrieveLevel, VR.CS, level.name());
			for (DicomParam p : keys) {
				addAttributes(attributes, p);
			}
			LOG.info("Calling PACS, C-FIND with level: {}", level);
			for (int i = 0; i < keys.length; i++) {
				LOG.info("Tag: {}, Value: {}", keys[i].getTagName(), Arrays.toString(keys[i].getValues()));
			}
			association.cfind(cuid, Priority.NORMAL, attributes, null, rspHandler);
	        if (association.isReadyForDataTransfer()) {
	            association.waitForOutstandingRSP();
	        }
		} catch (IOException | InterruptedException e) {
			LOG.error("Error in c-find query: ", e);
		}
		return state.getDicomRSP();
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
