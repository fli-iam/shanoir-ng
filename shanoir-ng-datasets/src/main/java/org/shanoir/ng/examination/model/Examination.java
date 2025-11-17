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

package org.shanoir.ng.examination.model;

import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.validation.constraints.NotNull;

/**
 * Examination.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "examinationDate", "studyInstanceUID", "centerId", "subjectId", "studyId", "preclinical" })
public class Examination extends HalEntity {

    /**
     * UID
     */
    private static final long serialVersionUID = -5513725630019270395L;

    /** Acquisition Center. */
    @NotNull
    private Long centerId;

    /**
     * A comment on the dataset. In case of importing from dicom files, it could
     * be the series description for instance.
     */
    private String comment;

    /** Dataset acquisitions. */
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "examination", cascade = CascadeType.ALL)
    private List<DatasetAcquisition> datasetAcquisitions;

    /** Examination date. */
    @NotNull
    @LocalDateAnnotations
    private LocalDate examinationDate;

    /**
     * Experimental group of subjects. Can be null only if subject is not null.
     */
    private Long experimentalGroupOfSubjectsId;

    /** List of extra files directly attached to the examinations. */
    @ElementCollection
    @CollectionTable(name = "extra_data_file_path")
    @Column(name = "path")
    private List<String> extraDataFilePathList;

    /** List of the instrumentBasedAssessment related to this examination. */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "examination", cascade = { CascadeType.ALL })
    private List<InstrumentBasedAssessment> instrumentBasedAssessmentList;

    /** Center of the investigator if he is external. */
    private Long investigatorCenterId;

    /**
     * True if the investigator come from an other center than the acquisition
     * center.
     */
    @NotNull
    private boolean investigatorExternal;

    /** Investigator. */
    // @NotNull
    private Long investigatorId;

    /** Notes about this examination. */
	@JdbcTypeCode(Types.LONGVARCHAR)
    private String note;

    /** Study. */
    @ManyToOne
    @JoinColumn(name = "study_id")
    @NotNull
    private Study study;

    /** Subject. Can be null only if experimentalGroupOfSubjects is not null. */
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    /**
     * Subject weight at the time of the examination
     */
    private Double subjectWeight;

    /** Study Timepoint */
    private Long timepointId;

    /** The unit of weight, can be in kg or g */
    private Integer weightUnitOfMeasure;

    /** Flag to set the examination as pre-clinical  */ 
    @Column(nullable=false)
    @ColumnDefault("false")
    private boolean preclinical;

    /**
     * The DICOM StudyInstanceUID present in the backup PACS of Shanoir,
     * dcm4chee arc light, and generated during examination creation.
     */
    @Column(name = "study_instance_uid")
    private String studyInstanceUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Examination source;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "source", cascade = CascadeType.ALL)
    private List<Examination> copies;

    public Examination() {
    }

    public Examination(Examination other, Study study, Subject subject) {
        this.centerId = other.centerId;
        this.comment = other.comment;
        this.examinationDate = other.examinationDate;
        this.experimentalGroupOfSubjectsId = other.experimentalGroupOfSubjectsId;
        this.extraDataFilePathList = null;

        this.instrumentBasedAssessmentList = new ArrayList<>(other.getInstrumentBasedAssessmentList().size());
        for (InstrumentBasedAssessment instru : other.getInstrumentBasedAssessmentList()) {
            this.instrumentBasedAssessmentList.add(new InstrumentBasedAssessment(instru));
        }
        this.investigatorCenterId = other.investigatorCenterId;
        this.investigatorExternal = other.investigatorExternal;
        this.investigatorId = other.investigatorId;
        this.note = other.note;

        this.study = study;
        this.subject = subject;

        this.subjectWeight = other.subjectWeight;
        this.timepointId = other.timepointId;
        this.weightUnitOfMeasure = other.weightUnitOfMeasure;
        this.preclinical = other.preclinical;
        this.source = other.source;
        this.copies = other.copies;
        this.studyInstanceUID = other.studyInstanceUID;
    }

    /**
     * Init HATEOAS links
     */
    @PostLoad
    public void initLinks() {
        this.addLink(Links.REL_SELF, "examination/" + getId());
    }

    /**
     * @return the centerId
     */
    public Long getCenterId() {
        return centerId;
    }

    /**
     * @param centerId
     *            the centerId to set
     */
    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the datasetAcquisitions
     */
    public List<DatasetAcquisition> getDatasetAcquisitions() {
        return datasetAcquisitions;
    }

    /**
     * @param datasetAcquisitions
     *            the datasetAcquisitionList to set
     */
    public void setDatasetAcquisitions(List<DatasetAcquisition> datasetAcquisitions) {
        this.datasetAcquisitions = datasetAcquisitions;
    }

    public void addDatasetAcquisitions(DatasetAcquisition acquisition) {
        if (getDatasetAcquisitions() == null) setDatasetAcquisitions(new ArrayList<>());
        getDatasetAcquisitions().add(acquisition);
    }

    /**
     * @return the examinationDate
     */
    public LocalDate getExaminationDate() {
        return examinationDate;
    }

    /**
     * @param examinationDate
     *            the examinationDate to set
     */
    public void setExaminationDate(LocalDate examinationDate) {
        this.examinationDate = examinationDate;
    }

    /**
     * @return the experimentalGroupOfSubjectsId
     */
    public Long getExperimentalGroupOfSubjectsId() {
        return experimentalGroupOfSubjectsId;
    }

    /**
     * @param experimentalGroupOfSubjectsId
     *            the experimentalGroupOfSubjectsId to set
     */
    public void setExperimentalGroupOfSubjectsId(Long experimentalGroupOfSubjectsId) {
        this.experimentalGroupOfSubjectsId = experimentalGroupOfSubjectsId;
    }

    /**
     * @return the extraDataFilePathList
     */
    public List<String> getExtraDataFilePathList() {
        return extraDataFilePathList;
    }

    /**
     * @param extraDataFilePathList
     *            the extraDataFilePathList to set
     */
    public void setExtraDataFilePathList(List<String> extraDataFilePathList) {
        this.extraDataFilePathList = extraDataFilePathList;
    }

    /**
     * @return the instrumentBasedAssessmentList
     */
    public List<InstrumentBasedAssessment> getInstrumentBasedAssessmentList() {
        return instrumentBasedAssessmentList;
    }

    /**
     * @param instrumentBasedAssessmentList
     *            the instrumentBasedAssessmentList to set
     */
    public void setInstrumentBasedAssessmentList(List<InstrumentBasedAssessment> instrumentBasedAssessmentList) {
        this.instrumentBasedAssessmentList = instrumentBasedAssessmentList;
    }

    /**
     * @return the investigatorCenterId
     */
    public Long getInvestigatorCenterId() {
        return investigatorCenterId;
    }

    /**
     * @param investigatorCenterId
     *            the investigatorCenterId to set
     */
    public void setInvestigatorCenterId(Long investigatorCenterId) {
        this.investigatorCenterId = investigatorCenterId;
    }

    /**
     * @return the investigatorExternal
     */
    public boolean isInvestigatorExternal() {
        return investigatorExternal;
    }

    /**
     * @param investigatorExternal
     *            the investigatorExternal to set
     */
    public void setInvestigatorExternal(boolean investigatorExternal) {
        this.investigatorExternal = investigatorExternal;
    }

    /**
     * @return the investigatorId
     */
    public Long getInvestigatorId() {
        return investigatorId;
    }

    /**
     * @param investigatorId
     *            the investigatorId to set
     */
    public void setInvestigatorId(Long investigatorId) {
        this.investigatorId = investigatorId;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note
     *            the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the studyId
     */
    public Long getStudyId() {
        return getStudy() != null ? getStudy().getId() : null;
    }
    
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     * @return the subjectWeight
     */
    public Double getSubjectWeight() {
        return subjectWeight;
    }

    /**
     * @param subjectWeight
     *            the subjectWeight to set
     */
    public void setSubjectWeight(Double subjectWeight) {
        this.subjectWeight = subjectWeight;
    }

    /**
     * @return the timepointId
     */
    public Long getTimepointId() {
        return timepointId;
    }

    /**
     * @param timepointId
     *            the timepointId to set
     */
    public void setTimepointId(Long timepointId) {
        this.timepointId = timepointId;
    }

    /**
     * @return the weightUnitOfMeasure
     */
    public UnitOfMeasure getWeightUnitOfMeasure() {
        return UnitOfMeasure.getUnit(weightUnitOfMeasure);
    }

    /**
     * @param weightUnitOfMeasure
     *            the weightUnitOfMeasure to set
     */
    public void setWeightUnitOfMeasure(UnitOfMeasure weightUnitOfMeasure) {
        if (weightUnitOfMeasure == null) {
            this.weightUnitOfMeasure = null;
        } else {
            this.weightUnitOfMeasure = weightUnitOfMeasure.getId();
        }
    }
    
    public boolean isPreclinical() {
        return preclinical;
    }

    public void setPreclinical(boolean preclinical) {
        this.preclinical = preclinical;
    }

    public Examination getSource() {
        return source;
    }

    public void setSource(Examination source) {
        this.source = source;
    }

    public List<Examination> getCopies() {
        return copies;
    }

    public void setCopies(List<Examination> copies) {
        this.copies = copies;
    }

    @Override
    public String toString() {
        return "Examination [centerId=" + centerId + ", comment=" + comment + ", examinationDate=" + examinationDate
                + ", extraDataFilePathList=" + extraDataFilePathList + ", note=" + note + ", subject=" + subject.getName()
                + ", preclinical=" + preclinical + ", studyInstanceUID=" + studyInstanceUID + "]";
    }

}