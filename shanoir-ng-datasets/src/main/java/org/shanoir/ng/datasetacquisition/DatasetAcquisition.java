package org.shanoir.ng.datasetacquisition;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class DatasetAcquisition extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5487256834701104296L;

	/** Related Acquisition Equipment. */
	@NotNull
	private Long acquisitionEquipmentId;

	/** Datasets. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "datasetAcquisition", cascade = CascadeType.ALL)
	private List<Dataset> datasetList;

	/** Related Examination. */
//	@ManyToOne
//	@JoinColumn(name = "examination_id")
	// private Examination examination;
	private Long examinationId;

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
	 * @return the datasetList
	 */
	public List<Dataset> getDatasetList() {
		return datasetList;
	}

	/**
	 * @param datasetList
	 *            the datasetList to set
	 */
	public void setDatasetList(List<Dataset> datasetList) {
		this.datasetList = datasetList;
	}

	/**
	 * @return the examinationId
	 */
	public Long getExaminationId() {
		return examinationId;
	}

	/**
	 * @param examinationId
	 *            the examinationId to set
	 */
	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
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

}
