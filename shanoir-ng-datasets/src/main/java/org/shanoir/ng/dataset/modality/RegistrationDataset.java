package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.Dataset;

/**
 * Registration dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class RegistrationDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2435422041970785779L;

	/** Registration Dataset Nature. */
	private Integer registrationDatasetType;

	/**
	 * @return the registrationDatasetType
	 */
	public RegistrationDatasetType getRegistrationDatasetType() {
		return RegistrationDatasetType.getType(registrationDatasetType);
	}

	/**
	 * @param registrationDatasetType
	 *            the registrationDatasetType to set
	 */
	public void setRegistrationDatasetType(RegistrationDatasetType registrationDatasetType) {
		if (registrationDatasetType == null) {
			this.registrationDatasetType = null;
		} else {
			this.registrationDatasetType = registrationDatasetType.getId();
		}
	}

}
