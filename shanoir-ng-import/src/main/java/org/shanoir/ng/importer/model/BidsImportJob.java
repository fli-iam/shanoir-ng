package org.shanoir.ng.importer.model;

public class BidsImportJob extends ImportJob {

	/** Bids Modality for nifti files => anat / func / dwi / ... */
	private String modality;

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}
}
