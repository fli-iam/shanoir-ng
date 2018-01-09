package org.shanoir.ng.datasetacquisition.mr;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.datasetacquisition.DatasetAcquisition;

/**
 * MR dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class MrDatasetAcquisition extends DatasetAcquisition {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2532815427171578710L;

	/** MR protocol. */
	@OneToOne(cascade = CascadeType.ALL)
	private MrProtocol mrProtocol;

	/**
	 * @return the mrProtocol
	 */
	public MrProtocol getMrProtocol() {
		return mrProtocol;
	}

	/**
	 * @param mrProtocol
	 *            the mrProtocol to set
	 */
	public void setMrProtocol(MrProtocol mrProtocol) {
		this.mrProtocol = mrProtocol;
	}

}
