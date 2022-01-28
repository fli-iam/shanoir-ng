package org.shanoir.ng.dataset.modality;

public enum BidsDataType {

	/* task based and resting state functional MRI.*/
	FUNC("func"),
	
	/* diffusion weighted imaging.*/
	DWI("dwi"),
	
	/* field inhomogeneity mapping data such as field maps.*/
	FMAP("fmap"),
	
	/* structural imaging such as T1, T2, PD, and so on.*/
	ANAT("anat"),
	
	/* perfusion.*/
	PERF("perf");

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
