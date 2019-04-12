package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * PET dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class PetDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -209384115208882224L;

	@Override
	public String getType() {
		return "Pet";
	}

}
