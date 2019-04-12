package org.shanoir.ng.datasetacquisition.model.pet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PET dataset acquisition.
 * 
 * @author msimon
 *
 */
@Entity
@JsonTypeName("Pet")
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

	@Override
	public String getType() {
		return "Pet";
	}

}
