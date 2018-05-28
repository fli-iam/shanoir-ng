package org.shanoir.ng.datasetacquisition;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.datasetacquisition.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.pet.PetDatasetAcquisition;
import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.shared.model.AbstractGenericItem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
@DatasetsModalityTypeCheck
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({  
    @Type(value = CtDatasetAcquisition.class, name = "Ct"),  
    @Type(value = MrDatasetAcquisition.class, name = "Mr"),
    @Type(value = PetDatasetAcquisition.class, name = "Pet")})
public abstract class DatasetAcquisition extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5487256834701104296L;

	/** Related Acquisition Equipment. */
	@NotNull
	private Long acquisitionEquipmentId;

	/** Datasets. */
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "datasetAcquisition", cascade = CascadeType.ALL)
	private List<Dataset> datasets;

	/** Related Examination. */
	@ManyToOne
	@JoinColumn(name = "examination_id")
	private Examination examination;

	/** Rank of the session in the examination protocol. */
	private Integer rank;

	/** Software release. */
	private String softwareRelease;

	/** (0020,0011) Series number from dicom tags. */
	private Integer sortingIndex;

	/**
	 * @return the acquisitionEquipmentId
	 */
	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	/**
	 * @param acquisitionEquipmentId
	 *            the acquisitionEquipmentId to set
	 */
	public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
		this.acquisitionEquipmentId = acquisitionEquipmentId;
	}

	/**
	 * @return the datasets
	 */
	public List<Dataset> getDatasets() {
		return datasets;
	}

	/**
	 * @param datasets
	 *            the datasets to set
	 */
	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	/**
	 * @return the examination
	 */
	public Examination getExamination() {
		return examination;
	}

	/**
	 * @param examination
	 *            the examination to set
	 */
	public void setExamination(Examination examination) {
		this.examination = examination;
	}

	/**
	 * @return the rank
	 */
	public Integer getRank() {
		return rank;
	}

	/**
	 * @param rank
	 *            the rank to set
	 */
	public void setRank(Integer rank) {
		this.rank = rank;
	}

	/**
	 * @return the softwareRelease
	 */
	public String getSoftwareRelease() {
		return softwareRelease;
	}

	/**
	 * @param softwareRelease
	 *            the softwareRelease to set
	 */
	public void setSoftwareRelease(String softwareRelease) {
		this.softwareRelease = softwareRelease;
	}

	/**
	 * @return the sortingIndex
	 */
	public Integer getSortingIndex() {
		return sortingIndex;
	}

	/**
	 * @param sortingIndex
	 *            the sortingIndex to set
	 */
	public void setSortingIndex(Integer sortingIndex) {
		this.sortingIndex = sortingIndex;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Transient
	public abstract String getType();


}
