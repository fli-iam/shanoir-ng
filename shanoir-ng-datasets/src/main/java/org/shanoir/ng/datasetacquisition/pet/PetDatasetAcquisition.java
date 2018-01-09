package org.shanoir.ng.datasetacquisition.pet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.datasetacquisition.DatasetAcquisition;

/**
 * PET dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class PetDatasetAcquisition extends DatasetAcquisition {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -7819600575968825257L;

	/** PET protocol. */
	@OneToOne(cascade = CascadeType.ALL)
	private PetProtocol petProtocol;

	/**
	 * @return the petProtocol
	 */
	public PetProtocol getPetProtocol() {
		return petProtocol;
	}

	/**
	 * @param petProtocol
	 *            the petProtocol to set
	 */
	public void setPetProtocol(PetProtocol petProtocol) {
		this.petProtocol = petProtocol;
	}

}
