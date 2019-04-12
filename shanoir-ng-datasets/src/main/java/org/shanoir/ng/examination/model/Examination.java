package org.shanoir.ng.examination.model;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Examination.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "examinationDate", "centerId", "subjectId", "studyId", "preclinical" })
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
	@Lob
	private String note;

	/** Study. */
	@NotNull
	private Long studyId;

	/** Subject. Can be null only if experimentalGroupOfSubjects is not null. */
	private Long subjectId;

	/**
	 * Subject weight at the time of the examination
	 */
	private Double subjectWeight;

	/** Study Timepoint */
	private Long timepointId;

	/** The unit of weight, can be in kg or g */
	private Integer weightUnitOfMeasure;

	/** Flag to set the examination as pre-clinical  */ 
	@Column(nullable=false, columnDefinition="BOOLEAN DEFAULT false")
	private boolean preclinical;
		
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
	 * @param datasetAcquisitionList
	 *            the datasetAcquisitionList to set
	 */
	public void setDatasetAcquisitions(List<DatasetAcquisition> datasetAcquisitions) {
		this.datasetAcquisitions = datasetAcquisitions;
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
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the subjectId
	 */
	public Long getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId
	 *            the subjectId to set
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
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

}