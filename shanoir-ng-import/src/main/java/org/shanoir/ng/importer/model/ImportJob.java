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

 package org.shanoir.ng.importer.model;

 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections4.CollectionUtils;
 import org.shanoir.ng.importer.dicom.query.DicomQuery;
 import org.shanoir.ng.shared.event.ShanoirEvent;
 import org.shanoir.ng.shared.quality.QualityTag;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 /**
  * One ImportJob is related to the import of ONE DICOM STUDY,
  * which equals ONE EXAM in Shanoir. We are doing this, as one
  * DICOM study can have a size of up to 10Gb nowadays. This means
  * we process already a huge amount of data for one import, that
  * can take up to 30-45 minutes. There is no sense in extending this
  * further for the future to anything like multi-exam in one import,
  * so the model has to be kept:
  * 1 ImportJob (1 DICOM study/exam) - 1 subject relation
  *                                  - 1 exam relation
  * IF in an ImportJob contains a subject object, it means to create one
  * in ms studies during the import.
  * If it contains a subjectName, an existing subject is to use.
  * Same logic for the exams.
  * 
  * @todo: later we will remove the patients list from here, that is a
  * legacy error, that has to be corrected, e.g. move the subject out into
  * import job as written above.
  * 
  * @author mkain
  */
 public class ImportJob implements Serializable {
 
	 private static final long serialVersionUID = 8804929608059674037L;
	 
	 private long timestamp;
 
	 /* DicomQuery, that has been used to extract the DICOM study = ImportJob */
	 private DicomQuery dicomQuery;
 
	 private boolean fromDicomZip;
 
	 private boolean fromShanoirUploader;
 
	 private boolean fromPacs;
	 
	 private String workFolder;
 
	 // @todo: remove this list here later
	 private List<Patient> patients;
 
	 // DICOM patient for this import job
	 private Patient patient;
 
	 private PatientVerification patientVerification;
	 
	 // DICOM study for this import job
	 private Study study;
 
	 // series to import with this import job
	 private Set<Serie> selectedSeries;
 
	 // Shanoir study
	 private Long studyId;
	 
	 private String studyName;
 
	 private Long studyCardId;
	 
	 private String studyCardName;
	 
	 private Long acquisitionEquipmentId;
	 
	 // subject: use already existing
	 private String subjectName;
 
	 // subject: create new subject in ms studies based on these values
	 private Subject subject;
 
	 // examination: use already existing
	 private Long examinationId;
 
	 private String examinationComment;
 
	 private String anonymisationProfileToUse;
 
	 private String archive;
	 
	 private ShanoirEvent shanoirEvent;
 
	 private Long userId;
 
	 private String username;
 
	 private Long centerId;
 
	 private String errorMessage;
 
	 private QualityTag qualityTag;
 
	 // Used by ShanoirUploader to store the upload state
	 private UploadState uploadState;
 
	 // Used by ShanoirUploader to store the upload percentage
	 private String uploadPercentage; 
 
	 public long getTimestamp() {
		 return timestamp;
	 }
 
	 public void setTimestamp(long timestamp) {
		 this.timestamp = timestamp;
	 }
 
	 public String getArchive() {
		 return archive;
	 }
 
	 public void setArchive(final String archive) {
		 this.archive = archive;
	 }
 
	 public boolean isFromDicomZip() {
		 return fromDicomZip;
	 }
 
	 public void setFromDicomZip(final boolean fromDicomZip) {
		 this.fromDicomZip = fromDicomZip;
	 }
 
	 public boolean isFromShanoirUploader() {
		 return fromShanoirUploader;
	 }
 
	 public void setFromShanoirUploader(final boolean fromShanoirUploader) {
		 this.fromShanoirUploader = fromShanoirUploader;
	 }
 
	 public boolean isFromPacs() {
		 return fromPacs;
	 }
 
	 public void setFromPacs(final boolean fromPacs) {
		 this.fromPacs = fromPacs;
	 }
 
	 public List<Patient> getPatients() {
		 return patients;
	 }
 
	 public void setPatients(final List<Patient> patients) {
		 this.patients = patients;
	 }
 
	 public Long getExaminationId() {
		 return examinationId;
	 }
 
	 public void setExaminationId(final Long examinationId) {
		 this.examinationId = examinationId;
	 }
 
	 public String getWorkFolder() {
		 return workFolder;
	 }
 
	 public void setWorkFolder(String workFolder) {
		 this.workFolder = workFolder;
	 }
 
	 public Long getStudyId() {
		 return studyId;
	 }
 
	 public void setStudyId(final Long studyId) {
		 this.studyId = studyId;
	 }
 
	 public String getStudyCardName() {
		 return studyCardName;
	 }
 
	 public void setStudyCardName(String studyCardName) {
		 this.studyCardName = studyCardName;
	 }
 
	 public Long getAcquisitionEquipmentId() {
		 return acquisitionEquipmentId;
	 }
 
	 public void setAcquisitionEquipmentId(final Long acquisitionEquipmentId) {
		 this.acquisitionEquipmentId = acquisitionEquipmentId;
	 }
 
	 public Long getStudyCardId() {
		 return studyCardId;
	 }
 
	 public void setStudyCardId(Long studyCardId) {
		 this.studyCardId = studyCardId;
	 }
 
	 public String getAnonymisationProfileToUse() {
		 return anonymisationProfileToUse;
	 }
 
	 public void setAnonymisationProfileToUse(String anonymisationProfileToUse) {
		 this.anonymisationProfileToUse = anonymisationProfileToUse;
	 }
 
	 public String getSubjectName() {
		 return subjectName;
	 }
 
	 public void setSubjectName(String subjectName) {
		 this.subjectName = subjectName;
	 }
 
	 public String getStudyName() {
		 return studyName;
	 }
 
	 public void setStudyName(String studyName) {
		 this.studyName = studyName;
	 }
 
	 public ShanoirEvent getShanoirEvent() {
		 return shanoirEvent;
	 }
 
	 public void setShanoirEvent(ShanoirEvent shanoirEvent) {
		 this.shanoirEvent = shanoirEvent;
	 }
 
	 public Long getUserId() {
		 return userId;
	 }
 
	 public void setUserId(Long userId) {
		 this.userId = userId;
	 }
 
	 public String getUsername() {
		 return username;
	 }
 
	 public void setUsername(String username) {
		 this.username = username;
	 }
 
	 public Long getCenterId() {
		 return centerId;
	 }
 
	 public void setCenterId(Long centerId) {
		 this.centerId = centerId;
	 }
 
	 public Subject getSubject() {
		 return subject;
	 }
 
	 public void setSubject(Subject subject) {
		 this.subject = subject;
	 }
 
	 public Study getStudy() {
		 return study;
	 }
 
	 public void setStudy(Study study) {
		 this.study = study;
	 }
 
	 public Patient getPatient() {
		 return patient;
	 }
 
	 public void setPatient(Patient patient) {
		 this.patient = patient;
	 }
 
	 public Set<Serie> getSelectedSeries() {
		 return selectedSeries;
	 }
 
	 public void setSelectedSeries(Set<Serie> selectedSeries) {
		 this.selectedSeries = selectedSeries;
	 }
 
	 public DicomQuery getDicomQuery() {
		 return dicomQuery;
	 }
 
	 public void setDicomQuery(DicomQuery dicomQuery) {
		 this.dicomQuery = dicomQuery;
	 }
 
	 public String getErrorMessage() {
		 return errorMessage;
	 }
 
	 public void setErrorMessage(String errorMessage) {
		 this.errorMessage = errorMessage;
	 }
 
	 public String getExaminationComment() {
		 return examinationComment;
	 }
 
	 public void setExaminationComment(String examinationComment) {
		 this.examinationComment = examinationComment;
	 }
 
	 public PatientVerification getPatientVerification() {
		 return patientVerification;
	 }
 
	 public void setPatientVerification(PatientVerification patientVerification) {
		 this.patientVerification = patientVerification;
	 }
 
	 public QualityTag getQualityTag() {
		 return qualityTag;
	 }
 
	 public void setQualityTag(QualityTag qualityTag) {
		 this.qualityTag = qualityTag;
	 }
 
	 public UploadState getUploadState() {
		 return uploadState;
	 }
 
	 public void setUploadState(UploadState uploadState) {
		 this.uploadState = uploadState;
	 }
 
	 public String getUploadPercentage() {
		 return uploadPercentage;
	 }
 
	 public void setUploadPercentage(String uploadPercentage) {
		 this.uploadPercentage = uploadPercentage;
	 }
 
	 @Override
	 public String toString() {
		 String importType;
		 if (fromDicomZip) {
			 importType = "ZIP";
		 } else if (fromShanoirUploader) {
			 importType = "SHUP";
		 } else if (fromPacs) {
			 importType = "PACS";
		 } else {
			 importType = "UNSUPPORTED";
		 }
		 int numberOfSeries = 0;
		 StringBuffer seriesNames = new StringBuffer();
		 seriesNames.append("[");
		 String modality = "unknown";
		 boolean enhanced = false;
		 if (CollectionUtils.isNotEmpty(patients)) {
			 Patient patient = patients.get(0);
			 if (CollectionUtils.isNotEmpty(patient.getStudies())) {
				 Study study = patient.getStudies().get(0);
				 List<Serie> series = study.getSeries();
				 if (CollectionUtils.isNotEmpty(series)) {
					 numberOfSeries = series.size(); // only selected series remain at the stage of the logging call
					 Serie serie = study.getSeries().get(0);
					 modality = serie.getModality();
					 enhanced = serie.getIsEnhanced();
					 for (Iterator iterator = series.iterator(); iterator.hasNext();) {
						 serie = (Serie) iterator.next();
						 if (iterator.hasNext()) {
							 seriesNames.append(serie.getSequenceName() + ",");
						 } else {
							 seriesNames.append(serie.getSequenceName() + "]");
						 }
					 }
				 }
			 }
		 }
		 return 	"userId=" + userId + ",studyName=" + studyName + ",studyCardId=" + studyCardId + ",type=" + importType +
				 ",workFolder=" + workFolder + ",pseudoProfile=" + anonymisationProfileToUse + ",modality=" + modality + ",enhanced=" + enhanced +
				 ",subjectName=" + subjectName + ",examId=" + examinationId + ",numberOfSeries=" + numberOfSeries +
				 ",seriesNames=" + seriesNames.toString();
	 }
 
	 @JsonIgnore
	 public Serie getFirstSelectedSerie() {
		 if (CollectionUtils.isNotEmpty(selectedSeries)) {
			 return selectedSeries.iterator().next();
		 }
		 return null;
	 }	
 
 }