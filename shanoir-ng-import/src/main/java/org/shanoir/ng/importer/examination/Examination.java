package org.shanoir.ng.importer.examination;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
@JsonPropertyOrder({ "_links", "id", "data" })
public class Examination extends HalEntity {
	
	
	private String comment;
	private Date examinationDate;
	
	//private List<String> extraDataFilePathList;
	private boolean investigatorExternal = false;
	private String note;
	private Double subjectWeight;
	
	
	@ManyToOne
	@JoinColumn(name = "timepoint",nullable = true, updatable = true)
	private Timepoint timepoint;
	
	@ManyToOne
	@JoinColumn(name = "weightUnitOfMeasure",updatable = true, nullable = true)
	private UnitOfMeasure weightUnitOfMeasure;

	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "examination", cascade = CascadeType.ALL)
	private List<DatasetAcquisition> datasetAcquisitionList;
	
	


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


	/*public List<String> getExtraDataFilePathList() {
		return extraDataFilePathList;
	}


	public void setExtraDataFilePathList(List<String> extraDataFilePathList) {
		this.extraDataFilePathList = extraDataFilePathList;
	}*/


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


	public UnitOfMeasure getWeightUnitOfMeasure() {
		return weightUnitOfMeasure;
	}


	public void setWeightUnitOfMeasure(UnitOfMeasure weightUnitOfMeasure) {
		this.weightUnitOfMeasure = weightUnitOfMeasure;
	}

    

}
