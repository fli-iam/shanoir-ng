package org.shanoir.ng.importer.examination;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DatasetAcquisition.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "dataset_acquisition")
@JsonPropertyOrder({ "_links", "id", "AquisitionEquipmentId", "rank", "softwareRelease", "sortingIndex", "examination"})
public class DatasetAcquisition extends HalEntity {

	private Long AquisitionEquipmentId;
	private Integer rank;
	private String softwareRelease;
	private Integer sortingIndex;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "examination", nullable = true, updatable = true)
	private Examination examination;
	
	//private List<Dataset> datasetList ;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getSoftwareRelease() {
		return softwareRelease;
	}

	public void setSoftwareRelease(String softwareRelease) {
		this.softwareRelease = softwareRelease;
	}

	public Integer getSortingIndex() {
		return sortingIndex;
	}

	public void setSortingIndex(Integer sortingIndex) {
		this.sortingIndex = sortingIndex;
	}

	public Examination getExamination() {
		return examination;
	}

	public void setExamination(Examination examination) {
		this.examination = examination;
	}

	public Long getAquisitionEquipmentId() {
		return AquisitionEquipmentId;
	}

	public void setAquisitionEquipmentId(Long aquisitionEquipmentId) {
		AquisitionEquipmentId = aquisitionEquipmentId;
	}

	


}
