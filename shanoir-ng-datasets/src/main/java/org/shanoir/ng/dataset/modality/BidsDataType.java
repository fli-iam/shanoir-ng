package org.shanoir.ng.dataset.modality;

public enum BidsDataType {

	/* positron emission tomography.*/
	BIDS_PET("pet"),

	/* task based and resting state functional MRI.*/
	BIDS_FUNC("func"),
	
	/* diffusion weighted imaging.*/
	BIDS_DWI("dwi"),
	
	/* field inhomogeneity mapping data such as field maps.*/
	BIDS_FMAP("fmap"),
	
	/* structural imaging such as T1, T2, PD, and so on.*/
	BIDS_ANAT("anat"),
	
	/* perfusion.*/
	BIDS_PERF("perf"),
	
	/* magnetoencephalography.*/
	BIDS_MEG("meg"),
	
	/* electroencephalography.*/
	BIDS_EEG("eeg"),
	
	/* intracranial electroencephalography.*/
	BIDS_IEEG("ieeg"),
	
	/* behavioral.*/
	BIDS_BEH("beh");

	private String folderName;

	BidsDataType(String string) {
		this.setFolderName(string);
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

}
