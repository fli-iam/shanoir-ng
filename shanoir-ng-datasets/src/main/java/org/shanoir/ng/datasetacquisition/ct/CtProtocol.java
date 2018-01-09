package org.shanoir.ng.datasetacquisition.ct;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * CT protocol.
 * 
 * @author msimon
 *
 */
@Entity
public class CtProtocol extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5062475142212117502L;

	/** The MR Dataset acquisition. */
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "ctProtocol")
	private CtDatasetAcquisition ctDatasetAcquisition;

	/**
	 * @return the ctDatasetAcquisition
	 */
	public CtDatasetAcquisition getCtDatasetAcquisition() {
		return ctDatasetAcquisition;
	}

	/**
	 * @param ctDatasetAcquisition
	 *            the ctDatasetAcquisition to set
	 */
	public void setCtDatasetAcquisition(CtDatasetAcquisition ctDatasetAcquisition) {
		this.ctDatasetAcquisition = ctDatasetAcquisition;
	}

}
