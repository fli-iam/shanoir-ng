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

import org.apache.commons.collections4.CollectionUtils;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.quality.QualityTag;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * One ImportJobBase is related to the import of ONE DICOM STUDY,
 * which equals ONE EXAMINATION in Shanoir. We are doing this, as one
 * DICOM study can have a size of up to 10Gb nowadays. This means
 * we process already a huge amount of data for one import, that
 * can take up to 30-45 minutes. There is no sense in extending this
 * further for the future to anything like multi-exam in one import,
 * so the model has to be kept:
 * 1 ImportJobBase (1 DICOM study/exam) - 1 subject relation
 * - 1 exam relation
 * If an ImportJobBase contains a subject object, it means to create one
 * in ms studies during the import.
 * If it contains a subjectName, an existing subject is to use.
 * Same logic for the exams.
 *
 * Therefore one ImportJobBase contains as well the DICOM StudyInstanceUID
 * of the DICOM study (== examination in Shanoir). This is required to
 * use the same UID in MS Import (pseudo) and in MS Datasets (exam).
 *
 * This class is the target model for the future: it is intentionally
 * lighter than the legacy {@link ImportJob}, which still carries the
 * historical "patients" list and "selectedSeries" for ongoing legacy
 * imports. {@link ImportJob} extends this class during the migration
 * period; once legacy imports are fully migrated, ImportJob can be
 * deleted and this class renamed to ImportJob.
 *
 * @author mkain
 */
public class ImportJobBase implements Serializable {

    private static final long serialVersionUID = 8804929608059674037L;

    private long timestamp;

    /* DicomQuery, that has been used to extract the DICOM study = ImportJobBase */
    private DicomQuery dicomQuery;

    private boolean fromDicomZip;

    private boolean fromShanoirUploader;

    private boolean fromPacs;

    private String workFolder;

    // DICOM patient for this import job
    private Patient patient;

    private PatientVerification patientVerification;

    // DICOM study for this import job
    private Study study;

    // series to import with this import job
    private List<Serie> series;

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

    private Boolean examinationDataReuseAgreement;

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

    private String studyInstanceUID;

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

    public List<Serie> getSeries() {
        return series;
    }

    public void setSeries(List<Serie> series) {
        this.series = series;
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

    public String getImportType() {
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
        return importType;
    }

    @Override
    public String toString() {
        int numberOfSeries = 0;
        StringBuffer seriesNames = new StringBuffer();
        seriesNames.append("[");
        String modality = "unknown";
        boolean enhanced = false;
        if (CollectionUtils.isNotEmpty(series)) {
            numberOfSeries = series.size();
            Serie serie = series.get(0);
            modality = serie.getModality();
            enhanced = serie.getIsEnhanced();
            for (Iterator<Serie> iterator = series.iterator(); iterator.hasNext();) {
                serie = (Serie) iterator.next();
                if (iterator.hasNext()) {
                    seriesNames.append(serie.getSequenceName() + ",");
                } else {
                    seriesNames.append(serie.getSequenceName() + "]");
                }
            }
        }
        return "userId=" + userId + ",studyName=" + studyName + ",studyCardId=" + studyCardId + ",type=" + getImportType()
                + ",workFolder=" + workFolder + ",pseudoProfile=" + anonymisationProfileToUse + ",modality=" + modality
                + ",enhanced=" + enhanced
                + ",subjectName=" + subjectName + ",examinationId=" + examinationId + ",StudyInstanceUID="
                + studyInstanceUID + ",numberOfSeries=" + numberOfSeries
                + ",seriesNames=" + seriesNames.toString();
    }

    @JsonIgnore
    public Serie getFirstSerie() {
        if (CollectionUtils.isNotEmpty(series)) {
            return series.getFirst();
        }
        return null;
    }

    /**
     * Some reconstructed DICOM series do not carry the institution and/or
     * equipment information in their DICOM files (both can be null on such
     * a Serie). As these values are the same for all series of one DICOM
     * study/exam, we do not simply take the first selected serie, but look
     * for the first selected serie that actually contains both, an
     * InstitutionDicom and an EquipmentDicom.
     *
     * @return the first selected Serie with both institution and equipment
     *         set, or null if none of the selected series contains both.
     */
    @JsonIgnore
    public Serie getFirstSerieWithInstitutionAndEquipment() {
        if (CollectionUtils.isNotEmpty(series)) {
            for (Serie serie : series) {
                if (serie.getInstitution().isKnown() && serie.getEquipment().isKnown()) {
                    return serie;
                }
            }
            // In case all are unknown, return first
            return series.getFirst();
        }
        return null;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public Boolean getExaminationDataReuseAgreement() {
        return examinationDataReuseAgreement;
    }

    public void setExaminationDataReuseAgreement(Boolean examinationDataReuseAgreement) {
        this.examinationDataReuseAgreement = examinationDataReuseAgreement;
    }

}
