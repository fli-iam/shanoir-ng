package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * 
 * @author fli
 *
 */
@Entity
public class BidsDataset extends Dataset {

	
	/** Bids Modality for nifti files => anat / func / dwi / ... */
	private String modality;

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	@Override
	public String getType() {
		return "Bids";
	}
}
