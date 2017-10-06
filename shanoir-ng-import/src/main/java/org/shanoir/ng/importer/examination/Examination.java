package org.shanoir.ng.importer.examination;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Examination.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "examination")
@JsonPropertyOrder({ "_links", "id", "centerId", "comment", "examinationDate", "extraDataFilePathList", "investigatorExternal", "investigatorCenterId", 
	"note", "subjectWeight", "timepoint", "weightUnitOfMeasure", "datasetAcquisitionList", "subjectId", "studyId", "investigatorId", "instrumentBasedAssessmentList"})
public class Examination extends HalEntity {
	

	private Long centerId;
	private String comment;
	private Date examinationDate;
	
	@ElementCollection
	@CollectionTable(name = "extra_data_file_pathlist_table")
	@Column(name = "extra_data_file_pathlist")
	private List<String> extraDataFilePathList;
	
	private boolean investigatorExternal = false;
	
	private Long investigatorCenterId;
	private String note;
	private Double subjectWeight;
	
	
	@ManyToOne
	@JoinColumn(name = "timepoint",nullable = true, updatable = true)
	private Timepoint timepoint;
	
	private Integer weightUnitOfMeasure;

	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "examination", cascade = CascadeType.ALL)
	private List<DatasetAcquisition> datasetAcquisitionList;
	
	private Long subjectId;
	
	private Long studyId;
	
	private Long investigatorId;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "examination", cascade = { CascadeType.ALL })
	private List<InstrumentBasedAssessment> instrumentBasedAssessmentList;


	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public Date getExaminationDate() {
		return examinationDate;
	}


	public void setExaminationDate(Date examinationDate) {
		this.examinationDate = examinationDate;
	}



	public boolean isInvestigatorExternal() {
		return investigatorExternal;
	}


	public void setInvestigatorExternal(boolean investigatorExternal) {
		this.investigatorExternal = investigatorExternal;
	}


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}


	public Double getSubjectWeight() {
		return subjectWeight;
	}


	public void setSubjectWeight(Double subjectWeight) {
		this.subjectWeight = subjectWeight;
	}


	public Timepoint getTimepoint() {
		return timepoint;
	}


	public void setTimepoint(Timepoint timepoint) {
		this.timepoint = timepoint;
	}


	public Integer getWeightUnitOfMeasure() {
		return weightUnitOfMeasure;
	}


	public void setWeightUnitOfMeasure(Integer weightUnitOfMeasure) {
		this.weightUnitOfMeasure = weightUnitOfMeasure;
	}



	public List<String> getExtraDataFilePathList() {
		return extraDataFilePathList;
	}


	public void setExtraDataFilePathList(List<String> extraDataFilePathList) {
		this.extraDataFilePathList = extraDataFilePathList;
	}



	public List<DatasetAcquisition> getDatasetAcquisitionList() {
		return datasetAcquisitionList;
	}


	public void setDatasetAcquisitionList(List<DatasetAcquisition> datasetAcquisitionList) {
		this.datasetAcquisitionList = datasetAcquisitionList;
	}


	public List<InstrumentBasedAssessment> getInstrumentBasedAssessmentList() {
		return instrumentBasedAssessmentList;
	}


	public void setInstrumentBasedAssessmentList(List<InstrumentBasedAssessment> instrumentBasedAssessmentList) {
		this.instrumentBasedAssessmentList = instrumentBasedAssessmentList;
	}


	public Long getCenterId() {
		return centerId;
	}


	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}


	public Long getInvestigatorCenterId() {
		return investigatorCenterId;
	}


	public void setInvestigatorCenterId(Long investigatorCenterId) {
		this.investigatorCenterId = investigatorCenterId;
	}


	public Long getSubjectId() {
		return subjectId;
	}


	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}


	public Long getStudyId() {
		return studyId;
	}


	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}


	public Long getInvestigatorId() {
		return investigatorId;
	}


	public void setInvestigatorId(Long investigatorId) {
		this.investigatorId = investigatorId;
	}
	
	

    

}
